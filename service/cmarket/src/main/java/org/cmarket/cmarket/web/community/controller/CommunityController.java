package org.cmarket.cmarket.web.community.controller;

import jakarta.validation.Valid;
import org.cmarket.cmarket.domain.community.app.service.CommunityService;
import org.cmarket.cmarket.web.common.response.ResponseCode;
import org.cmarket.cmarket.web.common.response.SuccessResponse;
import org.cmarket.cmarket.web.common.security.SecurityUtils;
import org.cmarket.cmarket.web.community.dto.CommentCreateRequest;
import org.cmarket.cmarket.web.community.dto.CommentListResponse;
import org.cmarket.cmarket.web.community.dto.CommentResponse;
import org.cmarket.cmarket.web.community.dto.PostCreateRequest;
import org.cmarket.cmarket.web.community.dto.PostDetailResponse;
import org.cmarket.cmarket.web.community.dto.PostListResponse;
import org.cmarket.cmarket.web.community.dto.PostResponse;
import org.cmarket.cmarket.web.community.dto.PostUpdateRequest;
import org.cmarket.cmarket.domain.community.model.BoardType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 커뮤니티 관련 컨트롤러
 * 
 * 게시글 등록, 조회 등 커뮤니티 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/community")
public class CommunityController {
    
    private final CommunityService communityService;
    
    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }
    
    /**
     * 게시글 등록
     * 
     * POST /api/community/posts
     * 
     * 현재 로그인한 사용자가 게시글을 등록합니다.
     * 
     * @param request 게시글 등록 요청
     * @return 생성된 게시글 정보
     */
    @PostMapping("/posts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<PostResponse>> createPost(
            @Valid @RequestBody PostCreateRequest request
    ) {
        // 현재 로그인한 사용자의 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 웹 DTO → 앱 DTO 변환
        org.cmarket.cmarket.domain.community.app.dto.PostCreateCommand command = request.toCommand();
        
        // 앱 서비스 호출
        org.cmarket.cmarket.domain.community.app.dto.PostDto postDto = communityService.createPost(email, command);
        
        // 앱 DTO → 웹 DTO 변환
        PostResponse response = PostResponse.fromDto(postDto);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse<>(ResponseCode.CREATED, response));
    }
    
    /**
     * 게시글 목록 조회
     * 
     * GET /api/community/posts
     * 
     * 커뮤니티 게시판의 게시글 목록을 조회합니다.
     * - 비회원도 조회 가능
     * - 정렬 기준: latest (최신순), oldest (오래된순), views (조회수 많은순), comments (댓글 많은순)
     * - 게시판 유형: QUESTION (질문있어요), INFO (정보공유)
     * - 검색 타입: title (제목), title_content (제목+내용), writer (작성자)
     * 
     * @param sortBy 정렬 기준 (기본값: "latest")
     * @param boardType 게시판 유형 (선택사항, null이면 전체 조회)
     * @param searchType 검색 타입 (선택사항: "title", "title_content", "writer")
     * @param keyword 검색어 (선택사항, searchType과 함께 사용)
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 20)
     * @return 게시글 목록
     */
    @GetMapping("/posts")
    public ResponseEntity<SuccessResponse<PostListResponse>> getPostList(
            @RequestParam(required = false, defaultValue = "latest") String sortBy,
            @RequestParam(required = false) BoardType boardType,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        // 앱 서비스 호출
        org.cmarket.cmarket.domain.community.app.dto.PostListDto postListDto = 
                communityService.getPostList(sortBy, boardType, searchType, keyword, page, size);
        
        // 앱 DTO → 웹 DTO 변환
        PostListResponse response = PostListResponse.fromDto(postListDto);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }
    
    /**
     * 게시글 상세 조회
     *
     * GET /api/community/posts/{postId}
     *
     * 커뮤니티 게시글의 상세 정보를 조회합니다.
     * - 비회원도 조회 가능
     * - 로그인했다면 조회수 증가 및 본인 여부 판단에 활용됩니다.
     *
     * @param postId 게시글 ID
     * @return 게시글 상세 정보
     */
    @GetMapping("/posts/{postId}")
    public ResponseEntity<SuccessResponse<PostDetailResponse>> getPostDetail(
            @PathVariable Long postId
    ) {
        String email = SecurityUtils.isAuthenticated() ? SecurityUtils.getCurrentUserEmail() : null;
        
        org.cmarket.cmarket.domain.community.app.dto.PostDetailDto postDetailDto =
                communityService.getPostDetail(postId, email);
        
        PostDetailResponse response = PostDetailResponse.fromDto(postDetailDto);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }
    
    /**
     * 게시글 수정
     * 
     * PATCH /api/community/posts/{postId}
     * 
     * 등록된 게시글의 정보를 수정합니다.
     * - 작성자 본인만 수정 가능
     * - 수정 가능한 항목: 제목, 내용, 이미지
     * 
     * @param postId 게시글 ID
     * @param request 게시글 수정 요청
     * @return 수정된 게시글 정보
     */
    @PatchMapping("/posts/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<PostResponse>> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest request
    ) {
        // 현재 로그인한 사용자의 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 웹 DTO → 앱 DTO 변환
        org.cmarket.cmarket.domain.community.app.dto.PostUpdateCommand command = request.toCommand();
        
        // 앱 서비스 호출
        org.cmarket.cmarket.domain.community.app.dto.PostDto postDto = 
                communityService.updatePost(postId, command, email);
        
        // 앱 DTO → 웹 DTO 변환
        PostResponse response = PostResponse.fromDto(postDto);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }
    
    /**
     * 게시글 삭제
     * 
     * DELETE /api/community/posts/{postId}
     * 
     * 등록된 게시글을 삭제합니다.
     * - 작성자 본인만 삭제 가능
     * - 게시글 삭제 시 관련 댓글도 함께 삭제됩니다.
     * 
     * @param postId 게시글 ID
     * @return 성공 응답
     */
    @DeleteMapping("/posts/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<Void>> deletePost(
            @PathVariable Long postId
    ) {
        // 현재 로그인한 사용자의 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 앱 서비스 호출
        communityService.deletePost(postId, email);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, null));
    }
    
    /**
     * 댓글 작성
     * 
     * POST /api/community/posts/{postId}/comments
     * 
     * 현재 로그인한 사용자가 게시글에 댓글을 작성합니다.
     * - 최대 3단계까지 작성 가능 (댓글 → 대댓글 → 대대댓글)
     * 
     * @param postId 게시글 ID
     * @param request 댓글 작성 요청
     * @return 생성된 댓글 정보
     */
    @PostMapping("/posts/{postId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<CommentResponse>> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request
    ) {
        // 현재 로그인한 사용자의 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 웹 DTO → 앱 DTO 변환
        org.cmarket.cmarket.domain.community.app.dto.CommentCreateCommand command = request.toCommand();
        
        // 앱 서비스 호출
        org.cmarket.cmarket.domain.community.app.dto.CommentDto commentDto = 
                communityService.createComment(postId, command, email);
        
        // 앱 DTO → 웹 DTO 변환
        CommentResponse response = CommentResponse.fromDto(commentDto);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse<>(ResponseCode.CREATED, response));
    }
    
    /**
     * 댓글 목록 조회
     * 
     * GET /api/community/posts/{postId}/comments
     * 
     * 게시글의 댓글 목록을 조회합니다.
     * - 부모 댓글만 조회 (하위 댓글은 별도 API로 조회)
     * - 비회원도 조회 가능
     * 
     * @param postId 게시글 ID
     * @return 댓글 목록
     */
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<SuccessResponse<CommentListResponse>> getCommentList(
            @PathVariable Long postId
    ) {
        // 앱 서비스 호출
        org.cmarket.cmarket.domain.community.app.dto.CommentListDto commentListDto = 
                communityService.getCommentList(postId);
        
        // 앱 DTO → 웹 DTO 변환
        CommentListResponse response = CommentListResponse.fromDto(commentListDto);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }
    
    /**
     * 하위 댓글 목록 조회
     * 
     * GET /api/community/comments/{commentId}/replies
     * 
     * 특정 댓글의 하위 댓글 목록을 조회합니다.
     * - 비회원도 조회 가능
     * 
     * @param commentId 부모 댓글 ID
     * @return 하위 댓글 목록
     */
    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<SuccessResponse<CommentListResponse>> getReplyList(
            @PathVariable Long commentId
    ) {
        // 앱 서비스 호출
        org.cmarket.cmarket.domain.community.app.dto.CommentListDto replyListDto = 
                communityService.getReplyList(commentId);
        
        // 앱 DTO → 웹 DTO 변환
        CommentListResponse response = CommentListResponse.fromDto(replyListDto);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }
    
    /**
     * 댓글 삭제
     * 
     * DELETE /api/community/comments/{commentId}
     * 
     * 등록된 댓글을 삭제합니다.
     * - 작성자 본인만 삭제 가능
     * - 하위 댓글이 있으면 댓글만 삭제 (하위 댓글은 유지)
     * - 하위 댓글이 없으면 댓글 삭제 및 게시글의 댓글 개수 감소
     * 
     * @param commentId 댓글 ID
     * @return 성공 응답
     */
    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<Void>> deleteComment(
            @PathVariable Long commentId
    ) {
        // 현재 로그인한 사용자의 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 앱 서비스 호출
        communityService.deleteComment(commentId, email);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, null));
    }
}

