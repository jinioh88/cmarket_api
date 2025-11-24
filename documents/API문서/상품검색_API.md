# 상품 검색 API 문서

> 반려동물 용품 중고거래 서비스 – **상품 통합 검색** API

본 문서는 상품 검색, 필터링, 정렬 기능을 통합한 API에 대한 상세 설명을 제공합니다.  
프론트엔드 개발자가 클라이언트 기능을 구현할 때 필요한 요청/응답 규격과 사용 예시를 모두 포함합니다.

---

## 목차

1. [공통 사항](#공통-사항)
2. [Enum 목록](#enum-목록)
3. [상품 통합 검색 API](#상품-통합-검색-api)
   - [3-1. 상품 통합 검색](#3-1-상품-통합-검색-get-apiproductssearch)
4. [검색 기능 상세](#검색-기능-상세)
   - [4-1. 키워드 검색](#4-1-키워드-검색)
   - [4-2. 필터링](#4-2-필터링)
   - [4-3. 정렬](#4-3-정렬)
5. [사용 예시](#사용-예시)
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

> `Authorization`은 선택 사항입니다.  
> 로그인한 사용자는 찜 여부(`isFavorite`)가 표시되며, 비로그인 사용자는 `isFavorite = false`로 반환됩니다.

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

---

## Enum 목록

| Enum | 값 | 설명 |
|------|----|----|
| `PetType` | `REPTILE`, `MAMMAL`, `BIRD`, `FISH`, `AMPHIBIAN`, `ETC` | 반려동물 대분류 |
| `PetDetailType` | `SNAKE`, `LIZARD`, `TURTLE`, `REPTILE_ETC`, `DOG`, `CAT`, `HAMSTER`, `RABBIT`, `GUINEA_PIG`, `MAMMAL_ETC`, `PARROT`, `CANARY`, `BIRD_ETC`, `GOLDFISH`, `TROPICAL_FISH`, `FISH_ETC`, `FROG`, `SALAMANDER`, `AMPHIBIAN_ETC`, `ETC` | 반려동물 상세 종류 |
| `Category` | `FOOD`, `TOY`, `HOUSE`, `CLOTHING`, `HEALTH`, `GROOMING`, `WALKING`, `ETC` | 상품 카테고리 |
| `ProductStatus` | `NEW`, `LIKE_NEW`, `USED`, `NEED_REPAIR` | 상품 상태 (새 상품, 거의 새것, 사용감 있음, 수리 필요) |
| `ProductType` | `SELL`, `REQUEST` | 상품 타입 (판매, 판매요청) |
| `TradeStatus` | `SELLING`, `BUYING`, `RESERVED`, `COMPLETED` | 거래 상태<br/>※ 판매 상품은 `SELLING/RESERVED/COMPLETED`, 판매 요청은 `BUYING/RESERVED/COMPLETED`를 사용합니다. |

---

## 상품 통합 검색 API

### 3-1. 상품 통합 검색 (GET /api/products/search)

- **인증 필요**: 아니오 (로그인 시 찜 여부 포함)
- **설명**: 상품을 검색하고 필터링 및 정렬할 수 있는 통합 API입니다. 모든 파라미터는 선택적이며, 조건이 없으면 전체 상품을 대상으로 합니다.

#### Request (Query Parameters)

| 파라미터 | 타입 | 필수 | 기본값 | 설명                                                                                                                            |
|---------|------|------|--------|-------------------------------------------------------------------------------------------------------------------------------|
| `keyword` | String | 아니오 | - | 검색어 (제목, 설명, 카테고리명 검색)<br/>공백으로 구분된 여러 키워드는 AND 조건으로 처리됩니다.                                                                   |
| `productType` | `ProductType` | 아니오 | - | 상품 타입 필터<br/>- `null` 또는 미지정: 전체 상품 (판매 + 판매요청)<br/>- `SELL`: 판매 상품만<br/>- `REQUEST`: 판매 요청만<br/>※ 화면의 탭 기능 (전체 상품, 판매, 판매요청) |
| `petType` | `PetType` | 아니오 | - | 반려동물 대분류 필터                                                                                                                   |
| `petDetailType` | `PetDetailType` | 아니오 | - | 반려동물 상세 종류 필터                                                                                                                 |
| `categories` | `Category[]` | 아니오 | - | 상품 카테고리 필터 (여러 개 선택 가능)<br/>예: `categories=FOOD&categories=TOY`                                                               |
| `productStatuses` | `ProductStatus[]` | 아니오 | - | 상품 상태 필터 (여러 개 선택 가능)<br/>예: `productStatuses=NEW&productStatuses=LIKE_NEW`                                                   |
| `minPrice` | Long | 아니오 | - | 최소 가격 (0 이상)                                                                                                                  |
| `maxPrice` | Long | 아니오 | - | 최대 가격 (0 이상)                                                                                                                  |
| `addressSido` | String | 아니오 | - | 시/도 필터 (예: "서울특별시", "경기광역시", "인천광역시")                                                                                         |
| `addressGugun` | String | 아니오 | - | 시/군/구 필터 (예: "강남구", "수원시")                                                                                                    |
| `sortBy` | String | 아니오 | `"createdAt"` | 정렬 기준<br/>- `"createdAt"`: 최신순<br/>- `"price"`: 가격순<br/>- `"favoriteCount"`: 찜 많은 순                                           |
| `sortOrder` | String | 아니오 | `"desc"` | 정렬 방향<br/>- `"asc"`: 오름차순<br/>- `"desc"`: 내림차순                                                                                |
| `page` | Integer | 아니오 | `0` | 페이지 번호 (0부터 시작)                                                                                                               |
| `size` | Integer | 아니오 | `20` | 페이지 크기 (1 이상)                                                                                                                 |

#### Response (`ProductSearchResponse`)

| 필드 | 타입 | 설명 |
|------|------|------|
| `page` | Integer | 현재 페이지 번호 |
| `size` | Integer | 페이지 크기 |
| `total` | Long | 전체 상품 개수 |
| `content` | `ProductSearchItemResponse[]` | 상품 목록 (배열) |
| `totalPages` | Integer | 전체 페이지 수 |
| `hasNext` | Boolean | 다음 페이지 존재 여부 |
| `hasPrevious` | Boolean | 이전 페이지 존재 여부 |
| `totalElements` | Long | 전체 요소 개수 (total과 동일) |
| `numberOfElements` | Long | 현재 페이지의 요소 개수 |

#### `content` 항목 (`ProductSearchItemResponse`)

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | Long | 상품 ID |
| `mainImageUrl` | String | 대표 이미지 URL |
| `petDetailType` | `PetDetailType` | 반려동물 상세 종류 |
| `productStatus` | `ProductStatus` | 상품 상태 |
| `productType` | `ProductType` | 상품 타입 (`SELL`: 판매, `REQUEST`: 판매요청) |
| `tradeStatus` | `TradeStatus` | 거래 상태 |
| `title` | String | 상품명 |
| `price` | Long | 가격 (판매 상품: 판매 가격, 판매 요청: 희망 가격) |
| `createdAt` | String (ISO 8601) | 등록일시 (예: "2025-01-15T10:30:00") |
| `favoriteCount` | Long | 찜 개수 |
| `isFavorite` | Boolean | 로그인 사용자의 찜 여부 (비로그인 시 `false`) |

#### 예제 요청

**1. 전체 상품 조회 (최신순)**
```
GET /api/products/search?page=0&size=20
```

**2. 키워드 검색**
```
GET /api/products/search?keyword=강아지 장난감&page=0&size=20
```

**3. 판매 상품만 조회 (강아지용, 카테고리: 장난감, 가격: 1만원 이상)**
```
GET /api/products/search?productType=SELL&petDetailType=DOG&categories=TOY&minPrice=10000&page=0&size=20
```

**4. 여러 카테고리 선택 (사료/간식 또는 장난감)**
```
GET /api/products/search?categories=FOOD&categories=TOY&page=0&size=20
```

**5. 여러 상품 상태 선택 (새 상품 또는 거의 새것)**
```
GET /api/products/search?productStatuses=NEW&productStatuses=LIKE_NEW&page=0&size=20
```

**6. 가격대 필터 (1만원 이상 5만원 이하)**
```
GET /api/products/search?minPrice=10000&maxPrice=50000&page=0&size=20
```

**7. 지역 필터 (서울특별시 강남구)**
```
GET /api/products/search?addressSido=서울특별시&addressGugun=강남구&page=0&size=20
```

**8. 정렬 (가격 낮은 순)**
```
GET /api/products/search?sortBy=price&sortOrder=asc&page=0&size=20
```

**9. 복합 조건 (키워드 + 필터 + 정렬)**
```
GET /api/products/search?keyword=강아지&productType=SELL&petDetailType=DOG&categories=TOY&minPrice=10000&sortBy=price&sortOrder=asc&page=0&size=20
```

#### 예제 응답

```json
{
  "code": {
    "code": 200,
    "message": "성공"
  },
  "message": "성공",
  "data": {
    "page": 0,
    "size": 20,
    "total": 150,
    "content": [
      {
        "id": 1,
        "mainImageUrl": "https://example.com/images/product1.jpg",
        "petDetailType": "DOG",
        "productStatus": "NEW",
        "productType": "SELL",
        "tradeStatus": "SELLING",
        "title": "강아지 장난감 공",
        "price": 15000,
        "createdAt": "2025-01-15T10:30:00",
        "favoriteCount": 5,
        "isFavorite": true
      },
      {
        "id": 2,
        "mainImageUrl": "https://example.com/images/product2.jpg",
        "petDetailType": "CAT",
        "productStatus": "LIKE_NEW",
        "productType": "SELL",
        "tradeStatus": "SELLING",
        "title": "고양이 사료",
        "price": 25000,
        "createdAt": "2025-01-14T15:20:00",
        "favoriteCount": 3,
        "isFavorite": false
      }
    ],
    "totalPages": 8,
    "hasNext": true,
    "hasPrevious": false,
    "totalElements": 150,
    "numberOfElements": 20
  }
}
```

---

## 검색 기능 상세

### 4-1. 키워드 검색

#### 검색 대상 필드
- **제목** (`title`): 상품명에서 검색
- **설명** (`description`): 상품 설명에서 검색
- **카테고리명** (`category`): 카테고리 Enum 값을 문자열로 변환하여 검색

#### 검색 방식
- **부분 일치 검색**: 키워드가 포함된 상품을 검색합니다 (LIKE 쿼리)
- **대소문자 구분 없음**: 대소문자를 구분하지 않습니다
- **특수문자/이모지 제거**: 검색 시 불필요한 특수문자와 이모지는 자동으로 제거됩니다

#### 다중 키워드 처리
- **AND 조건**: 공백으로 구분된 여러 키워드는 모두 포함된 상품만 검색됩니다
  - 예: `keyword=강아지 장난감` → 제목, 설명, 카테고리명 중 "강아지"와 "장난감"이 모두 포함된 상품만 검색

#### 검색 우선순위 정렬
키워드 검색 시 결과는 다음 우선순위로 정렬됩니다:
1. **제목 일치** (가장 높은 우선순위)
2. **설명 일치**
3. **카테고리 일치** (가장 낮은 우선순위)

> 단, `sortBy` 파라미터를 지정하면 검색 우선순위 정렬은 적용되지 않고 지정한 정렬 기준으로 정렬됩니다.

### 4-2. 필터링

#### 상품 타입 필터 (`productType`)
화면의 탭 기능과 연동됩니다:
- **전체 상품**: `productType` 파라미터를 생략하거나 `null`로 전달 → 판매 상품(`SELL`)과 판매 요청(`REQUEST`) 모두 조회
- **판매**: `productType=SELL` → 판매 상품만 조회
- **판매요청**: `productType=REQUEST` → 판매 요청만 조회

#### 반려동물 필터
- **대분류** (`petType`): 파충류, 포유류, 조류, 어류, 양서류, 기타
- **상세 종류** (`petDetailType`): 강아지, 고양이, 토끼, 햄스터, 뱀, 도마뱀 등
- 두 필터는 독립적으로 또는 함께 사용할 수 있습니다

#### 상품 카테고리 필터 (`categories`)
- **여러 개 선택 가능**: 배열 형태로 여러 카테고리를 전달하면 OR 조건으로 처리됩니다
  - 예: `categories=FOOD&categories=TOY` → 사료/간식 또는 장난감 카테고리 상품 조회
- **카테고리 Enum 값**:
  - `FOOD`: 사료/간식
  - `TOY`: 장난감
  - `HOUSE`: 하우스/케이지
  - `CLOTHING`: 의류/악세서리
  - `HEALTH`: 건강/의료용품
  - `GROOMING`: 미용/목욕용품
  - `WALKING`: 산책용품
  - `ETC`: 기타

#### 상품 상태 필터 (`productStatuses`)
- **여러 개 선택 가능**: 배열 형태로 여러 상태를 전달하면 OR 조건으로 처리됩니다
  - 예: `productStatuses=NEW&productStatuses=LIKE_NEW` → 새 상품 또는 거의 새것 상태 상품 조회
- **상태 Enum 값**:
  - `NEW`: 새 상품
  - `LIKE_NEW`: 거의 새것
  - `USED`: 사용감 있음
  - `NEED_REPAIR`: 수리 필요

#### 가격대 필터
- **최소 가격** (`minPrice`): 지정한 가격 이상인 상품만 조회
- **최대 가격** (`maxPrice`): 지정한 가격 이하인 상품만 조회
- **가격 범위**: `minPrice`와 `maxPrice`를 함께 사용하여 범위 검색 가능
- **가격대 예시**:
  - 1만원 이하: `maxPrice=10000`
  - 1만원 이상: `minPrice=10000`
  - 5만원 이상: `minPrice=50000`
  - 10만원 이상: `minPrice=100000`
  - 1만원 이상 5만원 이하: `minPrice=10000&maxPrice=50000`

#### 지역 필터
- **시/도** (`addressSido`): 서울특별시, 경기광역시, 인천광역시, 부산광역시, 대구, 광주, 대전, 울산 등
- **시/군/구** (`addressGugun`): 강남구, 수원시 등
- 두 필터는 독립적으로 또는 함께 사용할 수 있습니다
- `addressSido`만 지정하면 해당 시/도의 모든 구/군 상품이 조회됩니다

#### 필터 조합
모든 필터는 독립적으로 또는 조합하여 사용할 수 있습니다.  
여러 필터를 함께 사용하면 **AND 조건**으로 처리됩니다.

예: `productType=SELL&petDetailType=DOG&categories=TOY&minPrice=10000`
→ 판매 상품이면서, 강아지용이면서, 장난감 카테고리이면서, 1만원 이상인 상품만 조회

### 4-3. 정렬

#### 정렬 기준 (`sortBy`)
- **`"createdAt"`** (기본값): 등록일시 기준 정렬
- **`"price"`**: 가격 기준 정렬
- **`"favoriteCount"`**: 찜 개수 기준 정렬

#### 정렬 방향 (`sortOrder`)
- **`"desc"`** (기본값): 내림차순 (큰 값 → 작은 값)
- **`"asc"`**: 오름차순 (작은 값 → 큰 값)

#### 정렬 예시
- **최신순**: `sortBy=createdAt&sortOrder=desc` (또는 파라미터 생략)
- **가격 낮은 순**: `sortBy=price&sortOrder=asc`
- **가격 높은 순**: `sortBy=price&sortOrder=desc`
- **찜 많은 순**: `sortBy=favoriteCount&sortOrder=desc`

#### 정렬 주의사항
- `sortBy`에 허용되지 않은 값(화이트리스트 외)을 전달하면 `InvalidSortCriteriaException`이 발생합니다.
- 허용된 값: `"createdAt"`, `"price"`, `"favoriteCount"`만 가능합니다.

---

## 사용 예시

### 시나리오 1: 홈 화면 - 전체 상품 최신순 조회

```http
GET /api/products/search?page=0&size=20
```

**설명**: 모든 필터 없이 전체 상품을 최신순으로 조회합니다.

---

### 시나리오 2: 검색창 - 키워드 검색

```http
GET /api/products/search?keyword=강아지 장난감&page=0&size=20
```

**설명**: "강아지"와 "장난감"이 모두 포함된 상품을 검색합니다.

---

### 시나리오 3: 탭 전환 - 판매 상품만 조회

```http
GET /api/products/search?productType=SELL&page=0&size=20
```

**설명**: 판매 상품 탭을 클릭했을 때 사용합니다.

---

### 시나리오 4: 필터 적용 - 강아지용 장난감, 새 상품, 1만원 이상

```http
GET /api/products/search?productType=SELL&petDetailType=DOG&categories=TOY&productStatuses=NEW&minPrice=10000&page=0&size=20
```

**설명**: 여러 필터를 조합하여 원하는 상품만 조회합니다.

---

### 시나리오 5: 정렬 변경 - 가격 낮은 순

```http
GET /api/products/search?sortBy=price&sortOrder=asc&page=0&size=20
```

**설명**: 드롭다운에서 "가격 낮은 순"을 선택했을 때 사용합니다.

---

### 시나리오 6: 페이지네이션 - 다음 페이지 조회

```http
GET /api/products/search?page=1&size=20
```

**설명**: 첫 번째 페이지(0) 다음 페이지를 조회합니다.

---

## FAQ 및 참고

### 1. **검색 키워드는 어떻게 처리되나요?**
- 공백으로 구분된 여러 키워드는 AND 조건으로 처리됩니다.
- 예: `keyword=강아지 장난감` → "강아지"와 "장난감"이 모두 포함된 상품만 검색
- 특수문자와 이모지는 자동으로 제거됩니다.

### 2. **필터를 여러 개 선택하면 어떻게 되나요?**
- 모든 필터는 AND 조건으로 처리됩니다.
- 예: `petDetailType=DOG&categories=TOY` → 강아지용이면서 장난감 카테고리인 상품만 조회
- 단, `categories`와 `productStatuses`는 배열로 여러 값을 전달하면 OR 조건으로 처리됩니다.

### 3. **카테고리나 상품 상태를 여러 개 선택하려면?**
- 쿼리 파라미터를 여러 번 반복하여 전달합니다.
- 예: `categories=FOOD&categories=TOY` → 사료/간식 또는 장난감 카테고리 상품 조회
- 예: `productStatuses=NEW&productStatuses=LIKE_NEW` → 새 상품 또는 거의 새것 상태 상품 조회

### 4. **찜 여부(`isFavorite`)는 언제 표시되나요?**
- 로그인한 사용자만 `isFavorite` 값이 실제 찜 여부로 표시됩니다.
- 비로그인 사용자는 항상 `isFavorite = false`로 반환됩니다.
- 로그인 시 `Authorization` 헤더에 `Bearer <Access Token>`을 포함해야 합니다.

### 5. **정렬 기준에 허용되지 않은 값을 전달하면?**
- `InvalidSortCriteriaException`이 발생하며, HTTP 400 Bad Request가 반환됩니다.
- 허용된 값: `"createdAt"`, `"price"`, `"favoriteCount"`만 가능합니다.

### 6. **가격 필터에서 `minPrice`와 `maxPrice`를 함께 사용할 수 있나요?**
- 네, 가능합니다. 범위 검색이 가능합니다.
- 예: `minPrice=10000&maxPrice=50000` → 1만원 이상 5만원 이하인 상품 조회

### 7. **지역 필터는 어떻게 사용하나요?**
- `addressSido`만 지정하면 해당 시/도의 모든 구/군 상품이 조회됩니다.
- `addressSido`와 `addressGugun`을 함께 지정하면 더 구체적인 지역 필터링이 가능합니다.
- 예: `addressSido=서울&addressGugun=강남구` → 서울특별시 강남구 상품만 조회

### 8. **페이지네이션은 어떻게 동작하나요?**
- `page`는 0부터 시작합니다 (첫 페이지 = 0).
- `size`는 1 이상의 값을 지정해야 합니다.
- 기본값: `page=0`, `size=20`
- 응답의 `hasNext`와 `hasPrevious`로 다음/이전 페이지 존재 여부를 확인할 수 있습니다.

### 9. **검색 결과가 없으면?**
- `content` 배열이 빈 배열(`[]`)로 반환됩니다.
- `total`은 0이 되며, `totalPages`는 0이 됩니다.

### 10. **소프트 삭제된 상품은 조회되나요?**
- 아니오, 소프트 삭제된 상품(`deletedAt`이 null이 아닌 상품)은 검색 결과에 포함되지 않습니다.

### 11. **검색 우선순위 정렬은 언제 적용되나요?**
- `sortBy` 파라미터를 지정하지 않았을 때만 적용됩니다.
- `sortBy`를 지정하면 검색 우선순위 정렬은 무시되고 지정한 정렬 기준으로 정렬됩니다.

### 12. **성능 최적화 팁**
- 페이지네이션을 적절히 사용하여 한 번에 너무 많은 데이터를 조회하지 않도록 합니다.
- 필터를 조합하여 불필요한 데이터를 미리 제외하는 것이 효율적입니다.
- 인덱스가 적용된 필드(`title`, `description`, `category`, `petType`, `price`, `createdAt`, `favoriteCount`)를 활용한 필터링을 권장합니다.

---

## 버전 기록

| 버전 | 날짜 | 변경 내용 |
|------|------|-----------|
| 1.0.0 | 2025-01-15 | 최초 작성 |


