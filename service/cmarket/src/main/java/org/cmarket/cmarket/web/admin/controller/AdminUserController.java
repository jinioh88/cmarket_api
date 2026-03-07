package org.cmarket.cmarket.web.admin.controller;

import lombok.RequiredArgsConstructor;
import org.cmarket.cmarket.domain.admin.app.dto.AdminUserDto;
import org.cmarket.cmarket.domain.admin.app.service.AdminService;
import org.cmarket.cmarket.domain.admin.app.service.AdminUserQueryService;
import org.cmarket.cmarket.domain.auth.model.UserRole;
import org.cmarket.cmarket.web.admin.dto.AdminUserListResponse;
import org.cmarket.cmarket.web.admin.dto.AdminUserResponse;
import org.cmarket.cmarket.web.common.response.ResponseCode;
import org.cmarket.cmarket.web.common.response.SuccessResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 어드민 회원 관리 컨트롤러
 *
 * 회원 목록 조회, 역할 부여 등 어드민 전용 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminService adminService;
    private final AdminUserQueryService adminUserQueryService;

    /**
     * 전체 유저 목록을 조회합니다. (검색/필터/페이징)
     *
     * @param keyword 검색어 (닉네임, 이메일, 이름, ID)
     * @param status 상태 (ACTIVE, WITHDRAWN)
     * @param role 권한 (USER, ADMIN)
     * @param page 페이지 번호 (0부터)
     * @param size 페이지 크기 (기본 10)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<SuccessResponse<AdminUserListResponse>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UserRole role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var pageResult = adminUserQueryService.getUsersForAdmin(keyword, status, role, page, size);
        AdminUserListResponse response = AdminUserListResponse.fromPageResult(pageResult);
        return ResponseEntity.ok(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }

    /**
     * 특정 회원에게 어드민 역할을 부여합니다.
     *
     * @param userId 대상 회원 ID
     * @return 변경된 회원 정보
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{userId}/role")
    public ResponseEntity<SuccessResponse<AdminUserResponse>> grantAdminRole(
            @PathVariable Long userId
    ) {
        AdminUserDto result = adminService.grantAdminRole(userId);
        AdminUserResponse response = AdminUserResponse.fromDto(result);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }
}
