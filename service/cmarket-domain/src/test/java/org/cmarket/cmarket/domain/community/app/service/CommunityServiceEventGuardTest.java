package org.cmarket.cmarket.domain.community.app.service;

import org.cmarket.cmarket.domain.auth.model.User;
import org.cmarket.cmarket.domain.auth.repository.UserRepository;
import org.cmarket.cmarket.domain.community.app.dto.CommentCreateCommand;
import org.cmarket.cmarket.domain.community.app.dto.PostCreateCommand;
import org.cmarket.cmarket.domain.community.app.dto.PostListDto;
import org.cmarket.cmarket.domain.community.app.dto.PostUpdateCommand;
import org.cmarket.cmarket.domain.community.app.exception.EventBoardWriteNotAllowedException;
import org.cmarket.cmarket.domain.community.model.BoardType;
import org.cmarket.cmarket.domain.community.model.Post;
import org.cmarket.cmarket.domain.community.repository.CommentRepository;
import org.cmarket.cmarket.domain.community.repository.PostRepository;
import org.cmarket.cmarket.domain.report.repository.UserBlockRepository;
import org.cmarket.cmarket.domain.view.app.service.ViewLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 행사 소식(EVENT)은 수집 주소로만 들어온다. 일반 글쓰기·수정으로 만들거나 옮기지 못하게 막는다.
 */
@ExtendWith(MockitoExtension.class)
class CommunityServiceEventGuardTest {

    @Mock PostRepository postRepository;
    @Mock UserRepository userRepository;
    @Mock CommentRepository commentRepository;
    @Mock UserBlockRepository userBlockRepository;
    @Mock ViewLogService viewLogService;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks CommunityServiceImpl communityService;

    @Test
    void createPost_withEventBoard_throws() {
        PostCreateCommand command = PostCreateCommand.builder()
                .title("제목").content("내용").boardType(BoardType.EVENT).build();

        assertThatThrownBy(() -> communityService.createPost("a@b.com", command))
                .isInstanceOf(EventBoardWriteNotAllowedException.class);
        verify(postRepository, never()).save(any());
    }

    @Test
    void updatePost_otherBoardToEvent_throws() {
        Post post = postOf(BoardType.INFO);
        when(postRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(post));
        PostUpdateCommand command = PostUpdateCommand.builder()
                .title("제목").content("내용").boardType(BoardType.EVENT).build();

        assertThatThrownBy(() -> communityService.updatePost(1L, command, "a@b.com"))
                .isInstanceOf(EventBoardWriteNotAllowedException.class);
        assertThat(post.getBoardType()).isEqualTo(BoardType.INFO);
    }

    @Test
    void updatePost_eventPostStayingEvent_allowed() {
        // 운영 계정이 자기 행사 글을 고치는 것은 막지 않는다(설계 7-1절)
        Post post = postOf(BoardType.EVENT);
        when(postRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(post));
        User author = mock(User.class);
        when(author.getId()).thenReturn(52L);
        when(userRepository.findByEmailAndDeletedAtIsNull("news@b.com")).thenReturn(Optional.of(author));
        PostUpdateCommand command = PostUpdateCommand.builder()
                .title("고친 제목").content("고친 내용").boardType(BoardType.EVENT).build();

        communityService.updatePost(1L, command, "news@b.com");

        assertThat(post.getTitle()).isEqualTo("고친 제목");
        assertThat(post.getBoardType()).isEqualTo(BoardType.EVENT);
    }

    private Post postOf(BoardType boardType) {
        return Post.builder().authorId(52L).authorNickname("커들소식")
                .title("제목").content("내용").boardType(boardType).build();
    }

    // 행사 소식을 진행 상태로 거를 때, 걸러진 글 번호만 목록에 남긴다(설계 7-2절)

    @Test
    void getPostList_onlyPostIds_passedToRepository() {
        when(postRepository.findPosts(any(), any(), any(), any(), any(), anyList(), eq(List.of(5L, 6L)), any()))
                .thenReturn(Page.empty());

        communityService.getPostList("latest", BoardType.EVENT, null, null, 0, 10, null, List.of(5L, 6L));

        verify(postRepository).findPosts(any(), any(), eq(BoardType.EVENT), any(), any(), anyList(), eq(List.of(5L, 6L)), any());
    }

    @Test
    void getPostList_emptyOnlyPostIds_returnsEmptyPageWithoutQuery() {
        PostListDto result = communityService.getPostList("latest", BoardType.EVENT, null, null, 0, 10, null, List.of());

        assertThat(result.posts().content()).isEmpty();
        assertThat(result.posts().totalElements()).isZero();
        verify(postRepository, never()).findPosts(any(), any(), any(), any(), any(), anyList(), any(), any());
    }

    // 행사 소식에는 댓글 영역을 두지 않는다. 화면에서만 숨기면 주소를 직접 불러 달 수 있다(설계 7-1절)

    @Test
    void createComment_onEventPost_throws() {
        when(postRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(postOf(BoardType.EVENT)));
        CommentCreateCommand command = CommentCreateCommand.builder().content("댓글").build();

        assertThatThrownBy(() -> communityService.createComment(1L, command, "a@b.com"))
                .isInstanceOf(EventBoardWriteNotAllowedException.class)
                .hasMessageContaining("댓글");
        verify(commentRepository, never()).save(any());
    }
}
