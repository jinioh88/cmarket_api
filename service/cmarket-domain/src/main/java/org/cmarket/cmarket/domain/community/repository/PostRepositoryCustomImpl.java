package org.cmarket.cmarket.domain.community.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.cmarket.cmarket.domain.community.model.Post;
import org.cmarket.cmarket.domain.community.model.QPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Post 엔티티 커스텀 레포지토리 구현체
 * 
 * QueryDSL을 사용한 복잡한 쿼리를 구현합니다.
 */
public class PostRepositoryCustomImpl implements PostRepositoryCustom {
    
    private final JPAQueryFactory queryFactory;
    private final QPost post = QPost.post;
    
    public PostRepositoryCustomImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }
    
    @Override
    public Page<Post> findPosts(String sortBy, String sortOrder, Pageable pageable) {
        // 기본 조건: 소프트 삭제되지 않은 게시글만
        JPAQuery<Post> query = queryFactory
                .selectFrom(post)
                .where(post.deletedAt.isNull())
                .orderBy(buildOrderSpecifiers(sortBy, sortOrder))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());
        
        // 카운트 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(post.count())
                .from(post)
                .where(post.deletedAt.isNull());
        
        // 페이지네이션 결과 반환
        List<Post> content = query.fetch();
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
    
    /**
     * 정렬 조건 생성
     * 
     * @param sortBy 정렬 기준 ("latest", "oldest", "views", "comments")
     * @param sortOrder 정렬 방향 ("asc", "desc") - 현재는 사용하지 않음 (각 sortBy에 맞는 정렬 방향 고정)
     * @return 정렬 조건 배열
     */
    private OrderSpecifier<?>[] buildOrderSpecifiers(String sortBy, String sortOrder) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        
        if (sortBy == null || sortBy.isEmpty()) {
            // 기본 정렬: 최신순
            orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, post.createdAt));
        } else {
            switch (sortBy.toLowerCase()) {
                case "latest":
                    // 최신순: createdAt DESC
                    orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, post.createdAt));
                    break;
                case "oldest":
                    // 오래된순: createdAt ASC
                    orderSpecifiers.add(new OrderSpecifier<>(Order.ASC, post.createdAt));
                    break;
                case "views":
                    // 조회수 많은순: viewCount DESC
                    orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, post.viewCount));
                    break;
                case "comments":
                    // 댓글 많은순: commentCount DESC
                    orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, post.commentCount));
                    break;
                default:
                    // 기본 정렬: 최신순
                    orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, post.createdAt));
                    break;
            }
        }
        
        return orderSpecifiers.toArray(new OrderSpecifier[0]);
    }
}

