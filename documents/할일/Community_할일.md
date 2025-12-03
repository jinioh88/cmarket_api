# Community 기능 구현 할일

> 반려동물 용품 중고거래 서비스 - **Community** 영역 구현 작업 목록

---

## 개요

본 문서는 `05_Community.md` 요구사항 정의서를 기반으로 구현할 작업을 순차적으로 정리한 것입니다.
아키텍처 가이드(`architecture-guide.md`) 원칙을 준수하여 구현합니다.

---

## 구현 순서

### Step 1: 도메인 모델 생성

#### 1-1. Post 엔티티 생성
- **작업 내용**:
  - 커뮤니티 게시글 정보를 저장하는 엔티티 생성
  - 필드:
    - id, authorId (작성자 ID, User 참조)
    - title (제목, 2-50자)
    - content (내용, 2-1000자)
    - imageUrls (이미지 URL 리스트, 최대 5장, JSON 또는 별도 테이블)
    - viewCount (조회수, 기본값 0)
    - commentCount (댓글 개수, 기본값 0)
    - createdAt, updatedAt, deletedAt
  - 소프트 삭제 지원 (deletedAt)
  - 인덱스: authorId, createdAt
  - Post 엔티티에 비즈니스 메서드 추가:
    - `update(String title, String content, List<String> imageUrls)`: 게시글 정보 수정
    - `increaseViewCount()`: 조회수 증가
    - `increaseCommentCount()`: 댓글 개수 증가
    - `decreaseCommentCount()`: 댓글 개수 감소
    - `softDelete()`: 소프트 삭제
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/community/model/Post.java`

#### 1-2. Comment 엔티티 생성
- **작업 내용**:
  - 게시글 댓글 정보를 저장하는 엔티티 생성
  - 필드:
    - id, postId (게시글 ID, Post 참조)
    - authorId (작성자 ID, User 참조)
    - parentId (부모 댓글 ID, 대댓글/대대댓글용, nullable)
    - content (내용, 2-500자)
    - depth (댓글 깊이, 1=댓글, 2=대댓글, 3=대대댓글)
    - createdAt, updatedAt, deletedAt
  - 소프트 삭제 지원 (deletedAt)
  - 인덱스: postId, parentId, createdAt
  - Comment 엔티티에 비즈니스 메서드 추가:
    - `update(String content)`: 댓글 내용 수정
    - `softDelete()`: 소프트 삭제
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/community/model/Comment.java`

---

### Step 2: 도메인 레포지토리 인터페이스 생성

#### 2-1. PostRepository 인터페이스 생성
- **작업 내용**:
  - `JpaRepository<Post, Long>`, `PostRepositoryCustom` 상속
  - 작성자별 게시글 목록 조회: `Page<Post> findByAuthorIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long authorId, Pageable pageable)`
  - 소프트 삭제된 게시글 제외 조회: `Optional<Post> findByIdAndDeletedAtIsNull(Long id)`
  - 게시글 존재 확인: `boolean existsByIdAndDeletedAtIsNull(Long id)`
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/community/repository/PostRepository.java`

#### 2-2. PostRepositoryCustom 인터페이스 및 구현체 생성
- **작업 내용**:
  - QueryDSL을 사용한 게시글 목록 조회 메서드 정의
  - `PostRepositoryCustom` 인터페이스 생성
  - `PostRepositoryCustomImpl` 구현체 생성 (JPAQueryFactory 사용)
  - 게시글 목록 조회 메서드: `Page<Post> findPosts(String sortBy, String sortOrder, Pageable pageable)`
    - sortBy: 정렬 기준 ("latest", "oldest", "views", "comments")
    - sortOrder: 정렬 방향 ("asc", "desc") - 기본값 "desc"
    - 소프트 삭제된 게시글 제외 (deletedAt IS NULL)
    - 정렬 기준에 따라 동적으로 정렬:
      - "latest": createdAt DESC (기본값)
      - "oldest": createdAt ASC
      - "views": viewCount DESC
      - "comments": commentCount DESC
  - 화이트리스트 검증으로 인젝션 방지
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/community/repository/PostRepositoryCustom.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/community/repository/PostRepositoryCustomImpl.java`

