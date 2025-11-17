# Product 기능 구현 할일

> 반려동물 용품 중고거래 서비스 - **Product** 영역 구현 작업 목록

---

## 개요

본 문서는 `03_Product.md` 요구사항 정의서를 기반으로 구현할 작업을 순차적으로 정리한 것입니다.
아키텍처 가이드(`architecture-guide.md`) 원칙을 준수하여 구현합니다.

---

## 구현 순서

### Step 1: 도메인 모델 생성

#### 1-1. Enum 타입 생성
- **작업 내용**:
  - `PetType`: 동물 종류 (파충류, 포유류 등)
  - `PetDetailType`: 반려동물 상세 종류
  - `Category`: 상품 카테고리
  - `ProductType`: 상품 의도 (판매중, 판매요청)
  - `TradeStatus`: 거래 상태 (판매중/예약중/거래완료, 삽니다/예약중/거래완료)
  - `ProductStatus`: 상품 상태 (새 상품, 거의 새것, 사용감 있음, 수리필요)
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/model/PetType.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/model/PetDetailType.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/model/Category.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/model/ProductType.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/model/TradeStatus.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/model/ProductStatus.java`

#### 1-2. Product 엔티티 생성
- **작업 내용**:
  - 판매 상품 정보를 저장하는 엔티티 생성
  - 필드:
    - id, sellerId (판매자 ID, User 참조)
    - productType (판매중/판매요청)
    - petType, petDetailType, category
    - title (상품명, 2-50자)
    - description (상품 설명, 최대 1000자)
    - price (가격)
    - productStatus (상품 상태)
    - tradeStatus (거래 상태)
    - mainImageUrl (대표 이미지 URL)
    - subImageUrls (서브 이미지 URL 리스트, 최대 4장)
    - addressSido, addressGugun (거래 희망 지역)
    - isDeliveryAvailable (택배 거래 가능 여부)
    - preferredMeetingPlace (선호하는 만남 장소)
    - viewCount (조회수, 기본값 0)
    - favoriteCount (찜 개수, 기본값 0)
    - createdAt, updatedAt, deletedAt
  - 소프트 삭제 지원 (deletedAt)
  - 인덱스: sellerId, productType, tradeStatus, createdAt
  - Product 엔티티에 비즈니스 메서드 추가:
    - `update()`: 상품 정보 수정
    - `updateTradeStatus()`: 거래 상태 변경
    - `increaseViewCount()`: 조회수 증가
    - `increaseFavoriteCount()`: 찜 개수 증가
    - `decreaseFavoriteCount()`: 찜 개수 감소
    - `softDelete()`: 소프트 삭제
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/model/Product.java`

#### 1-3. Favorite 엔티티 생성
- **작업 내용**:
  - 관심 목록(찜) 정보를 저장하는 엔티티 생성
  - 필드: id, userId (찜한 사용자 ID), productId (상품 ID), createdAt
  - userId와 productId의 복합 unique 제약조건 (중복 찜 방지)
  - 인덱스: userId, productId
  - **필요 이유**:
    - FR-019: "마이페이지에서 자신이 찜한 상품 목록을 조회" - 찜한 상품 목록을 조회하려면 Favorite 엔티티 필요
    - FR-011, FR-009: "찜 여부" 표시 - 특정 사용자가 특정 상품을 찜했는지 확인하려면 Favorite 엔티티 필요
    - Product의 favoriteCount는 전체 찜 개수만 저장하며, 개별 사용자의 찜 여부는 Favorite 엔티티로 관리
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/model/Favorite.java`

---

### Step 2: 도메인 레포지토리 인터페이스 생성

#### 2-1. ProductRepository 인터페이스 생성
- **작업 내용**:
  - `JpaRepository<Product, Long>` 상속
  - 판매자별 상품 목록 조회: `Page<Product> findBySellerIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long sellerId, Pageable pageable)`
  - 판매 상품 목록 조회 (판매중만): `Page<Product> findByProductTypeAndDeletedAtIsNullOrderByCreatedAtDesc(ProductType productType, Pageable pageable)`
  - 판매 요청 목록 조회: `Page<Product> findByProductTypeAndDeletedAtIsNullOrderByCreatedAtDesc(ProductType productType, Pageable pageable)`
  - 판매자별 상품 목록 조회 (판매 상품 + 판매 요청): `Page<Product> findBySellerIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long sellerId, Pageable pageable)`
  - 판매자별 다른 상품 목록 조회 (상세 페이지용): `List<Product> findBySellerIdAndIdNotAndDeletedAtIsNullOrderByCreatedAtDesc(Long sellerId, Long excludeProductId, Pageable pageable)`
  - 소프트 삭제된 상품 제외 조회: `Optional<Product> findByIdAndDeletedAtIsNull(Long id)`
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/repository/ProductRepository.java`

