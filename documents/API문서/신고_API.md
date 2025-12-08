# 신고 API 문서

> 반려동물 용품 중고거래 서비스 - **신고/차단** API 문서

본 문서는 신고 및 사용자 차단 관련 API에 대한 상세 설명을 제공합니다.

---

## 목차

- [공통 사항](#공통-사항)
- [Enum 목록](#enum-목록)
- [사용자 차단 API](#사용자-차단-api)
  - [1. 사용자 차단](#1-사용자-차단-post-apireportsblocksusersblockeduserid)
- [신고 API](#신고-api)
  - [2. 사용자 신고](#2-사용자-신고-post-apireportsuserstargetuserid)
  - [3. 상품 신고](#3-상품-신고-post-apireportsproductsproductid)
  - [4. 커뮤니티 게시글 신고](#4-커뮤니티-게시글-신고-post-apireportscommunity-postspostid)
- [관리자 신고 관리 API](#관리자-신고-관리-api)
  - [5. 신고 목록 조회](#5-신고-목록-조회-get-apiadminreports)
  - [6. 신고 검토](#6-신고-검토-patch-apiadminreportsreportidreview)

---

## 공통 사항

### Base URL

```
http://localhost:8080
```

### 공통 응답 형식

#### 성공 응답

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": { ... }
}
```

#### 에러 응답

```json
{
  "code": "BAD_REQUEST",
  "message": "에러 메시지",
  "traceId": "e1e4456f40d648c7a24fc7d5cd85e4af",
  "timestamp": "2025-11-14T15:45:00"
}
```

### HTTP 상태 코드

| 코드 | 설명 |
|------|------|
| 200 | 성공 |
| 201 | 생성됨 |
| 400 | 잘못된 요청 (입력값 검증 실패) |
| 401 | 인증 필요 |
| 403 | 권한 없음 (관리자 전용 API) |
| 404 | 리소스를 찾을 수 없음 |
| 409 | 충돌 (예: 이미 신고된 대상, 이미 차단된 사용자) |
| 500 | 서버 오류 |

### 공통 헤더

| 헤더명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| Content-Type | String | 예 | `multipart/form-data` (신고 API), `application/json` (차단/관리자 API) |
| Authorization | String | 예 | `Bearer <Access Token>` 형식 (모든 API 인증 필요) |
| X-Trace-Id | String | 아니오 | 요청 추적 ID (자동 생성) |

### ResponseCode Enum

모든 API 응답에서 사용되는 `ResponseCode` enum의 값들입니다.

| Enum 값 | HTTP 상태 코드 | 설명 |
|---------|---------------|------|
| SUCCESS | 200 | 요청이 성공적으로 처리됨 |
| CREATED | 201 | 리소스가 성공적으로 생성됨 |
| BAD_REQUEST | 400 | 요청 파라미터가 잘못되었거나 검증 실패 |
| UNAUTHORIZED | 401 | 인증이 필요하거나 인증 정보가 유효하지 않음 |
| FORBIDDEN | 403 | 인증은 되었지만 권한이 없음 (관리자 전용) |
| NOT_FOUND | 404 | 요청한 리소스를 찾을 수 없음 |
| CONFLICT | 409 | 리소스 충돌 (예: 이미 신고된 대상) |

---

## Enum 목록

### ReportTargetType (신고 대상 타입)

| Enum 값 | 설명 |
|---------|------|
| `USER` | 사용자 신고 |
| `PRODUCT` | 상품 신고 |
| `COMMUNITY_POST` | 커뮤니티 게시글 신고 |

### ReportStatus (신고 처리 상태)

| Enum 값 | 설명 |
|---------|------|
| `PENDING` | 대기 중 (신고 접수 후 관리자 검토 전) |
| `REVIEWED` | 검토 완료 |
| `REJECTED` | 거절됨 |
| `ACTION_TAKEN` | 조치 완료 |

### UserReportReason (사용자 신고 사유)

| Enum 값 | 설명 |
|---------|------|
| `ABUSE_OR_HARASSMENT` | 욕설, 비방, 괴롭힘 |
| `FRAUD_OR_SCAM` | 사기, 허위 거래 시도 |
| `INAPPROPRIATE_CONTENT` | 음란물 또는 불건전 행위 |
| `SPAM_OR_AD` | 스팸/광고성 메시지 |
| `UNDER_14` | 만 14세 미만 유저입니다 |
| `NICKNAME_ISSUE` | 사용자 닉네임 신고 |
| `PROFILE_IMAGE_ISSUE` | 사용자 프로필 이미지 신고 |
| `ETC` | 기타 (직접 입력) |

### ProductReportReason (상품 신고 사유)

| Enum 값 | 설명 |
|---------|------|
| `FALSE_OR_SCAM` | 허위/사기성 상품 |
| `ILLEGAL_ITEM` | 불법 또는 금지 품목 |
| `INAPPROPRIATE_IMAGE` | 부적절한 이미지 |
| `DUPLICATE_POST` | 중복 게시물 |
| `SPAM_OR_AD` | 스팸/광고성 게시물 |
| `PROXY_PAYMENT_OR_TRADE` | 대리 결제/구매/판매 행위 |
| `PROFESSIONAL_SELLER` | 전문 판매 업자 |
| `ETC` | 기타 (직접 입력) |

### CommunityReportReason (커뮤니티 게시글 신고 사유)

| Enum 값 | 설명 |
|---------|------|
| `ABUSE_OR_HATE` | 욕설/비방/혐오 표현 |
| `SPAM_OR_AD` | 스팸/광고성 게시물 |
| `INAPPROPRIATE_CONTENT` | 음란물/불건전 콘텐츠 |
| `REPETITIVE_POST` | 도배 게시물 |
| `SELF_HARM_OR_SUICIDE` | 자해 또는 자살 의도를 포함 |
| `ETC` | 기타 (직접 입력) |

---

## 사용자 차단 API

### 1. 사용자 차단 (POST /api/reports/blocks/users/{blockedUserId})

사용자를 차단합니다. 차단된 사용자와는 채팅할 수 없고, 서로의 게시글과 댓글을 볼 수 없으며, 상호작용이 차단됩니다.

#### 엔드포인트

```
POST /api/reports/blocks/users/{blockedUserId}
```

#### 설명

- 현재 로그인한 사용자가 특정 사용자를 차단합니다.
- 인증이 필요합니다 (`Authorization` 헤더 필수).
- 자기 자신을 차단할 수 없습니다.
- 이미 차단된 사용자를 다시 차단할 수 없습니다.
- 차단 기능 수행 시:
  - 로그인 유저가 차단한 유저와는 채팅할 수 없습니다.
  - 차단된 사용자의 게시글과 댓글을 볼 수 없습니다.
  - 차단된 사용자 또한 차단한 사용자의 게시글과 댓글을 볼 수 없습니다.
  - 서로의 콘텐츠(게시글/댓글)에 대한 상호작용이 차단됩니다.

#### Path Parameters

| 파라미터명 | 타입 | 필수 | 설명 |
|-----------|------|------|------|
| blockedUserId | Long | 예 | 차단할 사용자 ID |

#### Request Body

| 필드명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| - | - | - | Request Body 없음 (또는 빈 객체 `{}`) |

#### Request Headers

| 헤더명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| Authorization | String | 예 | `Bearer <Access Token>` |
| Content-Type | String | 예 | `application/json` |

#### Response Body

##### 성공 응답 (201 Created)

| 필드명 | 타입 | 설명 |
|--------|------|------|
| code | String | 응답 코드 ("CREATED") |
| message | String | 응답 메시지 ("생성됨") |
| data | Object | 차단 결과 |
| data.blockerId | Long | 차단한 사용자 ID |
| data.blockedUserId | Long | 차단당한 사용자 ID |
| data.blockedNickname | String | 차단당한 사용자 닉네임 |
| data.blockedProfileImageUrl | String | 차단당한 사용자 프로필 이미지 URL (nullable) |
| data.createdAt | String | 차단 일시 (ISO 8601 형식: YYYY-MM-DDTHH:mm:ss) |

##### 에러 응답

| HTTP 상태 코드 | 설명 |
|---------------|------|
| 400 | 자기 자신을 차단할 수 없음, 이미 차단된 사용자 |
| 401 | 인증되지 않음 (토큰이 없거나 유효하지 않음) |
| 404 | 차단할 사용자를 찾을 수 없음 |
| 500 | 서버 내부 오류 |

#### Request 예시

```http
POST /api/reports/blocks/users/123 HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

또는 빈 Request Body:

```json
{}
```

#### Response 예시

```json
{
  "code": "CREATED",
  "message": "생성됨",
  "data": {
    "blockerId": 1,
    "blockedUserId": 123,
    "blockedNickname": "차단된사용자",
    "blockedProfileImageUrl": "https://example.com/profile.jpg",
    "createdAt": "2025-01-15T10:30:00"
  }
}
```

---

## 신고 API

### 2. 사용자 신고 (POST /api/reports/users/{targetUserId})

사용자를 신고합니다.

#### 엔드포인트

```
POST /api/reports/users/{targetUserId}
```

#### 설명

- 로그인한 사용자가 다른 사용자를 신고합니다.
- 인증이 필요합니다 (`Authorization` 헤더 필수).
- 한 사용자당 동일 유저 1회만 신고 가능합니다.
- 신고 사유는 다중 선택 가능합니다.
- 이미지 첨부 가능 (최대 3장).
- 이미 신고 접수된 유저에 대해 중복 신고 요청 시 "이미 신고된 유저입니다." 메시지를 반환합니다.

#### Path Parameters

| 파라미터명 | 타입 | 필수 | 설명 |
|-----------|------|------|------|
| targetUserId | Long | 예 | 신고 대상 사용자 ID |

#### Request Body (multipart/form-data)

| 필드명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| reasonCodes | String[] | 예 | 신고 사유 코드 리스트 (UserReportReason enum name, 최소 1개) |
| detailReason | String | 아니오 | 상세 사유 (최대 300자) |
| imageFiles | File[] | 아니오 | 이미지 파일 리스트 (최대 3장, 각 파일 최대 5MB) |

#### Request Headers

| 헤더명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| Authorization | String | 예 | `Bearer <Access Token>` |
| Content-Type | String | 예 | `multipart/form-data` |

#### Response Body

##### 성공 응답 (201 Created)

| 필드명 | 타입 | 설명 |
|--------|------|------|
| code | String | 응답 코드 ("CREATED") |
| message | String | 응답 메시지 ("생성됨") |
| data | Object | 신고 결과 |
| data.id | Long | 신고 ID |
| data.reporterId | Long | 신고자 ID |
| data.targetType | String | 신고 대상 타입 ("USER") |
| data.targetId | Long | 신고 대상 ID (사용자 ID) |
| data.reasonCodes | String[] | 신고 사유 코드 리스트 |
| data.detailReason | String | 상세 사유 (nullable) |
| data.imageUrls | String[] | 이미지 URL 리스트 (nullable) |
| data.status | String | 신고 상태 ("PENDING") |
| data.createdAt | String | 신고 일시 (ISO 8601 형식: YYYY-MM-DDTHH:mm:ss) |

##### 에러 응답

| HTTP 상태 코드 | 설명 |
|---------------|------|
| 400 | 신고 사유를 최소 1개 이상 선택해야 함, 상세 사유 최대 300자 초과 |
| 401 | 인증되지 않음 (토큰이 없거나 유효하지 않음) |
| 404 | 신고 대상 사용자를 찾을 수 없음 |
| 409 | 이미 신고된 유저입니다 |
| 500 | 서버 내부 오류 |

#### Request 예시

```http
POST /api/reports/users/123 HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW

------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="reasonCodes"

ABUSE_OR_HARASSMENT
------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="reasonCodes"

SPAM_OR_AD
------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="detailReason"

부적절한 행위를 반복적으로 하고 있습니다.
------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="imageFiles"; filename="screenshot1.png"
Content-Type: image/png

[이미지 파일 바이너리 데이터]
------WebKitFormBoundary7MA4YWxkTrZu0gW
```

#### Response 예시

```json
{
  "code": "CREATED",
  "message": "생성됨",
  "data": {
    "id": 1,
    "reporterId": 1,
    "targetType": "USER",
    "targetId": 123,
    "reasonCodes": ["ABUSE_OR_HARASSMENT", "SPAM_OR_AD"],
    "detailReason": "부적절한 행위를 반복적으로 하고 있습니다.",
    "imageUrls": ["/api/images/user/1/2025/01/15/uuid-screenshot1.png"],
    "status": "PENDING",
    "createdAt": "2025-01-15T10:30:00"
  }
}
```

---

### 3. 상품 신고 (POST /api/reports/products/{productId})

상품을 신고합니다.

#### 엔드포인트

```
POST /api/reports/products/{productId}
```

#### 설명

- 로그인한 사용자가 부적절하거나 불법적인 상품을 신고합니다.
- 인증이 필요합니다 (`Authorization` 헤더 필수).
- 한 상품당 한 사용자 1회만 신고 가능합니다.
- 신고 사유는 다중 선택 가능합니다.
- 이미지 첨부 가능 (최대 3장).
- 이미 신고 접수된 상품에 대해 중복 신고 요청 시 "이미 신고된 상품입니다." 메시지를 반환합니다.

#### Path Parameters

| 파라미터명 | 타입 | 필수 | 설명 |
|-----------|------|------|------|
| productId | Long | 예 | 신고 대상 상품 ID |

#### Request Body (multipart/form-data)

| 필드명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| reasonCodes | String[] | 예 | 신고 사유 코드 리스트 (ProductReportReason enum name, 최소 1개) |
| detailReason | String | 아니오 | 상세 사유 (최대 300자) |
| imageFiles | File[] | 아니오 | 이미지 파일 리스트 (최대 3장, 각 파일 최대 5MB) |

#### Request Headers

| 헤더명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| Authorization | String | 예 | `Bearer <Access Token>` |
| Content-Type | String | 예 | `multipart/form-data` |

#### Response Body

##### 성공 응답 (201 Created)

| 필드명 | 타입 | 설명 |
|--------|------|------|
| code | String | 응답 코드 ("CREATED") |
| message | String | 응답 메시지 ("생성됨") |
| data | Object | 신고 결과 |
| data.id | Long | 신고 ID |
| data.reporterId | Long | 신고자 ID |
| data.targetType | String | 신고 대상 타입 ("PRODUCT") |
| data.targetId | Long | 신고 대상 ID (상품 ID) |
| data.reasonCodes | String[] | 신고 사유 코드 리스트 |
| data.detailReason | String | 상세 사유 (nullable) |
| data.imageUrls | String[] | 이미지 URL 리스트 (nullable) |
| data.status | String | 신고 상태 ("PENDING") |
| data.createdAt | String | 신고 일시 (ISO 8601 형식: YYYY-MM-DDTHH:mm:ss) |

##### 에러 응답

| HTTP 상태 코드 | 설명 |
|---------------|------|
| 400 | 신고 사유를 최소 1개 이상 선택해야 함, 상세 사유 최대 300자 초과 |
| 401 | 인증되지 않음 (토큰이 없거나 유효하지 않음) |
| 404 | 신고 대상 상품을 찾을 수 없음 |
| 409 | 이미 신고된 상품입니다 |
| 500 | 서버 내부 오류 |

#### Request 예시

```http
POST /api/reports/products/456 HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW

------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="reasonCodes"

FALSE_OR_SCAM
------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="detailReason"

허위 상품 정보를 제공하고 있습니다.
------WebKitFormBoundary7MA4YWxkTrZu0gW
```

#### Response 예시

```json
{
  "code": "CREATED",
  "message": "생성됨",
  "data": {
    "id": 2,
    "reporterId": 1,
    "targetType": "PRODUCT",
    "targetId": 456,
    "reasonCodes": ["FALSE_OR_SCAM"],
    "detailReason": "허위 상품 정보를 제공하고 있습니다.",
    "imageUrls": [],
    "status": "PENDING",
    "createdAt": "2025-01-15T10:35:00"
  }
}
```

---

### 4. 커뮤니티 게시글 신고 (POST /api/reports/community-posts/{postId})

커뮤니티 게시글을 신고합니다.

#### 엔드포인트

```
POST /api/reports/community-posts/{postId}
```

#### 설명

- 로그인한 사용자가 부적절한 커뮤니티 게시글을 신고합니다.
- 인증이 필요합니다 (`Authorization` 헤더 필수).
- 한 게시글당 한 사용자 1회만 신고 가능합니다.
- 신고 사유는 다중 선택 가능합니다.
- 이미지 첨부 가능 (최대 3장).
- 이미 신고 접수된 게시글에 대해 중복 신고 요청 시 "이미 신고된 게시글입니다." 메시지를 반환합니다.

#### Path Parameters

| 파라미터명 | 타입 | 필수 | 설명 |
|-----------|------|------|------|
| postId | Long | 예 | 신고 대상 게시글 ID |

#### Request Body (multipart/form-data)

| 필드명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| reasonCodes | String[] | 예 | 신고 사유 코드 리스트 (CommunityReportReason enum name, 최소 1개) |
| detailReason | String | 아니오 | 상세 사유 (최대 300자) |
| imageFiles | File[] | 아니오 | 이미지 파일 리스트 (최대 3장, 각 파일 최대 5MB) |

#### Request Headers

| 헤더명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| Authorization | String | 예 | `Bearer <Access Token>` |
| Content-Type | String | 예 | `multipart/form-data` |

#### Response Body

##### 성공 응답 (201 Created)

| 필드명 | 타입 | 설명 |
|--------|------|------|
| code | String | 응답 코드 ("CREATED") |
| message | String | 응답 메시지 ("생성됨") |
| data | Object | 신고 결과 |
| data.id | Long | 신고 ID |
| data.reporterId | Long | 신고자 ID |
| data.targetType | String | 신고 대상 타입 ("COMMUNITY_POST") |
| data.targetId | Long | 신고 대상 ID (게시글 ID) |
| data.reasonCodes | String[] | 신고 사유 코드 리스트 |
| data.detailReason | String | 상세 사유 (nullable) |
| data.imageUrls | String[] | 이미지 URL 리스트 (nullable) |
| data.status | String | 신고 상태 ("PENDING") |
| data.createdAt | String | 신고 일시 (ISO 8601 형식: YYYY-MM-DDTHH:mm:ss) |

##### 에러 응답

| HTTP 상태 코드 | 설명 |
|---------------|------|
| 400 | 신고 사유를 최소 1개 이상 선택해야 함, 상세 사유 최대 300자 초과 |
| 401 | 인증되지 않음 (토큰이 없거나 유효하지 않음) |
| 404 | 신고 대상 게시글을 찾을 수 없음 |
| 409 | 이미 신고된 게시글입니다 |
| 500 | 서버 내부 오류 |

#### Request 예시

```http
POST /api/reports/community-posts/789 HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW

------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="reasonCodes"

ABUSE_OR_HATE
------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="detailReason"

혐오 표현이 포함된 게시글입니다.
------WebKitFormBoundary7MA4YWxkTrZu0gW
```

#### Response 예시

```json
{
  "code": "CREATED",
  "message": "생성됨",
  "data": {
    "id": 3,
    "reporterId": 1,
    "targetType": "COMMUNITY_POST",
    "targetId": 789,
    "reasonCodes": ["ABUSE_OR_HATE"],
    "detailReason": "혐오 표현이 포함된 게시글입니다.",
    "imageUrls": [],
    "status": "PENDING",
    "createdAt": "2025-01-15T10:40:00"
  }
}
```

---

## 관리자 신고 관리 API

### 5. 신고 목록 조회 (GET /api/admin/reports)

관리자가 신고 목록을 조회합니다.

#### 엔드포인트

```
GET /api/admin/reports
```

#### 설명

- 관리자 권한이 필요합니다 (`ROLE_ADMIN`).
- 신고 목록을 페이지네이션으로 조회합니다.
- 신고 대상 타입과 상태로 필터링할 수 있습니다.
- 최신순으로 정렬됩니다 (생성일시 기준 내림차순).

#### Query Parameters

| 파라미터명 | 타입 | 필수 | 설명 | 기본값 |
|-----------|------|------|------|--------|
| targetType | String | 아니오 | 신고 대상 타입 (USER, PRODUCT, COMMUNITY_POST) | - |
| status | String | 아니오 | 신고 상태 (PENDING, REVIEWED, REJECTED, ACTION_TAKEN) | - |
| page | Integer | 아니오 | 페이지 번호 (0부터 시작) | 0 |
| size | Integer | 아니오 | 페이지 크기 | 20 |

#### Request Headers

| 헤더명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| Authorization | String | 예 | `Bearer <Access Token>` (관리자 권한 필요) |
| Content-Type | String | 예 | `application/json` |

#### Response Body

##### 성공 응답 (200 OK)

| 필드명 | 타입 | 설명 |
|--------|------|------|
| code | String | 응답 코드 ("SUCCESS") |
| message | String | 응답 메시지 ("성공") |
| data | Object | 신고 목록 결과 |
| data.content | Array | 신고 목록 |
| data.content[].id | Long | 신고 ID |
| data.content[].reporterId | Long | 신고자 ID |
| data.content[].targetType | String | 신고 대상 타입 |
| data.content[].targetId | Long | 신고 대상 ID |
| data.content[].reasonCodes | String[] | 신고 사유 코드 리스트 |
| data.content[].detailReason | String | 상세 사유 (nullable) |
| data.content[].imageUrls | String[] | 이미지 URL 리스트 (nullable) |
| data.content[].status | String | 신고 상태 |
| data.content[].createdAt | String | 신고 일시 (ISO 8601 형식) |
| data.content[].reviewedAt | String | 검토 일시 (ISO 8601 형식, nullable) |
| data.content[].rejectedReason | String | 거절 사유 (nullable) |
| data.page | Integer | 현재 페이지 번호 (0부터 시작) |
| data.size | Integer | 페이지 크기 |
| data.total | Long | 전체 신고 개수 |
| data.totalPages | Integer | 전체 페이지 수 |
| data.hasNext | Boolean | 다음 페이지 존재 여부 |

##### 에러 응답

| HTTP 상태 코드 | 설명 |
|---------------|------|
| 401 | 인증되지 않음 (토큰이 없거나 유효하지 않음) |
| 403 | 권한 없음 (관리자 권한이 필요함) |
| 500 | 서버 내부 오류 |

#### Request 예시

```http
GET /api/admin/reports?targetType=USER&status=PENDING&page=0&size=20 HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

#### Response 예시

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": {
    "content": [
      {
        "id": 1,
        "reporterId": 1,
        "targetType": "USER",
        "targetId": 123,
        "reasonCodes": ["ABUSE_OR_HARASSMENT", "SPAM_OR_AD"],
        "detailReason": "부적절한 행위를 반복적으로 하고 있습니다.",
        "imageUrls": ["/api/images/user/1/2025/01/15/uuid-screenshot1.png"],
        "status": "PENDING",
        "createdAt": "2025-01-15T10:30:00",
        "reviewedAt": null,
        "rejectedReason": null
      }
    ],
    "page": 0,
    "size": 20,
    "total": 1,
    "totalPages": 1,
    "hasNext": false
  }
}
```

---

### 6. 신고 검토 (PATCH /api/admin/reports/{reportId}/review)

관리자가 신고를 검토하고 상태를 변경합니다.

#### 엔드포인트

```
PATCH /api/admin/reports/{reportId}/review
```

#### 설명

- 관리자 권한이 필요합니다 (`ROLE_ADMIN`).
- 신고 상태를 변경합니다 (PENDING → REVIEWED/REJECTED/ACTION_TAKEN).
- PENDING 상태인 신고만 검토할 수 있습니다.
- 이미 처리된 신고는 상태를 변경할 수 없습니다.

#### Path Parameters

| 파라미터명 | 타입 | 필수 | 설명 |
|-----------|------|------|------|
| reportId | Long | 예 | 신고 ID |

#### Request Body

| 필드명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| status | String | 예 | 변경할 신고 상태 (REVIEWED, REJECTED, ACTION_TAKEN) |
| rejectedReason | String | 아니오 | 거절 사유 (status가 REJECTED일 때 권장, 최대 300자) |
| actionNote | String | 아니오 | 조치 메모 (최대 500자) |

#### Request Headers

| 헤더명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| Authorization | String | 예 | `Bearer <Access Token>` (관리자 권한 필요) |
| Content-Type | String | 예 | `application/json` |

#### Response Body

##### 성공 응답 (200 OK)

| 필드명 | 타입 | 설명 |
|--------|------|------|
| code | String | 응답 코드 ("SUCCESS") |
| message | String | 응답 메시지 ("성공") |
| data | Object | 신고 결과 |
| data.id | Long | 신고 ID |
| data.reporterId | Long | 신고자 ID |
| data.targetType | String | 신고 대상 타입 |
| data.targetId | Long | 신고 대상 ID |
| data.reasonCodes | String[] | 신고 사유 코드 리스트 |
| data.detailReason | String | 상세 사유 (nullable) |
| data.imageUrls | String[] | 이미지 URL 리스트 (nullable) |
| data.status | String | 신고 상태 (변경된 상태) |
| data.createdAt | String | 신고 일시 (ISO 8601 형식) |

##### 에러 응답

| HTTP 상태 코드 | 설명 |
|---------------|------|
| 400 | 유효하지 않은 신고 상태 전환 (이미 처리된 신고) |
| 401 | 인증되지 않음 (토큰이 없거나 유효하지 않음) |
| 403 | 권한 없음 (관리자 권한이 필요함) |
| 404 | 신고를 찾을 수 없음 |
| 500 | 서버 내부 오류 |

#### Request 예시

```http
PATCH /api/admin/reports/1/review HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "status": "REVIEWED",
  "actionNote": "신고 내용을 검토하여 적절한 조치를 취했습니다."
}
```

#### Response 예시

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": {
    "id": 1,
    "reporterId": 1,
    "targetType": "USER",
    "targetId": 123,
    "reasonCodes": ["ABUSE_OR_HARASSMENT", "SPAM_OR_AD"],
    "detailReason": "부적절한 행위를 반복적으로 하고 있습니다.",
    "imageUrls": ["/api/images/user/1/2025/01/15/uuid-screenshot1.png"],
    "status": "REVIEWED",
    "createdAt": "2025-01-15T10:30:00"
  }
}
```

---

## FAQ 및 참고

### 신고 사유 코드 사용 방법

- 신고 사유는 다중 선택이 가능합니다.
- `reasonCodes`는 배열로 전달하며, 각 값은 해당 Enum의 이름을 문자열로 전달합니다.
- 예: `["ABUSE_OR_HARASSMENT", "SPAM_OR_AD"]`

### 이미지 업로드

- 신고 시 이미지 첨부가 가능합니다 (최대 3장).
- 각 이미지 파일은 최대 5MB까지 업로드 가능합니다.
- 지원 형식: jpg, jpeg, png, gif, webp
- 업로드된 이미지는 자동으로 URL로 변환되어 응답에 포함됩니다.

### 차단 기능

- 차단된 사용자와는 채팅할 수 없습니다.
- 차단된 사용자의 게시글과 댓글을 볼 수 없습니다.
- 차단된 사용자 또한 차단한 사용자의 게시글과 댓글을 볼 수 없습니다.
- 서로의 콘텐츠(게시글/댓글)에 대한 상호작용이 차단됩니다.

### 관리자 권한

- 신고 목록 조회 및 검토는 관리자 권한(`ROLE_ADMIN`)이 필요합니다.
- 일반 사용자는 신고만 가능하며, 신고 목록 조회는 불가능합니다.

---

**문서 버전**: 1.0  
**최종 업데이트**: 2025-01-15

