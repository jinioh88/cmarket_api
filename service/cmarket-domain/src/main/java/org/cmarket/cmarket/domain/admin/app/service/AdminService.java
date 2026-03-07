package org.cmarket.cmarket.domain.admin.app.service;

import org.cmarket.cmarket.domain.admin.app.dto.AdminUserDto;

/**
 * 어드민 서비스 인터페이스
 *
 * 회원 역할 부여 등 어드민 전용 기능을 제공합니다.
 */
public interface AdminService {

    /**
     * 특정 회원에게 ADMIN 역할을 부여합니다.
     *
     * @param userId 대상 회원 ID
     * @return 역할이 변경된 회원 정보
     */
    AdminUserDto grantAdminRole(Long userId);

    /**
     * 탈퇴한 회원을 복구합니다.
     *
     * @param userId 대상 회원 ID (탈퇴한 회원)
     * @return 복구된 회원 정보
     */
    AdminUserDto restoreWithdrawnUser(Long userId);
}
