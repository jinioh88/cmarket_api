package org.cmarket.cmarket.domain.eventfeed.app;

import org.cmarket.cmarket.domain.auth.model.User;
import org.cmarket.cmarket.domain.auth.repository.UserRepository;
import org.cmarket.cmarket.domain.community.model.BoardType;
import org.cmarket.cmarket.domain.community.model.Post;
import org.cmarket.cmarket.domain.community.repository.PostRepository;
import org.cmarket.cmarket.domain.eventfeed.app.dto.EventIngestCommand;
import org.cmarket.cmarket.domain.eventfeed.app.dto.EventIngestResult;
import org.cmarket.cmarket.domain.eventfeed.model.EventIngestRecord;
import org.cmarket.cmarket.domain.eventfeed.model.EventIngestStatus;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
                .eventStartDate(LocalDate.of(2026, 10, 31));
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
}
