package org.cmarket.cmarket.domain.search.app.service;

import org.cmarket.cmarket.domain.auth.model.User;
import org.cmarket.cmarket.domain.auth.repository.UserRepository;
import org.cmarket.cmarket.domain.product.model.Product;
import org.cmarket.cmarket.domain.product.repository.FavoriteRepository;
import org.cmarket.cmarket.domain.product.repository.ProductRepository;
import org.cmarket.cmarket.domain.profile.app.dto.PageResult;
import org.cmarket.cmarket.domain.search.app.dto.ProductSearchCommand;
import org.cmarket.cmarket.domain.search.app.dto.ProductSearchItemDto;
import org.cmarket.cmarket.domain.search.app.dto.ProductSearchResultDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 검색 서비스 구현체
 * 
 * 상품 통합 검색 기능을 구현합니다.
 */
@Service
public class SearchServiceImpl implements SearchService {
    
    private final ProductRepository productRepository;
    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    
    // 특수문자 및 이모지 제거를 위한 정규식
    private static final Pattern SPECIAL_CHARS_PATTERN = Pattern.compile("[^\\p{L}\\p{N}\\s]");
    
    public SearchServiceImpl(
            ProductRepository productRepository,
            FavoriteRepository favoriteRepository,
            UserRepository userRepository
    ) {
        this.productRepository = productRepository;
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
    }
    
    @Override
    @Transactional(readOnly = true)
    public ProductSearchResultDto searchProducts(ProductSearchCommand command, String email) {
        // 키워드 전처리 및 다중 키워드 분리
        // keyword가 있으면 공백으로 분리하여 keywords 리스트로 변환 (다중 단어 검색 지원)
        List<String> keywords = null;
        if (command.getKeyword() != null && !command.getKeyword().trim().isEmpty()) {
            // keyword를 공백으로 분리하여 다중 키워드로 처리
            String[] keywordArray = command.getKeyword().trim().split("\\s+");
            keywords = new ArrayList<>();
            for (String kw : keywordArray) {
                String processedKw = preprocessKeyword(kw);
                if (processedKw != null && !processedKw.isEmpty()) {
                    keywords.add(processedKw);
                }
            }
            if (keywords.isEmpty()) {
                keywords = null;
            }
        } else if (command.getKeywords() != null && !command.getKeywords().isEmpty()) {
            // keywords가 직접 전달된 경우
            keywords = preprocessKeywords(command.getKeywords());
        }
        
        // 단일 keyword는 null로 설정 (다중 키워드로 처리하므로)
        String keyword = null;
        
        // 페이지네이션 정보 생성
        Pageable pageable = createPageable(
                command.getPage() != null ? command.getPage() : 0,
                command.getSize() != null ? command.getSize() : 20
        );
        
        // 정렬 기준 검증 및 기본값 설정
        String sortBy = validateAndGetSortBy(command.getSortBy());
        String sortOrder = validateAndGetSortOrder(command.getSortOrder());
        
        // Repository를 통한 검색
        Page<Product> productPage = productRepository.searchProducts(
                keyword,
                keywords,
                command.getProductType(),
                command.getPetType(),
                command.getPetDetailType(),
                command.getCategories(),
                command.getProductStatuses(),
                command.getMinPrice(),
                command.getMaxPrice(),
                command.getAddressSido(),
                command.getAddressGugun(),
                sortBy,
                sortOrder,
                pageable
        );
        
        // 현재 로그인한 사용자 ID 조회 (비로그인 시 null)
        final Long userId = email != null
                ? userRepository.findByEmailAndDeletedAtIsNull(email)
                        .map(User::getId)
                        .orElse(null)
                : null;
        
        // N+1 문제 방지: 한 번의 쿼리로 찜한 상품 ID 목록 조회
        final Set<Long> favoriteProductIds = userId != null && !productPage.getContent().isEmpty()
                ? new HashSet<>(favoriteRepository.findProductIdsByUserIdAndProductIdIn(
                        userId,
                        productPage.getContent().stream()
                                .map(Product::getId)
                                .toList()
                ))
                : new HashSet<>();
        
        // 각 상품의 찜 여부 확인 및 DTO 변환 후 PageResult로 변환
        PageResult<ProductSearchItemDto> pageResult = PageResult.fromPage(
                productPage.map(product -> {
                    Boolean isFavorite = userId != null && favoriteProductIds.contains(product.getId());
                    return ProductSearchItemDto.fromEntity(product, isFavorite);
                })
        );
        
        return new ProductSearchResultDto(pageResult);
    }
    
    /**
     * 키워드 전처리
     * - 불필요한 특수문자 및 이모지 제거
     * - 공백 제거 및 trim
     */
    private String preprocessKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        
        // 특수문자 및 이모지 제거
        String cleaned = SPECIAL_CHARS_PATTERN.matcher(keyword).replaceAll(" ");
        
        // 연속된 공백을 하나로 변환하고 trim
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        
        return cleaned.isEmpty() ? null : cleaned;
    }
    
    /**
     * 다중 키워드 전처리
     * - 각 키워드에 대해 전처리 수행
     * - 빈 키워드 제거
     */
    private List<String> preprocessKeywords(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return null;
        }
        
        List<String> processed = new ArrayList<>();
        for (String kw : keywords) {
            String processedKw = preprocessKeyword(kw);
            if (processedKw != null && !processedKw.isEmpty()) {
                processed.add(processedKw);
            }
        }
        
        return processed.isEmpty() ? null : processed;
    }
    
    /**
     * 페이지네이션 정보 생성
     */
    private Pageable createPageable(Integer page, Integer size) {
        int pageNumber = page != null ? page : 0;
        int pageSize = size != null ? size : 20;
        
        // 최소값 검증
        if (pageNumber < 0) {
            pageNumber = 0;
        }
        if (pageSize < 1) {
            pageSize = 20;
        }
        
        return PageRequest.of(pageNumber, pageSize);
    }
    
    /**
     * 정렬 기준 검증 및 기본값 반환
     */
    private String validateAndGetSortBy(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "createdAt";
        }
        
        String lowerSortBy = sortBy.toLowerCase();
        // 화이트리스트 검증
        if (lowerSortBy.equals("createdat") || 
            lowerSortBy.equals("price") || 
            lowerSortBy.equals("favoritecount")) {
            return lowerSortBy;
        }
        
        // 잘못된 값이면 기본값 반환
        return "createdAt";
    }
    
    /**
     * 정렬 방향 검증 및 기본값 반환
     */
    private String validateAndGetSortOrder(String sortOrder) {
        if (sortOrder == null || sortOrder.trim().isEmpty()) {
            return "desc";
        }
        
        String lowerSortOrder = sortOrder.toLowerCase();
        if (lowerSortOrder.equals("asc") || lowerSortOrder.equals("desc")) {
            return lowerSortOrder;
        }
        
        // 잘못된 값이면 기본값 반환
        return "desc";
    }
}

