package org.cmarket.cmarket.domain.admin.app.service;

import org.cmarket.cmarket.domain.admin.app.dto.AdminProductListItemDto;
import org.cmarket.cmarket.domain.auth.model.User;
import org.cmarket.cmarket.domain.auth.repository.UserRepository;
import org.cmarket.cmarket.domain.product.model.Category;
import org.cmarket.cmarket.domain.product.model.Product;
import org.cmarket.cmarket.domain.product.model.ProductType;
import org.cmarket.cmarket.domain.product.repository.ProductRepository;
import org.cmarket.cmarket.domain.profile.app.dto.PageResult;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 어드민 상품 조회 서비스
 */
@Service
public class AdminProductQueryService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public AdminProductQueryService(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PageResult<AdminProductListItemDto> getProductsForAdmin(
            String keyword,
            ProductType productType,
            Category category,
            int page,
            int size
    ) {
        List<String> keywords = keyword != null && !keyword.isBlank()
                ? List.of(keyword.trim().split("\\s+"))
                : null;

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Category> categories = category != null ? List.of(category) : null;

        var productPage = productRepository.searchProducts(
                keyword,
                keywords,
                productType,
                null,
                null,
                categories,
                null,
                null,
                null,
                null,
                null,
                "createdAt",
                "desc",
                pageable
        );

        List<Product> products = productPage.getContent();
        Map<Long, String> sellerNicknames = resolveSellerNicknames(
                products.stream().map(Product::getSellerId).collect(Collectors.toSet())
        );

        List<AdminProductListItemDto> content = products.stream()
                .map(p -> toAdminListItemDto(p, sellerNicknames.get(p.getSellerId())))
                .toList();

        return PageResult.fromPage(
                new org.springframework.data.domain.PageImpl<>(
                        content,
                        pageable,
                        productPage.getTotalElements()
                )
        );
    }

    private Map<Long, String> resolveSellerNicknames(Set<Long> sellerIds) {
        if (sellerIds.isEmpty()) {
            return Map.of();
        }
        return userRepository.findAllById(sellerIds).stream()
                .collect(Collectors.toMap(User::getId, User::getNickname, (a, b) -> a));
    }

    private AdminProductListItemDto toAdminListItemDto(Product product, String sellerNickname) {
        return AdminProductListItemDto.builder()
                .id(product.getId())
                .title(product.getTitle())
                .price(product.getPrice())
                .productType(product.getProductType())
                .category(product.getCategory())
                .petDetailType(product.getPetDetailType())
                .productStatus(product.getProductStatus())
                .tradeStatus(product.getTradeStatus())
                .sellerNickname(sellerNickname != null ? sellerNickname : "")
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
