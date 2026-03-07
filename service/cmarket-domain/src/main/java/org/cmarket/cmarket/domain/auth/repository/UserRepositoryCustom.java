package org.cmarket.cmarket.domain.auth.repository;

import org.cmarket.cmarket.domain.auth.model.User;
import org.cmarket.cmarket.domain.auth.model.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * User 엔티티 커스텀 레포지토리 인터페이스
 *
 * 어드민 유저 목록 검색 등 복잡한 쿼리를 정의합니다.
 */
public interface UserRepositoryCustom {

    /**
     * 어드민 목록용 유저 검색 (검색/필터/페이징)
     *
     * @param keyword 검색어 (닉네임, 이메일, 이름, ID) - null 가능
     * @param statusFilter 상태 (ACTIVE: deletedAt null, WITHDRAWN: deletedAt not null) - null이면 전체
     * @param roleFilter 권한 (USER, ADMIN) - null이면 전체
     * @param pageable 페이지네이션 정보
     * @return 유저 목록 (ID 오름차순)
     */
    Page<User> searchUsersForAdmin(
            String keyword,
            String statusFilter,
            UserRole roleFilter,
            Pageable pageable
    );

    /**
     * 탈퇴 회원 목록 조회 (deletedAt IS NOT NULL)
     *
     * @param keyword 검색어 (닉네임, 이메일, 이름, ID) - null 가능
     * @param pageable 페이지네이션 정보
     * @return 탈퇴 회원 목록 (ID 오름차순)
     */
    Page<User> findWithdrawnUsers(String keyword, Pageable pageable);
}
