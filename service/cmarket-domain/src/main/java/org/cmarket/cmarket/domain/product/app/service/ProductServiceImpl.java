package org.cmarket.cmarket.domain.product.app.service;

import lombok.RequiredArgsConstructor;
import org.cmarket.cmarket.domain.auth.model.User;
import org.cmarket.cmarket.domain.auth.repository.UserRepository;
import org.cmarket.cmarket.domain.product.app.dto.FavoriteItemDto;
import org.cmarket.cmarket.domain.product.app.dto.FavoriteListDto;
import org.cmarket.cmarket.domain.product.app.dto.ProductCreateCommand;
import org.cmarket.cmarket.domain.product.app.dto.ProductDetailDto;
import org.cmarket.cmarket.domain.product.app.dto.ProductDto;
import org.cmarket.cmarket.domain.product.app.dto.ProductRequestCreateCommand;
import org.cmarket.cmarket.domain.product.app.dto.ProductRequestDetailDto;
import org.cmarket.cmarket.domain.product.app.dto.ProductRequestListDto;
import org.cmarket.cmarket.domain.product.app.dto.ProductRequestListItemDto;
import org.cmarket.cmarket.domain.product.app.dto.MyProductListDto;
import org.cmarket.cmarket.domain.product.app.dto.MyProductListItemDto;
import org.cmarket.cmarket.domain.product.app.dto.ProductRequestUpdateCommand;
import org.cmarket.cmarket.domain.product.app.dto.ProductUpdateCommand;
import org.cmarket.cmarket.domain.product.app.dto.TradeStatusUpdateCommand;
import org.cmarket.cmarket.domain.product.app.dto.ProductListDto;
import org.cmarket.cmarket.domain.product.app.dto.ProductListItemDto;
import org.cmarket.cmarket.domain.product.app.dto.SellerInfoDto;
import org.cmarket.cmarket.domain.product.model.Favorite;
import org.cmarket.cmarket.domain.product.model.Product;
import org.cmarket.cmarket.domain.product.model.ProductType;
import org.cmarket.cmarket.domain.product.model.TradeStatus;
import org.cmarket.cmarket.domain.product.repository.FavoriteRepository;
import org.cmarket.cmarket.domain.product.repository.ProductRepository;
import org.cmarket.cmarket.domain.profile.app.dto.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 상품 서비스 구현체
 * 
 * 상품 관련 비즈니스 로직을 구현합니다.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    
    @Override
    public ProductDto createProduct(String email, ProductCreateCommand command) {
        // 사용자 조회 (판매자 확인)
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new org.cmarket.cmarket.domain.auth.app.exception.UserNotFoundException("사용자를 찾을 수 없습니다."));
        
        Long sellerId = user.getId();
        // Product 엔티티 생성
        Product product = Product.builder()
                .sellerId(sellerId)
                .productType(ProductType.SELL)  // 판매 상품
                .petType(command.getPetType())
                .petDetailType(command.getPetDetailType())
                .category(command.getCategory())
                .title(command.getTitle())
                .description(command.getDescription())
                .price(command.getPrice())
                .productStatus(command.getProductStatus())
                .tradeStatus(TradeStatus.SELLING)  // 초기 상태: 판매중
                .mainImageUrl(command.getMainImageUrl())
                .subImageUrls(command.getSubImageUrls())
                .addressSido(command.getAddressSido())
                .addressGugun(command.getAddressGugun())
                .isDeliveryAvailable(command.getIsDeliveryAvailable())
                .preferredMeetingPlace(command.getPreferredMeetingPlace())
                .build();
        
        // 저장
        Product savedProduct = productRepository.save(product);
        
        // DTO로 변환하여 반환
        return ProductDto.fromEntity(savedProduct);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ProductListDto getProductList(Pageable pageable, String email) {
        // 판매 상품 목록 조회 (ProductType.SELL, 최신순 정렬)
        Page<Product> productPage = productRepository.findByProductTypeAndDeletedAtIsNullOrderByCreatedAtDesc(
                ProductType.SELL,
                pageable
        );
        
        // 현재 로그인한 사용자 ID 조회 (비로그인 시 null)
        final Long userId = email != null
                ? userRepository.findByEmailAndDeletedAtIsNull(email)
                        .map(User::getId)
                        .orElse(null)
                : null;
        
        // N+1 문제 방지: 한 번의 쿼리로 찜한 상품 ID 목록 조회
        final java.util.Set<Long> favoriteProductIds = userId != null && !productPage.getContent().isEmpty()
                ? new java.util.HashSet<>(favoriteRepository.findProductIdsByUserIdAndProductIdIn(
                        userId,
                        productPage.getContent().stream()
                                .map(Product::getId)
                                .toList()
                ))
                : java.util.Collections.emptySet();
        
        // 각 상품의 찜 여부 확인 및 DTO 변환 후 PageResult로 변환
        PageResult<ProductListItemDto> pageResult = PageResult.fromPage(
                productPage.map(product -> {
                    Boolean isFavorite = userId != null && favoriteProductIds.contains(product.getId());
                    return ProductListItemDto.fromEntity(product, isFavorite);
                })
        );
        
        return new ProductListDto(pageResult);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ProductDetailDto getProductDetail(Long productId, String email) {
        // 상품 조회 (소프트 삭제된 상품 제외)
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new org.cmarket.cmarket.domain.product.app.exception.ProductNotFoundException("상품을 찾을 수 없습니다."));
        
        // 판매자 정보 조회
        User seller = userRepository.findById(product.getSellerId())
                .orElseThrow(() -> new org.cmarket.cmarket.domain.auth.app.exception.UserNotFoundException("판매자를 찾을 수 없습니다."));
        
        SellerInfoDto sellerInfo = SellerInfoDto.builder()
                .sellerId(seller.getId())
                .sellerNickname(seller.getNickname())
                .sellerProfileImageUrl(seller.getProfileImageUrl())
                .build();
        
        // 판매자의 다른 상품 목록 조회 (현재 상품 제외, 최대 5개)
        Pageable pageable = PageRequest.of(0, 5);
        List<Product> sellerOtherProducts = productRepository.findBySellerIdAndIdNotAndDeletedAtIsNullOrderByCreatedAtDesc(
                product.getSellerId(),
                productId,
                pageable
        );
        
        // 현재 로그인한 사용자 ID 조회 (비로그인 시 null)
        final Long userId = email != null
                ? userRepository.findByEmailAndDeletedAtIsNull(email)
                        .map(User::getId)
                        .orElse(null)
                : null;
        
        // 찜 여부 확인
        Boolean isFavorite = null;
        if (userId != null) {
            isFavorite = favoriteRepository.existsByUserIdAndProductId(userId, productId);
        }
        
        // 판매자의 다른 상품 목록 DTO 변환 (찜 여부는 확인하지 않음 - 간단하게 false로 설정)
        List<ProductListItemDto> sellerOtherProductsDto = sellerOtherProducts.stream()
                .map(p -> ProductListItemDto.fromEntity(p, false))
                .toList();
        
        // 조회수 증가 처리 (로그인한 사용자이고 판매자가 아닌 경우)
        if (email != null && !product.getSellerId().equals(userId)) {
            // 별도 트랜잭션으로 조회수 증가 처리 (비동기 또는 별도 트랜잭션)
            increaseViewCountInSeparateTransaction(productId, email);
        }
        
        // ProductDetailDto 생성 및 반환
        return ProductDetailDto.builder()
                .id(product.getId())
                .productType(product.getProductType())
                .tradeStatus(product.getTradeStatus())
                .petDetailType(product.getPetDetailType())
                .category(product.getCategory())
                .productStatus(product.getProductStatus())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .mainImageUrl(product.getMainImageUrl())
                .subImageUrls(product.getSubImageUrls())
                .addressSido(product.getAddressSido())
                .addressGugun(product.getAddressGugun())
                .createdAt(product.getCreatedAt())
                .viewCount(product.getViewCount())
                .favoriteCount(product.getFavoriteCount())
                .isFavorite(isFavorite)
                .sellerInfo(sellerInfo)
                .sellerOtherProducts(sellerOtherProductsDto)
                .build();
    }
    
    /**
     * 별도 트랜잭션으로 조회수 증가 처리
     * 
     * 상세 조회와 트랜잭션을 분리하여 조회수 증가를 처리합니다.
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void increaseViewCountInSeparateTransaction(Long productId, String email) {
        increaseViewCount(productId, email);
    }
    
    @Override
    @Transactional
    public void increaseViewCount(Long productId, String email) {
        // 상품 조회
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new org.cmarket.cmarket.domain.product.app.exception.ProductNotFoundException("상품을 찾을 수 없습니다."));
        
        // 사용자 조회
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new org.cmarket.cmarket.domain.auth.app.exception.UserNotFoundException("사용자를 찾을 수 없습니다."));
        
        Long userId = user.getId();
        
        // 판매자 본인이 조회한 경우 조회수 증가하지 않음
        if (product.getSellerId().equals(userId)) {
            return;
        }
        
        // 조회수 증가
        product.increaseViewCount();
        productRepository.save(product);
    }
    
    @Override
    @Transactional
    public ProductDto updateProduct(Long productId, ProductUpdateCommand command, String email) {
        // 상품 조회 (소프트 삭제된 상품 제외)
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new org.cmarket.cmarket.domain.product.app.exception.ProductNotFoundException("상품을 찾을 수 없습니다."));
        
        // 사용자 조회
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new org.cmarket.cmarket.domain.auth.app.exception.UserNotFoundException("사용자를 찾을 수 없습니다."));
        
        Long userId = user.getId();
        
        // 권한 확인: 판매자 본인만 수정 가능
        if (!product.getSellerId().equals(userId)) {
            throw new org.cmarket.cmarket.domain.auth.app.exception.AuthenticationFailedException("상품을 수정할 권한이 없습니다.");
        }
        
        // 상품 정보 수정
        product.update(
                command.petType(),
                command.petDetailType(),
                command.category(),
                command.title(),
                command.description(),
                command.price(),
                command.productStatus(),
                command.mainImageUrl(),
                command.subImageUrls(),
                command.isDeliveryAvailable(),
                command.addressSido(),
                command.addressGugun(),
                command.preferredMeetingPlace()
        );
        
        // 저장
        Product updatedProduct = productRepository.save(product);
        
        // DTO로 변환하여 반환
        return ProductDto.fromEntity(updatedProduct);
    }
    
    @Override
    @Transactional
    public ProductDto updateTradeStatus(Long productId, TradeStatusUpdateCommand command, String email) {
        // 상품 조회 (소프트 삭제된 상품 제외)
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new org.cmarket.cmarket.domain.product.app.exception.ProductNotFoundException("상품을 찾을 수 없습니다."));
        
        // 사용자 조회
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new org.cmarket.cmarket.domain.auth.app.exception.UserNotFoundException("사용자를 찾을 수 없습니다."));
        
        Long userId = user.getId();
        
        // 권한 확인: 판매자 본인만 거래 상태 변경 가능
        if (!product.getSellerId().equals(userId)) {
            throw new org.cmarket.cmarket.domain.auth.app.exception.AuthenticationFailedException("거래 상태를 변경할 권한이 없습니다.");
        }
        
        // 거래 상태 변경
        product.updateTradeStatus(command.tradeStatus());
        
        // 저장
        Product updatedProduct = productRepository.save(product);
        
        // DTO로 변환하여 반환
        return ProductDto.fromEntity(updatedProduct);
    }
    
    @Override
    @Transactional
    public void deleteProduct(Long productId, String email) {
        // 상품 조회 (소프트 삭제된 상품 제외)
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new org.cmarket.cmarket.domain.product.app.exception.ProductNotFoundException("상품을 찾을 수 없습니다."));
        
        // 사용자 조회
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new org.cmarket.cmarket.domain.auth.app.exception.UserNotFoundException("사용자를 찾을 수 없습니다."));
        
        Long userId = user.getId();
        
        // 권한 확인: 판매자 본인만 삭제 가능
        // TODO: 향후 Admin 도메인 연동 시 어드민 계정도 삭제 가능하도록 권한 확인 로직 추가
        if (!product.getSellerId().equals(userId)) {
            throw new org.cmarket.cmarket.domain.auth.app.exception.AuthenticationFailedException("상품을 삭제할 권한이 없습니다.");
        }
        
        // 소프트 삭제 처리
        product.softDelete();
        productRepository.save(product);
    }
    
    @Override
    @Transactional
    public ProductDto toggleFavorite(Long productId, String email) {
        // 상품 조회 (소프트 삭제된 상품 제외)
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new org.cmarket.cmarket.domain.product.app.exception.ProductNotFoundException("상품을 찾을 수 없습니다."));
        
        // 사용자 조회
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new org.cmarket.cmarket.domain.auth.app.exception.UserNotFoundException("사용자를 찾을 수 없습니다."));
        
        Long userId = user.getId();
        
        // 찜 여부 확인
        boolean isFavorite = favoriteRepository.existsByUserIdAndProductId(userId, productId);
        
        if (isFavorite) {
            // 이미 찜한 경우: 찜 삭제, favoriteCount 감소
            favoriteRepository.deleteByUserIdAndProductId(userId, productId);
            product.decreaseFavoriteCount();
        } else {
            // 찜하지 않은 경우: 찜 생성, favoriteCount 증가
            Favorite favorite = Favorite.builder()
                    .userId(userId)
                    .productId(productId)
                    .build();
            favoriteRepository.save(favorite);
            product.increaseFavoriteCount();
        }
        
        // 저장
        Product updatedProduct = productRepository.save(product);
        
        // 찜 여부 반전 (토글 후 상태)
        boolean newIsFavorite = !isFavorite;
        
        // DTO로 변환하여 반환 (isFavorite 포함)
        return ProductDto.fromEntity(updatedProduct, newIsFavorite);
    }
    
    @Override
    @Transactional(readOnly = true)
    public FavoriteListDto getFavoriteList(Pageable pageable, String email) {
        // 사용자 조회
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new org.cmarket.cmarket.domain.auth.app.exception.UserNotFoundException("사용자를 찾을 수 없습니다."));
        
        Long userId = user.getId();
        
        // 찜 목록 조회 (페이지네이션, 최신순 정렬)
        Page<Favorite> favoritePage = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        // N+1 문제 방지: 찜한 상품 ID 목록 추출
        List<Long> productIds = favoritePage.getContent().stream()
                .map(Favorite::getProductId)
                .toList();
        
        // 상품 정보 일괄 조회 (소프트 삭제된 상품 제외)
        List<Product> products = productIds.isEmpty()
                ? java.util.Collections.emptyList()
                : productRepository.findAllById(productIds).stream()
                        .filter(product -> product.getDeletedAt() == null)
                        .toList();
        
        // productId를 키로 하는 Map 생성 (순서 유지)
        java.util.Map<Long, Product> productMap = products.stream()
                .collect(java.util.stream.Collectors.toMap(Product::getId, product -> product));
        
        // Favorite 순서대로 FavoriteItemDto 생성
        List<FavoriteItemDto> favoriteItems = favoritePage.getContent().stream()
                .map(favorite -> {
                    Product product = productMap.get(favorite.getProductId());
                    if (product == null) {
                        // 소프트 삭제된 상품은 제외
                        return null;
                    }
                    return FavoriteItemDto.builder()
                            .id(product.getId())
                            .mainImageUrl(product.getMainImageUrl())
                            .title(product.getTitle())
                            .price(product.getPrice())
                            .viewCount(product.getViewCount())
                            .tradeStatus(product.getTradeStatus())
                            .build();
                })
                .filter(java.util.Objects::nonNull)
                .toList();
        
        // PageResult 생성 (필터링된 결과에 맞춰 total 조정)
        PageResult<FavoriteItemDto> pageResult = new PageResult<>(
                favoritePage.getNumber(),
                favoritePage.getSize(),
                favoritePage.getTotalElements(),
                favoriteItems,
                favoritePage.getTotalPages(),
                favoritePage.hasNext(),
                favoritePage.hasPrevious(),
                favoritePage.getTotalElements(),
                favoriteItems.size()
        );
        
        return new FavoriteListDto(pageResult);
    }
    
    @Override
    @Transactional
    public ProductDto createProductRequest(String email, ProductRequestCreateCommand command) {
        // 사용자 조회 (게시자 확인)
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new org.cmarket.cmarket.domain.auth.app.exception.UserNotFoundException("사용자를 찾을 수 없습니다."));
        
        Long sellerId = user.getId();
        
        // Product 엔티티 생성 (판매 요청)
        Product product = Product.builder()
                .sellerId(sellerId)
                .productType(ProductType.REQUEST)  // 판매 요청
                .petType(command.getPetType())
                .petDetailType(command.getPetDetailType())
                .category(command.getCategory())
                .title(command.getTitle())
                .description(command.getDescription())
                .price(command.getDesiredPrice())  // 희망 가격
                .productStatus(org.cmarket.cmarket.domain.product.model.ProductStatus.NEW)  // 판매 요청은 기본값으로 NEW 설정
                .tradeStatus(TradeStatus.BUYING)  // 초기 상태: 삽니다
                .mainImageUrl(command.getMainImageUrl())
                .subImageUrls(command.getSubImageUrls())
                .addressSido(command.getAddressSido())
                .addressGugun(command.getAddressGugun())
                .isDeliveryAvailable(false)  // 판매 요청은 기본값으로 false
                .preferredMeetingPlace(null)  // 판매 요청은 선호 만남 장소 없음
                .build();
        
        // 저장
        Product savedProduct = productRepository.save(product);
        
        // DTO로 변환하여 반환
        return ProductDto.fromEntity(savedProduct);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ProductRequestListDto getProductRequestList(Pageable pageable, String email) {
        // 판매 요청 목록 조회 (ProductType.REQUEST, 최신순 정렬)
        Page<Product> productPage = productRepository.findByProductTypeAndDeletedAtIsNullOrderByCreatedAtDesc(
                ProductType.REQUEST,
                pageable
        );
        
        // 각 상품을 DTO로 변환 후 PageResult로 변환 (찜 여부 제외)
        PageResult<ProductRequestListItemDto> pageResult = PageResult.fromPage(
                productPage.map(product -> ProductRequestListItemDto.fromEntity(product, false))
        );
        
        return new ProductRequestListDto(pageResult);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ProductRequestDetailDto getProductRequestDetail(Long productId, String email) {
        // 상품 조회 (소프트 삭제된 상품 제외)
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new org.cmarket.cmarket.domain.product.app.exception.ProductNotFoundException("상품을 찾을 수 없습니다."));
        
        // 판매 요청인지 확인
        if (product.getProductType() != ProductType.REQUEST) {
            throw new org.cmarket.cmarket.domain.product.app.exception.ProductNotFoundException("판매 요청 게시글이 아닙니다.");
        }
        
        // 게시자 정보 조회
        User seller = userRepository.findById(product.getSellerId())
                .orElseThrow(() -> new org.cmarket.cmarket.domain.auth.app.exception.UserNotFoundException("게시자를 찾을 수 없습니다."));
        
        SellerInfoDto sellerInfo = SellerInfoDto.builder()
                .sellerId(seller.getId())
                .sellerNickname(seller.getNickname())
                .sellerProfileImageUrl(seller.getProfileImageUrl())
                .build();
        
        // ProductRequestDetailDto 생성 및 반환
        return ProductRequestDetailDto.builder()
                .id(product.getId())
                .productType(product.getProductType())
                .tradeStatus(product.getTradeStatus())
                .petDetailType(product.getPetDetailType())
                .category(product.getCategory())
                .title(product.getTitle())
                .description(product.getDescription())
                .desiredPrice(product.getPrice())  // 희망 가격
                .mainImageUrl(product.getMainImageUrl())
                .subImageUrls(product.getSubImageUrls())
                .addressSido(product.getAddressSido())
                .addressGugun(product.getAddressGugun())
                .createdAt(product.getCreatedAt())
                .viewCount(product.getViewCount())
                .favoriteCount(product.getFavoriteCount())
                .sellerInfo(sellerInfo)
                .build();
    }
    
    @Override
    @Transactional
    public ProductDto updateProductRequest(Long productId, ProductRequestUpdateCommand command, String email) {
        // 상품 조회 (소프트 삭제된 상품 제외)
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new org.cmarket.cmarket.domain.product.app.exception.ProductNotFoundException("상품을 찾을 수 없습니다."));
        
        // 판매 요청인지 확인
        if (product.getProductType() != ProductType.REQUEST) {
            throw new org.cmarket.cmarket.domain.product.app.exception.ProductNotFoundException("판매 요청 게시글이 아닙니다.");
        }
        
        // 사용자 조회
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new org.cmarket.cmarket.domain.auth.app.exception.UserNotFoundException("사용자를 찾을 수 없습니다."));
        
        Long userId = user.getId();
        
        // 권한 확인: 게시자 본인만 수정 가능
        if (!product.getSellerId().equals(userId)) {
            throw new org.cmarket.cmarket.domain.auth.app.exception.AuthenticationFailedException("판매 요청을 수정할 권한이 없습니다.");
        }
        
        // 거래 상태 확인: BUYING 상태일 때만 수정 가능
        if (product.getTradeStatus() != TradeStatus.BUYING) {
            throw new org.cmarket.cmarket.domain.product.app.exception.ProductNotFoundException("거래 상태가 '삽니다'인 경우에만 수정할 수 있습니다.");
        }
        
        // 판매 요청 정보 수정
        product.update(
                command.petType(),
                command.petDetailType(),
                command.category(),
                command.title(),
                command.description(),
                command.desiredPrice(),  // 희망 가격
                org.cmarket.cmarket.domain.product.model.ProductStatus.NEW,  // 판매 요청은 기본값으로 NEW 유지
                command.mainImageUrl(),
                command.subImageUrls(),
                false,  // 판매 요청은 택배 거래 불가
                command.addressSido(),
                command.addressGugun(),
                null  // 판매 요청은 선호 만남 장소 없음
        );
        
        // 저장
        Product updatedProduct = productRepository.save(product);
        
        // DTO로 변환하여 반환
        return ProductDto.fromEntity(updatedProduct);
    }
    
    @Override
    @Transactional(readOnly = true)
    public MyProductListDto getMyProductList(Pageable pageable, String email) {
        // 사용자 조회
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new org.cmarket.cmarket.domain.auth.app.exception.UserNotFoundException("사용자를 찾을 수 없습니다."));
        
        Long userId = user.getId();
        
        // 내가 등록한 상품 목록 조회 (판매 상품 + 판매 요청 모두 포함, 최신순 정렬)
        Page<Product> productPage = productRepository.findBySellerIdAndDeletedAtIsNullOrderByCreatedAtDesc(
                userId,
                pageable
        );
        
        // 각 상품을 DTO로 변환 후 PageResult로 변환
        PageResult<MyProductListItemDto> pageResult = PageResult.fromPage(
                productPage.map(MyProductListItemDto::fromEntity)
        );
        
        return new MyProductListDto(pageResult);
    }
}

