# 상품 API 문서

> 반려동물 용품 중고거래 서비스 – **상품/판매요청** API

본 문서는 상품(팝니다)과 판매요청(삽니다) 관련 API에 대한 상세 설명을 제공합니다.  
프론트엔드 개발자가 클라이언트 기능을 구현할 때 필요한 요청/응답 규격과 Enum 목록을 모두 포함합니다.

---

## 목차

1. [공통 사항](#공통-사항)
2. [Enum 목록](#enum-목록)
3. [판매 상품 API](#판매-상품-api)
   - [3-1. 판매 상품 등록](#3-1-판매-상품-등록-post-apiproducts)
   - [3-2. 판매 상품 목록 조회](#3-2-판매-상품-목록-조회-get-apiproducts)
   - [3-3. 판매 상품 상세 조회](#3-3-판매-상품-상세-조회-get-apiproductsid)
   - [3-4. 판매 상품 수정](#3-4-판매-상품-수정-patch-apiproductsid)
   - [3-5. 거래 상태 변경](#3-5-거래-상태-변경-patch-apiproductsidtrade-status)
   - [3-6. 판매 상품 삭제](#3-6-판매-상품삭제-delete-apiproductsid)
   - [3-7. 관심(찜) 토글](#3-7-관심찜-토글-post-apiproductsidfavorite)
   - [3-8. 관심 목록 조회](#3-8-관심-목록-조회-get-apiproductsfavorites)
4. [판매 요청 API](#판매-요청-apis)
   - [4-1. 판매 요청 등록](#4-1-판매-요청-등록-post-apiproductsrequests)
   - [4-2. 판매 요청 목록 조회](#4-2-판매-요청-목록-조회-get-apiproductsrequests)
   - [4-3. 판매 요청 상세 조회](#4-3-판매-요청-상세-조회-get-apiproductsrequestsid)
   - [4-4. 판매 요청 수정](#4-4-판매-요청-수정-patch-apiproductsrequestsid)
5. [내가 등록한 상품 목록](#내가-등록한-상품-목록-get-apiproductsme)
6. [FAQ 및 참고](#faq-및-참고)

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

> `Authorization`은 인증이 필요한 모든 API(등록/수정/삭제/찜/내 목록)에 필수입니다.  
> 목록/상세 조회 API는 인증 없이 호출할 수 있지만, 로그인 시 찜 여부, 조회수 증가 등이 반영됩니다.

### 공통 응답 형식

#### 성공
```json
{
  "code": { "code": 200, "message": "성공" },
  "message": "성공",
  "data": { ... }   // API별 응답 본문
}
```

#### 에러
```json
{
  "code": { "code": 400, "message": "잘못된 요청" },
  "message": "에러 메시지",
  "traceId": "e1e4-...",
  "timestamp": "2025-01-15T10:30:00"
}
```

### 페이지네이션 파라미터

모든 목록 조회 API는 Spring `page`/`size` 쿼리 파라미터를 사용합니다.  
기본값: `page=0`, `size=20`.  
응답은 `page`, `size`, `total`, `content`, `totalPages`, `hasNext` 등을 포함합니다.

---

## Enum 목록

| Enum | 값 |
|------|----|
| `PetType` | `REPTILE`, `MAMMAL`, `BIRD`, `FISH`, `AMPHIBIAN`, `ETC` |
| `PetDetailType` | `SNAKE`, `LIZARD`, `TURTLE`, `REPTILE_ETC`, `DOG`, `CAT`, `HAMSTER`, `RABBIT`, `GUINEA_PIG`, `MAMMAL_ETC`, `PARROT`, `CANARY`, `BIRD_ETC`, `GOLDFISH`, `TROPICAL_FISH`, `FISH_ETC`, `FROG`, `SALAMANDER`, `AMPHIBIAN_ETC`, `ETC` |
| `Category` | `FOOD`, `TOY`, `HOUSE`, `CLOTHING`, `HEALTH`, `GROOMING`, `WALKING`, `ETC` |
| `ProductStatus` | `NEW`, `LIKE_NEW`, `USED`, `NEED_REPAIR` |
| `ProductType` | `SELL`, `REQUEST` |
| `TradeStatus` | `SELLING`, `BUYING`, `RESERVED`, `COMPLETED` <br/>※ 판매 상품은 `SELLING/RESERVED/COMPLETED`, 판매 요청은 `BUYING/RESERVED/COMPLETED`를 사용합니다. |

---

## 판매 상품 API

### 3-1. 판매 상품 등록 (POST /api/products)

- **인증 필요**: 예 (`Bearer` 토큰)
- **설명**: 로그인 사용자가 판매 상품(팝니다) 게시글을 생성합니다.

#### Request Body

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| petType | `PetType` | 예 | 동물 대분류 |
| petDetailType | `PetDetailType` | 예 | 동물 소분류 |
| category | `Category` | 예 | 상품 카테고리 |
| title | String | 예 | 2~50자 |
| description | String | 옵션 | 최대 1000자 |
| price | Long | 예 | 0 이상 |
| productStatus | `ProductStatus` | 예 | 상품 상태 |
| mainImageUrl | String | 예 | 대표 이미지 URL |
| subImageUrls | String[] | 옵션 | 최대 4개 URL |
| addressSido | String | 예 | 거래 희망 지역 (시/도) |
| addressGugun | String | 예 | 거래 희망 지역 (구/군) |
| isDeliveryAvailable | Boolean | 옵션 | 기본 false |
| preferredMeetingPlace | String | 옵션 | 선호 만남 장소 |

#### Response Body (`ProductResponse`)

| 필드 | 설명 |
|------|------|
| id | 상품 ID |
| sellerId | 판매자 ID |
| productType | 항상 `SELL` |
| petType / petDetailType / category | 입력값 반영 |
| title / description / price | 입력값 반영 |
| productStatus / tradeStatus | `productStatus`, `SELLING`이 기본 |
| mainImageUrl / subImageUrls | 이미지 URL |
| addressSido / addressGugun / isDeliveryAvailable / preferredMeetingPlace | 지역/거래 정보 |
| viewCount / favoriteCount | 초기값 0 |
| isFavorite | false (본인 등록 상품 기준) |
| createdAt / updatedAt | 생성일시 |

---

### 3-2. 판매 상품 목록 조회 (GET /api/products)

- **인증 필요**: 아니오 (로그인 시 찜 여부 포함)
- **쿼리 파라미터**:

| 파라미터 | 타입 | 기본값 | 설명 |
|----------|------|--------|------|
| page | int | 0 | 페이지 번호 |
| size | int | 20 | 페이지 크기 |

#### Response (`ProductListResponse`)

- 페이지 정보 + `content` 배열

`content` 항목 (`ProductListItemResponse`):

| 필드 | 설명 |
|------|------|
| id | 상품 ID |
| mainImageUrl | 대표 이미지 |
| petDetailType | 동물 소분류 |
| productStatus | 상품 상태 |
| tradeStatus | 거래 상태 |
| title | 제목 |
| price | 가격 |
| createdAt | 등록일 |
| favoriteCount | 찜 개수 |
| isFavorite | 로그인 사용자의 찜 여부 |

---

### 3-3. 판매 상품 상세 조회 (GET /api/products/{id})

- **인증 필요**: 아니오 (로그인 시 찜 여부/조회수 처리)
- **조회수 증가**: 로그인 사용자가 판매자가 아닌 경우에만 서버 내부에서 자동 증가합니다.

#### Path

| 파라미터 | 설명 |
|----------|------|
| id | 상품 ID |

#### Response (`ProductDetailResponse`)

| 섹션 | 필드 | 설명 |
|------|------|------|
| 상품 정보 | id, productType, tradeStatus, petDetailType, category, productStatus, title, description, price, mainImageUrl, subImageUrls, addressSido, addressGugun, createdAt, viewCount, favoriteCount, isFavorite | 상세 정보 일체 |
| 판매자 정보 | sellerInfo { sellerId, sellerNickname, sellerProfileImageUrl } | 게시자 정보 |
| 판매자의 다른 상품 | sellerOtherProducts[] | 최대 5개, `ProductListItemResponse` 구조 |

---

### 3-4. 판매 상품 수정 (PATCH /api/products/{id})

- **인증 필요**: 예
- **권한**: 게시자 본인만 가능

Request Body는 [판매 상품 등록](#3-1-판매-상품-등록-post-apiproducts)과 동일 구조입니다.  
응답은 `ProductResponse`.

---

### 3-5. 거래 상태 변경 (PATCH /api/products/{id}/trade-status)

- **인증 필요**: 예
- **권한**: 게시자 본인만 가능

#### Request Body (`TradeStatusUpdateRequest`)

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| tradeStatus | `TradeStatus` | 예 | 판매 상품: `SELLING/RESERVED/COMPLETED`, 판매 요청: `BUYING/RESERVED/COMPLETED` |

응답은 `ProductResponse`.

---

### 3-6. 판매 상품삭제 (DELETE /api/products/{id})

- **인증 필요**: 예
- **권한**: 게시자 본인만 가능
- **동작**: 소프트 삭제 (`deletedAt` 설정). 응답 데이터는 없습니다 (`data: null`).

---

### 3-7. 관심(찜) 토글 (POST /api/products/{id}/favorite)

- **인증 필요**: 예
- **동작**: 
  - 이미 찜한 상태 → 찜 해제, `favoriteCount -1`
  - 찜하지 않은 상태 → 찜 추가, `favoriteCount +1`
- **응답**: `ProductResponse` (토글 후 상태, `isFavorite` 갱신)

---

### 3-8. 관심 목록 조회 (GET /api/products/favorites)

- **인증 필요**: 예
- **쿼리 파라미터**: `page`, `size`

`content` 항목 (`FavoriteItemResponse`) 필드:

| 필드 | 설명 |
|------|------|
| id | 상품 ID |
| mainImageUrl | 대표 이미지 |
| title | 제목 |
| price | 가격 |
| viewCount | 조회수 |
| tradeStatus | 거래 상태 |

---

## 판매 요청 APIs

판매 요청은 구매 희망(‘삽니다’) 게시글로, `ProductType.REQUEST`, `TradeStatus.BUYING`을 기본값으로 사용합니다.

### 4-1. 판매 요청 등록 (POST /api/products/requests)

- **인증 필요**: 예

#### Request Body (`ProductRequestCreateRequest`)

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| petType / petDetailType / category | Enum | 예 | 동물/카테고리 |
| title | String | 예 | 2~50자 |
| description | String | 옵션 | 최대 1000자 |
| desiredPrice | Long | 예 | 희망 가격 |
| mainImageUrl | String | 옵션 |
| subImageUrls | String[] | 옵션, 최대 4개 |
| addressSido / addressGugun | String | 예 | 거래 희망 지역 |

응답은 `ProductResponse` (productType=REQUEST, tradeStatus=BUYING).

---

### 4-2. 판매 요청 목록 조회 (GET /api/products/requests)

- **인증 필요**: 아니오  
- **쿼리 파라미터**: `page`, `size`
- 응답 구조: `ProductRequestListResponse`

`content` 항목 (`ProductRequestListItemResponse`) 필드:

| 필드 | 설명 |
|------|------|
| id | 게시글 ID |
| mainImageUrl | 대표 이미지 |
| petDetailType | 동물 소분류 |
| productStatus | 요청자가 원하는 상품 상태 |
| tradeStatus | `BUYING/RESERVED/COMPLETED` |
| title | 제목 |
| price | 희망 가격 |
| createdAt | 등록일 |
| favoriteCount | 찜 개수 |
| isFavorite | 로그인 사용자의 찜 여부 (현재 false 고정) |

---

### 4-3. 판매 요청 상세 조회 (GET /api/products/requests/{id})

- **인증 필요**: 아니오
- **조회수 증가**: 로그인 사용자가 게시자가 아닐 경우 자동 증가

응답 (`ProductRequestDetailResponse`):

| 필드 | 설명 |
|------|------|
| id, productType, tradeStatus | 게시글 정보 |
| petDetailType, category, title, description | 상품 정보 |
| desiredPrice | 희망 가격 |
| mainImageUrl, subImageUrls | 이미지 |
| addressSido, addressGugun | 지역 |
| createdAt, viewCount, favoriteCount | 메타 정보 |
| sellerInfo | 게시자 정보 |

---

### 4-4. 판매 요청 수정 (PATCH /api/products/requests/{id})

- **인증 필요**: 예
- **권한**: 게시자 본인 + `TradeStatus.BUYING` 상태에서만 수정 가능
- Request Body 구조: [판매 요청 등록](#4-1-판매-요청-등록-post-apiproductsrequests)과 동일 (`ProductRequestUpdateRequest`)
- 응답: `ProductResponse`

---

## 내가 등록한 상품 목록 (GET /api/products/me)

- **인증 필요**: 예
- **설명**: 로그인 사용자가 등록한 판매 상품/판매 요청 전체 목록(혼합)을 최신순으로 조회합니다.
- **쿼리 파라미터**: `page`, `size`
- **응답**: `MyProductListResponse`

`content` 항목 (`MyProductListItemResponse`):

| 필드 | 설명 |
|------|------|
| id | 게시글 ID |
| mainImageUrl | 대표 이미지 |
| title | 제목 |
| price | 가격 또는 희망 가격 |
| viewCount | 조회수 |
| tradeStatus | 거래 상태 |
| createdAt | 등록일 |

---

## FAQ 및 참고

1. **조회수는 언제 증가하나요?**  
   - 판매 상품/요청 상세 API 호출 시, 로그인 사용자가 게시자가 아닌 경우에만 서버가 자동으로 `increaseViewCount`를 호출합니다. 별도의 API 호출이 필요 없습니다.

2. **찜 여부(`isFavorite`)는 어디에 포함되나요?**  
   - 판매 상품 목록/상세, 관심 목록 응답에 포함됩니다. 비로그인 시 `false`.
   - 판매 요청 목록/상세에서는 현재 제외되어 있습니다(요구사항 반영).

3. **이미지 업로드**  
   - 이미지는 `/api/images` (별도 문서: `이미지_API.md`)를 통해 업로드한 뒤 반환받은 URL을 본 API 요청의 `mainImageUrl`, `subImageUrls`로 전달하십시오.

4. **소프트 삭제**  
   - DELETE API는 실제 행을 삭제하지 않고 `deletedAt`을 설정합니다. 삭제 후 조회 시 `ProductNotFoundException`이 발생합니다.

5. **거래 상태 제약**  
   - 판매 요청 수정은 `TradeStatus.BUYING`일 때만 가능하도록 서버가 검증합니다.

---

## 버전 기록

| 버전 | 날짜 | 변경 내용 |
|------|------|-----------|
| 1.0.0 | 2025-01-15 | 최초 작성 |