#### 2-2. FavoriteRepository 인터페이스 생성
- **작업 내용**:
  - `JpaRepository<Favorite, Long>` 상속
  - 찜 여부 확인: `boolean existsByUserIdAndProductId(Long userId, Long productId)`
  - 찜 삭제: `void deleteByUserIdAndProductId(Long userId, Long productId)`
  - 사용자별 찜 목록 조회: `Page<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable)`
  - 상품별 찜 개수 조회: `long countByProductId(Long productId)`
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/repository/FavoriteRepository.java`

---

### Step 3: 판매 상품 등록 구현 (FR-008)

#### 3-1. 판매 상품 등록 웹 DTO 및 앱 DTO 생성
- **작업 내용**:
  - 웹 DTO: `ProductCreateRequest` (petType, petDetailType, category, title, description, price, productStatus, mainImageUrl, subImageUrls, addressSido, addressGugun, isDeliveryAvailable, preferredMeetingPlace)
  - 검증 어노테이션 추가:
    - title: @NotBlank, @Size(min=2, max=50)
    - description: @Size(max=1000)
    - price: @NotNull, @Min(0)
    - mainImageUrl: @NotBlank
    - subImageUrls: @Size(max=4) (최대 4장)
    - addressSido, addressGugun: @NotBlank
  - 앱 DTO: `ProductCreateCommand`
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/product/dto/ProductCreateRequest.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/app/dto/ProductCreateCommand.java`

