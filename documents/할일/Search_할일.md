# Search 기능 구현 할일

> 반려동물 용품 중고거래 서비스 - **Search** 영역 구현 작업 목록

---

## 개요

본 문서는 `04_Search.md` 요구사항 정의서를 기반으로 구현할 작업을 순차적으로 정리한 것입니다.
아키텍처 가이드(`architecture-guide.md`) 원칙을 준수하여 구현합니다.

---

## 구현 순서

### Step 1: 검색용 Repository 메서드 추가

#### 1-1. ProductRepository에 검색 메서드 추가
- **작업 내용**:
  - 통합 검색을 위한 복잡한 쿼리 메서드 추가
  - QueryDSL 또는 `@Query` 어노테이션 사용
  - 검색 조건:
    - 키워드: 제목(title), 설명(description), 카테고리명(category) 부분 포함 검색 (대소문자 구분 없음)
    - 다중 키워드: AND 조건 (모든 키워드가 포함된 상품만 조회)
    - 우선순위 정렬: 제목 일치 > 설명 일치 > 카테고리 일치
  - 필터 조건:
    - 상품 타입 (productType): 전체/판매(SELL)/판매요청(REQUEST) - 화면의 탭 기능 (전체 상품, 판매, 판매요청)
    - 반려동물 대분류 (petType): 전체/포유류/조류/파충류/수생동물/곤충,절지동물
    - 반려동물 상세 종류 (petDetailType): 강아지, 고양이, 토끼 등
    - 상품 카테고리 (category): 사료/간식(FOOD), 장난감(TOY), 하우스/케이지(HOUSE), 의류/악세서리(CLOTHING), 건강/의료용품(HEALTH), 미용/목욕용품(GROOMING), 산책용품(WALKING), 기타(ETC)
    - 상품 상태 (productStatus): 새 상품/거의 새것/사용감 있음/수리 필요
    - 가격대: 1만원 이하/1만원 이상/5만원 이상/10만원 이상
    - 지역: 시/도 (addressSido), 시/군/구 (addressGugun)
  - 정렬 조건:
    - 최신순 (기본): createdAt DESC
    - 낮은 가격순: price ASC
    - 높은 가격순: price DESC
    - 찜 많은 순: favoriteCount DESC
  - 소프트 삭제된 상품 제외 (deletedAt IS NULL)
