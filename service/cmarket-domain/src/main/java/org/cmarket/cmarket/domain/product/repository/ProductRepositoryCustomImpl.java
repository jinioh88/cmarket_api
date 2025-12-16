package org.cmarket.cmarket.domain.product.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.cmarket.cmarket.domain.product.model.Category;
import org.cmarket.cmarket.domain.product.model.PetDetailType;
import org.cmarket.cmarket.domain.product.model.PetType;
import org.cmarket.cmarket.domain.product.model.Product;
import org.cmarket.cmarket.domain.product.model.ProductStatus;
import org.cmarket.cmarket.domain.product.model.ProductType;
import org.cmarket.cmarket.domain.product.model.QProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Product 엔티티 커스텀 레포지토리 구현체
 * 
 * QueryDSL을 사용한 복잡한 검색 쿼리를 구현합니다.
 */
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {
    
    private final JPAQueryFactory queryFactory;
    private final QProduct product = QProduct.product;
    
    public ProductRepositoryCustomImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }
    
    @Override
    public Page<Product> searchProducts(
            String keyword,
            List<String> keywords,
            ProductType productType,
            PetType petType,
            PetDetailType petDetailType,
            List<Category> categories,
            List<ProductStatus> productStatuses,
            Long minPrice,
            Long maxPrice,
            String addressSido,
            String addressGugun,
            String sortBy,
            String sortOrder,
            Pageable pageable
    ) {
        // 기본 조건: 소프트 삭제되지 않은 상품만
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(product.deletedAt.isNull());
        
        // 상품 타입 필터
        if (productType != null) {
            builder.and(product.productType.eq(productType));
        }
        
        // 반려동물 대분류 필터
        if (petType != null) {
            builder.and(product.petType.eq(petType));
        }
        
        // 반려동물 상세 종류 필터
        if (petDetailType != null) {
            builder.and(product.petDetailType.eq(petDetailType));
        }
        
        // 상품 카테고리 필터 (여러 개 선택 가능)
        if (categories != null && !categories.isEmpty()) {
            builder.and(product.category.in(categories));
        }
        
        // 상품 상태 필터 (여러 개 선택 가능)
        if (productStatuses != null && !productStatuses.isEmpty()) {
            builder.and(product.productStatus.in(productStatuses));
        }
        
        // 가격 범위 필터
        if (minPrice != null) {
            builder.and(product.price.goe(minPrice));
        }
        if (maxPrice != null) {
            builder.and(product.price.loe(maxPrice));
        }
        
        // 지역 필터
        if (addressSido != null && !addressSido.isEmpty()) {
            builder.and(product.addressSido.eq(addressSido));
        }
        if (addressGugun != null && !addressGugun.isEmpty()) {
            builder.and(product.addressGugun.eq(addressGugun));
        }
        
        // 키워드 검색 조건
        BooleanBuilder keywordBuilder = new BooleanBuilder();
        String searchKeyword = null;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 단일 키워드: 제목, 설명, 카테고리명 검색
            searchKeyword = keyword.trim().toLowerCase();
            // 카테고리 enum을 문자열로 변환하여 검색 (enum name 사용)
            keywordBuilder.or(product.title.lower().contains(searchKeyword))
                    .or(product.description.lower().contains(searchKeyword))
                    .or(Expressions.stringTemplate("UPPER({0})", product.category).contains(searchKeyword.toUpperCase()));
        } else if (keywords != null && !keywords.isEmpty()) {
            // 다중 키워드: AND 조건 (모든 키워드가 포함된 상품만)
            for (String kw : keywords) {
                if (kw != null && !kw.trim().isEmpty()) {
                    String trimmedKw = kw.trim().toLowerCase();
                    BooleanBuilder kwBuilder = new BooleanBuilder();
                    kwBuilder.or(product.title.lower().contains(trimmedKw))
                            .or(product.description.lower().contains(trimmedKw))
                            .or(Expressions.stringTemplate("UPPER({0})", product.category).contains(trimmedKw.toUpperCase()));
                    keywordBuilder.and(kwBuilder);
                }
            }
            // 우선순위 정렬을 위한 첫 번째 키워드 저장
            if (!keywords.isEmpty() && keywords.get(0) != null && !keywords.get(0).trim().isEmpty()) {
                searchKeyword = keywords.get(0).trim().toLowerCase();
            }
        }
        
        if (keywordBuilder.hasValue()) {
            builder.and(keywordBuilder);
        }
        
        // 정렬 조건 생성 (키워드 우선순위 + 사용자 지정 정렬)
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        
        // 키워드 검색 시 우선순위 정렬 추가 (제목 일치 > 설명 일치 > 카테고리 일치)
        if (searchKeyword != null) {
            String likePattern = buildLikePattern(searchKeyword.toLowerCase());
            String likePatternUpper = buildLikePattern(searchKeyword.toUpperCase());
            
            NumberExpression<Integer> titleMatchScore = new CaseBuilder()
                    .when(product.title.lower().like(likePattern, '!')).then(1)
                    .otherwise(0);
            NumberExpression<Integer> descriptionMatchScore = new CaseBuilder()
                    .when(product.description.lower().like(likePattern, '!')).then(1)
                    .otherwise(0);
            StringExpression categoryUpper = product.category.stringValue().upper();
            NumberExpression<Integer> categoryMatchScore = new CaseBuilder()
                    .when(categoryUpper.like(likePatternUpper, '!')).then(1)
                    .otherwise(0);
            
            orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, titleMatchScore));
            orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, descriptionMatchScore));
            orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, categoryMatchScore));
        }
        
        // 사용자 지정 정렬 조건 추가
        OrderSpecifier<?>[] userOrderSpecifiers = buildOrderSpecifiers(sortBy, sortOrder);
        for (OrderSpecifier<?> spec : userOrderSpecifiers) {
            orderSpecifiers.add(spec);
        }
        
        // 쿼리 생성
        JPAQuery<Product> query = queryFactory
                .selectFrom(product)
                .where(builder)
                .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());
        
        // 카운트 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(product.count())
                .from(product)
                .where(builder);
        
        // 페이지네이션 결과 반환
        List<Product> content = query.fetch();
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
    
    /**
     * 정렬 조건 생성
     */
    private OrderSpecifier<?>[] buildOrderSpecifiers(String sortBy, String sortOrder) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        Order order = "asc".equalsIgnoreCase(sortOrder) ? Order.ASC : Order.DESC;
        
        if (sortBy == null || sortBy.isEmpty()) {
            // 기본 정렬: 최신순 (createdAt DESC, id DESC)
            orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, product.createdAt));
            orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, product.id));
        } else {
            switch (sortBy.toLowerCase()) {
                case "createdat":
                    // createdAt 기준 정렬 (동일 시간일 경우 id로 추가 정렬)
                    orderSpecifiers.add(new OrderSpecifier<>(order, product.createdAt));
                    orderSpecifiers.add(new OrderSpecifier<>(order, product.id));
                    break;
                case "price":
                    orderSpecifiers.add(new OrderSpecifier<>(order, product.price));
                    break;
                case "favoritecount":
                    orderSpecifiers.add(new OrderSpecifier<>(order, product.favoriteCount));
                    break;
                default:
                    // 기본 정렬: 최신순 (createdAt DESC, id DESC)
                    orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, product.createdAt));
                    orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, product.id));
                    break;
            }
        }
        
        return orderSpecifiers.toArray(new OrderSpecifier[0]);
    }

    /**
     * LIKE 검색용 패턴 생성 (escape 문자 '!' 사용)
     */
    private String buildLikePattern(String keyword) {
        if (keyword == null) {
            return null;
        }
        String escaped = keyword
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
        return "%" + escaped + "%";
    }
}

