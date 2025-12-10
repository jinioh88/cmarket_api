package org.cmarket.cmarket.domain.community.app.service;

import org.cmarket.cmarket.domain.community.app.dto.PostCreateCommand;
import org.cmarket.cmarket.domain.community.app.dto.PostDetailDto;
import org.cmarket.cmarket.domain.community.app.dto.PostDto;
import org.cmarket.cmarket.domain.community.app.dto.PostListDto;
import org.cmarket.cmarket.domain.community.model.BoardType;

/**
 * 커뮤니티 서비스 인터페이스
 * 
 * 커뮤니티 관련 비즈니스 로직을 담당합니다.
 */
public interface CommunityService {
    
    /**
     * 게시글 등록
     * 
     * @param email 현재 로그인한 사용자 이메일
     * @param command 게시글 등록 명령
     * @return 생성된 게시글 정보
     */
    PostDto createPost(String email, PostCreateCommand command);
    
    /**
     * 게시글 목록 조회
     *
     * @param sortBy 정렬 기준 ("latest", "oldest", "views", "comments")
     * @param boardType 게시판 유형 (null이면 전체 조회)
     * @param searchType 검색 타입 ("title", "title_content", "writer", null이면 검색 안함)
     * @param keyword 검색어 (null이면 검색 안함)
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 게시글 목록
     */
    PostListDto getPostList(String sortBy, BoardType boardType, String searchType, String keyword, Integer page, Integer size);

    /**
     * 게시글 상세 조회
     *
     * @param postId 게시글 ID
     * @param email 현재 로그인한 사용자 이메일 (비로그인 시 null)
     * @return 게시글 상세 정보
     */
    PostDetailDto getPostDetail(Long postId, String email);
    
    /**
     * 게시글 수정
     * 
     * @param postId 게시글 ID
     * @param command 게시글 수정 명령
     * @param email 현재 로그인한 사용자 이메일
     * @return 수정된 게시글 정보
     */
    PostDto updatePost(Long postId, org.cmarket.cmarket.domain.community.app.dto.PostUpdateCommand command, String email);
    
    /**
     * 게시글 삭제
     * 
     * @param postId 게시글 ID
     * @param email 현재 로그인한 사용자 이메일
     */
    void deletePost(Long postId, String email);
    
    /**
     * 댓글 작성
     * 
     * @param postId 게시글 ID
     * @param command 댓글 작성 명령
     * @param email 현재 로그인한 사용자 이메일
     * @return 생성된 댓글 정보
     */
    org.cmarket.cmarket.domain.community.app.dto.CommentDto createComment(
            Long postId,
            org.cmarket.cmarket.domain.community.app.dto.CommentCreateCommand command,
            String email
    );
    
    /**
     * 댓글 목록 조회
     * 
     * @param postId 게시글 ID
     * @return 댓글 목록 (부모 댓글만)
     */
    org.cmarket.cmarket.domain.community.app.dto.CommentListDto getCommentList(Long postId);
    
    /**
     * 하위 댓글 목록 조회
     * 
     * @param commentId 부모 댓글 ID
     * @return 하위 댓글 목록
     */
    org.cmarket.cmarket.domain.community.app.dto.CommentListDto getReplyList(Long commentId);
    
    /**
     * 댓글 삭제
     * 
     * @param commentId 댓글 ID
     * @param email 현재 로그인한 사용자 이메일
     */
    void deleteComment(Long commentId, String email);
}

