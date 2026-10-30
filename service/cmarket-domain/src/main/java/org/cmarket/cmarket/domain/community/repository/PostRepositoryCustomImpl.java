package org.cmarket.cmarket.domain.community.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.cmarket.cmarket.domain.community.model.BoardType;
import org.cmarket.cmarket.domain.community.model.Post;
import org.cmarket.cmarket.domain.community.model.QPost;
import org.cmarket.cmarket.domain.eventfeed.model.EventIngestStatus;
import org.cmarket.cmarket.domain.eventfeed.model.QEventIngestRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Post 엔티티 커스텀 레포지토리 구현체
 * 
 * QueryDSL을 사용한 복잡한 쿼리를 구현합니다.
 */
public class PostRepositoryCustomImpl implements PostRepositoryCustom {
    
    private final JPAQueryFactory queryFactory;
    private final QPost post = QPost.post;
    private final QEventIngestRecord eventIngestRecord = QEventIngestRecord.eventIngestRecord;
    
    public PostRepositoryCustomImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }
    
    @Override
    public Page<Post> findPosts(String sortBy, String sortOrder, BoardType boardType, String searchType, String keyword,
                                List<Long> excludedAuthorIds, Collection<Long> onlyPostIds, Pageable pageable) {
        // 기본 조건: 소프트 삭제되지 않은 게시글만
        BooleanExpression whereCondition = post.deletedAt.isNull();

        // 차단한 사용자의 글 제외
        //
        // 차단 안내가 「게시글이 숨김 처리됩니다」라고 약속한다. 상품은 #809에서
        // 걸러지게 했고, 글은 여기서 한다.
        //
        // 화면에 다 받아 온 뒤 걸러내면 안 된다 — 한 페이지에 10개를 달라고 했는데
        // 8개만 남는 식이 되어 페이지 수와 무한스크롤이 어긋난다. 쿼리에서 뺀다.
        if (excludedAuthorIds != null && !excludedAuthorIds.isEmpty()) {
            whereCondition = whereCondition.and(post.authorId.notIn(excludedAuthorIds));
        }

        // 행사 소식을 진행 상태로 거를 때 그 글 번호만 남긴다 (빈 목록은 서비스가 미리 끝낸다)
        if (onlyPostIds != null && !onlyPostIds.isEmpty()) {
            whereCondition = whereCondition.and(post.id.in(onlyPostIds));
        }

        // 게시판 유형 필터링 (boardType이 null이 아니면 해당 유형만 조회)
        //
        // 게시판을 고르지 않은 목록에는 행사 소식(EVENT)을 넣지 않는다.
        // 수집한 글이 사람이 쓴 글 사이에 섞이지 않게 하려는 것이고,
        // boardType=EVENT를 줄 때만 나온다.
        if (boardType != null) {
            whereCondition = whereCondition.and(post.boardType.eq(boardType));
        } else {
            whereCondition = whereCondition.and(post.boardType.ne(BoardType.EVENT));
        }
        
        // 검색 조건 추가
        if (searchType != null && keyword != null && !keyword.trim().isEmpty()) {
            String trimmedKeyword = keyword.trim();
            switch (searchType.toLowerCase()) {
                case "title":
                    // 제목만 검색
                    whereCondition = whereCondition.and(post.title.containsIgnoreCase(trimmedKeyword));
                    break;
                case "title_content":
                    // 제목 또는 내용 검색
                    whereCondition = whereCondition.and(
                            post.title.containsIgnoreCase(trimmedKeyword)
                                    .or(post.content.containsIgnoreCase(trimmedKeyword))
                    );
                    break;
                case "writer":
                    // 작성자 검색
                    whereCondition = whereCondition.and(post.authorNickname.containsIgnoreCase(trimmedKeyword));
                    break;
                default:
                    // 알 수 없는 검색 타입은 무시
                    break;
            }
        }
        
        JPAQuery<Post> query = queryFactory
                .selectFrom(post)
                .where(whereCondition)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        // 행사 소식의 「곧 끝나는 순」: 수집 기록의 기간으로 정렬한다(설계 7-2절)
        if ("ending".equalsIgnoreCase(sortBy) && boardType == BoardType.EVENT) {
            query.leftJoin(eventIngestRecord)
                    .on(eventIngestRecord.postId.eq(post.id).and(eventIngestRecord.status.eq(EventIngestStatus.POSTED)))
                    .orderBy(buildEndingOrder());
        } else {
            query.orderBy(buildOrderSpecifiers(sortBy, sortOrder));
        }
        
        // 카운트 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(post.count())
                .from(post)
                .where(whereCondition);
        
        // 페이지네이션 결과 반환
        List<Post> content = query.fetch();
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
    
    /**
     * 「곧 끝나는 순」 정렬
     * 
     * 끝나는 날(없으면 시작일)이 오늘(한국 날짜)보다 앞이면 끝난 행사다. 끝나지 않은 행사를 먼저,
     * 그 안에서 끝나는 날이 가까운 순으로 둔다. 끝난 행사는 맨 아래로 간다. 웹 상태 칩과 같은 규칙이다.
     */
    private OrderSpecifier<?>[] buildEndingOrder() {
        DateExpression<LocalDate> endDate = eventIngestRecord.eventEndDate.coalesce(eventIngestRecord.eventStartDate);
        LocalDate today = LocalDate.now(KOREA);
        NumberExpression<Integer> ended = new CaseBuilder().when(endDate.lt(today)).then(1).otherwise(0);
        return new OrderSpecifier<?>[]{
                ended.asc(),
                endDate.asc(),
                post.createdAt.desc()
        };
    }

    private static final ZoneId KOREA = ZoneId.of("Asia/Seoul");

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

