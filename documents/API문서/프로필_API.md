# 프로필 API 문서

> 반려동물 용품 중고거래 서비스 - **프로필** API 문서

본 문서는 프로필 관련 API에 대한 상세 설명을 제공합니다.

---

## 목차

- [공통 사항](#공통-사항)
- [사용자 정보 조회](#1-사용자-정보-조회)
- [내가 찜한 상품 목록 조회](#2-내가-찜한-상품-목록-조회)
- [내가 등록한 판매 상품 목록 조회](#3-내가-등록한-판매-상품-목록-조회)
- [내가 등록한 판매 요청 목록 조회](#4-내가-등록한-판매-요청-목록-조회)
- [프로필 정보 수정](#5-프로필-정보-수정)
- [유저 프로필 조회](#6-유저-프로필-조회)
- [다른 유저가 등록한 판매 상품 목록 조회](#7-다른-유저가-등록한-판매-상품-목록-조회)
- [차단한 유저 목록 조회](#8-차단한-유저-목록-조회)
- [유저 차단 해제](#9-유저-차단-해제)

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
| 400 | 잘못된 요청 (입력값 검증 실패) |
| 401 | 인증 필요 |
| 404 | 리소스를 찾을 수 없음 |
| 409 | 충돌 (예: 닉네임 중복) |
| 500 | 서버 오류 |

### 공통 헤더

| 헤더명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| Content-Type | String | 예 | application/json |
| Authorization | String | 예 | `Bearer <Access Token>` 형식 (모든 API 인증 필요) |
| X-Trace-Id | String | 아니오 | 요청 추적 ID (자동 생성) |

### ResponseCode Enum

모든 API 응답에서 사용되는 `ResponseCode` enum의 값들입니다. JSON 응답에서는 enum 이름이 문자열로 직렬화됩니다.

| Enum 값 | HTTP 상태 코드 | 설명 |
|---------|---------------|------|
| SUCCESS | 200 | 요청이 성공적으로 처리됨 |
| CREATED | 201 | 리소스가 성공적으로 생성됨 |
| NO_CONTENT | 204 | 요청은 성공했지만 반환할 내용이 없음 |
| BAD_REQUEST | 400 | 요청 파라미터가 잘못되었거나 검증 실패 |
| UNAUTHORIZED | 401 | 인증이 필요하거나 인증 정보가 유효하지 않음 |
| FORBIDDEN | 403 | 인증은 되었지만 권한이 없음 |
| NOT_FOUND | 404 | 요청한 리소스를 찾을 수 없음 |
| CONFLICT | 409 | 리소스 충돌 (예: 중복된 닉네임) |
| INTERNAL_SERVER_ERROR | 500 | 서버 내부 오류 발생 |

---

## 1. 사용자 정보 조회

현재 로그인한 사용자의 기본 정보를 조회합니다.

### 엔드포인트

```
GET /api/profile/me
```

### 설명

- 현재 로그인한 사용자의 기본 프로필 정보를 조회합니다.
- 인증이 필요합니다 (`Authorization` 헤더 필수).
- 상품 목록은 별도 API를 통해 조회할 수 있습니다.

### Request

| 항목 | 타입 | 필수 | 설명 |
|------|------|------|------|
| - | - | - | Request Body 없음 |

### Request Headers

| 헤더명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| Authorization | String | 예 | `Bearer <Access Token>` |

### Response

#### 성공 응답 (200 OK)

| 필드명 | 타입 | 설명 |
|--------|------|------|
| code | String | 응답 코드 (예: "SUCCESS", "BAD_REQUEST") |
| message | String | 응답 메시지 ("성공") |
| data | Object | 사용자 정보 |
| data.id | Long | 사용자 ID |
| data.profileImageUrl | String | 프로필 이미지 URL (nullable) |
| data.nickname | String | 닉네임 |
| data.name | String | 이름 |
| data.introduction | String | 소개글 (nullable, 최대 1000자) |
| data.birthDate | String | 생년월일 (ISO 8601 형식: YYYY-MM-DD, nullable) |
| data.email | String | 이메일 |
| data.addressSido | String | 거주지 시/도 (nullable) |
| data.addressGugun | String | 거주지 구/군 (nullable) |
| data.createdAt | String | 가입일시 (ISO 8601 형식: YYYY-MM-DDTHH:mm:ss) |

#### 에러 응답

| HTTP 상태 코드 | 설명 |
|---------------|------|
| 401 | 인증되지 않음 (토큰이 없거나 유효하지 않음) |
| 404 | 사용자를 찾을 수 없음 |
| 500 | 서버 내부 오류 |

### Request 예시

```http
GET /api/profile/me HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

### Response 예시

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": {
    "id": 1,
    "profileImageUrl": "https://s3.amazonaws.com/profile/user123.jpg",
    "nickname": "길동이",
    "name": "홍길동",
    "introduction": "반려동물을 사랑하는 사람입니다.",
    "birthDate": "1990-01-01",
    "email": "hong@example.com",
    "addressSido": "서울특별시",
    "addressGugun": "강남구",
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

---

## 2. 내가 찜한 상품 목록 조회

현재 로그인한 사용자가 찜한 상품 목록을 조회합니다.

### 엔드포인트

```
GET /api/profile/me/favorites
```

### 설명

- 현재 로그인한 사용자가 찜한 상품 목록을 조회합니다.
- 인증이 필요합니다 (`Authorization` 헤더 필수).
- 페이지네이션을 지원합니다.
- 최신순으로 정렬됩니다 (찜한 날짜 기준 내림차순).
- 소프트 삭제된 상품은 목록에서 제외됩니다.

### Request

#### Query Parameters

| 파라미터명 | 타입 | 필수 | 설명 | 기본값 |
|-----------|------|------|------|--------|
| page | Integer | 아니오 | 페이지 번호 (0부터 시작) | 0 |
| size | Integer | 아니오 | 페이지 크기 | 20 |
| sort | String | 아니오 | 정렬 기준 (예: createdAt,desc) | createdAt,desc |

### Request Headers

| 헤더명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| Authorization | String | 예 | `Bearer <Access Token>` |

### Response

#### 성공 응답 (200 OK)

| 필드명 | 타입 | 설명 |
|--------|------|------|
| code | String | 응답 코드 (예: "SUCCESS", "BAD_REQUEST") |
| message | String | 응답 메시지 ("성공") |
| data | Object | 찜한 상품 목록 정보 |
| data.page | Integer | 현재 페이지 번호 (0부터 시작) |
| data.size | Integer | 페이지 크기 |
| data.total | Long | 전체 항목 수 |
| data.content | Array | 찜한 상품 목록 |
| data.content[].id | Long | 상품 ID |
| data.content[].mainImageUrl | String | 대표 이미지 URL (nullable) |
| data.content[].title | String | 상품명 |
| data.content[].price | Long | 가격 |
| data.content[].viewCount | Long | 조회수 |
| data.content[].tradeStatus | String | 거래 상태 (SELLING, RESERVED, SOLD_OUT, BUYING) |
| data.totalPages | Integer | 전체 페이지 수 |
| data.hasNext | Boolean | 다음 페이지 존재 여부 |
| data.hasPrevious | Boolean | 이전 페이지 존재 여부 |
| data.totalElements | Long | 전체 항목 수 |
| data.numberOfElements | Integer | 현재 페이지의 항목 수 |

#### 에러 응답

| HTTP 상태 코드 | 설명 |
|---------------|------|
| 401 | 인증되지 않음 (토큰이 없거나 유효하지 않음) |
| 404 | 사용자를 찾을 수 없음 |
| 500 | 서버 내부 오류 |

### Request 예시

```http
GET /api/profile/me/favorites?page=0&size=20 HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

### Response 예시

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": {
    "page": 0,
    "size": 20,
    "total": 5,
    "content": [
      {
        "id": 10,
        "mainImageUrl": "https://s3.amazonaws.com/products/product10.jpg",
        "title": "강아지 장난감",
        "price": 15000,
        "viewCount": 42,
        "tradeStatus": "SELLING"
      },
      {
        "id": 8,
        "mainImageUrl": "https://s3.amazonaws.com/products/product8.jpg",
        "title": "고양이 캣타워",
        "price": 50000,
        "viewCount": 128,
        "tradeStatus": "SELLING"
      }
    ],
    "totalPages": 1,
    "hasNext": false,
    "hasPrevious": false,
    "totalElements": 5,
    "numberOfElements": 5
  }
}
```

---

## 3. 내가 등록한 판매 상품 목록 조회

현재 로그인한 사용자가 등록한 판매 상품 목록을 조회합니다.

### 엔드포인트

```
GET /api/profile/me/products
```

### 설명

- 현재 로그인한 사용자가 등록한 판매 상품 목록을 조회합니다.
- 인증이 필요합니다 (`Authorization` 헤더 필수).
- 페이지네이션을 지원합니다.
- 최신순으로 정렬됩니다 (등록일 기준 내림차순).
- 판매 상품(SELL)만 조회되며, 판매 요청(REQUEST)은 제외됩니다.

### Request

#### Query Parameters

| 파라미터명 | 타입 | 필수 | 설명 | 기본값 |
|-----------|------|------|------|--------|
| page | Integer | 아니오 | 페이지 번호 (0부터 시작) | 0 |
| size | Integer | 아니오 | 페이지 크기 | 20 |
| sort | String | 아니오 | 정렬 기준 (예: createdAt,desc) | createdAt,desc |

### Request Headers

| 헤더명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| Authorization | String | 예 | `Bearer <Access Token>` |

### Response

#### 성공 응답 (200 OK)

| 필드명 | 타입 | 설명 |
|--------|------|------|
| code | String | 응답 코드 (예: "SUCCESS", "BAD_REQUEST") |
| message | String | 응답 메시지 ("성공") |
| data | Object | 판매 상품 목록 정보 |
| data.page | Integer | 현재 페이지 번호 (0부터 시작) |
| data.size | Integer | 페이지 크기 |
| data.total | Long | 전체 항목 수 |
| data.content | Array | 판매 상품 목록 |
| data.content[].id | Long | 상품 ID |
| data.content[].mainImageUrl | String | 대표 이미지 URL (nullable) |
| data.content[].petDetailType | String | 반려동물 상세 종류 (DOG_POMERANIAN, DOG_POODLE, CAT_PERSIAN 등) |
| data.content[].productStatus | String | 상품 상태 (NEW, LIKE_NEW, USED, DAMAGED) |
| data.content[].productType | String | 상품 타입 (SELL: 판매 상품) |
| data.content[].tradeStatus | String | 거래 상태 (SELLING, RESERVED, SOLD_OUT) |
| data.content[].title | String | 상품명 |
| data.content[].price | Long | 가격 |
| data.content[].createdAt | String | 등록일시 (ISO 8601 형식: YYYY-MM-DDTHH:mm:ss) |
| data.content[].viewCount | Long | 조회수 |
| data.content[].favoriteCount | Long | 찜 개수 |
| data.content[].isFavorite | Boolean | 찜 여부 (본인 상품이므로 항상 false) |
| data.totalPages | Integer | 전체 페이지 수 |
| data.hasNext | Boolean | 다음 페이지 존재 여부 |
| data.hasPrevious | Boolean | 이전 페이지 존재 여부 |
| data.totalElements | Long | 전체 항목 수 |
| data.numberOfElements | Integer | 현재 페이지의 항목 수 |

#### 에러 응답

| HTTP 상태 코드 | 설명 |
|---------------|------|
| 401 | 인증되지 않음 (토큰이 없거나 유효하지 않음) |
| 404 | 사용자를 찾을 수 없음 |
| 500 | 서버 내부 오류 |

### Request 예시

```http
GET /api/profile/me/products?page=0&size=20 HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

### Response 예시

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": {
    "page": 0,
    "size": 20,
    "total": 3,
    "content": [
      {
        "id": 15,
        "mainImageUrl": "https://s3.amazonaws.com/products/product15.jpg",
        "petDetailType": "DOG_POMERANIAN",
        "productStatus": "NEW",
        "productType": "SELL",
        "tradeStatus": "SELLING",
        "title": "강아지 사료",
        "price": 30000,
        "createdAt": "2024-01-20T14:30:00",
        "viewCount": 120,
        "favoriteCount": 5,
        "isFavorite": false
      },
      {
        "id": 12,
        "mainImageUrl": "https://s3.amazonaws.com/products/product12.jpg",
        "petDetailType": "CAT_PERSIAN",
        "productStatus": "LIKE_NEW",
        "productType": "SELL",
        "tradeStatus": "RESERVED",
        "title": "고양이 화장실",
        "price": 25000,
        "createdAt": "2024-01-18T10:15:00",
        "viewCount": 85,
        "favoriteCount": 3,
        "isFavorite": false
      }
    ],
    "totalPages": 1,
    "hasNext": false,
    "hasPrevious": false,
    "totalElements": 3,
    "numberOfElements": 3
  }
}
```

---

## 4. 내가 등록한 판매 요청 목록 조회

현재 로그인한 사용자가 등록한 판매 요청 목록을 조회합니다.

### 엔드포인트

```
GET /api/profile/me/purchase-requests
```

### 설명

- 현재 로그인한 사용자가 등록한 판매 요청 목록을 조회합니다.
- 인증이 필요합니다 (`Authorization` 헤더 필수).
- 페이지네이션을 지원합니다.
- 최신순으로 정렬됩니다 (등록일 기준 내림차순).
- 판매 요청(REQUEST)만 조회되며, 판매 상품(SELL)은 제외됩니다.

### Request

#### Query Parameters

| 파라미터명 | 타입 | 필수 | 설명 | 기본값 |
|-----------|------|------|------|--------|
| page | Integer | 아니오 | 페이지 번호 (0부터 시작) | 0 |
| size | Integer | 아니오 | 페이지 크기 | 20 |
| sort | String | 아니오 | 정렬 기준 (예: createdAt,desc) | createdAt,desc |

### Request Headers

| 헤더명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| Authorization | String | 예 | `Bearer <Access Token>` |

### Response

#### 성공 응답 (200 OK)

| 필드명 | 타입 | 설명 |
|--------|------|------|
| code | String | 응답 코드 (예: "SUCCESS", "BAD_REQUEST") |
| message | String | 응답 메시지 ("성공") |
| data | Object | 판매 요청 목록 정보 |
| data.page | Integer | 현재 페이지 번호 (0부터 시작) |
| data.size | Integer | 페이지 크기 |
| data.total | Long | 전체 항목 수 |
| data.content | Array | 판매 요청 목록 |
| data.content[].id | Long | 상품 ID |
| data.content[].mainImageUrl | String | 대표 이미지 URL (nullable) |
| data.content[].petDetailType | String | 반려동물 상세 종류 (DOG_POMERANIAN, DOG_POODLE, CAT_PERSIAN 등) |
| data.content[].productStatus | String | 상품 상태 (NEW, LIKE_NEW, USED, DAMAGED) |
| data.content[].productType | String | 상품 타입 (REQUEST: 판매 요청) |
| data.content[].tradeStatus | String | 거래 상태 (BUYING) |
| data.content[].title | String | 상품명 |
| data.content[].price | Long | 희망 가격 |
| data.content[].createdAt | String | 등록일시 (ISO 8601 형식: YYYY-MM-DDTHH:mm:ss) |
| data.content[].viewCount | Long | 조회수 |
| data.content[].favoriteCount | Long | 찜 개수 |
| data.content[].isFavorite | Boolean | 찜 여부 (본인 상품이므로 항상 false) |
| data.totalPages | Integer | 전체 페이지 수 |
| data.hasNext | Boolean | 다음 페이지 존재 여부 |
| data.hasPrevious | Boolean | 이전 페이지 존재 여부 |
| data.totalElements | Long | 전체 항목 수 |
| data.numberOfElements | Integer | 현재 페이지의 항목 수 |

#### 에러 응답

| HTTP 상태 코드 | 설명 |
|---------------|------|
| 401 | 인증되지 않음 (토큰이 없거나 유효하지 않음) |
| 404 | 사용자를 찾을 수 없음 |
| 500 | 서버 내부 오류 |

### Request 예시

```http
GET /api/profile/me/purchase-requests?page=0&size=20 HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

### Response 예시

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": {
    "page": 0,
    "size": 20,
    "total": 2,
    "content": [
      {
        "id": 20,
        "mainImageUrl": "https://s3.amazonaws.com/products/product20.jpg",
        "petDetailType": "DOG_POMERANIAN",
        "productStatus": "NEW",
        "productType": "REQUEST",
        "tradeStatus": "BUYING",
        "title": "강아지 장난감 구매 원합니다",
        "price": 20000,
        "createdAt": "2024-01-22T09:20:00",
        "viewCount": 45,
        "favoriteCount": 2,
        "isFavorite": false
      },
      {
        "id": 18,
        "mainImageUrl": "https://s3.amazonaws.com/products/product18.jpg",
        "petDetailType": "CAT_PERSIAN",
        "productStatus": "LIKE_NEW",
        "productType": "REQUEST",
        "tradeStatus": "BUYING",
        "title": "고양이 캣타워 구매 원합니다",
        "price": 40000,
        "createdAt": "2024-01-19T16:45:00",
        "viewCount": 38,
        "favoriteCount": 1,
        "isFavorite": false
      }
    ],
    "totalPages": 1,
    "hasNext": false,
    "hasPrevious": false,
    "totalElements": 2,
    "numberOfElements": 2
  }
}
```

---

## 5. 프로필 정보 수정

현재 로그인한 사용자의 프로필 정보를 수정합니다.

### 엔드포인트

```
PATCH /api/profile/me
```

### 설명

- 현재 로그인한 사용자의 프로필 정보를 수정합니다.
- 인증이 필요합니다 (`Authorization` 헤더 필수).
- 닉네임, 지역, 프로필 이미지 URL, 소개글을 수정할 수 있습니다.
- 닉네임은 중복 검증이 수행되며, 본인의 기존 닉네임은 중복 체크에서 제외됩니다.
- 프로필 이미지는 별도 업로드 API를 통해 업로드한 후 URL을 전달해야 합니다.

### Request

#### Request Body

| 필드명 | 타입 | 필수 | 설명 | 제약조건 |
|--------|------|------|------|----------|
| nickname | String | 예 | 닉네임 | 1자 이상 10자 이하, 공백 불가 |
| addressSido | String | 아니오 | 거주지 시/도 | - |
| addressGugun | String | 아니오 | 거주지 구/군 | - |
| profileImageUrl | String | 아니오 | 프로필 이미지 URL | - |
| introduction | String | 아니오 | 소개글 | 최대 1000자 |

### Request Headers

| 헤더명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| Authorization | String | 예 | `Bearer <Access Token>` |
| Content-Type | String | 예 | application/json |

### Response

#### 성공 응답 (200 OK)

| 필드명 | 타입 | 설명 |
|--------|------|------|
| code | String | 응답 코드 (예: "SUCCESS", "BAD_REQUEST") |
| message | String | 응답 메시지 ("성공") |
| data | Object | 수정된 사용자 정보 |
| data.id | Long | 사용자 ID |
| data.email | String | 이메일 |
| data.name | String | 이름 |
| data.nickname | String | 닉네임 |
| data.birthDate | String | 생년월일 (ISO 8601 형식: YYYY-MM-DD, nullable) |
| data.addressSido | String | 거주지 시/도 (nullable) |
| data.addressGugun | String | 거주지 구/군 (nullable) |

#### 에러 응답

| HTTP 상태 코드 | 설명 |
|---------------|------|
| 400 | 입력값 검증 실패 (닉네임 길이, 소개글 길이 등) |
| 401 | 인증되지 않음 (토큰이 없거나 유효하지 않음) |
| 404 | 사용자를 찾을 수 없음 |
| 409 | 닉네임이 이미 사용 중임 |
| 500 | 서버 내부 오류 |

### Request 예시

```http
PATCH /api/profile/me HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "nickname": "새로운닉네임",
  "addressSido": "서울특별시",
  "addressGugun": "강남구",
  "profileImageUrl": "https://s3.amazonaws.com/profile/new-image.jpg",
  "introduction": "안녕하세요. 반려동물을 사랑하는 사람입니다."
}
```

### Response 예시

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": {
    "id": 1,
    "email": "hong@example.com",
    "name": "홍길동",
    "nickname": "새로운닉네임",
    "birthDate": "1990-01-01",
    "addressSido": "서울특별시",
    "addressGugun": "강남구"
  }
}
```

---

## 6. 유저 프로필 조회

특정 사용자의 프로필 정보를 조회합니다.

### 엔드포인트

```
GET /api/profile/{userId}
```

### 설명

- 특정 사용자의 공개 프로필 정보를 조회합니다.
- 인증이 필요합니다 (`Authorization` 헤더 필수).
- 소프트 삭제된 사용자는 조회할 수 없습니다.
- 본인 정보 조회 API(`GET /api/profile/me`)와 동일한 응답 구조를 사용합니다.

### Request

#### Path Parameters

| 파라미터명 | 타입 | 필수 | 설명 |
|-----------|------|------|------|
| userId | Long | 예 | 조회할 사용자 ID |

### Request Headers

| 헤더명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| Authorization | String | 예 | `Bearer <Access Token>` |

### Response

#### 성공 응답 (200 OK)

| 필드명 | 타입 | 설명 |
|--------|------|------|
| code | String | 응답 코드 (예: "SUCCESS", "BAD_REQUEST") |
| message | String | 응답 메시지 ("성공") |
| data | Object | 유저 프로필 정보 |
| data.id | Long | 사용자 ID |
| data.profileImageUrl | String | 프로필 이미지 URL (nullable) |
| data.nickname | String | 닉네임 |
| data.name | String | 이름 |
| data.introduction | String | 소개글 (nullable, 최대 1000자) |
| data.birthDate | String | 생년월일 (ISO 8601 형식: YYYY-MM-DD, nullable) |
| data.email | String | 이메일 |
| data.addressSido | String | 거주지 시/도 (nullable) |
| data.addressGugun | String | 거주지 구/군 (nullable) |
| data.createdAt | String | 가입일시 (ISO 8601 형식: YYYY-MM-DDTHH:mm:ss) |

#### 에러 응답

| HTTP 상태 코드 | 설명 |
|---------------|------|
| 401 | 인증되지 않음 (토큰이 없거나 유효하지 않음) |
| 404 | 사용자를 찾을 수 없음 (삭제된 사용자 포함) |
| 500 | 서버 내부 오류 |

### Request 예시

```http
GET /api/profile/5 HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

### Response 예시

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": {
    "id": 5,
    "profileImageUrl": "https://s3.amazonaws.com/profile/user5.jpg",
    "nickname": "다른유저",
    "name": "김철수",
    "introduction": "반려동물 용품을 판매하고 있습니다.",
    "birthDate": "1995-05-15",
    "email": "kim@example.com",
    "addressSido": "서울특별시",
    "addressGugun": "강남구",
    "createdAt": "2024-02-01T09:15:00"
  }
}
```

---

## 7. 다른 유저가 등록한 판매 상품 목록 조회

특정 사용자가 등록한 판매 상품 목록을 조회합니다.

### 엔드포인트

```
GET /api/profile/{userId}/products
```

### 설명

- 특정 사용자가 등록한 판매 상품 목록을 조회합니다.
- 인증이 필요합니다 (`Authorization` 헤더 필수).
- 페이지네이션을 지원합니다.
- 최신순으로 정렬됩니다 (등록일 기준 내림차순).
- 판매 상품(SELL)만 조회되며, 판매 요청(REQUEST)은 제외됩니다.
- 소프트 삭제된 사용자의 상품은 조회할 수 없습니다.
- 현재 로그인한 사용자의 찜 여부가 포함됩니다.

### Request

#### Path Parameters

| 파라미터명 | 타입 | 필수 | 설명 |
|-----------|------|------|------|
| userId | Long | 예 | 조회할 사용자 ID |

#### Query Parameters

| 파라미터명 | 타입 | 필수 | 설명 | 기본값 |
|-----------|------|------|------|--------|
| page | Integer | 아니오 | 페이지 번호 (0부터 시작) | 0 |
| size | Integer | 아니오 | 페이지 크기 | 20 |
| sort | String | 아니오 | 정렬 기준 (예: createdAt,desc) | createdAt,desc |

### Request Headers

| 헤더명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| Authorization | String | 예 | `Bearer <Access Token>` |

### Response

#### 성공 응답 (200 OK)

| 필드명 | 타입 | 설명 |
|--------|------|------|
| code | String | 응답 코드 (예: "SUCCESS", "BAD_REQUEST") |
| message | String | 응답 메시지 ("성공") |
| data | Object | 판매 상품 목록 정보 |
| data.page | Integer | 현재 페이지 번호 (0부터 시작) |
| data.size | Integer | 페이지 크기 |
| data.total | Long | 전체 항목 수 |
| data.content | Array | 판매 상품 목록 |
| data.content[].id | Long | 상품 ID |
| data.content[].mainImageUrl | String | 대표 이미지 URL (nullable) |
| data.content[].petDetailType | String | 반려동물 상세 종류 (DOG_POMERANIAN, DOG_POODLE, CAT_PERSIAN 등) |
| data.content[].productStatus | String | 상품 상태 (NEW, LIKE_NEW, USED, NEED_REPAIR) |
| data.content[].productType | String | 상품 타입 (SELL: 판매 상품) |
| data.content[].tradeStatus | String | 거래 상태 (SELLING, RESERVED, COMPLETED) |
| data.content[].title | String | 상품명 |
| data.content[].price | Long | 가격 |
| data.content[].createdAt | String | 등록일시 (ISO 8601 형식: YYYY-MM-DDTHH:mm:ss) |
| data.content[].viewCount | Long | 조회수 |
| data.content[].favoriteCount | Long | 찜 개수 |
| data.content[].isFavorite | Boolean | 찜 여부 (현재 로그인한 사용자가 찜했는지 여부) |
| data.totalPages | Integer | 전체 페이지 수 |
| data.hasNext | Boolean | 다음 페이지 존재 여부 |
| data.hasPrevious | Boolean | 이전 페이지 존재 여부 |
| data.totalElements | Long | 전체 항목 수 |
| data.numberOfElements | Integer | 현재 페이지의 항목 수 |

#### 에러 응답

| HTTP 상태 코드 | 설명 |
|---------------|------|
| 401 | 인증되지 않음 (토큰이 없거나 유효하지 않음) |
| 404 | 사용자를 찾을 수 없음 (삭제된 사용자 포함) |
| 500 | 서버 내부 오류 |

### Request 예시

```http
GET /api/profile/5/products?page=0&size=20 HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

### Response 예시

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": {
    "page": 0,
    "size": 20,
    "total": 3,
    "content": [
      {
        "id": 15,
        "mainImageUrl": "https://s3.amazonaws.com/products/product15.jpg",
        "petDetailType": "DOG_POMERANIAN",
        "productStatus": "NEW",
        "productType": "SELL",
        "tradeStatus": "SELLING",
        "title": "강아지 사료",
        "price": 30000,
        "createdAt": "2024-01-20T14:30:00",
        "viewCount": 120,
        "favoriteCount": 5,
        "isFavorite": true
      },
      {
        "id": 12,
        "mainImageUrl": "https://s3.amazonaws.com/products/product12.jpg",
        "petDetailType": "CAT_PERSIAN",
        "productStatus": "LIKE_NEW",
        "productType": "SELL",
        "tradeStatus": "RESERVED",
        "title": "고양이 화장실",
        "price": 25000,
        "createdAt": "2024-01-18T10:15:00",
        "viewCount": 85,
        "favoriteCount": 3,
        "isFavorite": false
      }
    ],
    "totalPages": 1,
    "hasNext": false,
    "hasPrevious": false,
    "totalElements": 3,
    "numberOfElements": 3
  }
}
```

---

## 8. 차단한 유저 목록 조회

현재 로그인한 사용자가 차단한 유저 목록을 조회합니다.

### 엔드포인트

```
GET /api/profile/me/blocked-users
```

### 설명

- 현재 로그인한 사용자가 차단한 유저 목록을 조회합니다.
- 인증이 필요합니다 (`Authorization` 헤더 필수).
- 페이지네이션을 지원합니다.
- 최신순으로 정렬됩니다 (차단한 날짜 기준 내림차순).
- 삭제된 사용자는 목록에서 제외됩니다.

### Request

#### Query Parameters

| 파라미터명 | 타입 | 필수 | 설명 | 기본값 |
|-----------|------|------|------|--------|
| page | Integer | 아니오 | 페이지 번호 (0부터 시작) | 0 |
| size | Integer | 아니오 | 페이지 크기 | 10 |
| sort | String | 아니오 | 정렬 기준 (예: createdAt,desc) | createdAt,desc |

### Request Headers

| 헤더명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| Authorization | String | 예 | `Bearer <Access Token>` |

### Response

#### 성공 응답 (200 OK)

| 필드명 | 타입 | 설명 |
|--------|------|------|
| code | String | 응답 코드 (예: "SUCCESS", "BAD_REQUEST") |
| message | String | 응답 메시지 ("성공") |
| data | Object | 차단한 유저 목록 정보 |
| data.blockedUsers | Object | 페이지네이션 결과 |
| data.blockedUsers.page | Integer | 현재 페이지 번호 (0부터 시작) |
| data.blockedUsers.size | Integer | 페이지 크기 |
| data.blockedUsers.total | Long | 전체 항목 수 |
| data.blockedUsers.content | Array | 차단한 유저 목록 |
| data.blockedUsers.content[].blockedUserId | Long | 차단한 유저 ID |
| data.blockedUsers.content[].nickname | String | 차단한 유저 닉네임 |
| data.blockedUsers.content[].profileImageUrl | String | 차단한 유저 프로필 이미지 URL (nullable) |
| data.blockedUsers.totalPages | Integer | 전체 페이지 수 |
| data.blockedUsers.hasNext | Boolean | 다음 페이지 존재 여부 |
| data.blockedUsers.hasPrevious | Boolean | 이전 페이지 존재 여부 |
| data.blockedUsers.totalElements | Long | 전체 항목 수 (total과 동일) |
| data.blockedUsers.numberOfElements | Integer | 현재 페이지의 항목 수 |

#### 에러 응답

| HTTP 상태 코드 | 설명 |
|---------------|------|
| 401 | 인증되지 않음 (토큰이 없거나 유효하지 않음) |
| 404 | 사용자를 찾을 수 없음 |
| 500 | 서버 내부 오류 |

### Request 예시

```http
GET /api/profile/me/blocked-users?page=0&size=10 HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

### Response 예시

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": {
    "blockedUsers": {
      "page": 0,
      "size": 10,
      "total": 2,
      "content": [
        {
          "blockedUserId": 5,
          "nickname": "차단된유저1",
          "profileImageUrl": "https://s3.amazonaws.com/profile/user5.jpg"
        },
        {
          "blockedUserId": 7,
          "nickname": "차단된유저2",
          "profileImageUrl": null
        }
      ],
      "totalPages": 1,
      "hasNext": false,
      "hasPrevious": false,
      "totalElements": 2,
      "numberOfElements": 2
    }
  }
}
```

---

## 9. 유저 차단 해제

현재 로그인한 사용자가 차단한 유저를 차단 해제합니다.

### 엔드포인트

```
DELETE /api/profile/me/blocked-users/{blockedUserId}
```

### 설명

- 현재 로그인한 사용자가 차단한 유저를 차단 해제합니다.
- 인증이 필요합니다 (`Authorization` 헤더 필수).
- 차단 관계가 존재하지 않는 경우에도 성공으로 처리됩니다 (idempotent).
- 동일한 요청을 여러 번 호출해도 안전합니다.

### Request

#### Path Parameters

| 파라미터명 | 타입 | 필수 | 설명 |
|-----------|------|------|------|
| blockedUserId | Long | 예 | 차단 해제할 사용자 ID |

### Request Headers

| 헤더명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| Authorization | String | 예 | `Bearer <Access Token>` |

### Response

#### 성공 응답 (200 OK)

| 필드명 | 타입 | 설명 |
|--------|------|------|
| code | String | 응답 코드 (예: "SUCCESS", "BAD_REQUEST") |
| message | String | 응답 메시지 ("성공") |
| data | null | 응답 데이터 없음 |

#### 에러 응답

| HTTP 상태 코드 | 설명 |
|---------------|------|
| 401 | 인증되지 않음 (토큰이 없거나 유효하지 않음) |
| 404 | 사용자를 찾을 수 없음 |
| 500 | 서버 내부 오류 |

### Request 예시

```http
DELETE /api/profile/me/blocked-users/5 HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

### Response 예시

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": null
}
```

---

## 주의사항

### 인증

모든 프로필 API는 인증이 필요합니다. 요청 시 `Authorization` 헤더에 유효한 JWT 토큰을 포함해야 합니다.

```
Authorization: Bearer <Access Token>
```

### 프로필 이미지 업로드

프로필 이미지를 업로드하려면 별도의 이미지 업로드 API를 사용해야 합니다. 업로드된 이미지의 URL을 `profileImageUrl` 필드에 전달하면 됩니다.

### 페이지네이션

다음 API들은 페이지네이션을 지원합니다. `page`와 `size` 파라미터를 사용하여 원하는 페이지를 조회할 수 있습니다.

- `page`: 페이지 번호 (0부터 시작)
- `size`: 페이지 크기

페이지네이션을 지원하는 API:
- 내가 찜한 상품 목록 조회 (기본값: size=20)
- 내가 등록한 판매 상품 목록 조회 (기본값: size=20)
- 내가 등록한 판매 요청 목록 조회 (기본값: size=20)
- 다른 유저가 등록한 판매 상품 목록 조회 (기본값: size=20)
- 차단한 유저 목록 조회 (기본값: size=10)

---

## 에러 코드 상세

### 400 Bad Request

입력값 검증 실패 시 반환됩니다.

```json
{
  "code": "BAD_REQUEST",
  "message": "닉네임은 1자 이상 10자 이하여야 합니다.",
  "traceId": "e1e4456f40d648c7a24fc7d5cd85e4af"
}
```

### 401 Unauthorized

인증이 필요하거나 토큰이 유효하지 않을 때 반환됩니다.

```json
{
  "code": "UNAUTHORIZED",
  "message": "인증되지 않은 사용자입니다.",
  "traceId": "e1e4456f40d648c7a24fc7d5cd85e4af"
}
```

### 404 Not Found

요청한 리소스를 찾을 수 없을 때 반환됩니다.

```json
{
  "code": "NOT_FOUND",
  "message": "사용자를 찾을 수 없습니다.",
  "traceId": "e1e4456f40d648c7a24fc7d5cd85e4af"
}
```

### 409 Conflict

리소스 충돌이 발생했을 때 반환됩니다 (예: 닉네임 중복).

```json
{
  "code": "CONFLICT",
  "message": "이미 사용 중인 닉네임입니다.",
  "traceId": "e1e4456f40d648c7a24fc7d5cd85e4af"
}
```

---

## 참고사항

- 모든 날짜/시간 필드는 ISO 8601 형식을 따릅니다.
- `nullable`로 표시된 필드는 `null` 값이 반환될 수 있습니다.
- 페이지네이션은 0부터 시작합니다.
- 차단 해제 API는 idempotent하므로 동일한 요청을 여러 번 호출해도 안전합니다.