#### 2-3. CommentRepository 인터페이스 생성
- **작업 내용**:
  - `JpaRepository<Comment, Long>` 상속
  - 게시글별 댓글 목록 조회 (부모 댓글만): `List<Comment> findByPostIdAndParentIdIsNullAndDeletedAtIsNullOrderByCreatedAtAsc(Long postId)`
  - 부모 댓글별 하위 댓글 목록 조회: `List<Comment> findByParentIdAndDeletedAtIsNullOrderByCreatedAtAsc(Long parentId)`
  - 게시글별 댓글 개수 조회: `long countByPostIdAndDeletedAtIsNull(Long postId)`
  - 게시글별 모든 댓글 삭제: `void deleteByPostId(Long postId)` (소프트 삭제)
  - 댓글 존재 확인: `boolean existsByIdAndDeletedAtIsNull(Long id)`
  - 부모 댓글의 depth 조회: `Optional<Integer> findDepthById(Long id)`
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/community/repository/CommentRepository.java`

---

### Step 3: 커뮤니티 게시글 등록 구현

#### 3-1. 게시글 등록 웹 DTO 및 앱 DTO 생성
- **작업 내용**:
  - 웹 DTO: `PostCreateRequest` (title, content, imageUrls)
  - 검증 어노테이션 추가:
    - title: @NotBlank, @Size(min=2, max=50)
    - content: @NotBlank, @Size(min=2, max=1000)
    - imageUrls: @Size(max=5) (최대 5장)
  - 앱 DTO: `PostCreateCommand`
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/community/dto/PostCreateRequest.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/community/app/dto/PostCreateCommand.java`

#### 3-2. 게시글 등록 앱 서비스 구현
- **작업 내용**:
  - `CommunityService.createPost()` 메서드 구현
  - 현재 로그인한 사용자 ID로 작성자 확인
  - 이미지 URL 개수 검증 (최대 5장)
  - Post 엔티티 생성 및 저장
  - PostDto 반환
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/community/app/service/CommunityService.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/community/app/service/CommunityServiceImpl.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/community/app/dto/PostDto.java`

#### 3-3. 게시글 등록 컨트롤러 구현
- **작업 내용**:
  - `POST /api/community/posts` 엔드포인트
  - 인증 필수 (`@PreAuthorize("isAuthenticated()")`)
  - 현재 로그인한 사용자 정보 추출 (SecurityUtils 사용)
  - 웹 DTO → 앱 DTO 변환
  - 앱 서비스 호출
  - `SuccessResponse<PostResponse>` 반환
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/community/controller/CommunityController.java`
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/community/dto/PostResponse.java`

#### 3-4. 게시글 이미지 업로드 연동
- **작업 내용**:
  - 게시글 등록 시 이미지 업로드는 기존 ImageController 활용
  - 이미지 업로드 후 반환된 URL을 imageUrls에 포함하여 게시글 등록
  - 이미지 유효성 검증 (파일 형식, 크기 제한: 한 장당 5MB, 총 25MB)
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/community/controller/CommunityController.java` (수정)

---

### Step 4: 커뮤니티 게시글 목록 조회 구현

#### 4-1. 게시글 목록 조회 웹 DTO 및 앱 DTO 생성
- **작업 내용**:
  - 웹 DTO: `PostListResponse` (게시글 목록, 페이지네이션 정보)
  - 게시글 정보: id, title, authorNickname, viewCount, commentCount, createdAt, updatedAt, isModified
  - isModified: updatedAt이 createdAt과 다르면 true (수정됨 표시용)
  - 앱 DTO: `PostListDto` (동일한 필드)
  - 페이지네이션: PageResult 사용
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/community/dto/PostListResponse.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/community/app/dto/PostListDto.java`

#### 4-2. 게시글 목록 조회 앱 서비스 구현
- **작업 내용**:
  - `CommunityService.getPostList()` 메서드 구현
  - PostRepositoryCustom의 `findPosts()` 메서드 호출 (정렬 기준과 정렬 방향 파라미터 전달)
  - sortBy 값 변환: "latest" → "createdAt", "oldest" → "createdAt", "views" → "viewCount", "comments" → "commentCount"
  - sortOrder 기본값: "desc" (최신순, 조회수 많은순, 댓글 많은순), "oldest"의 경우 "asc"
  - 작성자 정보 조회 (User 엔티티)
  - isModified 계산 (updatedAt != createdAt)
  - PostListDto 반환
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/community/app/service/CommunityService.java` (메서드 추가)

