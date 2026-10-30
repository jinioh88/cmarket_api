package org.cmarket.cmarket.domain.eventfeed.app;

import org.cmarket.cmarket.domain.auth.model.User;
import org.cmarket.cmarket.domain.auth.repository.UserRepository;
import org.cmarket.cmarket.domain.community.model.BoardType;
import org.cmarket.cmarket.domain.community.model.Post;
import org.cmarket.cmarket.domain.community.repository.PostRepository;
import org.cmarket.cmarket.domain.eventfeed.app.dto.EventInfoDto;
import org.cmarket.cmarket.domain.eventfeed.app.dto.EventIngestCommand;
import org.cmarket.cmarket.domain.eventfeed.app.dto.EventIngestResult;
import org.cmarket.cmarket.domain.eventfeed.model.EventIngestRecord;
import org.cmarket.cmarket.domain.eventfeed.model.EventIngestStatus;
import org.cmarket.cmarket.domain.eventfeed.model.EventPeriodStatus;
import org.cmarket.cmarket.domain.eventfeed.repository.EventIngestRecordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 행사 수집 서비스
 * 
 * 크롤러가 보낸 행사 글을 운영 계정 이름으로 행사 소식 게시판에 올린다.
 * 같은 출처 글은 두 번 만들지 않고, 출처가 달라도 같은 행사는 한 번만 올린다.
 */
@Service
public class EventIngestService {

    private static final int MAX_TITLE_LENGTH = 50;    // Post.title
    private static final int MAX_CONTENT_LENGTH = 1000; // Post.content

    private final EventIngestRecordRepository recordRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final String authorUserId;

    public EventIngestService(
            EventIngestRecordRepository recordRepository,
            PostRepository postRepository,
            UserRepository userRepository,
            @Value("${ingest.author-user-id:}") String authorUserId
    ) {
        this.recordRepository = recordRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.authorUserId = authorUserId;
    }

    @Transactional(readOnly = true)
    public List<String> findUnseen(String sourceCode, List<String> externalIds) {
        Set<String> seen = recordRepository.findBySourceCodeAndExternalIdIn(sourceCode, externalIds).stream()
                .map(EventIngestRecord::getExternalId)
                .collect(Collectors.toSet());
        return externalIds.stream().filter(id -> !seen.contains(id)).distinct().toList();
    }

    /**
     * 행사 글 번호별 행사 칸. 행사 글이 아니면 Map에 없다.
     */
    @Transactional(readOnly = true)
    public Map<Long, EventInfoDto> findEventInfo(Collection<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Map.of();
        }
        return recordRepository.findByStatusAndPostIdIn(EventIngestStatus.POSTED, postIds).stream()
                .collect(Collectors.toMap(EventIngestRecord::getPostId, EventInfoDto::from, (a, b) -> a));
    }

    /**
     * 진행 상태가 맞는 행사 글 번호
     * 
     * 행사 글은 1년에 수십 건이라 게시 기록을 모두 읽어 자바에서 거른다.
     * 그래야 상태 규칙이 EventPeriodStatus.of 한 곳에만 있다.
     */
    @Transactional(readOnly = true)
    public List<Long> findPostIdsByPeriodStatus(EventPeriodStatus periodStatus, LocalDate today) {
        return recordRepository.findByStatus(EventIngestStatus.POSTED).stream()
                .filter(record -> record.getPostId() != null)
                .filter(record -> EventPeriodStatus.of(record.getEventStartDate(), record.getEventEndDate(), today) == periodStatus)
                .map(EventIngestRecord::getPostId)
                .toList();
    }

    @Transactional
    public EventIngestResult ingest(EventIngestCommand command) {
        if (command.getStatus() == EventIngestStatus.POSTED) {
            validateLength(command);
        }

        if (recordRepository.existsBySourceCodeAndExternalId(command.getSourceCode(), command.getExternalId())) {
            return new EventIngestResult(EventIngestResult.Status.ALREADY_EXISTS, null);
        }

        String titleKey = EventTitleKey.normalize(command.getTitle());

        if (command.getStatus() != EventIngestStatus.POSTED) {
            saveRecord(command, command.getStatus(), null, titleKey);
            return new EventIngestResult(EventIngestResult.Status.RECORDED_SKIP, null);
        }

        // 출처가 달라도 같은 행사(행사명 열쇠와 시작일이 같다)면 한 번만 올린다
        if (command.getEventStartDate() != null && !titleKey.isEmpty()
                && recordRepository.existsByStatusAndTitleKeyAndEventStartDate(
                        EventIngestStatus.POSTED, titleKey, command.getEventStartDate())) {
            saveRecord(command, EventIngestStatus.SKIPPED_DUPLICATE, null, titleKey);
            return new EventIngestResult(EventIngestResult.Status.DUPLICATE, null);
        }

        User author = findAuthor();
        Post post = postRepository.save(Post.builder()
                .authorId(author.getId())
                .authorNickname(author.getNickname())
                .authorProfileImageUrl(author.getProfileImageUrl())
                .title(command.getTitle())
                .content(command.getContent())
                .imageUrls(command.getImageUrl() != null ? List.of(command.getImageUrl()) : List.of())
                .boardType(BoardType.EVENT)
                .build());

        saveRecord(command, EventIngestStatus.POSTED, post.getId(), titleKey);
        return new EventIngestResult(EventIngestResult.Status.CREATED, post.getId());
    }

    private void validateLength(EventIngestCommand command) {
        if (command.getTitle() == null || command.getTitle().isBlank()
                || command.getTitle().length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("행사 글 제목은 1~50자여야 합니다.");
        }
        if (command.getContent() == null || command.getContent().isBlank()
                || command.getContent().length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("행사 글 본문은 1~1000자여야 합니다.");
        }
    }

    private User findAuthor() {
        if (authorUserId == null || authorUserId.isBlank()) {
            throw new IllegalStateException("ingest.author-user-id가 설정되지 않았습니다.");
        }
        return userRepository.findByIdAndDeletedAtIsNull(Long.valueOf(authorUserId.trim()))
                .orElseThrow(() -> new IllegalStateException("행사 글 운영 계정을 찾을 수 없습니다."));
    }

    private void saveRecord(EventIngestCommand command, EventIngestStatus status, Long postId, String titleKey) {
        recordRepository.save(EventIngestRecord.builder()
                .sourceCode(command.getSourceCode())
                .externalId(command.getExternalId())
                .status(status)
                .postId(postId)
                .sourceUrl(command.getSourceUrl())
                .titleKey(titleKey.isEmpty() ? null : titleKey)
                .eventStartDate(command.getEventStartDate())
                .eventEndDate(command.getEventEndDate())
                .region(command.getRegion())
                .place(command.getPlace())
                .organizer(command.getOrganizer())
                .summary(command.getSummary())
                .target(command.getTarget())
                .fee(command.getFee())
                .activities(joinLines(command.getActivities()))
                .checks(joinLines(command.getChecks()))
                .imageUrl(command.getImageUrl())
                .build());
    }

    /**
     * 목록 칸을 줄바꿈으로 이어 한 칸에 둔다. 항목 안의 줄바꿈은 빈칸으로, 빈 항목은 뺀다
     */
    private static String joinLines(List<String> items) {
        if (items == null) {
            return null;
        }
        String joined = items.stream()
                .filter(item -> item != null && !item.isBlank())
                .map(item -> item.replaceAll("\\s*\\n\\s*", " ").trim())
                .collect(Collectors.joining("\n"));
        return joined.isEmpty() ? null : joined;
    }
}