- **출력물**:
  <!-- - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/repository/ProductRepository.java` (메서드 추가) -->
  - QueryDSL 사용 `ProductRepositoryCustom` 인터페이스 및 구현체 생성

---

### Step 2: 검색 + 필터링 + 정렬 통합 API 구현 (FR-015, FR-016, FR-028)

#### 2-1. 통합 검색 웹 DTO 생성
- **작업 내용**:
  - 웹 DTO: `ProductSearchRequest` (keyword, productType, petType, petDetailType, category, productStatus, minPrice, maxPrice, addressSido, addressGugun, sortBy, sortOrder, page, size)
    - 검색, 필터링, 정렬 모든 조건을 포함하는 통합 요청 DTO
    - keyword: 검색어 (String, 선택적) - 제목, 설명, 카테고리명 검색
    - productType: 상품 타입 - 전체(null)/판매(SELL)/판매요청(REQUEST) (선택적, 화면의 탭 기능)
    - petType: 반려동물 대분류 (선택적)
    - petDetailType: 반려동물 상세 종류 (선택적)
    - category: 상품 카테고리 (선택적)
    - productStatus: 상품 상태 (선택적)
    - minPrice: 최소 가격 (선택적)
    - maxPrice: 최대 가격 (선택적)
    - addressSido: 시/도 (선택적)
    - addressGugun: 시/군/구 (선택적)
    - sortBy: 정렬 기준 - "createdAt" | "price" | "favoriteCount" (기본값: "createdAt")
    - sortOrder: 정렬 방향 - "asc" | "desc" (기본값: "desc")
    - page, size: 페이지네이션 파라미터 (기본값: page=0, size=20)
  - 웹 DTO: `ProductSearchResponse` (상품 목록, 페이지네이션 정보)
    - 상품 정보: mainImageUrl, petDetailType, productStatus, tradeStatus, title, price, createdAt, favoriteCount, isFavorite
  - 앱 DTO: `ProductSearchCommand` (동일한 필드)
  - 앱 DTO: `ProductSearchResultDto` (상품 목록, 페이지네이션 정보)
  - 페이지네이션: PageResult 사용 (아키텍처 가이드 준수)
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/search/dto/ProductSearchRequest.java`
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/search/dto/ProductSearchResponse.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/search/app/dto/ProductSearchCommand.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/search/app/dto/ProductSearchResultDto.java`

#### 2-2. 통합 검색 앱 서비스 구현
- **작업 내용**:
  - `SearchService.searchProducts()` 메서드 구현
  - 검색 + 필터링 + 정렬을 모두 지원하는 통합 메서드
  - **검색 기능 (FR-015)**:
    - 키워드 전처리:
      - 불필요한 특수문자 및 이모지 제거
      - 한글 정규화 (다중 키워드 AND 조건 처리)
      - 대소문자 구분 없음 처리
    - 검색 필드: 제목(title), 설명(description), 카테고리명(category.name)
    - 우선순위 정렬: 제목 일치 > 설명 일치 > 카테고리 일치 (키워드 검색 시)
    - 다중 키워드: 모든 키워드가 포함된 상품만 조회 (AND 조건)
  - **필터링 기능 (FR-016)**:
    - 상품 타입 (productType): 전체(null)/판매(SELL)/판매요청(REQUEST) - 화면의 탭 기능
    - 반려동물 대분류 (petType): 전체/포유류/조류/파충류/수생동물/곤충,절지동물
    - 반려동물 상세 종류 (petDetailType): 강아지, 고양이, 토끼, 햄스터, 기니피그, 페럿, 친칠라, 고슴도치, 잉꼬, 앵무새, 가나리아, 모란앵무, 도마뱀, 뱀, 거북이, 게코, 금붕어, 열대어, 체리새우, 사마귀, 딱정벌레, 거미
    - 상품 카테고리 (category): 사료/간식(FOOD), 장난감(TOY), 하우스/케이지(HOUSE), 의류/악세서리(CLOTHING), 건강/의료용품(HEALTH), 미용/목욕용품(GROOMING), 산책용품(WALKING), 기타(ETC)
    - 상품 상태 (productStatus): 새 상품/거의 새것/사용감 있음/수리 필요
    - 가격대: 1만원 이하/1만원 이상/5만원 이상/10만원 이상
    - 지역: 시/도 (addressSido), 시/군/구 (addressGugun)
    - 각 필터는 독립적으로 또는 조합하여 적용 가능
  - **정렬 기능 (FR-028)**:
    - 최신순 (기본): createdAt DESC
    - 낮은 가격순: price ASC
    - 높은 가격순: price DESC
    - 찜 많은 순: favoriteCount DESC
    - 정렬 기준 화이트리스트 검증 (인젝션 방지)
  - 소프트 삭제된 상품 제외 (deletedAt IS NULL)
  - 찜 여부 확인 (로그인한 사용자만, 비로그인 시 false)
  - N+1 문제 방지: 한 번의 쿼리로 찜한 상품 ID 목록 조회
  - ProductSearchResultDto 반환
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/search/app/service/SearchService.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/search/app/service/SearchServiceImpl.java`

#### 2-3. 통합 검색 컨트롤러 구현
- **작업 내용**:
  - `GET /api/products/search` 엔드포인트
  - 인증 선택 (로그인 여부에 따라 찜 여부 표시)
  - 쿼리 파라미터: keyword, productType, petType, petDetailType, category, productStatus, minPrice, maxPrice, addressSido, addressGugun, sortBy, sortOrder, page, size
    - 모든 파라미터는 선택적 (필터 조건이 없으면 전체 상품 대상)
  - 현재 로그인한 사용자 정보 추출 (선택적)
  - 웹 DTO → 앱 DTO 변환
  - 앱 서비스 호출
  - 앱 DTO → 웹 DTO 변환
  - `SuccessResponse<ProductSearchResponse>` 반환
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/search/controller/SearchController.java`

---

### Step 3: 커스텀 예외 클래스 생성

#### 3-1. Search 관련 예외 클래스 생성
- **작업 내용**:
  - `InvalidSearchKeywordException` (잘못된 검색어 형식)
  - `InvalidSortCriteriaException` (잘못된 정렬 기준)
  - `InvalidFilterCriteriaException` (잘못된 필터 조건)
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/search/app/exception/` 패키지 내 예외 클래스들