#### 4-3. 게시글 목록 조회 컨트롤러 구현
- **작업 내용**:
  - `GET /api/community/posts` 엔드포인트
  - 인증 선택 (비회원도 조회 가능)
  - 쿼리 파라미터: sortBy (기본값: "latest"), page, size (기본값: page=0, size=20)
  - sortBy 값: "latest" (최신순), "oldest" (오래된순), "views" (조회수 많은순), "comments" (댓글 많은순)
  - 앱 서비스 호출
  - 앱 DTO → 웹 DTO 변환
  - `SuccessResponse<PostListResponse>` 반환
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/community/controller/CommunityController.java` (메서드 추가)

---

### Step 5: 커뮤니티 게시글 상세 조회 구현

#### 5-1. 게시글 상세 조회 웹 DTO 및 앱 DTO 생성
- **작업 내용**:
  - 웹 DTO: `PostDetailResponse` (게시글 상세 정보, 작성자 정보, 댓글 목록)
  - 게시글 정보: id, title, content, imageUrls, authorId, authorNickname, authorProfileImageUrl, viewCount, commentCount, createdAt, updatedAt
  - 댓글 목록: CommentListResponse와 동일한 구조 (부모 댓글만, 하위 댓글은 별도 API로 조회)
  - 앱 DTO: `PostDetailDto` (동일한 필드)
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/community/dto/PostDetailResponse.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/community/app/dto/PostDetailDto.java`

#### 5-2. 게시글 상세 조회 앱 서비스 구현
- **작업 내용**:
  - `CommunityService.getPostDetail()` 메서드 구현
  - postId로 게시글 조회 (소프트 삭제된 게시글 제외)
  - 작성자 정보 조회 (User 엔티티)
  - 부모 댓글 목록 조회 (최상위 댓글만, 최신순)
  - 댓글 작성자 정보 조회 (User 엔티티)
  - PostDetailDto 반환
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/community/app/service/CommunityService.java` (메서드 추가)

#### 5-3. 게시글 상세 조회 컨트롤러 구현
- **작업 내용**:
  - `GET /api/community/posts/{postId}` 엔드포인트
  - 인증 선택 (비회원도 조회 가능)
  - 앱 서비스 호출
  - 앱 DTO → 웹 DTO 변환
  - `SuccessResponse<PostDetailResponse>` 반환
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/community/controller/CommunityController.java` (메서드 추가)

#### 5-4. 게시글 조회수 증가 구현
- **작업 내용**:
  - `CommunityService.increaseViewCount()` 메서드 구현
  - 작성자 본인이 조회한 경우 조회수 증가하지 않음 (authorId와 현재 사용자 ID 비교)
  - 작성자가 아닌 경우에만 Post 엔티티의 viewCount 증가
  - 중복 방지 로직 없음 (같은 사용자가 여러 번 조회해도 매번 증가)
  - 상세 조회 API 호출 시 조회수 증가 API도 함께 호출 (프론트엔드에서 처리하거나, 백엔드에서 비동기 처리)
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/community/app/service/CommunityService.java` (메서드 추가)
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/community/controller/CommunityController.java` (메서드 추가 또는 수정)

---

### Step 6: 커뮤니티 게시글 수정 구현

#### 6-1. 게시글 수정 웹 DTO 및 앱 DTO 생성
- **작업 내용**:
  - 웹 DTO: `PostUpdateRequest` (title, content, imageUrls)
  - 검증 어노테이션 추가 (등록과 동일)
  - 앱 DTO: `PostUpdateCommand`
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/community/dto/PostUpdateRequest.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/community/app/dto/PostUpdateCommand.java`

#### 6-2. 게시글 수정 앱 서비스 구현
- **작업 내용**:
  - `CommunityService.updatePost()` 메서드 구현
  - postId로 게시글 조회 후 authorId로 권한 확인 (post.getAuthorId()와 비교)
  - 이미지 URL 개수 검증 (최대 5장)
  - Post 엔티티의 `update()` 메서드 호출
  - 업데이트된 Post 정보를 DTO로 변환하여 반환
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/community/app/service/CommunityService.java` (메서드 추가)

