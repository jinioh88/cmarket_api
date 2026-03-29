# 지도 API 문서

> 커들마켓 지도 기반 생활 서비스 - **지도 / 장소 / 리뷰 / 어드민 장소 관리** API

본 문서는 현재 백엔드에 구현된 지도 기능 관련 API를 정리한 문서입니다.  
프론트엔드에서 지도 화면, 장소 상세, 리뷰, 관리자 장소 관리 화면 구현 시 참고할 수 있도록 요청/응답 규격과 예시를 포함합니다.

---

## 목차

1. [공통 사항](#공통-사항)
2. [Enum 목록](#enum-목록)
3. [사용자 지도 API](#사용자-지도-api)
   - [3-1. 장소 목록 조회](#3-1-장소-목록-조회-get-apiplaces)
   - [3-2. 장소 상세 조회](#3-2-장소-상세-조회-get-apiplacesplaceid)
4. [리뷰 API](#리뷰-api)
   - [4-1. 장소 리뷰 목록 조회](#4-1-장소-리뷰-목록-조회-get-apiplacesplaceidreviews)
   - [4-2. 장소 리뷰 작성](#4-2-장소-리뷰-작성-post-apiplacesplaceidreviews)
5. [어드민 장소 관리 API](#어드민-장소-관리-api)
   - [5-1. 어드민 장소 목록 조회](#5-1-어드민-장소-목록-조회-get-apiadminplaces)
   - [5-2. 어드민 장소 등록](#5-2-어드민-장소-등록-post-apiadminplaces)
   - [5-3. 어드민 장소 수정](#5-3-어드민-장소-수정-patch-apiadminplacesplaceid)
   - [5-4. 추천 여부 수정](#5-4-추천-여부-수정-patch-apiadminplacesplaceidrecommendation)
   - [5-5. 공공데이터 동물병원 import](#5-5-공공데이터-동물병원-import-post-apiadminplacesimporthospitals)
6. [FAQ 및 참고](#faq-및-참고)

---

## 공통 사항

### Base URL

```text
http://localhost:8080
```

### 공통 헤더

| 헤더명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| Authorization | String | 조건 | `Bearer <Access Token>` |
| Content-Type | String | 조건 | JSON 요청 시 `application/json` |
| X-Trace-Id | String | 옵션 | 요청 추적 ID |

### 공통 응답 형식

#### 성공

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": { }
}
```

#### 에러

```json
{
  "code": "BAD_REQUEST",
  "message": "에러 메시지",
  "traceId": "e1e4-...",
  "timestamp": "2026-03-29T10:30:00"
}
```

### 인증 정책

- 장소 목록/상세 조회: 인증 불필요
- 리뷰 목록 조회: 인증 불필요
- 리뷰 작성: 인증 필요
- 어드민 장소 관리 API: `ADMIN` 권한 필요

### 기본 좌표 정책

- `GET /api/places` 호출 시 `latitude`, `longitude`를 보내지 않으면 서버는 서울 시청 좌표를 기본값으로 사용합니다.
- 기본 좌표:
  - `latitude = 37.5666`
  - `longitude = 126.9784`

---

## Enum 목록

### PlaceCategory

| 값 | 설명 |
|----|------|
| `HOSPITAL` | 동물병원 |
| `CAFE` | 카페 |
| `RESTAURANT` | 식당 |
| `ACCOMMODATION` | 숙소 |

### AnimalType

| 값 | 설명 |
|----|------|
| `REPTILE` | 파충류 |
| `BIRD` | 조류 |

### PlaceSourceType

| 값 | 설명 |
|----|------|
| `NAVER` | 외부 API/외부 데이터 기반 |
| `ADMIN` | 관리자 수기 등록 |

---

## 사용자 지도 API

### 3-1. 장소 목록 조회 (GET /api/places)

- **인증 필요**: 아니오
- **설명**: 지도 화면에서 반경 기반으로 장소를 조회합니다.
- **특징**:
  - 카테고리별 조회
  - 추천 장소는 `isRecommended=true` 필터로만 조회
  - 병원 카테고리일 때만 병원 전용 필터 사용 가능
  - `latitude`, `longitude`가 없으면 서버 기본 좌표(서울 시청) 사용

#### Query Parameters

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| `category` | `PlaceCategory` | 예 | - | 장소 카테고리 |
| `latitude` | Double | 아니오 | `37.5666` | 지도 중심 위도 |
| `longitude` | Double | 아니오 | `126.9784` | 지도 중심 경도 |
| `radius` | Double | 예 | - | 조회 반경(km), 최대 30km |
| `isRecommended` | Boolean | 아니오 | - | 추천 장소만 필터링 |
| `is24Hours` | Boolean | 아니오 | - | 병원 전용 필터 |
| `isEmergencyAvailable` | Boolean | 아니오 | - | 병원 전용 필터 |
| `animalTypes` | `AnimalType[]` | 아니오 | - | 병원 전용 다중 필터 |
| `page` | Integer | 아니오 | `0` | 페이지 번호 |
| `size` | Integer | 아니오 | `20` | 페이지 크기 |

#### 요청 예시

**1. 병원 목록 조회**

```text
GET /api/places?category=HOSPITAL&latitude=37.4979&longitude=127.0276&radius=5&page=0&size=20
```

**2. 24시 + 응급 병원 조회**

```text
GET /api/places?category=HOSPITAL&latitude=37.4979&longitude=127.0276&radius=5&is24Hours=true&isEmergencyAvailable=true
```

**3. 추천 카페만 조회**

```text
GET /api/places?category=CAFE&latitude=37.4979&longitude=127.0276&radius=3&isRecommended=true
```

**4. 기본 좌표 기준 식당 조회**

```text
GET /api/places?category=RESTAURANT&radius=3
```

#### Response Body (`PlaceListResponse`)

| 필드 | 타입 | 설명 |
|------|------|------|
| `page` | Integer | 현재 페이지 |
| `size` | Integer | 페이지 크기 |
| `total` | Long | 전체 개수 |
| `items` | `PlaceListItemResponse[]` | 장소 목록 |
| `totalPages` | Integer | 전체 페이지 수 |
| `hasNext` | Boolean | 다음 페이지 존재 여부 |
| `hasPrevious` | Boolean | 이전 페이지 존재 여부 |
| `totalElements` | Long | 전체 요소 수 |
| `numberOfElements` | Long | 현재 페이지 요소 수 |

#### `items` 항목 (`PlaceListItemResponse`)

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | Long | 장소 ID |
| `category` | `PlaceCategory` | 장소 카테고리 |
| `name` | String | 장소명 |
| `latitude` | Double | 위도 |
| `longitude` | Double | 경도 |
| `isRecommended` | Boolean | 추천 여부 |
| `reviewSummary.reviewCount` | Long | 리뷰 개수 |
| `reviewSummary.averageRating` | Double | 평균 평점 |
| `detail` | Object or null | 병원일 경우 상세 정보, 그 외 카테고리는 `null` |
| `detail.is24Hours` | Boolean | 24시간 운영 여부 |
| `detail.isEmergencyAvailable` | Boolean | 응급 진료 가능 여부 |
| `detail.animalTypes` | `AnimalType[]` | 진료 가능 동물 타입 |

#### 응답 예시

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": {
    "page": 0,
    "size": 20,
    "total": 2,
    "items": [
      {
        "id": 101,
        "category": "HOSPITAL",
        "name": "커들 24시 동물병원",
        "latitude": 37.4979,
        "longitude": 127.0276,
        "isRecommended": true,
        "reviewSummary": {
          "reviewCount": 18,
          "averageRating": 4.7
        },
        "detail": {
          "is24Hours": true,
          "isEmergencyAvailable": true,
          "animalTypes": ["REPTILE", "BIRD"]
        }
      },
      {
        "id": 205,
        "category": "CAFE",
        "name": "커들 펫 카페",
        "latitude": 37.4981,
        "longitude": 127.0281,
        "isRecommended": false,
        "reviewSummary": {
          "reviewCount": 3,
          "averageRating": 4.3
        },
        "detail": null
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

### 3-2. 장소 상세 조회 (GET /api/places/{placeId})

- **인증 필요**: 아니오
- **설명**: 마커 클릭 시 장소 상세 정보를 조회합니다.

#### Path Parameters

| 파라미터 | 설명 |
|----------|------|
| `placeId` | 장소 ID |

#### Response Body (`PlaceDetailResponse`)

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | Long | 장소 ID |
| `category` | `PlaceCategory` | 카테고리 |
| `name` | String | 장소명 |
| `address` | String | 주소 |
| `phone` | String | 전화번호 |
| `operatingHours` | String | 영업시간 |
| `imageUrl` | String | 대표 이미지 URL |
| `latitude` | Double | 위도 |
| `longitude` | Double | 경도 |
| `isRecommended` | Boolean | 추천 여부 |
| `reviewSummary` | Object | 리뷰 요약 |
| `detail` | Object or null | 병원 전용 상세 정보 |

#### 응답 예시

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": {
    "id": 101,
    "category": "HOSPITAL",
    "name": "커들 24시 동물병원",
    "address": "서울특별시 강남구 테헤란로 123",
    "phone": "02-1234-5678",
    "operatingHours": "00:00 ~ 24:00",
    "imageUrl": "https://example.com/place/hospital-101.jpg",
    "latitude": 37.4979,
    "longitude": 127.0276,
    "isRecommended": true,
    "reviewSummary": {
      "reviewCount": 18,
      "averageRating": 4.7
    },
    "detail": {
      "is24Hours": true,
      "isEmergencyAvailable": true,
      "animalTypes": ["REPTILE", "BIRD"]
    }
  }
}
```

---

## 리뷰 API

### 4-1. 장소 리뷰 목록 조회 (GET /api/places/{placeId}/reviews)

- **인증 필요**: 아니오
- **설명**: 특정 장소의 리뷰 목록을 조회합니다.

#### Path Parameters

| 파라미터 | 설명 |
|----------|------|
| `placeId` | 장소 ID |

#### Query Parameters

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| `sort` | String | 아니오 | `latest` | `latest`, `rating` |
| `page` | Integer | 아니오 | `0` | 페이지 번호 |
| `size` | Integer | 아니오 | `10` | 페이지 크기 |

#### Response Body (`PlaceReviewListResponse`)

| 필드 | 타입 | 설명 |
|------|------|------|
| `items` | `PlaceReviewResponse[]` | 리뷰 목록 |
| `page` | Integer | 현재 페이지 |
| `size` | Integer | 페이지 크기 |
| `totalCount` | Long | 전체 리뷰 수 |
| `totalPages` | Integer | 전체 페이지 수 |
| `hasNext` | Boolean | 다음 페이지 존재 여부 |
| `hasPrevious` | Boolean | 이전 페이지 존재 여부 |

#### `PlaceReviewResponse`

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | Long | 리뷰 ID |
| `nickname` | String | 작성자 닉네임 |
| `rating` | Integer | 평점 (1~5) |
| `content` | String | 리뷰 내용 |
| `imageUrls` | String[] | 리뷰 이미지 URL |
| `createdAt` | String | 작성 시각 (ISO 8601) |

#### 요청 예시

```text
GET /api/places/101/reviews?sort=latest&page=0&size=10
```

#### 응답 예시

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": {
    "items": [
      {
        "id": 1,
        "nickname": "댕댕이맘",
        "rating": 5,
        "content": "친절하고 응급 대응이 빨랐어요.",
        "imageUrls": [
          "https://example.com/reviews/1.jpg"
        ],
        "createdAt": "2026-03-29T09:30:00"
      }
    ],
    "page": 0,
    "size": 10,
    "totalCount": 1,
    "totalPages": 1,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

---

### 4-2. 장소 리뷰 작성 (POST /api/places/{placeId}/reviews)

- **인증 필요**: 예
- **설명**: 로그인 사용자가 특정 장소에 리뷰를 작성합니다.

#### Path Parameters

| 파라미터 | 설명 |
|----------|------|
| `placeId` | 장소 ID |

#### Request Body (`PlaceReviewCreateRequest`)

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `rating` | Integer | 예 | 1~5 |
| `content` | String | 예 | 최대 1000자 |
| `imageUrls` | String[] | 아니오 | 최대 5개 |

#### 요청 예시

```json
{
  "rating": 5,
  "content": "친절하고 좋아요",
  "imageUrls": [
    "https://example.com/reviews/review-01.jpg"
  ]
}
```

#### 응답 예시

```json
{
  "code": "CREATED",
  "message": "성공",
  "data": {
    "id": 12,
    "nickname": "반려인A",
    "rating": 5,
    "content": "친절하고 좋아요",
    "imageUrls": [
      "https://example.com/reviews/review-01.jpg"
    ],
    "createdAt": "2026-03-29T11:00:00"
  }
}
```

---

## 어드민 장소 관리 API

### 5-1. 어드민 장소 목록 조회 (GET /api/admin/places)

- **인증 필요**: 예 (`ADMIN`)
- **설명**: 어드민 화면에서 장소 목록을 조회합니다.

#### Query Parameters

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| `keyword` | String | 아니오 | - | 장소명/주소 검색 |
| `category` | `PlaceCategory` | 아니오 | - | 카테고리 필터 |
| `isRecommended` | Boolean | 아니오 | - | 추천 여부 필터 |
| `page` | Integer | 아니오 | `0` | 페이지 번호 |
| `size` | Integer | 아니오 | `20` | 페이지 크기 |

#### 응답 예시

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": {
    "content": [
      {
        "id": 205,
        "category": "CAFE",
        "name": "커들 펫 카페",
        "address": "서울특별시 강남구 역삼동 100",
        "isRecommended": true,
        "sourceType": "ADMIN",
        "updatedAt": "2026-03-29T11:30:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

---

### 5-2. 어드민 장소 등록 (POST /api/admin/places)

- **인증 필요**: 예 (`ADMIN`)
- **설명**: 관리자 수기 등록용 장소 생성 API입니다.

#### Request Body (`AdminPlaceRequest`)

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `category` | `PlaceCategory` | 예 | 장소 카테고리 |
| `name` | String | 예 | 장소명 |
| `address` | String | 예 | 주소 |
| `phone` | String | 아니오 | 전화번호 |
| `operatingHours` | String | 아니오 | 영업시간 |
| `imageUrl` | String | 아니오 | 대표 이미지 |
| `latitude` | Double | 예 | 위도 |
| `longitude` | Double | 예 | 경도 |
| `isRecommended` | Boolean | 아니오 | 추천 여부 |
| `is24Hours` | Boolean | 아니오 | 병원 전용 |
| `isEmergencyAvailable` | Boolean | 아니오 | 병원 전용 |
| `animalTypes` | `AnimalType[]` | 아니오 | 병원 전용 |

#### 요청 예시

```json
{
  "category": "CAFE",
  "name": "커들 펫 카페",
  "address": "서울특별시 강남구 역삼동 100",
  "phone": "02-1111-2222",
  "operatingHours": "10:00 ~ 21:00",
  "imageUrl": "https://example.com/places/cafe-205.jpg",
  "latitude": 37.4981,
  "longitude": 127.0281,
  "isRecommended": true
}
```

#### 응답 예시

```json
{
  "code": "CREATED",
  "message": "성공",
  "data": {
    "id": 205,
    "category": "CAFE",
    "name": "커들 펫 카페",
    "address": "서울특별시 강남구 역삼동 100",
    "phone": "02-1111-2222",
    "operatingHours": "10:00 ~ 21:00",
    "imageUrl": "https://example.com/places/cafe-205.jpg",
    "latitude": 37.4981,
    "longitude": 127.0281,
    "isRecommended": true,
    "detail": null
  }
}
```

---

### 5-3. 어드민 장소 수정 (PATCH /api/admin/places/{placeId})

- **인증 필요**: 예 (`ADMIN`)
- **설명**: 등록된 장소 정보를 수정합니다.
- **Request Body**: `POST /api/admin/places` 와 동일

#### 요청 예시

```json
{
  "category": "HOSPITAL",
  "name": "커들 특수동물 병원",
  "address": "서울특별시 강남구 테헤란로 200",
  "phone": "02-3333-4444",
  "operatingHours": "00:00 ~ 24:00",
  "imageUrl": "https://example.com/places/hospital-301.jpg",
  "latitude": 37.501,
  "longitude": 127.03,
  "isRecommended": false,
  "is24Hours": true,
  "isEmergencyAvailable": true,
  "animalTypes": ["REPTILE", "BIRD"]
}
```

---

### 5-4. 추천 여부 수정 (PATCH /api/admin/places/{placeId}/recommendation)

- **인증 필요**: 예 (`ADMIN`)
- **설명**: 추천 체크박스 토글용 API입니다.

#### Request Body (`PlaceRecommendationUpdateRequest`)

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `isRecommended` | Boolean | 예 | 추천 여부 |

#### 요청 예시

```json
{
  "isRecommended": true
}
```

#### 응답 예시

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": {
    "id": 205,
    "category": "CAFE",
    "name": "커들 펫 카페",
    "address": "서울특별시 강남구 역삼동 100",
    "phone": "02-1111-2222",
    "operatingHours": "10:00 ~ 21:00",
    "imageUrl": "https://example.com/places/cafe-205.jpg",
    "latitude": 37.4981,
    "longitude": 127.0281,
    "isRecommended": true,
    "detail": null
  }
}
```

---

### 5-5. 공공데이터 동물병원 import (POST /api/admin/places/import/hospitals)

- **인증 필요**: 예 (`ADMIN`)
- **설명**: 공공데이터포털의 동물병원 Open API를 호출해 병원 데이터를 가져오고, 내부 `Place/HospitalDetail` 구조로 저장 또는 갱신합니다.
- **동작 방식**:
  - 외부 병원 데이터 조회
  - 좌표계(EPSG:5174) -> WGS84 위경도 변환
  - `externalPlaceId` 기준 upsert
  - 좌표가 없는 데이터는 skip
- **기본 연동 URL**:
  - `https://apis.data.go.kr/1741000/animal_hospitals/info`

#### Request Body (`HospitalImportRequest`)

| 필드 | 타입 | 필수 | 기본값 | 설명 |
|------|------|------|--------|------|
| `pageNo` | Integer | 아니오 | `1` | 조회 시작 페이지 |
| `numOfRows` | Integer | 아니오 | `100` | 페이지당 건수 |
| `importAllPages` | Boolean | 아니오 | `false` | `true`면 여러 페이지를 순차 import |
| `opnAtmyGrpCd` | String | 아니오 | - | 개방자치단체코드 |
| `salesStatusCode` | String | 아니오 | - | 영업상태코드 필터 |
| `roadNmAddrKeyword` | String | 아니오 | - | 도로명주소 LIKE 검색 |
| `businessNameKeyword` | String | 아니오 | - | 사업장명 LIKE 검색 |
| `updatedFrom` | String | 아니오 | - | 데이터갱신시점 시작값 (YYYYMMDDHHMMSS) |
| `updatedTo` | String | 아니오 | - | 데이터갱신시점 종료값 (YYYYMMDDHHMMSS) |

#### 요청 예시

```json
{
  "pageNo": 1,
  "numOfRows": 100,
  "importAllPages": false,
  "roadNmAddrKeyword": "서울특별시",
  "businessNameKeyword": "동물병원"
}
```

#### 응답 예시

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": {
    "fetchedCount": 100,
    "requestedPageNo": 1,
    "requestedNumOfRows": 100,
    "importAllPages": false,
    "requestedStartPage": 1,
    "requestedEndPage": 1,
    "processedPages": 1,
    "apiTotalCount": 12543,
    "importedCount": 93,
    "skippedCount": 7
  }
}
```

---

### 5-6. 공공데이터 동물병원 전체 적재 (POST /api/admin/places/import/hospitals/full)

- **인증 필요**: 예 (`ADMIN`)
- **설명**: 운영 DB 적재용 전체 import API입니다. 공공데이터 동물병원 API를 `pageNo=1` 부터 `pageNo=105` 까지, `numOfRows=100` 기준으로 순회 호출한 뒤 저장 또는 갱신합니다.
- **요청 바디**: 없음
- **고정 동작**:
  - 시작 페이지: `1`
  - 종료 페이지: `105`
  - 페이지 크기: `100`
  - 총 최대 조회 시도 건수: `10,500`
  - `externalPlaceId` 기준 upsert

#### 호출 예시

```bash
curl -X POST "https://cmarket-api.duckdns.org/api/admin/places/import/hospitals/full" \
  -H "Authorization: Bearer {ADMIN_ACCESS_TOKEN}"
```

#### 응답 예시

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": {
    "fetchedCount": 10490,
    "requestedPageNo": 1,
    "requestedNumOfRows": 100,
    "importAllPages": true,
    "requestedStartPage": 1,
    "requestedEndPage": 105,
    "processedPages": 105,
    "apiTotalCount": 10490,
    "importedCount": 10312,
    "skippedCount": 178
  }
}
```

#### 운영 참고

- 서비스 키는 코드에 직접 넣지 않고 환경 변수로 주입합니다.
- 환경 변수:
  - `ANIMAL_HOSPITAL_API_SERVICE_KEY`
  - `ANIMAL_HOSPITAL_API_BASE_URL` (선택)
- 기본 `ANIMAL_HOSPITAL_API_BASE_URL` 값:
  - `https://apis.data.go.kr/1741000/animal_hospitals`

---

## FAQ 및 참고

### 1. 병원 전용 필터를 카페/식당/숙소에 보내면 어떻게 되나요?

- 현재 서버는 `category=HOSPITAL`이 아닌 경우 병원 전용 필터 조합을 허용하지 않습니다.
- 잘못된 조합은 `BAD_REQUEST`로 처리될 수 있습니다.

### 2. 추천 장소는 자동으로 상단 고정되나요?

- 아니오.
- 현재 구현은 추천 여부를 별도 필터(`isRecommended`)로만 사용합니다.
- 정렬은 거리순입니다.

### 3. 카페/식당/숙소 상세 응답의 `detail`은 무엇이 오나요?

- 현재는 병원만 전용 상세 정보가 있으므로 `null` 입니다.

### 4. 장소 데이터는 어디서 들어오나요?

- 현재 구현 기준으로는 관리자 수기 등록(`ADMIN`) 데이터가 우선 사용 가능합니다.
- 공공데이터 동물병원 Open API는 어드민 import API로 연동 가능합니다.
