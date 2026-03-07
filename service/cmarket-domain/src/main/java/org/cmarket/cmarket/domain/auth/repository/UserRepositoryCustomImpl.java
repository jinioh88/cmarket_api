package org.cmarket.cmarket.domain.auth.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.cmarket.cmarket.domain.auth.model.QUser;
import org.cmarket.cmarket.domain.auth.model.User;
import org.cmarket.cmarket.domain.auth.model.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

/**
 * User 엔티티 커스텀 레포지토리 구현체
 */
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QUser user = QUser.user;

    public UserRepositoryCustomImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public Page<User> searchUsersForAdmin(
            String keyword,
            String statusFilter,
            UserRole roleFilter,
            Pageable pageable
    ) {
        BooleanBuilder builder = new BooleanBuilder();

        // 키워드 검색 (닉네임, 이메일, 이름, ID)
        if (keyword != null && !keyword.isBlank()) {
            String trimmed = keyword.trim();
            BooleanBuilder keywordBuilder = new BooleanBuilder();
            keywordBuilder.or(user.nickname.containsIgnoreCase(trimmed));
            keywordBuilder.or(user.email.containsIgnoreCase(trimmed));
            keywordBuilder.or(user.name.containsIgnoreCase(trimmed));
            try {
                Long id = Long.parseLong(trimmed);
                keywordBuilder.or(user.id.eq(id));
            } catch (NumberFormatException ignored) {
                // ID가 숫자가 아니면 무시
            }
            builder.and(keywordBuilder);
        }

        // 상태 필터 (ACTIVE: 탈퇴 아님, WITHDRAWN: 탈퇴)
        if ("ACTIVE".equalsIgnoreCase(statusFilter)) {
            builder.and(user.deletedAt.isNull());
        } else if ("WITHDRAWN".equalsIgnoreCase(statusFilter)) {
            builder.and(user.deletedAt.isNotNull());
        }

        // 권한 필터
        if (roleFilter != null) {
            builder.and(user.role.eq(roleFilter));
        }

        List<User> content = queryFactory
                .selectFrom(user)
                .where(builder)
                .orderBy(user.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(user.id.count())
                .from(user)
                .where(builder);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<User> findWithdrawnUsers(String keyword, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(user.deletedAt.isNotNull());

        if (keyword != null && !keyword.isBlank()) {
            String trimmed = keyword.trim();
            BooleanBuilder keywordBuilder = new BooleanBuilder();
            keywordBuilder.or(user.nickname.containsIgnoreCase(trimmed));
            keywordBuilder.or(user.email.containsIgnoreCase(trimmed));
            keywordBuilder.or(user.name.containsIgnoreCase(trimmed));
            try {
                Long id = Long.parseLong(trimmed);
                keywordBuilder.or(user.id.eq(id));
            } catch (NumberFormatException ignored) {
            }
            builder.and(keywordBuilder);
        }

        List<User> content = queryFactory
                .selectFrom(user)
                .where(builder)
                .orderBy(user.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(user.id.count())
                .from(user)
                .where(builder);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}