#### 6-3. 게시글 수정 컨트롤러 구현
- **작업 내용**:
  - `PATCH /api/community/posts/{postId}` 엔드포인트
  - 인증 필수 (`@PreAuthorize("isAuthenticated()")`)
  - 현재 로그인한 사용자 정보 추출
  - 웹 DTO → 앱 DTO 변환
  - 앱 서비스 호출
  - `SuccessResponse<PostResponse>` 반환
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/community/controller/CommunityController.java` (메서드 추가)

---

### Step 7: 커뮤니티 게시글 삭제 구현

#### 7-1. 게시글 삭제 앱 서비스 구현
- **작업 내용**:
  - `CommunityService.deletePost()` 메서드 구현
  - postId로 게시글 조회 후 authorId로 권한 확인 (post.getAuthorId()와 비교)
  - 어드민 계정도 삭제 가능하도록 권한 확인 로직 추가 (향후 Admin 도메인 연동)
  - 게시글에 연결된 모든 댓글 소프트 삭제 (CommentRepository 사용)
  - Post 엔티티의 `softDelete()` 메서드 호출
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/community/app/service/CommunityService.java` (메서드 추가)

#### 7-2. 게시글 삭제 컨트롤러 구현
- **작업 내용**:
  - `DELETE /api/community/posts/{postId}` 엔드포인트
  - 인증 필수 (`@PreAuthorize("isAuthenticated()")`)
  - 현재 로그인한 사용자 정보 추출
  - 앱 서비스 호출
  - `SuccessResponse` 반환
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/community/controller/CommunityController.java` (메서드 추가)

---

### Step 8: 게시글 댓글 작성 구현

#### 8-1. 댓글 작성 웹 DTO 및 앱 DTO 생성
- **작업 내용**:
  - 웹 DTO: `CommentCreateRequest` (content, parentId)
  - 검증 어노테이션 추가:
    - content: @NotBlank, @Size(min=2, max=500)
    - parentId: 선택적 (대댓글/대대댓글 작성 시)
  - 앱 DTO: `CommentCreateCommand`
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/community/dto/CommentCreateRequest.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/community/app/dto/CommentCreateCommand.java`

#### 8-2. 댓글 작성 앱 서비스 구현
- **작업 내용**:
  - `CommunityService.createComment()` 메서드 구현
  - postId로 게시글 존재 확인 (소프트 삭제된 게시글 제외)
  - 현재 로그인한 사용자 ID로 작성자 확인
  - parentId가 있는 경우:
    - 부모 댓글 존재 확인
    - 부모 댓글의 depth 조회
    - depth가 3 이상이면 예외 발생 (최대 3단계까지만 허용)
    - depth = 부모 댓글의 depth + 1
  - parentId가 없는 경우: depth = 1
  - Comment 엔티티 생성 및 저장
  - Post의 commentCount 증가
  - CommentDto 반환
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/community/app/service/CommunityService.java` (메서드 추가)
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/community/app/dto/CommentDto.java`

#### 8-3. 댓글 작성 컨트롤러 구현
- **작업 내용**:
  - `POST /api/community/posts/{postId}/comments` 엔드포인트
  - 인증 필수 (`@PreAuthorize("isAuthenticated()")`)
  - 현재 로그인한 사용자 정보 추출
  - 웹 DTO → 앱 DTO 변환
  - 앱 서비스 호출
  - `SuccessResponse<CommentResponse>` 반환
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/community/controller/CommunityController.java` (메서드 추가)
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/community/dto/CommentResponse.java`

---

### Step 9: 게시글 댓글 목록 조회 구현

#### 9-1. 댓글 목록 조회 웹 DTO 및 앱 DTO 생성
- **작업 내용**:
  - 웹 DTO: `CommentListResponse` (댓글 목록)
  - 댓글 정보: id, content, authorId, authorNickname, authorProfileImageUrl, createdAt, depth, parentId, hasChildren (하위 댓글 존재 여부)
  - 하위 댓글 목록은 별도 API로 조회 (더보기 버튼 클릭 시)
  - 앱 DTO: `CommentListDto` (동일한 필드)
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/community/dto/CommentListResponse.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/community/app/dto/CommentListDto.java`

#### 9-2. 댓글 목록 조회 앱 서비스 구현
- **작업 내용**:
  - `CommunityService.getCommentList()` 메서드 구현
  - postId로 게시글 존재 확인
  - 부모 댓글 목록 조회 (parentId가 null인 댓글만, 최신순)
  - 각 댓글의 하위 댓글 존재 여부 확인 (hasChildren)
  - 댓글 작성자 정보 조회 (User 엔티티)
  - CommentListDto 반환
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/community/app/service/CommunityService.java` (메서드 추가)