#### 3-2. GlobalExceptionHandler에 커스텀 예외 처리 추가
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

2. **검색 키워드 처리**:
   - 불필요한 특수문자 및 이모지 제거
   - 대소문자 구분 없음 처리
   - 다중 키워드: 한글 정규화 후 AND 조건으로 처리
   - 부분 포함 검색 (LIKE 쿼리 또는 Full-Text Search)

3. **검색 우선순위**:
   - 제목 일치 > 설명 일치 > 카테고리 일치 순으로 정렬
   - CASE WHEN 또는 ORDER BY 절에서 우선순위 처리

4. **필터링**:
   - 각 필터는 독립적으로 또는 조합하여 적용 가능
   - 필터 조건이 없으면 전체 상품 대상
   - 상품 타입 필터 (productType): 화면의 탭 기능 (전체 상품, 판매, 판매요청)
     - 전체: productType = null (판매 상품 + 판매 요청 모두)
     - 판매: productType = SELL
     - 판매요청: productType = REQUEST
   - 가격대 필터: minPrice, maxPrice로 범위 검색

5. **정렬**:
   - 정렬 기준 화이트리스트 검증 (인젝션 방지)
   - 기본 정렬: 최신순 (createdAt DESC)
   - 정렬 기준: createdAt, price, favoriteCount

6. **성능 최적화**:
   - 복잡한 검색 쿼리는 QueryDSL 또는 네이티브 쿼리 사용 고려
   - 인덱스 활용: title, description, category, petType, price, createdAt, favoriteCount
   - 페이지네이션 필수

7. **찜 여부 확인**:
   - 로그인한 사용자만 찜 여부 표시
   - 비로그인 시 isFavorite = false
   - N+1 문제 방지: 한 번의 쿼리로 찜한 상품 ID 목록 조회

8. **소프트 삭제**:
   - deletedAt이 null인 상품만 조회

9. **예외 처리**:
   - 모든 예외는 GlobalExceptionHandler에서 처리
   - traceId 포함하여 로깅
   - 사용자 친화적인 에러 메시지 반환

10. **인증/인가**:
    - 검색/필터링은 로그인 여부와 관계없이 이용 가능
    - 찜 여부는 로그인한 사용자만 표시

11. **페이지네이션**:
    - Spring Data `Page` 직노출 금지
    - `PageResult<T>` 전용 타입 사용 (아키텍처 가이드 준수)
    - 기본값: page=0, size=20

12. **지역 필터**:
    - addressSido (시/도): 서울, 경기, 인천, 부산, 대구, 광주, 대전, 울산 등
    - addressGugun (시/군/구): 선택적

13. **가격대 필터**:
    - 1만원 이하: maxPrice = 10000
    - 1만원 이상: minPrice = 10000
    - 5만원 이상: minPrice = 50000
    - 10만원 이상: minPrice = 100000

14. **Product 도메인 활용**:
    - Product 엔티티 및 ProductRepository 활용
    - Favorite 엔티티 및 FavoriteRepository 활용 (찜 여부 확인)
    - User 엔티티 및 UserRepository 활용 (필요 시)

---

## 참고사항

- 아키텍처 가이드의 원칙을 반드시 준수합니다.
- 테스트는 각 Step 완료 후 작성합니다.
- 복잡한 검색 쿼리는 QueryDSL 사용을 고려합니다.
- 향후 확장 기능: 자동완성, 인기 검색어, 해시태그 검색 기능은 별도로 구현 예정입니다.

