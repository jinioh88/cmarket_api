package org.cmarket.cmarket.domain.community.app.service;

import lombok.RequiredArgsConstructor;
import org.cmarket.cmarket.domain.auth.model.User;
import org.cmarket.cmarket.domain.auth.repository.UserRepository;
import org.cmarket.cmarket.domain.community.app.dto.CommentSummaryDto;
import org.cmarket.cmarket.domain.community.app.dto.PostCreateCommand;
import org.cmarket.cmarket.domain.community.app.dto.PostDetailDto;
import org.cmarket.cmarket.domain.community.app.dto.PostDto;
import org.cmarket.cmarket.domain.community.app.dto.PostListDto;
import org.cmarket.cmarket.domain.community.app.dto.PostListItemDto;
import org.cmarket.cmarket.domain.community.app.exception.CommentAccessDeniedException;
import org.cmarket.cmarket.domain.community.model.BoardType;
import org.cmarket.cmarket.domain.community.app.exception.CommentDepthExceededException;
import org.cmarket.cmarket.domain.community.app.exception.CommentNotFoundException;
import org.cmarket.cmarket.domain.community.app.exception.InvalidImageCountException;
import org.cmarket.cmarket.domain.community.app.exception.PostAccessDeniedException;
import org.cmarket.cmarket.domain.community.app.exception.PostAlreadyDeletedException;
import org.cmarket.cmarket.domain.community.app.exception.PostNotFoundException;
import org.cmarket.cmarket.domain.community.model.Post;
import org.cmarket.cmarket.domain.community.model.Comment;
import org.cmarket.cmarket.domain.community.repository.PostRepository;
import org.cmarket.cmarket.domain.community.repository.CommentRepository;
import org.cmarket.cmarket.domain.profile.app.dto.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 커뮤니티 서비스 구현체
 * 
 * 커뮤니티 관련 비즈니스 로직을 구현합니다.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {
    
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    
    @Override
    public PostDto createPost(String email, PostCreateCommand command) {
        // 사용자 조회 (작성자 확인)
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new org.cmarket.cmarket.domain.auth.app.exception.UserNotFoundException("사용자를 찾을 수 없습니다."));
        
        Long authorId = user.getId();
        
        // 이미지 URL 개수 검증 (최대 5장)
        if (command.getImageUrls() != null && command.getImageUrls().size() > 5) {
            throw new IllegalArgumentException("이미지는 최대 5장까지 등록 가능합니다.");
        }
        
        // Post 엔티티 생성 (작성자 정보 스냅샷 저장)
        Post post = Post.builder()
                .authorId(authorId)
                .authorNickname(user.getNickname())
                .authorProfileImageUrl(user.getProfileImageUrl())
                .title(command.getTitle())
                .content(command.getContent())
                .imageUrls(command.getImageUrls())
                .boardType(command.getBoardType())
                .build();
        
        // 저장
        Post savedPost = postRepository.save(post);
        
        // DTO로 변환하여 반환
        return PostDto.fromEntity(savedPost);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PostListDto getPostList(String sortBy, BoardType boardType, Integer page, Integer size) {
        // 정렬 기준 기본값 설정
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "latest";
        }
        
        // 정렬 방향 설정
        String sortOrder = "desc";
        if ("oldest".equals(sortBy)) {
            sortOrder = "asc";
        }
        
        // 페이지네이션 정보 생성
        int pageNumber = page != null ? page : 0;
        int pageSize = size != null ? size : 20;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        
        // 게시글 목록 조회 (QueryDSL 사용)
        Page<Post> postPage = postRepository.findPosts(sortBy, sortOrder, boardType, pageable);
        
        // Post 엔티티를 PostListItemDto로 변환 (엔티티에 저장된 작성자 정보 사용)
        PageResult<PostListItemDto> pageResult = PageResult.fromPage(
                postPage.map(post -> {
                    String authorNickname = post.getAuthorNickname() != null 
                            ? post.getAuthorNickname() 
                            : "탈퇴한 사용자";
                    return PostListItemDto.fromEntity(post, authorNickname);
                })
        );
        
        return new PostListDto(pageResult);
    }
    
    @Override
    public PostDto updatePost(Long postId, org.cmarket.cmarket.domain.community.app.dto.PostUpdateCommand command, String email) {
        // 게시글 조회 (소프트 삭제된 게시글 제외)
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new PostNotFoundException());
        
        // 사용자 조회
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new org.cmarket.cmarket.domain.auth.app.exception.UserNotFoundException("사용자를 찾을 수 없습니다."));
        
        Long userId = user.getId();
        
        // 권한 확인: 작성자 본인만 수정 가능
        if (!post.getAuthorId().equals(userId)) {
            throw new PostAccessDeniedException();
        }
        
        // 이미지 URL 개수 검증 (최대 5장)
        if (command.getImageUrls() != null && command.getImageUrls().size() > 5) {
            throw new InvalidImageCountException();
        }
        
        // 게시글 정보 수정 (영속 상태 엔티티 변경은 트랜잭션 커밋 시 자동 반영)
        post.update(command.getTitle(), command.getContent(), command.getImageUrls(), command.getBoardType());
        
        // DTO로 변환하여 반환
        return PostDto.fromEntity(post);
    }
    
    @Override
    public void deletePost(Long postId, String email) {
        // 게시글 조회 (소프트 삭제된 게시글 제외)
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new PostNotFoundException());
        
        // 사용자 조회
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new org.cmarket.cmarket.domain.auth.app.exception.UserNotFoundException("사용자를 찾을 수 없습니다."));
        
        Long userId = user.getId();
        
        // 권한 확인: 작성자 본인만 삭제 가능
        // TODO: 향후 Admin 도메인 연동 시 어드민 계정도 삭제 가능하도록 권한 확인 로직 추가
        if (!post.getAuthorId().equals(userId)) {
            throw new PostAccessDeniedException();
        }
        
        // 게시글에 연결된 모든 댓글 소프트 삭제 (영속 상태 엔티티 변경은 트랜잭션 커밋 시 자동 반영)
        List<Comment> comments = commentRepository.findByPostIdAndDeletedAtIsNull(postId);
        for (Comment comment : comments) {
            comment.softDelete();
        }
        
        // 게시글 소프트 삭제 처리 (영속 상태 엔티티 변경은 트랜잭션 커밋 시 자동 반영)
        post.softDelete();
    }
    
    @Override
    @Transactional
    public org.cmarket.cmarket.domain.community.app.dto.CommentDto createComment(
            Long postId,
            org.cmarket.cmarket.domain.community.app.dto.CommentCreateCommand command,
            String email
    ) {
        // 게시글 존재 확인 (소프트 삭제된 게시글 제외)
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new PostNotFoundException());
        
        // 사용자 조회 (작성자 확인)
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new org.cmarket.cmarket.domain.auth.app.exception.UserNotFoundException("사용자를 찾을 수 없습니다."));
        
        Long authorId = user.getId();
        
        // depth 계산
        Integer depth;
        if (command.getParentId() != null) {
            // 부모 댓글 존재 확인 및 depth 조회 (소프트 삭제된 댓글 제외)
            Comment parentComment = commentRepository.findById(command.getParentId())
                    .orElseThrow(() -> new CommentNotFoundException("부모 댓글을 찾을 수 없습니다."));
            
            // 부모 댓글이 삭제되었는지 확인
            if (parentComment.isDeleted()) {
                throw new PostAlreadyDeletedException("삭제된 댓글에는 답글을 작성할 수 없습니다.");
            }
            
            // 부모 댓글이 같은 게시글의 댓글인지 확인
            if (!parentComment.getPostId().equals(postId)) {
                throw new IllegalArgumentException("다른 게시글의 댓글에는 답글을 작성할 수 없습니다.");
            }
            
            // 부모 댓글의 depth 확인
            Integer parentDepth = parentComment.getDepth();
            
            // 최대 3단계까지만 허용
            if (parentDepth >= 3) {
                throw new CommentDepthExceededException();
            }
            
            depth = parentDepth + 1;
        } else {
            // 부모 댓글이 없으면 depth = 1
            depth = 1;
        }
        
        // Comment 엔티티 생성 (작성자 정보 스냅샷 저장)
        Comment comment = Comment.builder()
                .postId(postId)
                .authorId(authorId)
                .authorNickname(user.getNickname())
                .authorProfileImageUrl(user.getProfileImageUrl())
                .parentId(command.getParentId())
                .content(command.getContent())
                .depth(depth)
                .build();
        
        // 저장
        Comment savedComment = commentRepository.save(comment);
        
        // Post의 commentCount 증가
        post.increaseCommentCount();
        postRepository.save(post);
        
        // DTO로 변환하여 반환
        return org.cmarket.cmarket.domain.community.app.dto.CommentDto.fromEntity(savedComment);
    }
    
    @Override
    @Transactional(readOnly = true)
    public org.cmarket.cmarket.domain.community.app.dto.CommentListDto getCommentList(Long postId) {
        // 게시글 존재 확인 (소프트 삭제된 게시글 제외)
        if (!postRepository.existsByIdAndDeletedAtIsNull(postId)) {
            throw new PostNotFoundException();
        }
        
        // 부모 댓글 목록 조회 (parentId가 null인 댓글만, 최신순)
        List<Comment> comments = commentRepository
                .findByPostIdAndParentIdIsNullAndDeletedAtIsNullOrderByCreatedAtAsc(postId);
        
        // 각 댓글의 하위 댓글 존재 여부 확인
        List<org.cmarket.cmarket.domain.community.app.dto.CommentListItemDto> commentDtos = comments.stream()
                .map(comment -> {
                    Boolean hasChildren = commentRepository.existsByParentIdAndDeletedAtIsNull(comment.getId());
                    return org.cmarket.cmarket.domain.community.app.dto.CommentListItemDto.fromEntity(comment, hasChildren);
                })
                .toList();
        
        return new org.cmarket.cmarket.domain.community.app.dto.CommentListDto(commentDtos);
    }
    
    @Override
    @Transactional(readOnly = true)
    public org.cmarket.cmarket.domain.community.app.dto.CommentListDto getReplyList(Long commentId) {
        // 부모 댓글 존재 확인 (소프트 삭제된 댓글 제외)
        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException());
        
        if (parentComment.isDeleted()) {
            throw new PostAlreadyDeletedException("삭제된 댓글의 하위 댓글을 조회할 수 없습니다.");
        }
        
        // 하위 댓글 목록 조회 (최신순)
        List<Comment> replies = commentRepository
                .findByParentIdAndDeletedAtIsNullOrderByCreatedAtAsc(commentId);
        
        // 각 댓글의 하위 댓글 존재 여부 확인
        List<org.cmarket.cmarket.domain.community.app.dto.CommentListItemDto> replyDtos = replies.stream()
                .map(reply -> {
                    Boolean hasChildren = commentRepository.existsByParentIdAndDeletedAtIsNull(reply.getId());
                    return org.cmarket.cmarket.domain.community.app.dto.CommentListItemDto.fromEntity(reply, hasChildren);
                })
                .toList();
        
        return new org.cmarket.cmarket.domain.community.app.dto.CommentListDto(replyDtos);
    }
    
    @Override
    @Transactional
    public void deleteComment(Long commentId, String email) {
        // 댓글 조회 (소프트 삭제된 댓글 제외)
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException());
        
        if (comment.isDeleted()) {
            throw new PostAlreadyDeletedException("이미 삭제된 댓글입니다.");
        }
        
        // 사용자 조회
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new org.cmarket.cmarket.domain.auth.app.exception.UserNotFoundException("사용자를 찾을 수 없습니다."));
        
        Long userId = user.getId();
        
        // 권한 확인: 작성자 본인만 삭제 가능
        // TODO: 향후 Admin 도메인 연동 시 어드민 계정도 삭제 가능하도록 권한 확인 로직 추가
        if (!comment.getAuthorId().equals(userId)) {
            throw new CommentAccessDeniedException();
        }
        
        // 하위 댓글 존재 여부 확인
        boolean hasChildren = commentRepository.existsByParentIdAndDeletedAtIsNull(commentId);
        
        // 댓글 소프트 삭제 처리 (영속 상태 엔티티 변경은 트랜잭션 커밋 시 자동 반영)
        comment.softDelete();
        
        // 하위 댓글이 없는 경우에만 Post의 commentCount 감소 (영속 상태 엔티티 변경은 트랜잭션 커밋 시 자동 반영)
        if (!hasChildren) {
            Post post = postRepository.findById(comment.getPostId())
                    .orElseThrow(() -> new PostNotFoundException());
            post.decreaseCommentCount();
        }
    }

    @Override
    public PostDetailDto getPostDetail(Long postId, String email) {
        // 게시글 조회 (소프트 삭제된 게시글 제외)
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new PostNotFoundException());

        // 조회수 증가 (로그인 사용자이면서 작성자 본인이 아닌 경우)
        increaseViewCount(post, email);

        // 댓글 목록 조회 (부모 댓글만)
        List<Comment> comments = commentRepository
                .findByPostIdAndParentIdIsNullAndDeletedAtIsNullOrderByCreatedAtAsc(postId);

        // 댓글 DTO 변환 (엔티티에 저장된 작성자 정보 사용)
        List<CommentSummaryDto> commentDtos = comments.stream()
                .map(comment -> CommentSummaryDto.fromEntity(
                        comment,
                        comment.getAuthorNickname() != null ? comment.getAuthorNickname() : "탈퇴한 사용자",
                        comment.getAuthorProfileImageUrl()
                ))
                .toList();

        // 게시글 상세 DTO 생성 (엔티티에 저장된 작성자 정보 사용)
        return PostDetailDto.fromEntity(
                post,
                post.getAuthorNickname() != null ? post.getAuthorNickname() : "탈퇴한 사용자",
                post.getAuthorProfileImageUrl(),
                commentDtos
        );
    }

    /**
     * 조회수 증가 처리
     *
     * 로그인한 사용자가 게시글을 조회했을 때, 작성자가 아니라면 조회수를 증가시킵니다.
     *
     * @param post 게시글 엔티티
     * @param email 현재 로그인한 사용자 이메일 (비로그인 시 null)
     */
    private void increaseViewCount(Post post, String email) {
        if (email == null) {
            return;
        }

        Long viewerId = userRepository.findByEmailAndDeletedAtIsNull(email)
                .map(User::getId)
                .orElse(null);

        if (viewerId == null) {
            return;
        }

        if (post.getAuthorId().equals(viewerId)) {
            return;  // 작성자가 본인일 경우 조회수 증가 없음
        }

        post.increaseViewCount();
    }
}