#### 9-3. 댓글 목록 조회 컨트롤러 구현
- **작업 내용**:
  - `GET /api/community/posts/{postId}/comments` 엔드포인트
  - 인증 선택 (비회원도 조회 가능)
  - 앱 서비스 호출
  - 앱 DTO → 웹 DTO 변환
  - `SuccessResponse<CommentListResponse>` 반환
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/community/controller/CommunityController.java` (메서드 추가)

#### 9-4. 하위 댓글 목록 조회 구현
- **작업 내용**:
  - `GET /api/community/comments/{commentId}/replies` 엔드포인트
  - 인증 선택 (비회원도 조회 가능)
  - parentId로 하위 댓글 목록 조회 (최신순)
  - 댓글 작성자 정보 조회 (User 엔티티)
  - `SuccessResponse<CommentListResponse>` 반환
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/community/app/service/CommunityService.java` (메서드 추가)
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/community/controller/CommunityController.java` (메서드 추가)

---

### Step 10: 게시글 댓글 삭제 구현

#### 10-1. 댓글 삭제 앱 서비스 구현
- **작업 내용**:
  - `CommunityService.deleteComment()` 메서드 구현
  - commentId로 댓글 조회 후 authorId로 권한 확인 (comment.getAuthorId()와 비교)
  - 어드민 계정도 삭제 가능하도록 권한 확인 로직 추가 (향후 Admin 도메인 연동)
  - 하위 댓글 존재 여부 확인
  - 하위 댓글이 있으면: 댓글만 소프트 삭제 (하위 댓글은 유지)
  - 하위 댓글이 없으면: 댓글 소프트 삭제
  - Post의 commentCount 감소 (하위 댓글이 없는 경우에만)
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/community/app/service/CommunityService.java` (메서드 추가)

#### 10-2. 댓글 삭제 컨트롤러 구현
- **작업 내용**:
  - `DELETE /api/community/comments/{commentId}` 엔드포인트
  - 인증 필수 (`@PreAuthorize("isAuthenticated()")`)
  - 현재 로그인한 사용자 정보 추출
  - 앱 서비스 호출
  - `SuccessResponse` 반환
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/community/controller/CommunityController.java` (메서드 추가)

---

### Step 11: 커스텀 예외 클래스 생성

#### 11-1. Community 관련 예외 클래스 생성
- **작업 내용**:
  - `PostNotFoundException` (게시글 없음)
  - `PostAccessDeniedException` (게시글 접근 권한 없음 - 본인 게시글만 수정/삭제 가능)
  - `PostAlreadyDeletedException` (이미 삭제된 게시글)
  - `CommentNotFoundException` (댓글 없음)
  - `CommentAccessDeniedException` (댓글 접근 권한 없음 - 본인 댓글만 삭제 가능)
  - `CommentDepthExceededException` (댓글 depth 초과 - 최대 3단계까지만 허용)
  - `InvalidImageCountException` (이미지 개수 초과 - 최대 5장)
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/community/app/exception/` 패키지 내 예외 클래스들

