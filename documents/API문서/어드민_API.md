# 어드민 API 문서

> 반려동물 용품 중고거래 서비스 - **어드민** API 문서

본 문서는 어드민(관리자) 전용 API에 대한 상세 설명을 제공합니다. **모든 API는 `ROLE_ADMIN` 권한이 필요합니다.**

---

## 목차

- [공통 사항](#공통-사항)
- [Enum 목록](#enum-목록)
- [회원 관리 API](#회원-관리-api)
  - [1. 전체 유저 목록 조회](#1-전체-유저-목록-조회-get-apiadminusers)
  - [2. 어드민 역할 부여](#2-어드민-역할-부여-patch-apiadminusersuseridrole)
- [탈퇴 관리 API](#탈퇴-관리-api)ReportListResponse
  - [3. 탈퇴 회원 목록 조회](#3-탈퇴-회원-목록-조회-get-apiadminwithdrawals)
  - [4. 탈퇴 회원 상세 조회](#4-탈퇴-회원-상세-조회-get-apiadminwithdrawalsuserid)
  - [5. 탈퇴 회원 복구](#5-탈퇴-회원-복구-post-apiadminwithdrawalsuseridrestore)
- [상품 관리 API](#상품-관리-api)
  - [6. 어드민 상품 목록 조회](#6-어드민-상품-목록-조회-get-apiadminproducts)
- [통계 API](#통계-api)
  - [7. 월별 가입/탈퇴 추세](#7-월별-가입탈퇴-추세-get-apiadminstatisticstrends)
  - [8. 탈퇴 사유별 통계](#8-탈퇴-사유별-통계-get-apiadminstatisticswithdrawal-reasons)
  - [9. 대시보드 요약 통계](#9-대시보드-요약-통계-get-apiadminstatisticssummary)
- [관련 API (기존 문서 참고)](#관련-api-기존-문서-참고)

---

## 공통 사항

### Base URL

```
http://localhost:8080
```

### 권한 요구사항

**모든 어드민 API는 `ROLE_ADMIN` 권한이 필요합니다.** 일반 사용자(`ROLE_USER`)가 호출 시 `403 Forbidden`이 반환됩니다.

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
  "code": "FORBIDDEN",
  "message": "접근 권한이 없습니다.",
  "traceId": "e1e4456f40d648c7a24fc7d5cd85e4af",
  "timestamp": "2025-11-14T15:45:00"
}
```

### HTTP 상태 코드

| 코드 | 설명 |
|------|------|
| 200 | 성공 |
| 400 | 잘못된 요청 (입력값 검증 실패) |
| 401 | 인증 필요 (토큰 없음 또는 만료) |
| 403 | 권한 없음 (관리자 권한 필요) |
| 404 | 리소스를 찾을 수 없음 |
| 500 | 서버 오류 |

### 공통 헤더

| 헤더명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| Content-Type | String | 예 | `application/json` |
| Authorization | String | 예 | `Bearer <Access Token>` (관리자 토큰) |

---

## Enum 목록

### UserRole (사용자 권한)

| Enum 값 | 설명 |
|---------|------|
| `USER` | 일반 사용자 |
| `ADMIN` | 관리자 |

### 유저 상태 (status 필드)

| 값 | 설명 |
|----|------|
| `ACTIVE` | 활성 (탈퇴하지 않은 회원) |
| `WITHDRAWN` | 탈퇴 |

### ProductType (상품 타입)

| Enum 값 | 설명 |
|---------|------|
| `SELL` | 판매 상품 (팝니다) |
| `REQUEST` | 판매 요청 (삽니다) |

### Category (상품 카테고리)

| Enum 값 | 설명 |
|---------|------|
| `FOOD` | 사료/간식 |
| `TOY` | 장난감 |
| `HOUSE` | 하우스/케이지 |
| `CLOTHING` | 의류/악세서리 |
| `HEALTH` | 건강/의료용품 |
| `GROOMING` | 미용/목욕용품 |
| `WALKING` | 산책용품 |
| `ETC` | 기타 |

### WithdrawalReasonType (탈퇴 사유)

| Enum 값 | 설명 |
|---------|------|
| `SERVICE_DISSATISFACTION` | 서비스 불만족 |
| `PRIVACY_CONCERN` | 개인정보 우려 |
| `LOW_USAGE` | 사용 빈도 낮음 |
| `COMPETITOR` | 경쟁 서비스 이용 |
| `OTHER` | 기타 |

---

## 회원 관리 API

### 1. 전체 유저 목록 조회 (GET /api/admin/users)

관리자가 전체 유저 목록을 검색/필터/페이징하여 조회합니다.

#### 엔드포인트

```
GET /api/admin/users
```

#### Query Parameters

| 파라미터명 | 타입 | 필수 | 설명 | 기본값 |
|-----------|------|------|------|--------|
| keyword | String | 아니오 | 검색어 (닉네임, 이메일, 이름, ID) | - |
| status | String | 아니오 | 상태 필터: `ACTIVE`, `WITHDRAWN` | - |
| role | UserRole | 아니오 | 권한 필터: `USER`, `ADMIN` | - |
| page | Integer | 아니오 | 페이지 번호 (0부터 시작) | 0 |
| size | Integer | 아니오 | 페이지 크기 | 10 |

#### Response (200 OK)

| 필드명 | 타입 | 설명 |
|--------|------|------|
| data.content | Array | 유저 목록 |
| data.content[].id | Long | 회원 ID |
| data.content[].email | String | 이메일 |
| data.content[].name | String | 이름 |
| data.content[].nickname | String | 닉네임 |
| data.content[].birthDate | String | 생년월일 (yyyy-MM-dd, nullable) |
| data.content[].addressSido | String | 시/도 (nullable) |
| data.content[].addressGugun | String | 구/군 (nullable) |
| data.content[].role | UserRole | 권한 (USER, ADMIN) |
| data.content[].createdAt | String | 가입일시 (ISO 8601) |
| data.content[].status | String | 상태 (ACTIVE, WITHDRAWN) |
| data.page | Integer | 현재 페이지 번호 |
| data.size | Integer | 페이지 크기 |
| data.totalElements | Long | 전체 유저 수 |
| data.totalPages | Integer | 전체 페이지 수 |
| data.hasNext | Boolean | 다음 페이지 존재 여부 |
| data.hasPrevious | Boolean | 이전 페이지 존재 여부 |

#### Request 예시

```http
GET /api/admin/users?keyword=홍길동&status=ACTIVE&role=USER&page=0&size=10 HTTP/1.1
Host: localhost:8080
Authorization: Bearer <ADMIN_ACCESS_TOKEN>
```

---

### 2. 어드민 역할 부여 (PATCH /api/admin/users/{userId}/role)

특정 회원에게 ADMIN 역할을 부여합니다.

#### 엔드포인트

```
PATCH /api/admin/users/{userId}/role
```

#### Path Parameters

| 파라미터명 | 타입 | 필수 | 설명 |
|-----------|------|------|------|
| userId | Long | 예 | 대상 회원 ID |

#### Request Body

없음 (요청 본문 없이 호출)

#### Response (200 OK)

| 필드명 | 타입 | 설명 |
|--------|------|------|
| data.id | Long | 회원 ID |
| data.email | String | 이메일 |
| data.name | String | 이름 |
| data.nickname | String | 닉네임 |
| data.birthDate | String | 생년월일 (nullable) |
| data.addressSido | String | 시/도 (nullable) |
| data.addressGugun | String | 구/군 (nullable) |
| data.role | UserRole | 권한 (ADMIN으로 변경됨) |
| data.createdAt | String | 가입일시 |

#### 에러

| HTTP | 설명 |
|------|------|
| 404 | 대상 회원을 찾을 수 없음 |

---

## 탈퇴 관리 API

### 3. 탈퇴 회원 목록 조회 (GET /api/admin/withdrawals)

탈퇴한 회원 목록을 조회합니다.

#### 엔드포인트

```
GET /api/admin/withdrawals
```

#### Query Parameters

| 파라미터명 | 타입 | 필수 | 설명 | 기본값 |
|-----------|------|------|------|--------|
| keyword | String | 아니오 | 검색어 (닉네임, 이메일, 이름, ID) | - |
| page | Integer | 아니오 | 페이지 번호 | 0 |
| size | Integer | 아니오 | 페이지 크기 | 10 |

#### Response (200 OK)

| 필드명 | 타입 | 설명 |
|--------|------|------|
| data.content | Array | 탈퇴 회원 목록 |
| data.content[].id | Long | 회원 ID |
| data.content[].email | String | 이메일 |
| data.content[].nickname | String | 닉네임 |
| data.content[].authorNickname | String | 작성자 닉네임 |
| data.content[].title | String | 제목 (탈퇴 상세 사유 요약) |
| data.content[].name | String | 이름 |
| data.content[].role | UserRole | 권한 |
| data.content[].addressSido | String | 시/도 (nullable) |
| data.content[].addressGugun | String | 구/군 (nullable) |
| data.content[].birthDate | String | 생년월일 (nullable) |
| data.content[].withdrawalReason | WithdrawalReasonType | 탈퇴 사유 |
| data.content[].withdrawalDetailReason | String | 탈퇴 상세 사유 (nullable) |
| data.content[].deletedAt | String | 탈퇴일시 (ISO 8601) |
| data.page | Integer | 현재 페이지 |
| data.size | Integer | 페이지 크기 |
| data.totalElements | Long | 전체 탈퇴 회원 수 |
| data.totalPages | Integer | 전체 페이지 수 |
| data.hasNext | Boolean | 다음 페이지 존재 여부 |
| data.hasPrevious | Boolean | 이전 페이지 존재 여부 |

---

### 4. 탈퇴 회원 상세 조회 (GET /api/admin/withdrawals/{userId})

탈퇴한 회원의 상세 정보를 조회합니다.

#### 엔드포인트

```
GET /api/admin/withdrawals/{userId}
```

#### Path Parameters

| 파라미터명 | 타입 | 필수 | 설명 |
|-----------|------|------|------|
| userId | Long | 예 | 탈퇴 회원 ID |

#### Response (200 OK)

| 필드명 | 타입 | 설명 |
|--------|------|------|
| data.id | Long | 회원 ID |
| data.email | String | 이메일 |
| data.nickname | String | 닉네임 |
| data.name | String | 이름 |
| data.role | UserRole | 권한 |
| data.addressSido | String | 시/도 (nullable) |
| data.addressGugun | String | 구/군 (nullable) |
| data.birthDate | String | 생년월일 (nullable) |
| data.createdAt | String | 가입일시 |
| data.profileImageUrl | String | 프로필 이미지 URL (nullable) |
| data.withdrawalReason | WithdrawalReasonType | 탈퇴 사유 |
| data.withdrawalDetailReason | String | 탈퇴 상세 사유 (nullable) |
| data.deletedAt | String | 탈퇴일시 |

#### 에러

| HTTP | 설명 |
|------|------|
| 404 | 탈퇴 회원을 찾을 수 없음 / 해당 회원은 탈퇴한 회원이 아님 |

---

### 5. 탈퇴 회원 복구 (POST /api/admin/withdrawals/{userId}/restore)

탈퇴한 회원의 계정을 복구합니다. 복구 시 해당 회원은 즉시 활성 상태로 전환됩니다.

#### 엔드포인트

```
POST /api/admin/withdrawals/{userId}/restore
```

#### Path Parameters

| 파라미터명 | 타입 | 필수 | 설명 |
|-----------|------|------|------|
| userId | Long | 예 | 복구할 탈퇴 회원 ID |

#### Request Body

없음

#### Response (200 OK)

| 필드명 | 타입 | 설명 |
|--------|------|------|
| data.id | Long | 회원 ID |
| data.email | String | 이메일 |
| data.name | String | 이름 |
| data.nickname | String | 닉네임 |
| data.birthDate | String | 생년월일 (nullable) |
| data.addressSido | String | 시/도 (nullable) |
| data.addressGugun | String | 구/군 (nullable) |
| data.role | UserRole | 권한 |
| data.createdAt | String | 가입일시 |

#### 에러

| HTTP | 설명 |
|------|------|
| 404 | 탈퇴 회원을 찾을 수 없음 / 해당 회원은 탈퇴한 회원이 아님 |

---

## 상품 관리 API

### 6. 어드민 상품 목록 조회 (GET /api/admin/products)

관리자용 상품 목록을 조회합니다. 일반 검색 API와 달리 **판매자 닉네임**, **카테고리**, **수정일시**가 포함됩니다.

#### 엔드포인트

```
GET /api/admin/products
```

#### Query Parameters

| 파라미터명 | 타입 | 필수 | 설명 | 기본값 |
|-----------|------|------|------|--------|
| keyword | String | 아니오 | 검색어 (제목, 설명, 카테고리) | - |
| productType | ProductType | 아니오 | 상품 타입: `SELL`, `REQUEST` | - |
| category | Category | 아니오 | 카테고리 필터 | - |
| page | Integer | 아니오 | 페이지 번호 | 0 |
| size | Integer | 아니오 | 페이지 크기 | 20 |

#### Response (200 OK)

| 필드명 | 타입 | 설명 |
|--------|------|------|
| data.content | Array | 상품 목록 |
| data.content[].id | Long | 상품 ID |
| data.content[].title | String | 상품명 |
| data.content[].price | Long | 가격 |
| data.content[].productType | ProductType | 상품 타입 (SELL, REQUEST) |
| data.content[].category | Category | 카테고리 |
| data.content[].petDetailType | PetDetailType | 반려동물 상세 종류 |
| data.content[].productStatus | ProductStatus | 상품 상태 |
| data.content[].tradeStatus | TradeStatus | 거래 상태 |
| data.content[].sellerNickname | String | 판매자 닉네임 |
| data.content[].createdAt | String | 등록일시 |
| data.content[].updatedAt | String | 수정일시 |
| data.page | Integer | 현재 페이지 |
| data.size | Integer | 페이지 크기 |
| data.totalElements | Long | 전체 상품 수 |
| data.totalPages | Integer | 전체 페이지 수 |
| data.hasNext | Boolean | 다음 페이지 존재 여부 |
| data.hasPrevious | Boolean | 이전 페이지 존재 여부 |

#### Request 예시

```http
GET /api/admin/products?productType=SELL&category=TOY&page=0&size=20 HTTP/1.1
Host: localhost:8080
Authorization: Bearer <ADMIN_ACCESS_TOKEN>
```

---

## 통계 API

### 7. 월별 가입/탈퇴 추세 (GET /api/admin/statistics/trends)

월별 가입 수와 탈퇴 수를 조회합니다.

#### 엔드포인트

```
GET /api/admin/statistics/trends
```

#### Query Parameters

| 파라미터명 | 타입 | 필수 | 설명 | 기본값 |
|-----------|------|------|------|--------|
| startMonth | String | 아니오 | 시작 월 (yyyy-MM) | 현재 기준 12개월 전 |
| endMonth | String | 아니오 | 종료 월 (yyyy-MM) | 현재 월 |

#### Response (200 OK)

| 필드명 | 타입 | 설명 |
|--------|------|------|
| data | Array | 월별 추세 목록 |
| data[].yearMonth | String | 년월 (yyyy-MM) |
| data[].signupCount | Long | 해당 월 가입 수 |
| data[].withdrawalCount | Long | 해당 월 탈퇴 수 |

#### Response 예시

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": [
    {
      "yearMonth": "2025-01",
      "signupCount": 150,
      "withdrawalCount": 12
    },
    {
      "yearMonth": "2025-02",
      "signupCount": 180,
      "withdrawalCount": 8
    }
  ]
}
```

---

### 8. 탈퇴 사유별 통계 (GET /api/admin/statistics/withdrawal-reasons)

탈퇴 사유별 건수를 조회합니다.

#### 엔드포인트

```
GET /api/admin/statistics/withdrawal-reasons
```

#### Response (200 OK)

| 필드명 | 타입 | 설명 |
|--------|------|------|
| data | Array | 탈퇴 사유별 통계 |
| data[].reason | WithdrawalReasonType | 탈퇴 사유 |
| data[].count | Long | 해당 사유로 탈퇴한 회원 수 |

#### Response 예시

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": [
    { "reason": "LOW_USAGE", "count": 45 },
    { "reason": "PRIVACY_CONCERN", "count": 32 },
    { "reason": "SERVICE_DISSATISFACTION", "count": 18 },
    { "reason": "OTHER", "count": 12 },
    { "reason": "COMPETITOR", "count": 5 }
  ]
}
```

---

### 9. 대시보드 요약 통계 (GET /api/admin/statistics/summary)

대시보드용 요약 통계를 조회합니다.

#### 엔드포인트

```
GET /api/admin/statistics/summary
```

#### Response (200 OK)

| 필드명 | 타입 | 설명 |
|--------|------|------|
| data.totalUserCount | Long | 전체 회원 수 (탈퇴 포함) |
| data.activeUserCount | Long | 활성 회원 수 (탈퇴 제외) |
| data.withdrawnUserCount | Long | 탈퇴 회원 수 |
| data.totalProductCount | Long | 전체 상품 수 (삭제 포함) |
| data.activeProductCount | Long | 활성 상품 수 (삭제 제외) |

#### Response 예시

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": {
    "totalUserCount": 1250,
    "activeUserCount": 1180,
    "withdrawnUserCount": 70,
    "totalProductCount": 3200,
    "activeProductCount": 2980
  }
}
```

---

## 관련 API (기존 문서 참고)

다음 API는 어드민 기능과 연관되어 있으며, 상세 내용은 해당 문서를 참고하세요.

| API | 문서 | 설명 |
|-----|------|------|
| 신고 목록 조회 | [신고_API.md](./신고_API.md#7-신고-목록-조회-get-apiadminreports) | `GET /api/admin/reports` |
| 신고 검토 | [신고_API.md](./신고_API.md#8-신고-검토-patch-apiadminreportsreportidreview) | `PATCH /api/admin/reports/{reportId}/review` |
| 상품 삭제 | [상품_API.md](./상품_API.md) | `DELETE /api/products/{id}` - 관리자도 삭제 가능 |
| 게시글 삭제 | [커뮤니티_API.md](./커뮤니티_API.md) | `DELETE /api/community/posts/{id}` - 관리자도 삭제 가능 |
| 유저 상세 조회 | [프로필_API.md](./프로필_API.md) | `GET /api/profile/{id}` - 관리자가 타인 프로필 조회 시 사용 |

---

## 관리자 권한 획득

최초 관리자 계정은 DB에서 `users.role`을 `ADMIN`으로 직접 수정하거나, 이미 ADMIN 권한이 있는 사용자가 `PATCH /api/admin/users/{userId}/role` API를 호출하여 다른 회원에게 ADMIN 역할을 부여할 수 있습니다.
