package org.cmarket.cmarket.web.admin.controller;

import lombok.RequiredArgsConstructor;
import org.cmarket.cmarket.domain.admin.app.dto.AdminUserDto;
import org.cmarket.cmarket.domain.admin.app.dto.WithdrawalDetailDto;
import org.cmarket.cmarket.domain.admin.app.dto.WithdrawalListItemDto;
import org.cmarket.cmarket.domain.admin.app.service.AdminService;
import org.cmarket.cmarket.domain.admin.app.service.AdminWithdrawalQueryService;
import org.cmarket.cmarket.domain.profile.app.dto.PageResult;
import org.cmarket.cmarket.web.admin.dto.AdminUserResponse;
import org.cmarket.cmarket.web.admin.dto.WithdrawalDetailResponse;
import org.cmarket.cmarket.web.admin.dto.WithdrawalListResponse;
import org.cmarket.cmarket.web.common.response.ResponseCode;
import org.cmarket.cmarket.web.common.response.SuccessResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 어드민 탈퇴 관리 컨트롤러
 */
@RestController
@RequestMapping("/api/admin/withdrawals")
@RequiredArgsConstructor
public class AdminWithdrawalController {

    private final AdminWithdrawalQueryService adminWithdrawalQueryService;
    private final AdminService adminService;

    /**
     * 탈퇴 회원 목록 조회
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<SuccessResponse<WithdrawalListResponse>> getWithdrawnUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResult<WithdrawalListItemDto> pageResult = adminWithdrawalQueryService.getWithdrawnUsers(keyword, page, size);
        WithdrawalListResponse response = WithdrawalListResponse.fromPageResult(pageResult);
        return ResponseEntity.ok(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }

    /**
     * 탈퇴 회원 상세 조회
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{userId}")
    public ResponseEntity<SuccessResponse<WithdrawalDetailResponse>> getWithdrawnUserDetail(
            @PathVariable Long userId
    ) {
        WithdrawalDetailDto dto = adminWithdrawalQueryService.getWithdrawnUserDetail(userId);
        WithdrawalDetailResponse response = WithdrawalDetailResponse.fromDto(dto);
        return ResponseEntity.ok(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }

    /**
     * 탈퇴 회원 복구
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{userId}/restore")
    public ResponseEntity<SuccessResponse<AdminUserResponse>> restoreWithdrawnUser(
            @PathVariable Long userId
    ) {
        AdminUserDto result = adminService.restoreWithdrawnUser(userId);
        AdminUserResponse response = AdminUserResponse.fromDto(result);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }
}