#### 11-2. GlobalExceptionHandler에 커스텀 예외 처리 추가
- **작업 내용**:
  - 각 커스텀 예외에 대한 핸들러 메서드 추가
  - 적절한 HTTP 상태 코드 및 에러 메시지 반환
  - traceId 포함하여 로깅
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/common/exception/GlobalExceptionHandler.java` (수정)

---

## 구현 시 주의사항

1. **아키텍처 원칙 준수**:
   - 웹 → 앱 → 도메인 의존 방향 준수
   - 도메인 모델은 웹 계층에서 직접 사용 금지
   - DTO 변환 필수

2. **게시글 이미지**:
   - 이미지 업로드는 기존 ImageController 활용
   - 이미지 파일 형식 검증 (jpg, png, gif 등)
   - 이미지 크기 제한: 한 장당 5MB, 총 25MB
   - 최대 5장까지 업로드 가능
   - imageUrls는 JSON 배열로 저장 (또는 별도 테이블)

3. **게시글 제목 및 내용**:
   - 제목: 최소 2자 ~ 최대 50자 (띄어쓰기 포함)
   - 내용: 최소 2자 ~ 최대 1000자

4. **댓글 내용**:
   - 댓글 내용: 최소 2자 ~ 최대 500자

5. **댓글 depth 제한**:
   - 최대 3단계까지만 허용 (댓글 → 대댓글 → 대대댓글)
   - 4단계 이상 요청 시 예외 발생

6. **조회수 관리**:
   - 로그인한 사용자만 조회수 증가
   - 작성자 본인이 조회한 경우 조회수 증가하지 않음 (authorId와 현재 사용자 ID 비교)
   - 중복 방지 로직 없음 (같은 사용자가 여러 번 조회해도 매번 증가)

7. **댓글 개수 관리**:
   - 댓글 작성 시 Post의 commentCount 증가
   - 댓글 삭제 시 Post의 commentCount 감소 (하위 댓글이 없는 경우에만)
   - 게시글 삭제 시 모든 댓글도 함께 삭제

8. **소프트 삭제**:
   - Post, Comment 엔티티는 deletedAt으로 소프트 삭제
   - 조회 시 deletedAt이 null인 것만 조회
   - 삭제 후 복구 불가능

9. **예외 처리**:
   - 모든 예외는 GlobalExceptionHandler에서 처리
   - traceId 포함하여 로깅
   - 사용자 친화적인 에러 메시지 반환

10. **인증/인가**:
    - 게시글 등록/수정/삭제는 인증 필수
    - 본인 게시글만 수정/삭제 가능
    - 댓글 작성/삭제는 인증 필수
    - 본인 댓글만 삭제 가능
    - 목록/상세 조회는 비로그인도 가능

11. **페이지네이션**:
    - Spring Data `Page` 직노출 금지
    - `PageResult<T>` 전용 타입 사용 (아키텍처 가이드 준수)
    - 게시글 목록: 20개씩

12. **QueryDSL 사용**:
    - 게시글 목록 조회는 QueryDSL을 사용하여 정렬 기준을 동적으로 처리
    - PostRepository는 PostRepositoryCustom을 상속받아 커스텀 메서드 사용
    - 정렬 기준 화이트리스트 검증으로 인젝션 방지
    - ProductRepositoryCustom과 동일한 패턴으로 구현

12. **댓글 목록 조회**:
    - 부모 댓글(Depth 1)은 기본적으로 전체 표시
    - 하위 댓글(대댓글, 대대댓글)은 "더보기" 버튼 클릭 시 별도 API로 조회
    - 각 댓글에는 작성자 닉네임, 내용, 작성일, 작성시간, 프로필 이미지 표시

13. **게시글 수정 표시**:
    - 게시글이 수정되었다면 목록의 제목에서 (수정됨) 표시
    - isModified: updatedAt != createdAt
    - 수정된 일시는 목록에 마우스 hover 시 표시 (프론트엔드 처리)

14. **댓글 삭제 정책**:
    - 상위 댓글만 삭제 가능
    - 하위 대댓글 존재 시 상위 댓글은 삭제되지만 하위 댓글은 유지
    - 하위 댓글이 없는 경우에만 Post의 commentCount 감소

15. **어드민 권한**:
    - 게시글 삭제: 본인 또는 어드민 계정 가능
    - 댓글 삭제: 본인 또는 관리자 계정 가능
    - 향후 Admin 도메인 연동 예정

---

## 완료 체크리스트

- [ ] Step 1: 도메인 모델 생성
- [ ] Step 2: 도메인 레포지토리 인터페이스 생성
- [ ] Step 3: 커뮤니티 게시글 등록 구현
- [ ] Step 4: 커뮤니티 게시글 목록 조회 구현
- [ ] Step 5: 커뮤니티 게시글 상세 조회 구현
- [ ] Step 6: 커뮤니티 게시글 수정 구현
- [ ] Step 7: 커뮤니티 게시글 삭제 구현
- [ ] Step 8: 게시글 댓글 작성 구현
- [ ] Step 9: 게시글 댓글 목록 조회 구현
- [ ] Step 10: 게시글 댓글 삭제 구현
- [ ] Step 11: 커스텀 예외 클래스 생성

---

## 참고사항

- 각 Step을 완료한 후 사용자 리뷰를 받고 다음 Step을 진행합니다.
- 아키텍처 가이드의 원칙을 반드시 준수합니다.
- 테스트는 각 Step 완료 후 작성합니다.
- 게시글 이미지는 기존 ImageController를 활용하여 업로드합니다.
- 조회수는 작성자 본인인 경우 증가하지 않습니다.
- 댓글은 최대 3단계까지만 허용됩니다.