#### 3-2. 판매 상품 등록 앱 서비스 구현
- **작업 내용**:
  - `ProductService.createProduct()` 메서드 구현
  - 현재 로그인한 사용자 ID로 판매자 확인
  - Product 엔티티 생성 및 저장
  - ProductDto 반환
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/app/service/ProductService.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/app/service/ProductServiceImpl.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/app/dto/ProductDto.java`

#### 3-3. 판매 상품 등록 컨트롤러 구현
- **작업 내용**:
  - `POST /api/products` 엔드포인트
  - 인증 필수 (`@PreAuthorize("isAuthenticated()")`)
  - 현재 로그인한 사용자 정보 추출 (SecurityUtils 사용)
  - 웹 DTO → 앱 DTO 변환
  - 앱 서비스 호출
  - `SuccessResponse<ProductResponse>` 반환
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/product/controller/ProductController.java`
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/product/dto/ProductResponse.java`

#### 3-4. 이미지 업로드/조회 컨트롤러 구현
- **작업 내용**:
  - `POST /api/images` 엔드포인트 (이미지 업로드)
  - `GET /api/images/**` 엔드포인트 (이미지 조회)
  - 인증 필수 (업로드 시)
  - MultipartFile 리스트로 이미지 파일 받기 (최대 5장)
  - 이미지 유효성 검증 (파일 형식, 크기 제한: 한 장당 5MB, 총 25MB)
  - 로컬 파일 시스템에 저장 (컨트롤러에서 처리, 아키텍처 가이드 준수)
  - 파일명 중복 방지를 위해 UUID 기반 고유 파일명 사용
  - 저장 경로: `user/{userId}/{yyyy}/{MM}/{dd}/{uuid}.{ext}`
  - 업로드된 이미지 URL 리스트 반환 (첫 번째 이미지는 대표 이미지, 나머지는 서브 이미지)
  - 상품, 프로필 등 다양한 도메인에서 공통으로 사용 가능하도록 별도 컨트롤러로 분리
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/product/controller/ImageController.java` (이미지 업로드/조회 컨트롤러)
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/product/service/ImageUploadService.java` (로컬 파일 업로드 서비스)
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/product/dto/ImageUploadResponse.java` (이미지 업로드 응답 DTO)
  - `service/cmarket/src/main/resources/application.properties` (이미지 업로드 설정 추가)

---

### Step 4: 판매 상품 목록 조회 구현 (FR-009)

#### 4-1. 판매 상품 목록 조회 웹 DTO 및 앱 DTO 생성
- **작업 내용**:
  - 웹 DTO: `ProductListResponse` (상품 목록, 페이지네이션 정보)
  - 상품 정보: mainImageUrl, petDetailType, productStatus, tradeStatus, title, price, createdAt, favoriteCount, isFavorite
  - 앱 DTO: `ProductListDto` (동일한 필드)
  - 페이지네이션: PageResult 사용 (아키텍처 가이드 준수)
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/product/dto/ProductListResponse.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/app/dto/ProductListDto.java`

#### 4-2. 판매 상품 목록 조회 앱 서비스 구현
- **작업 내용**:
  - `ProductService.getProductList()` 메서드 구현
  - ProductType.SELL로 필터링하여 조회 (페이지네이션, 최신순 정렬)
  - 소프트 삭제된 상품 제외
  - 찜 여부 확인 (로그인한 사용자만, 비로그인 시 false)
  - ProductListDto 반환
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/app/service/ProductService.java` (메서드 추가)

#### 4-3. 판매 상품 목록 조회 컨트롤러 구현
- **작업 내용**:
  - `GET /api/products` 엔드포인트
  - 인증 선택 (로그인 여부에 따라 찜 여부 표시)
  - 페이지네이션 파라미터: page, size (기본값: page=0, size=20)
  - 현재 로그인한 사용자 정보 추출 (선택적)
  - 앱 서비스 호출
  - 앱 DTO → 웹 DTO 변환
  - `SuccessResponse<ProductListResponse>` 반환
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/product/controller/ProductController.java` (메서드 추가)

---

### Step 5: 판매 상품 상세 조회 구현 (FR-011)

#### 5-1. 판매 상품 상세 조회 웹 DTO 및 앱 DTO 생성
- **작업 내용**:
  - 웹 DTO: `ProductDetailResponse` (상품 상세 정보, 판매자 정보, 판매자의 다른 상품 목록)
  - 상품 정보: mainImageUrl, subImageUrls, productType, tradeStatus, petDetailType, category, productStatus, title, price, addressSido, addressGugun, createdAt, viewCount, favoriteCount, description, isFavorite
  - 판매자 정보: sellerId, sellerNickname, sellerProfileImageUrl
  - 판매자의 다른 상품 목록: ProductListResponse와 동일한 구조
  - 앱 DTO: `ProductDetailDto` (동일한 필드)
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/product/dto/ProductDetailResponse.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/app/dto/ProductDetailDto.java`

#### 5-2. 판매 상품 상세 조회 앱 서비스 구현
- **작업 내용**:
  - `ProductService.getProductDetail()` 메서드 구현
  - productId로 상품 조회 (소프트 삭제된 상품 제외)
  - 판매자 정보 조회 (User 엔티티)
  - 판매자의 다른 상품 목록 조회 (현재 상품 제외, 최대 5개)
  - 찜 여부 확인 (로그인한 사용자만)
  - ProductDetailDto 반환
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/app/service/ProductService.java` (메서드 추가)

#### 5-3. 판매 상품 상세 조회 컨트롤러 구현
- **작업 내용**:
  - `GET /api/products/{productId}` 엔드포인트
  - 인증 선택 (로그인 여부에 따라 찜 여부 표시)
  - 현재 로그인한 사용자 정보 추출 (선택적)
  - 앱 서비스 호출
  - 앱 DTO → 웹 DTO 변환
  - `SuccessResponse<ProductDetailResponse>` 반환
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/product/controller/ProductController.java` (메서드 추가)

---

### Step 6: 상품 조회수 증가 구현 (FR-031)

#### 6-1. 상품 조회수 증가 앱 서비스 구현
- **작업 내용**:
  - `ProductService.increaseViewCount()` 메서드 구현
  - 판매자 본인이 조회한 경우 조회수 증가하지 않음 (sellerId와 현재 사용자 ID 비교)
  - 판매자가 아닌 경우에만 Product 엔티티의 viewCount 증가
  - 중복 방지 로직 없음 (같은 사용자가 여러 번 조회해도 매번 증가)
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/app/service/ProductService.java` (메서드 추가)

#### 6-2. 상품 조회수 증가 컨트롤러 구현
- **작업 내용**:
  - `POST /api/products/{productId}/view` 엔드포인트
  - 인증 필수 (`@PreAuthorize("isAuthenticated()")`)
  - 현재 로그인한 사용자 정보 추출
  - 앱 서비스 호출
  - `SuccessResponse` 반환
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/product/controller/ProductController.java` (메서드 추가)

#### 6-3. 상세 조회 API에 조회수 증가 연동
- **작업 내용**:
  - 상세 조회 API 호출 시 조회수 증가 API도 함께 호출 (프론트엔드에서 처리하거나, 백엔드에서 비동기 처리)
  - 또는 상세 조회 서비스 내부에서 조회수 증가 처리 (트랜잭션 분리 고려)
  - 판매자 본인인 경우 조회수 증가하지 않음
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/product/controller/ProductController.java` (수정)

---

### Step 7: 판매 상품 수정 구현 (FR-012)

#### 7-1. 판매 상품 수정 웹 DTO 및 앱 DTO 생성
- **작업 내용**:
  - 웹 DTO: `ProductUpdateRequest` (petType, petDetailType, category, title, description, price, productStatus, mainImageUrl, subImageUrls, isDeliveryAvailable, addressSido, addressGugun, preferredMeetingPlace)
  - 검증 어노테이션 추가 (등록과 동일)
  - 앱 DTO: `ProductUpdateCommand`
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/product/dto/ProductUpdateRequest.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/app/dto/ProductUpdateCommand.java`

#### 7-2. 판매 상품 수정 앱 서비스 구현
- **작업 내용**:
  - `ProductService.updateProduct()` 메서드 구현
  - productId로 상품 조회 후 sellerId로 권한 확인 (product.getSellerId()와 비교)
  - Product 엔티티의 `update()` 메서드 호출
  - 업데이트된 Product 정보를 DTO로 변환하여 반환
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/app/service/ProductService.java` (메서드 추가)

#### 7-3. 판매 상품 수정 컨트롤러 구현
- **작업 내용**:
  - `PATCH /api/products/{productId}` 엔드포인트
  - 인증 필수 (`@PreAuthorize("isAuthenticated()")`)
  - 현재 로그인한 사용자 정보 추출
  - 웹 DTO → 앱 DTO 변환
  - 앱 서비스 호출
  - `SuccessResponse<ProductResponse>` 반환
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/product/controller/ProductController.java` (메서드 추가)

---

### Step 8: 거래 상태 변경 구현 (FR-013)

#### 8-1. 거래 상태 변경 웹 DTO 및 앱 DTO 생성
- **작업 내용**:
  - 웹 DTO: `TradeStatusUpdateRequest` (tradeStatus)
  - 검증: tradeStatus는 TradeStatus enum 값
  - 앱 DTO: `TradeStatusUpdateCommand`
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/product/dto/TradeStatusUpdateRequest.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/app/dto/TradeStatusUpdateCommand.java`

#### 8-2. 거래 상태 변경 앱 서비스 구현
- **작업 내용**:
  - `ProductService.updateTradeStatus()` 메서드 구현
  - productId로 상품 조회 후 sellerId로 권한 확인 (product.getSellerId()와 비교)
  - Product 엔티티의 `updateTradeStatus()` 메서드 호출
  - 업데이트된 Product 정보를 DTO로 변환하여 반환
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/app/service/ProductService.java` (메서드 추가)

#### 8-3. 거래 상태 변경 컨트롤러 구현
- **작업 내용**:
  - `PATCH /api/products/{productId}/trade-status` 엔드포인트
  - 인증 필수 (`@PreAuthorize("isAuthenticated()")`)
  - 현재 로그인한 사용자 정보 추출
  - 웹 DTO → 앱 DTO 변환
  - 앱 서비스 호출
  - `SuccessResponse<ProductResponse>` 반환
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/product/controller/ProductController.java` (메서드 추가)

---

### Step 9: 판매 상품/판매요청 삭제 구현 (FR-014)

#### 9-1. 판매 상품/판매요청 삭제 앱 서비스 구현
- **작업 내용**:
  - `ProductService.deleteProduct()` 메서드 구현
  - productId로 상품 조회 후 sellerId로 권한 확인 (product.getSellerId()와 비교)
  - Product 엔티티의 `softDelete()` 메서드 호출
  - 어드민 계정도 삭제 가능하도록 권한 확인 로직 추가 (향후 Admin 도메인 연동)
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/app/service/ProductService.java` (메서드 추가)

#### 9-2. 판매 상품/판매요청 삭제 컨트롤러 구현
- **작업 내용**:
  - `DELETE /api/products/{productId}` 엔드포인트
  - 인증 필수 (`@PreAuthorize("isAuthenticated()")`)
  - 현재 로그인한 사용자 정보 추출
  - 앱 서비스 호출
  - `SuccessResponse` 반환
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/product/controller/ProductController.java` (메서드 추가)

---

### Step 10: 관심 목록 추가/삭제 구현 (FR-018)

#### 10-1. 관심 목록 추가/삭제 앱 서비스 구현
- **작업 내용**:
  - `ProductService.toggleFavorite()` 메서드 구현
  - 현재 로그인한 사용자 ID와 productId로 찜 여부 확인
  - 이미 찜한 경우: Favorite 삭제, Product의 favoriteCount 감소
  - 찜하지 않은 경우: Favorite 생성, Product의 favoriteCount 증가
  - ProductDto 반환 (isFavorite 필드 포함)
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/app/service/ProductService.java` (메서드 추가)

#### 10-2. 관심 목록 추가/삭제 컨트롤러 구현
- **작업 내용**:
  - `POST /api/products/{productId}/favorite` 엔드포인트
  - 인증 필수 (`@PreAuthorize("isAuthenticated()")`)
  - 현재 로그인한 사용자 정보 추출
  - 앱 서비스 호출
  - `SuccessResponse<ProductResponse>` 반환 (isFavorite 필드 포함)
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/product/controller/ProductController.java` (메서드 추가)

---

### Step 11: 관심 목록 조회 구현 (FR-019)

#### 11-1. 관심 목록 조회 웹 DTO 및 앱 DTO 생성
- **작업 내용**:
  - 웹 DTO: `FavoriteListResponse` (찜한 상품 목록, 페이지네이션 정보)
  - 상품 정보: mainImageUrl, title, price, viewCount, tradeStatus
  - 앱 DTO: `FavoriteListDto` (동일한 필드)
  - 페이지네이션: PageResult 사용
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/product/dto/FavoriteListResponse.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/app/dto/FavoriteListDto.java`

#### 11-2. 관심 목록 조회 앱 서비스 구현
- **작업 내용**:
  - `ProductService.getFavoriteList()` 메서드 구현
  - 현재 로그인한 사용자 ID로 찜 목록 조회 (페이지네이션, 최신순 정렬)
  - 각 찜에 대한 상품 정보 조회
  - FavoriteListDto 반환
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/app/service/ProductService.java` (메서드 추가)

#### 11-3. 관심 목록 조회 컨트롤러 구현
- **작업 내용**:
  - `GET /api/products/favorites` 엔드포인트
  - 인증 필수 (`@PreAuthorize("isAuthenticated()")`)
  - 페이지네이션 파라미터: page, size (기본값: page=0, size=20)
  - 현재 로그인한 사용자 정보 추출
  - 앱 서비스 호출
  - 앱 DTO → 웹 DTO 변환
  - `SuccessResponse<FavoriteListResponse>` 반환
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/product/controller/ProductController.java` (메서드 추가)

---

### Step 12: 판매 요청 등록 구현 (FR-032)

#### 12-1. 판매 요청 등록 웹 DTO 및 앱 DTO 생성
- **작업 내용**:
  - 웹 DTO: `ProductRequestCreateRequest` (petType, petDetailType, category, title, description, desiredPrice, mainImageUrl, subImageUrls, addressSido, addressGugun)
  - 검증 어노테이션 추가 (판매 상품 등록과 유사)
  - desiredPrice: 희망 가격
  - 앱 DTO: `ProductRequestCreateCommand`
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/product/dto/ProductRequestCreateRequest.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/app/dto/ProductRequestCreateCommand.java`

#### 12-2. 판매 요청 등록 앱 서비스 구현
- **작업 내용**:
  - `ProductService.createProductRequest()` 메서드 구현
  - 현재 로그인한 사용자 ID로 게시자 확인
  - Product 엔티티 생성 (productType = ProductType.REQUEST)
  - ProductDto 반환
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/app/service/ProductService.java` (메서드 추가)

#### 12-3. 판매 요청 등록 컨트롤러 구현
- **작업 내용**:
  - `POST /api/products/requests` 엔드포인트
  - 인증 필수 (`@PreAuthorize("isAuthenticated()")`)
  - 현재 로그인한 사용자 정보 추출
  - 웹 DTO → 앱 DTO 변환
  - 앱 서비스 호출
  - `SuccessResponse<ProductResponse>` 반환
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/product/controller/ProductController.java` (메서드 추가)

---

### Step 13: 판매 요청 목록 조회 구현 (FR-033)

#### 13-1. 판매 요청 목록 조회 웹 DTO 및 앱 DTO 생성
- **작업 내용**:
  - 웹 DTO: `ProductRequestListResponse` (판매 요청 목록, 페이지네이션 정보)
  - 상품 정보: mainImageUrl, petDetailType, productStatus, tradeStatus, title, price, createdAt, favoriteCount, isFavorite
  - 앱 DTO: `ProductRequestListDto` (동일한 필드)
  - 페이지네이션: PageResult 사용
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/product/dto/ProductRequestListResponse.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/app/dto/ProductRequestListDto.java`

#### 13-2. 판매 요청 목록 조회 앱 서비스 구현
- **작업 내용**:
  - `ProductService.getProductRequestList()` 메서드 구현
  - ProductType.REQUEST로 필터링하여 조회 (페이지네이션, 최신순 정렬)
  - 소프트 삭제된 상품 제외
  - 찜 여부 확인 (로그인한 사용자만)
  - ProductRequestListDto 반환
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/app/service/ProductService.java` (메서드 추가)

#### 13-3. 판매 요청 목록 조회 컨트롤러 구현
- **작업 내용**:
  - `GET /api/products/requests` 엔드포인트
  - 인증 선택 (로그인 여부에 따라 찜 여부 표시)
  - 페이지네이션 파라미터: page, size (기본값: page=0, size=20)
  - 현재 로그인한 사용자 정보 추출 (선택적)
  - 앱 서비스 호출
  - 앱 DTO → 웹 DTO 변환
  - `SuccessResponse<ProductRequestListResponse>` 반환
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/product/controller/ProductController.java` (메서드 추가)

---

### Step 14: 판매 요청글 상세 조회 구현

#### 14-1. 판매 요청글 상세 조회 웹 DTO 및 앱 DTO 생성
- **작업 내용**:
  - 웹 DTO: `ProductRequestDetailResponse` (판매 요청 상세 정보, 게시자 정보, 게시자의 다른 상품 목록)
  - 상품 정보: mainImageUrl, subImageUrls, productType, tradeStatus, petDetailType, category, title, desiredPrice, addressSido, addressGugun, createdAt, viewCount, favoriteCount, description, isFavorite
  - 게시자 정보: sellerId, sellerNickname, sellerProfileImageUrl
  - 게시자의 다른 상품 목록: ProductListResponse와 동일한 구조
  - 앱 DTO: `ProductRequestDetailDto` (동일한 필드)
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/product/dto/ProductRequestDetailResponse.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/app/dto/ProductRequestDetailDto.java`

#### 14-2. 판매 요청글 상세 조회 앱 서비스 구현
- **작업 내용**:
  - `ProductService.getProductRequestDetail()` 메서드 구현
  - productId로 상품 조회 (소프트 삭제된 상품 제외, ProductType.REQUEST 확인)
  - 게시자 정보 조회 (User 엔티티)
  - 게시자의 다른 상품 목록 조회 (현재 상품 제외, 최대 5개)
  - 찜 여부 확인 (로그인한 사용자만)
  - ProductRequestDetailDto 반환
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/app/service/ProductService.java` (메서드 추가)

#### 14-3. 판매 요청글 상세 조회 컨트롤러 구현
- **작업 내용**:
  - `GET /api/products/requests/{productId}` 엔드포인트
  - 인증 선택 (로그인 여부에 따라 찜 여부 표시)
  - 현재 로그인한 사용자 정보 추출 (선택적)
  - 앱 서비스 호출
  - 앱 DTO → 웹 DTO 변환
  - `SuccessResponse<ProductRequestDetailResponse>` 반환
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/product/controller/ProductController.java` (메서드 추가)

---

### Step 15: 판매 요청글 수정 구현

#### 15-1. 판매 요청글 수정 웹 DTO 및 앱 DTO 생성
- **작업 내용**:
  - 웹 DTO: `ProductRequestUpdateRequest` (petType, petDetailType, category, title, description, desiredPrice, mainImageUrl, subImageUrls, addressSido, addressGugun)
  - 검증 어노테이션 추가 (판매 요청 등록과 동일)
  - 앱 DTO: `ProductRequestUpdateCommand`
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/product/dto/ProductRequestUpdateRequest.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/app/dto/ProductRequestUpdateCommand.java`

#### 15-2. 판매 요청글 수정 앱 서비스 구현
- **작업 내용**:
  - `ProductService.updateProductRequest()` 메서드 구현
  - productId와 sellerId로 상품 조회 및 권한 확인 (ProductType.REQUEST 확인)
  - Product 엔티티의 `update()` 메서드 호출
  - 업데이트된 Product 정보를 DTO로 변환하여 반환
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/app/service/ProductService.java` (메서드 추가)

#### 15-3. 판매 요청글 수정 컨트롤러 구현
- **작업 내용**:
  - `PATCH /api/products/requests/{productId}` 엔드포인트
  - 인증 필수 (`@PreAuthorize("isAuthenticated()")`)
  - 현재 로그인한 사용자 정보 추출
  - 웹 DTO → 앱 DTO 변환
  - 앱 서비스 호출
  - `SuccessResponse<ProductResponse>` 반환
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/product/controller/ProductController.java` (메서드 추가)

---

### Step 16: 내가 등록한 상품 목록 조회 구현 (FR-032)

#### 16-1. 내가 등록한 상품 목록 조회 웹 DTO 및 앱 DTO 생성
- **작업 내용**:
  - 웹 DTO: `MyProductListResponse` (등록한 상품 목록, 페이지네이션 정보)
  - 상품 정보: mainImageUrl, title, price, viewCount, tradeStatus, createdAt
  - 앱 DTO: `MyProductListDto` (동일한 필드)
  - 페이지네이션: PageResult 사용
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/product/dto/MyProductListResponse.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/app/dto/MyProductListDto.java`

#### 16-2. 내가 등록한 상품 목록 조회 앱 서비스 구현
- **작업 내용**:
  - `ProductService.getMyProductList()` 메서드 구현
  - 현재 로그인한 사용자 ID로 상품 목록 조회 (판매 상품 + 판매 요청 모두 포함, 페이지네이션, 최신순 정렬)
  - 소프트 삭제된 상품 제외
  - MyProductListDto 반환
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/app/service/ProductService.java` (메서드 추가)

#### 16-3. 내가 등록한 상품 목록 조회 컨트롤러 구현
- **작업 내용**:
  - `GET /api/products/me` 엔드포인트
  - 인증 필수 (`@PreAuthorize("isAuthenticated()")`)
  - 페이지네이션 파라미터: page, size (기본값: page=0, size=20)
  - 현재 로그인한 사용자 정보 추출
  - 앱 서비스 호출
  - 앱 DTO → 웹 DTO 변환
  - `SuccessResponse<MyProductListResponse>` 반환
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/product/controller/ProductController.java` (메서드 추가)

---

### Step 17: 커스텀 예외 클래스 생성

#### 17-1. Product 관련 예외 클래스 생성
- **작업 내용**:
  - `ProductNotFoundException` (상품 없음)
  - `ProductAccessDeniedException` (상품 접근 권한 없음 - 본인 상품만 수정/삭제 가능)
  - `ProductAlreadyDeletedException` (이미 삭제된 상품)
  - `FavoriteNotFoundException` (찜 정보 없음)
  - `InvalidProductTypeException` (잘못된 상품 타입)
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/app/exception/` 패키지 내 예외 클래스들

#### 17-2. GlobalExceptionHandler에 커스텀 예외 처리 추가
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

2. **상품 이미지**:
   - S3 업로드는 컨트롤러에서 처리 (아키텍처 가이드 준수)
   - 이미지 파일 형식 검증 (jpg, png, gif 등)
   - 이미지 크기 제한: 한 장당 5MB, 총 25MB
   - 대표 이미지 1장, 서브 이미지 최대 4장

3. **상품명 및 설명**:
   - 상품명: 최소 2자 ~ 최대 50자
   - 상품 설명: 최대 1000자

4. **거래 상태**:
   - 판매 상품: 판매중 → 예약중 → 거래완료
   - 판매 요청: 삽니다 → 예약중 → 거래완료
   - 거래 상태 변경은 즉시 적용 (별도 저장 버튼 없음)

5. **조회수 관리**:
   - 로그인한 사용자만 조회수 증가
   - 판매자 본인이 조회한 경우 조회수 증가하지 않음 (sellerId와 현재 사용자 ID 비교)
   - 중복 방지 로직 없음 (같은 사용자가 여러 번 조회해도 매번 증가)
   - 상품 상세 조회 시 Product의 viewCount만 증가

6. **찜 기능**:
   - 로그인한 사용자만 찜 가능
   - 중복 찜 방지 (Favorite 엔티티의 복합 unique 제약조건)
   - 찜 추가/삭제 시 Product의 favoriteCount 자동 업데이트
   - **엔티티 설계 이유**: 
     - Product의 favoriteCount는 전체 찜 개수만 저장
     - Favorite 엔티티는 개별 사용자의 찜 여부와 찜 목록 조회를 위해 필요 (FR-019: 찜 목록 조회, FR-011/FR-009: 찜 여부 표시)

7. **소프트 삭제**:
   - Product 엔티티는 deletedAt으로 소프트 삭제
   - 조회 시 deletedAt이 null인 것만 조회
   - 삭제 후 복구 불가능

8. **예외 처리**:
   - 모든 예외는 GlobalExceptionHandler에서 처리
   - traceId 포함하여 로깅
   - 사용자 친화적인 에러 메시지 반환

9. **인증/인가**:
   - 상품 등록/수정/삭제는 인증 필수
   - 본인 상품만 수정/삭제 가능
   - 목록 조회는 비로그인도 가능 (단, 찜 여부는 로그인한 사용자만 표시)

10. **페이지네이션**:
    - Spring Data `Page` 직노출 금지
    - `PageResult<T>` 전용 타입 사용 (아키텍처 가이드 준수)
    - 판매 상품 목록: 20개씩
    - 관심 목록: 20개씩
    - 내가 등록한 상품 목록: 20개씩

11. **지역 정보**:
    - addressSido (시/도), addressGugun (구/군) 필수 입력
    - 지역 정보는 필수 항목

12. **ProductType 구분**:
    - ProductType.SELL: 판매 상품 (팝니다)
    - ProductType.REQUEST: 판매 요청 (삽니다)
    - 동일한 Product 엔티티로 관리하되, productType으로 구분

13. **가격 필드**:
    - 판매 상품: price (판매 가격)
    - 판매 요청: desiredPrice (희망 가격)
    - Product 엔티티에서는 price 필드 하나로 관리 (판매 요청의 경우 desiredPrice를 price에 저장)

---

## 완료 체크리스트

- [x] Step 1: 도메인 모델 생성
- [x] Step 2: 도메인 레포지토리 인터페이스 생성
- [x] Step 3: 판매 상품 등록 구현 (FR-008) (3-4 이미지 업로드 완료 - 로컬 파일 시스템 사용)
- [x] Step 4: 판매 상품 목록 조회 구현 (FR-009)
- [x] Step 5: 판매 상품 상세 조회 구현 (FR-011)
- [x] Step 6: 상품 조회수 증가 구현 (FR-031)
- [x] Step 7: 판매 상품 수정 구현 (FR-012)
- [x] Step 8: 거래 상태 변경 구현 (FR-013)
- [x] Step 9: 판매 상품/판매요청 삭제 구현 (FR-014)
- [x] Step 10: 관심 목록 추가/삭제 구현 (FR-018)
- [x] Step 11: 관심 목록 조회 구현 (FR-019)
- [x] Step 12: 판매 요청 등록 구현 (FR-032)
- [x] Step 13: 판매 요청 목록 조회 구현 (FR-033)
- [x] Step 14: 판매 요청글 상세 조회 구현
- [x] Step 15: 판매 요청글 수정 구현
- [x] Step 16: 내가 등록한 상품 목록 조회 구현 (FR-032)
- [x] Step 17: 커스텀 예외 클래스 생성

---

## 참고사항

- 각 Step을 완료한 후 사용자 리뷰를 받고 다음 Step을 진행합니다.
- 아키텍처 가이드의 원칙을 반드시 준수합니다.
- 테스트는 각 Step 완료 후 작성합니다.
- 상품 이미지는 S3에 업로드하며, 업로드된 URL을 DB에 저장합니다.
- 조회수는 캐시 또는 DB 업데이트 방식으로 성능을 고려하여 결정합니다.
- 판매자의 다른 상품 목록은 상세 조회 시 함께 반환됩니다.

