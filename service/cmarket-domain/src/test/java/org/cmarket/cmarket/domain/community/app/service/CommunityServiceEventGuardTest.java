package org.cmarket.cmarket.domain.community.app.service;

import org.cmarket.cmarket.domain.auth.repository.UserRepository;
import org.cmarket.cmarket.domain.community.app.dto.PostCreateCommand;
import org.cmarket.cmarket.domain.community.app.dto.PostUpdateCommand;
import org.cmarket.cmarket.domain.community.app.exception.EventBoardWriteNotAllowedException;
import org.cmarket.cmarket.domain.community.model.BoardType;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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
    void updatePost_toEventBoard_throws() {
        PostUpdateCommand command = PostUpdateCommand.builder()
                .title("제목").content("내용").boardType(BoardType.EVENT).build();

        assertThatThrownBy(() -> communityService.updatePost(1L, command, "a@b.com"))
                .isInstanceOf(EventBoardWriteNotAllowedException.class);
        verify(postRepository, never()).findByIdAndDeletedAtIsNull(any());
    }
}
