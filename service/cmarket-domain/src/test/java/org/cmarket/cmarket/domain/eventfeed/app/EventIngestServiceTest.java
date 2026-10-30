package org.cmarket.cmarket.domain.eventfeed.app;

import org.cmarket.cmarket.domain.auth.model.User;
import org.cmarket.cmarket.domain.auth.repository.UserRepository;
import org.cmarket.cmarket.domain.community.model.BoardType;
import org.cmarket.cmarket.domain.community.model.Post;
import org.cmarket.cmarket.domain.community.repository.PostRepository;
import org.cmarket.cmarket.domain.eventfeed.app.dto.EventIngestCommand;
import org.cmarket.cmarket.domain.eventfeed.app.dto.EventInfoDto;
import org.cmarket.cmarket.domain.eventfeed.app.dto.EventIngestResult;
import org.cmarket.cmarket.domain.eventfeed.model.EventIngestRecord;
import org.cmarket.cmarket.domain.eventfeed.model.EventIngestStatus;
import org.cmarket.cmarket.domain.eventfeed.model.EventPeriodStatus;
import org.cmarket.cmarket.domain.eventfeed.repository.EventIngestRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventIngestServiceTest {

    @Mock EventIngestRecordRepository recordRepository;
    @Mock PostRepository postRepository;
    @Mock UserRepository userRepository;

    EventIngestService service;

    @BeforeEach
    void setUp() {
        service = new EventIngestService(recordRepository, postRepository, userRepository, "52");
    }

    private EventIngestCommand.EventIngestCommandBuilder posted() {
        return EventIngestCommand.builder()
                .sourceCode("SEOUL").externalId("468820")
                .sourceUrl("https://www.seoul.go.kr/news/news_report.do?bbsNo=158&nttNo=468820")
                .status(EventIngestStatus.POSTED)
                .title("2026 서울 댕냥데이즈").content("본문")
                .eventStartDate(LocalDate.of(2026, 10, 31))
                .eventEndDate(LocalDate.of(2026, 11, 2))
                .region("서울").place("문화비축기지").organizer("서울특별시")
                .summary("반려견과 함께 즐기는 가을 축제")
                .target("반려견, 반려묘").fee("무료")
                .activities(List.of("반려견 놀이터", "건강 상담"))
                .checks(List.of("목줄을 챙기세요"));
    }

    @Test
    void 이미_본_출처_번호면_만들지_않는다() {
        when(recordRepository.existsBySourceCodeAndExternalId("SEOUL", "468820")).thenReturn(true);

        EventIngestResult result = service.ingest(posted().build());

        assertThat(result.getStatus()).isEqualTo(EventIngestResult.Status.ALREADY_EXISTS);
        verify(postRepository, never()).save(any());
        verify(recordRepository, never()).save(any());
    }

    @Test
    void 건너뜀_명령은_기록만_남긴다() {
        EventIngestResult result = service.ingest(posted().status(EventIngestStatus.SKIPPED_ENDED).build());

        assertThat(result.getStatus()).isEqualTo(EventIngestResult.Status.RECORDED_SKIP);
        ArgumentCaptor<EventIngestRecord> captor = ArgumentCaptor.forClass(EventIngestRecord.class);
        verify(recordRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(EventIngestStatus.SKIPPED_ENDED);
        assertThat(captor.getValue().getPostId()).isNull();
        verify(postRepository, never()).save(any());
    }

    @Test
    void 행사명과_시작일이_같은_게시_기록이_있으면_중복으로_둔다() {
        when(recordRepository.existsByStatusAndTitleKeyAndEventStartDate(
                EventIngestStatus.POSTED, "서울댕냥데이즈", LocalDate.of(2026, 10, 31))).thenReturn(true);

        EventIngestResult result = service.ingest(posted().title("서울 댕냥데이즈(2026)").build());

        assertThat(result.getStatus()).isEqualTo(EventIngestResult.Status.DUPLICATE);
        ArgumentCaptor<EventIngestRecord> captor = ArgumentCaptor.forClass(EventIngestRecord.class);
        verify(recordRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(EventIngestStatus.SKIPPED_DUPLICATE);
        verify(postRepository, never()).save(any());
    }

    @Test
    void 정상이면_운영_계정으로_행사_소식_글을_만든다() {
        User author = mock(User.class);
        when(author.getId()).thenReturn(52L);
        when(author.getNickname()).thenReturn("커들소식");
        when(userRepository.findByIdAndDeletedAtIsNull(52L)).thenReturn(Optional.of(author));
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> {
            Post p = inv.getArgument(0);
            ReflectionTestUtils.setField(p, "id", 123L);
            return p;
        });

        EventIngestResult result = service.ingest(posted().imageUrl("https://www.seoul.go.kr/a.jpg").build());

        assertThat(result.getStatus()).isEqualTo(EventIngestResult.Status.CREATED);
        assertThat(result.getPostId()).isEqualTo(123L);
        ArgumentCaptor<Post> post = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(post.capture());
        assertThat(post.getValue().getBoardType()).isEqualTo(BoardType.EVENT);
        assertThat(post.getValue().getAuthorId()).isEqualTo(52L);
        assertThat(post.getValue().getAuthorNickname()).isEqualTo("커들소식");
        assertThat(post.getValue().getImageUrls()).containsExactly("https://www.seoul.go.kr/a.jpg");
        ArgumentCaptor<EventIngestRecord> record = ArgumentCaptor.forClass(EventIngestRecord.class);
        verify(recordRepository).save(record.capture());
        assertThat(record.getValue().getPostId()).isEqualTo(123L);
        assertThat(record.getValue().getStatus()).isEqualTo(EventIngestStatus.POSTED);
        assertThat(record.getValue().getTitleKey()).isEqualTo("서울댕냥데이즈");
        assertThat(record.getValue().getEventEndDate()).isEqualTo(LocalDate.of(2026, 11, 2));
        assertThat(record.getValue().getRegion()).isEqualTo("서울");
        assertThat(record.getValue().getPlace()).isEqualTo("문화비축기지");
        assertThat(record.getValue().getOrganizer()).isEqualTo("서울특별시");
        assertThat(record.getValue().getSummary()).isEqualTo("반려견과 함께 즐기는 가을 축제");
        assertThat(record.getValue().getTarget()).isEqualTo("반려견, 반려묘");
        assertThat(record.getValue().getFee()).isEqualTo("무료");
        assertThat(record.getValue().getActivities()).isEqualTo("반려견 놀이터\n건강 상담");
        assertThat(record.getValue().getChecks()).isEqualTo("목줄을 챙기세요");
        assertThat(record.getValue().getImageUrl()).isEqualTo("https://www.seoul.go.kr/a.jpg");
    }

    @Test
    void 게시한_글_번호의_행사_칸만_돌려준다() {
        EventIngestRecord posted = EventIngestRecord.builder()
                .sourceCode("SEOUL").externalId("1").status(EventIngestStatus.POSTED).postId(1L).sourceUrl("u")
                .eventStartDate(LocalDate.of(2026, 10, 31)).eventEndDate(LocalDate.of(2026, 11, 2))
                .region("서울").place("문화비축기지").organizer("서울특별시").summary("소개")
                .target("반려견").fee("무료").activities("놀이터\n상담").checks("목줄")
                .imageUrl("https://www.seoul.go.kr/a.jpg").build();
        when(recordRepository.findByStatusAndPostIdIn(EventIngestStatus.POSTED, List.of(1L, 2L)))
                .thenReturn(List.of(posted));

        Map<Long, EventInfoDto> info = service.findEventInfo(List.of(1L, 2L));

        assertThat(info).containsOnlyKeys(1L);
        EventInfoDto dto = info.get(1L);
        assertThat(dto.getStartDate()).isEqualTo(LocalDate.of(2026, 10, 31));
        assertThat(dto.getEndDate()).isEqualTo(LocalDate.of(2026, 11, 2));
        assertThat(dto.getRegion()).isEqualTo("서울");
        assertThat(dto.getPlace()).isEqualTo("문화비축기지");
        assertThat(dto.getOrganizer()).isEqualTo("서울특별시");
        assertThat(dto.getSummary()).isEqualTo("소개");
        assertThat(dto.getTarget()).isEqualTo("반려견");
        assertThat(dto.getFee()).isEqualTo("무료");
        assertThat(dto.getActivities()).containsExactly("놀이터", "상담");
        assertThat(dto.getChecks()).containsExactly("목줄");
        assertThat(dto.getSourceUrl()).isEqualTo("u");
        assertThat(dto.getImageUrl()).isEqualTo("https://www.seoul.go.kr/a.jpg");
    }

    @Test
    void 글_번호가_없으면_저장소를_부르지_않는다() {
        assertThat(service.findEventInfo(List.of())).isEmpty();
        verifyNoInteractions(recordRepository);
    }

    @Test
    void 이미지가_없으면_빈_목록() {
        User author = mock(User.class);
        when(author.getId()).thenReturn(52L);
        when(userRepository.findByIdAndDeletedAtIsNull(52L)).thenReturn(Optional.of(author));
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        service.ingest(posted().build());

        ArgumentCaptor<Post> post = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(post.capture());
        assertThat(post.getValue().getImageUrls()).isEmpty();
    }

    @Test
    void 제목_50자_초과는_거절() {
        assertThatThrownBy(() -> service.ingest(posted().title("가".repeat(51)).build()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 본문_1000자_초과는_거절() {
        assertThatThrownBy(() -> service.ingest(posted().content("가".repeat(1001)).build()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 운영_계정_번호가_없으면_게시를_거절() {
        EventIngestService noAuthor = new EventIngestService(recordRepository, postRepository, userRepository, "");
        assertThatThrownBy(() -> noAuthor.ingest(posted().build()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 처음_보는_번호만_돌려준다() {
        EventIngestRecord seen = EventIngestRecord.builder()
                .sourceCode("SEOUL").externalId("1").status(EventIngestStatus.POSTED).sourceUrl("u").build();
        when(recordRepository.findBySourceCodeAndExternalIdIn(any(), any())).thenReturn(List.of(seen));

        assertThat(service.findUnseen("SEOUL", List.of("1", "2"))).containsExactly("2");
    }

    private EventIngestRecord postedRecord(long postId, LocalDate start, LocalDate end) {
        return EventIngestRecord.builder()
                .sourceCode("SEOUL").externalId(String.valueOf(postId)).status(EventIngestStatus.POSTED)
                .postId(postId).sourceUrl("u").eventStartDate(start).eventEndDate(end).build();
    }

    @Test
    void 진행_상태로_글_번호를_거른다() {
        LocalDate today = LocalDate.of(2026, 10, 30);
        when(recordRepository.findByStatus(EventIngestStatus.POSTED)).thenReturn(List.of(
                postedRecord(1L, LocalDate.of(2026, 11, 8), null),                          // 예정
                postedRecord(2L, LocalDate.of(2026, 10, 29), LocalDate.of(2026, 11, 2)),    // 진행 중
                postedRecord(3L, LocalDate.of(2026, 10, 25), null),                          // 종료
                postedRecord(4L, null, null)                                                 // 날짜 없음
        ));

        assertThat(service.findPostIdsByPeriodStatus(EventPeriodStatus.UPCOMING, today)).containsExactly(1L);
        assertThat(service.findPostIdsByPeriodStatus(EventPeriodStatus.ONGOING, today)).containsExactly(2L);
        assertThat(service.findPostIdsByPeriodStatus(EventPeriodStatus.ENDED, today)).containsExactly(3L);
    }

    @Test
    void 목록_칸은_줄바꿈을_빈칸으로_바꿔_한_줄씩_저장한다() {
        User author = mock(User.class);
        when(author.getId()).thenReturn(52L);
        when(userRepository.findByIdAndDeletedAtIsNull(52L)).thenReturn(Optional.of(author));
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        service.ingest(posted().activities(List.of("첫째\n줄", " ", "둘째")).checks(null).build());

        ArgumentCaptor<EventIngestRecord> record = ArgumentCaptor.forClass(EventIngestRecord.class);
        verify(recordRepository).save(record.capture());
        assertThat(record.getValue().getActivities()).isEqualTo("첫째 줄\n둘째");
        assertThat(record.getValue().getChecks()).isNull();
    }
}
