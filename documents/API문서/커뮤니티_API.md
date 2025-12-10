# 커뮤니티 API 문서

> 반려동물 용품 중고거래 서비스 – **커뮤니티** API

본 문서는 커뮤니티 게시판 관련 API에 대한 상세 설명을 제공합니다.  
프론트엔드 개발자가 클라이언트 기능을 구현할 때 필요한 요청/응답 규격과 Enum 목록을 모두 포함합니다.

---

## 목차

1. [공통 사항](#공통-사항)
2. [게시글 API](#게시글-api)
   - [2-1. 게시글 등록](#2-1-게시글-등록-post-apicommunityposts)
   - [2-2. 게시글 목록 조회](#2-2-게시글-목록-조회-get-apicommunityposts)
   - [2-3. 게시글 상세 조회](#2-3-게시글-상세-조회-get-apicommunitypostspostid)
   - [2-4. 게시글 수정](#2-4-게시글-수정-patch-apicommunitypostspostid)
   - [2-5. 게시글 삭제](#2-5-게시글-삭제-delete-apicommunitypostspostid)
3. [댓글 API](#댓글-api)
   - [3-1. 댓글 작성](#3-1-댓글-작성-post-apicommunitypostspostidcomments)
   - [3-2. 댓글 목록 조회](#3-2-댓글-목록-조회-get-apicommunitypostspostidcomments)
   - [3-3. 하위 댓글 목록 조회](#3-3-하위-댓글-목록-조회-get-apicommunitycommentscommentidreplies)
   - [3-4. 댓글 삭제](#3-4-댓글-삭제-delete-apicommunitycommentscommentid)
4. [FAQ 및 참고](#faq-및-참고)

---

## 공통 사항

### Base URL

```
http://localhost:8080
```

### 공통 헤더

| 헤더명        | 타입   | 필수 | 설명                                  |
|--------------|--------|------|---------------------------------------|
| Authorization| String | 조건 | `Bearer <Access Token>`. **필요 시만**|
| Content-Type | String | 조건 | JSON 요청: `application/json`         |
| X-Trace-Id   | String | 옵션 | 요청 추적 ID (자동 생성 가능)         |

> `Authorization`은 인증이 필요한 모든 API(등록/수정/삭제)에 필수입니다.  
> 목록/상세 조회 API는 인증 없이 호출할 수 있지만, 로그인 시 조회수 증가 등이 반영됩니다.

### 공통 응답 형식

#### 성공
```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": { ... }   // API별 응답 본문
}
```

#### 에러
```json
{
  "code": "BAD_REQUEST",
  "message": "에러 메시지",
  "traceId": "e1e4-...",
  "timestamp": "2025-01-15T10:30:00"
}
```

### 페이지네이션 파라미터

목록 조회 API는 Spring `page`/`size` 쿼리 파라미터를 사용합니다.  
기본값: `page=0`, `size=20`.  
응답은 `page`, `size`, `total`, `content`, `totalPages`, `hasNext`, `hasPrevious`, `totalElements`, `numberOfElements` 등을 포함합니다.

---

## 게시글 API

### 2-1. 게시글 등록 (POST /api/community/posts)

- **인증 필요**: 예 (`Bearer` 토큰)
- **설명**: 로그인 사용자가 커뮤니티 게시글을 생성합니다.

#### Request Body (`PostCreateRequest`)

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| title | String | 예 | 제목 (2~50자) |
| content | String | 예 | 내용 (2~1000자) |
| imageUrls | String[] | 옵션 | 이미지 URL 배열 (최대 5장) |
| boardType | String | 예 | 게시판 유형: `FREE`(자유게시판), `QUESTION`(질문있어요), `INFO`(정보공유) |

#### Request 예시

```json
{
  "title": "강아지 산책용품 추천해주세요",
  "content": "강아지 산책할 때 필요한 용품 추천 부탁드립니다.",
  "imageUrls": [
    "https://example.com/image1.jpg",
    "https://example.com/image2.jpg"
  ],
  "boardType": "QUESTION"
}
```

#### Response Body (`PostResponse`)

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 게시글 ID |
| authorId | Long | 작성자 ID |
| title | String | 제목 |
| content | String | 내용 |
| imageUrls | String[] | 이미지 URL 배열 |
| boardType | String | 게시판 유형: `FREE`(자유게시판), `QUESTION`(질문있어요), `INFO`(정보공유) |
| viewCount | Long | 조회수 (초기값 0) |
| commentCount | Long | 댓글 개수 (초기값 0) |
| createdAt | String | 생성일시 (ISO 8601) |
| updatedAt | String | 수정일시 (ISO 8601) |

#### Response 예시

```json
{
  "code": "CREATED",
  "message": "성공",
  "data": {
    "id": 1,
    "authorId": 10,
    "title": "강아지 산책용품 추천해주세요",
    "content": "강아지 산책할 때 필요한 용품 추천 부탁드립니다.",
    "imageUrls": [
      "https://example.com/image1.jpg",
      "https://example.com/image2.jpg"
    ],
    "boardType": "QUESTION",
    "viewCount": 0,
    "commentCount": 0,
    "createdAt": "2025-01-15T10:30:00",
    "updatedAt": "2025-01-15T10:30:00"
  }
}
```

---

### 2-2. 게시글 목록 조회 (GET /api/community/posts)

- **인증 필요**: 아니오 (로그인 시 작성자 본인 여부 판단에 활용)
- **설명**: 커뮤니티 게시판의 게시글 목록을 조회합니다.

#### Query Parameters

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| sortBy | String | 아니오 | "latest" | 정렬 기준: `latest`(최신순), `oldest`(오래된순), `views`(조회수 많은순), `comments`(댓글 많은순) |
| boardType | String | 아니오 | null | 게시판 유형 필터링: `FREE`(자유게시판), `QUESTION`(질문있어요), `INFO`(정보공유). null이면 전체 조회 |
| page | Integer | 아니오 | 0 | 페이지 번호 (0부터 시작) |
| size | Integer | 아니오 | 20 | 페이지 크기 |

#### Response Body (`PostListResponse`)

| 필드 | 타입 | 설명 |
|------|------|------|
| page | Integer | 현재 페이지 번호 |
| size | Integer | 페이지 크기 |
| total | Long | 전체 게시글 수 |
| content | `PostListItemResponse[]` | 게시글 목록 |
| totalPages | Integer | 전체 페이지 수 |
| hasNext | Boolean | 다음 페이지 존재 여부 |
| hasPrevious | Boolean | 이전 페이지 존재 여부 |
| totalElements | Long | 전체 요소 수 |
| numberOfElements | Integer | 현재 페이지 요소 수 |

#### `PostListItemResponse` 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 게시글 ID |
| title | String | 제목 |
| authorNickname | String | 작성자 닉네임 |
| boardType | String | 게시판 유형: `FREE`(자유게시판), `QUESTION`(질문있어요), `INFO`(정보공유) |
| viewCount | Long | 조회수 |
| commentCount | Long | 댓글 개수 |
| createdAt | String | 생성일시 (ISO 8601) |
| updatedAt | String | 수정일시 (ISO 8601) |
| isModified | Boolean | 수정 여부 (updatedAt != createdAt) |

#### Response 예시

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": {
    "page": 0,
    "size": 20,
    "total": 100,
    "content": [
      {
        "id": 1,
        "title": "강아지 산책용품 추천해주세요",
        "authorNickname": "강아지집사",
        "boardType": "QUESTION",
        "viewCount": 15,
        "commentCount": 3,
        "createdAt": "2025-01-15T10:30:00",
        "updatedAt": "2025-01-15T10:30:00",
        "isModified": false
      }
    ],
    "totalPages": 5,
    "hasNext": true,
    "hasPrevious": false,
    "totalElements": 100,
    "numberOfElements": 20
  }
}
```

---

### 2-3. 게시글 상세 조회 (GET /api/community/posts/{postId})

- **인증 필요**: 아니오 (로그인 시 조회수 증가 및 본인 여부 판단에 활용)
- **설명**: 커뮤니티 게시글의 상세 정보를 조회합니다.
- **조회수 증가**: 로그인 사용자가 작성자가 아닌 경우에만 서버 내부에서 자동 증가합니다.

#### Path Parameters

| 파라미터 | 설명 |
|----------|------|
| postId | 게시글 ID |

#### Response Body (`PostDetailResponse`)

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 게시글 ID |
| authorId | Long | 작성자 ID |
| authorNickname | String | 작성자 닉네임 |
| authorProfileImageUrl | String | 작성자 프로필 이미지 URL |
| title | String | 제목 |
| content | String | 내용 |
| imageUrls | String[] | 이미지 URL 배열 |
| boardType | String | 게시판 유형: `FREE`(자유게시판), `QUESTION`(질문있어요), `INFO`(정보공유) |
| viewCount | Long | 조회수 |
| commentCount | Long | 댓글 개수 |
| createdAt | String | 생성일시 (ISO 8601) |
| updatedAt | String | 수정일시 (ISO 8601) |
| comments | `CommentSummaryResponse[]` | 부모 댓글 목록 (최신순) |

#### `CommentSummaryResponse` 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 댓글 ID |
| authorId | Long | 작성자 ID |
| authorNickname | String | 작성자 닉네임 |
| authorProfileImageUrl | String | 작성자 프로필 이미지 URL |
| content | String | 댓글 내용 |
| createdAt | String | 생성일시 (ISO 8601) |
| depth | Integer | 댓글 깊이 (1=댓글, 2=대댓글, 3=대대댓글) |
| parentId | Long | 부모 댓글 ID (댓글인 경우 null) |

#### Response 예시

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": {
    "id": 1,
    "authorId": 10,
    "authorNickname": "강아지집사",
    "authorProfileImageUrl": "https://example.com/profile.jpg",
    "title": "강아지 산책용품 추천해주세요",
    "content": "강아지 산책할 때 필요한 용품 추천 부탁드립니다.",
    "imageUrls": [
      "https://example.com/image1.jpg",
      "https://example.com/image2.jpg"
    ],
    "boardType": "QUESTION",
    "viewCount": 15,
    "commentCount": 3,
    "createdAt": "2025-01-15T10:30:00",
    "updatedAt": "2025-01-15T10:30:00",
    "comments": [
      {
        "id": 1,
        "authorId": 11,
        "authorNickname": "고양이집사",
        "authorProfileImageUrl": "https://example.com/profile2.jpg",
        "content": "리드줄 추천드립니다!",
        "createdAt": "2025-01-15T11:00:00",
        "depth": 1,
        "parentId": null
      }
    ]
  }
}
```

---

### 2-4. 게시글 수정 (PATCH /api/community/posts/{postId})

- **인증 필요**: 예
- **권한**: 작성자 본인만 가능

#### Path Parameters

| 파라미터 | 설명 |
|----------|------|
| postId | 게시글 ID |

#### Request Body (`PostUpdateRequest`)

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| title | String | 예 | 제목 (2~50자) |
| content | String | 예 | 내용 (2~1000자) |
| imageUrls | String[] | 옵션 | 이미지 URL 배열 (최대 5장) |
| boardType | String | 예 | 게시판 유형: `FREE`(자유게시판), `QUESTION`(질문있어요), `INFO`(정보공유) |

#### Request 예시

```json
{
  "title": "강아지 산책용품 추천해주세요 (수정)",
  "content": "강아지 산책할 때 필요한 용품 추천 부탁드립니다. 특히 리드줄 추천 부탁드려요!",
  "imageUrls": [
    "https://example.com/image1.jpg",
    "https://example.com/image2.jpg",
    "https://example.com/image3.jpg"
  ],
  "boardType": "QUESTION"
}
```

#### Response Body

`PostResponse` 구조 (게시글 등록 응답과 동일)

---

### 2-5. 게시글 삭제 (DELETE /api/community/posts/{postId})

- **인증 필요**: 예
- **권한**: 작성자 본인만 가능
- **동작**: 소프트 삭제 (`deletedAt` 설정). 게시글 삭제 시 관련 댓글도 함께 삭제됩니다.

#### Path Parameters

| 파라미터 | 설명 |
|----------|------|
| postId | 게시글 ID |

#### Response 예시

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": null
}
```

---

## 댓글 API

### 3-1. 댓글 작성 (POST /api/community/posts/{postId}/comments)

- **인증 필요**: 예
- **설명**: 현재 로그인한 사용자가 게시글에 댓글을 작성합니다.
- **제한**: 최대 3단계까지 작성 가능 (댓글 → 대댓글 → 대대댓글)

#### Path Parameters

| 파라미터 | 설명 |
|----------|------|
| postId | 게시글 ID |

#### Request Body (`CommentCreateRequest`)

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| content | String | 예 | 댓글 내용 (2~500자) |
| parentId | Long | 옵션 | 부모 댓글 ID (대댓글/대대댓글 작성 시) |

#### Request 예시

**일반 댓글 작성:**
```json
{
  "content": "리드줄 추천드립니다!"
}
```

**대댓글 작성:**
```json
{
  "content": "어떤 브랜드 추천하시나요?",
  "parentId": 1
}
```

#### Response Body (`CommentResponse`)

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 댓글 ID |
| postId | Long | 게시글 ID |
| authorId | Long | 작성자 ID |
| authorNickname | String | 작성자 닉네임 |
| authorProfileImageUrl | String | 작성자 프로필 이미지 URL |
| parentId | Long | 부모 댓글 ID (댓글인 경우 null) |
| content | String | 댓글 내용 |
| depth | Integer | 댓글 깊이 (1=댓글, 2=대댓글, 3=대대댓글) |
| createdAt | String | 생성일시 (ISO 8601) |
| updatedAt | String | 수정일시 (ISO 8601) |

#### Response 예시

```json
{
  "code": "CREATED",
  "message": "성공",
  "data": {
    "id": 1,
    "postId": 1,
    "authorId": 11,
    "authorNickname": "고양이집사",
    "authorProfileImageUrl": "https://example.com/profile2.jpg",
    "parentId": null,
    "content": "리드줄 추천드립니다!",
    "depth": 1,
    "createdAt": "2025-01-15T11:00:00",
    "updatedAt": "2025-01-15T11:00:00"
  }
}
```

---

### 3-2. 댓글 목록 조회 (GET /api/community/posts/{postId}/comments)

- **인증 필요**: 아니오
- **설명**: 게시글의 댓글 목록을 조회합니다. 부모 댓글만 조회되며, 하위 댓글은 별도 API로 조회해야 합니다.

#### Path Parameters

| 파라미터 | 설명 |
|----------|------|
| postId | 게시글 ID |

#### Response Body (`CommentListResponse`)

| 필드 | 타입 | 설명 |
|------|------|------|
| comments | `CommentListItemResponse[]` | 댓글 목록 (최신순) |

#### `CommentListItemResponse` 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 댓글 ID |
| authorId | Long | 작성자 ID |
| authorNickname | String | 작성자 닉네임 |
| authorProfileImageUrl | String | 작성자 프로필 이미지 URL |
| content | String | 댓글 내용 |
| createdAt | String | 생성일시 (ISO 8601) |
| depth | Integer | 댓글 깊이 (1=댓글, 2=대댓글, 3=대대댓글) |
| parentId | Long | 부모 댓글 ID (댓글인 경우 null) |
| hasChildren | Boolean | 하위 댓글 존재 여부 |

#### Response 예시

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": {
    "comments": [
      {
        "id": 1,
        "authorId": 11,
        "authorNickname": "고양이집사",
        "authorProfileImageUrl": "https://example.com/profile2.jpg",
        "content": "리드줄 추천드립니다!",
        "createdAt": "2025-01-15T11:00:00",
        "depth": 1,
        "parentId": null,
        "hasChildren": true
      }
    ]
  }
}
```

---

### 3-3. 하위 댓글 목록 조회 (GET /api/community/comments/{commentId}/replies)

- **인증 필요**: 아니오
- **설명**: 특정 댓글의 하위 댓글 목록을 조회합니다.

#### Path Parameters

| 파라미터 | 설명 |
|----------|------|
| commentId | 부모 댓글 ID |

#### Response Body

`CommentListResponse` 구조 (댓글 목록 조회 응답과 동일)

#### Response 예시

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": {
    "comments": [
      {
        "id": 2,
        "authorId": 12,
        "authorNickname": "햄스터집사",
        "authorProfileImageUrl": "https://example.com/profile3.jpg",
        "content": "어떤 브랜드 추천하시나요?",
        "createdAt": "2025-01-15T11:30:00",
        "depth": 2,
        "parentId": 1,
        "hasChildren": false
      }
    ]
  }
}
```

---

### 3-4. 댓글 삭제 (DELETE /api/community/comments/{commentId})

- **인증 필요**: 예
- **권한**: 작성자 본인만 가능
- **동작**: 
  - 하위 댓글이 있으면: 댓글만 소프트 삭제 (하위 댓글은 유지)
  - 하위 댓글이 없으면: 댓글 삭제 및 게시글의 댓글 개수 감소

#### Path Parameters

| 파라미터 | 설명 |
|----------|------|
| commentId | 댓글 ID |

#### Response 예시

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": null
}
```

---

## FAQ 및 참고

### 이미지 업로드

- 게시글 이미지는 기존 `ImageController` (`/api/images`)를 사용하여 업로드합니다.
- 업로드된 이미지 URL을 `imageUrls` 배열에 포함하여 게시글 등록/수정 시 전달합니다.
- 이미지 파일 형식: jpg, png, gif 등
- 이미지 크기 제한: 한 장당 5MB, 총 25MB
- 최대 5장까지 업로드 가능

### 댓글 depth 제한

- 최대 3단계까지만 허용 (댓글 → 대댓글 → 대대댓글)
- 4단계 이상 요청 시 `COMMENT_DEPTH_EXCEEDED` 에러 발생

### 조회수 관리

- 게시글 상세 조회 시, 로그인 사용자가 작성자가 아닌 경우에만 조회수가 증가합니다.
- 비로그인 사용자의 조회는 조회수에 반영되지 않습니다.

### 정렬 기준 (sortBy)

| 값 | 설명 | 정렬 방향 |
|----|------|-----------|
| `latest` | 최신순 | 내림차순 (desc) |
| `oldest` | 오래된순 | 오름차순 (asc) |
| `views` | 조회수 많은순 | 내림차순 (desc) |
| `comments` | 댓글 많은순 | 내림차순 (desc) |

### 게시판 유형 (boardType)

| 값 | 설명 |
|----|------|
| `FREE` | 자유게시판 |
| `QUESTION` | 질문있어요 |
| `INFO` | 정보공유 |

게시글 목록 조회 시 `boardType` 쿼리 파라미터를 사용하여 특정 게시판 유형만 필터링할 수 있습니다.  
예: `GET /api/community/posts?boardType=QUESTION&sortBy=latest`

### 에러 코드

| 에러 코드 | HTTP 상태 | 설명 |
|-----------|-----------|------|
| `POST_NOT_FOUND` | 404 | 게시글을 찾을 수 없습니다. |
| `POST_ACCESS_DENIED` | 403 | 게시글에 대한 접근 권한이 없습니다. |
| `POST_ALREADY_DELETED` | 400 | 이미 삭제된 게시글입니다. |
| `COMMENT_NOT_FOUND` | 404 | 댓글을 찾을 수 없습니다. |
| `COMMENT_ACCESS_DENIED` | 403 | 댓글에 대한 접근 권한이 없습니다. |
| `COMMENT_DEPTH_EXCEEDED` | 400 | 댓글은 최대 3단계까지만 작성할 수 있습니다. |
| `INVALID_IMAGE_COUNT` | 400 | 이미지는 최대 5장까지 등록 가능합니다. |

---

## 참고사항

1. **작성자 정보 스냅샷**: 게시글과 댓글 작성 시점의 작성자 닉네임과 프로필 이미지가 저장됩니다. 작성자가 나중에 닉네임을 변경해도 과거 게시글/댓글의 작성자 정보는 변경되지 않습니다.

2. **소프트 삭제**: 게시글과 댓글은 물리적으로 삭제되지 않고 `deletedAt` 필드가 설정됩니다. 삭제된 게시글/댓글은 목록 조회에서 제외됩니다.

3. **댓글 구조**: 댓글은 계층 구조를 가지며, `parentId`로 부모 댓글을 참조합니다. `depth`는 댓글의 깊이를 나타내며, 1=댓글, 2=대댓글, 3=대대댓글입니다.

4. **페이지네이션**: 목록 조회 API는 페이지네이션을 지원하며, 기본값은 `page=0`, `size=20`입니다.

