package org.cmarket.cmarket.web.report.controller;

import lombok.RequiredArgsConstructor;
import org.cmarket.cmarket.domain.report.app.dto.BlockedUserListDto;
import org.cmarket.cmarket.domain.report.app.dto.UserBlockCreateCommand;
import org.cmarket.cmarket.domain.report.app.dto.UserBlockDto;
import org.cmarket.cmarket.domain.report.app.service.ReportService;
import org.cmarket.cmarket.web.common.response.ResponseCode;
import org.cmarket.cmarket.web.common.response.SuccessResponse;
import org.cmarket.cmarket.web.common.security.SecurityUtils;
import org.cmarket.cmarket.web.profile.dto.BlockedUserListResponse;
import org.cmarket.cmarket.web.report.dto.UserBlockResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports/blocks")
@RequiredArgsConstructor
public class UserBlockController {

    private final ReportService reportService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/users/{blockedUserId}")
    public ResponseEntity<SuccessResponse<UserBlockResponse>> blockUser(
            @PathVariable Long blockedUserId
    ) {
        String email = SecurityUtils.getCurrentUserEmail();

        UserBlockCreateCommand command = UserBlockCreateCommand.builder()
                .blockedUserId(blockedUserId)
                .build();

        UserBlockDto result = reportService.blockUser(email, command);
        UserBlockResponse response = UserBlockResponse.fromDto(result);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse<>(ResponseCode.CREATED, response));
    }

    /**
     * 차단한 유저 목록 조회
     * 
     * GET /api/reports/blocks/users
     * 
     * 현재 로그인한 사용자가 차단한 유저 목록을 조회합니다.
     * - 최신순 정렬 (createdAt DESC)
     * - 페이지네이션 지원 (기본값: page=0, size=10)
     * 
     * @param pageable 페이지네이션 정보 (기본값: page=0, size=10)
     * @return 차단한 유저 목록 (페이지네이션 포함)
     */
    @GetMapping("/users")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<BlockedUserListResponse>> getBlockedUsers(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        // 현재 로그인한 사용자의 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 앱 서비스 호출
        BlockedUserListDto blockedUserListDto = reportService.getBlockedUsers(email, pageable);
        
        // 앱 DTO → 웹 DTO 변환
        BlockedUserListResponse response = BlockedUserListResponse.fromDto(blockedUserListDto);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }

    /**
     * 유저 차단 해제
     * 
     * DELETE /api/reports/blocks/users/{blockedUserId}
     * 
     * 현재 로그인한 사용자가 차단한 유저를 차단 해제합니다.
     * 차단 관계가 존재하지 않는 경우에도 성공으로 처리합니다 (idempotent).
     * 
     * @param blockedUserId 차단 해제할 사용자 ID
     * @return 성공 응답
     */
    @DeleteMapping("/users/{blockedUserId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<Void>> unblockUser(
            @PathVariable Long blockedUserId
    ) {
        // 현재 로그인한 사용자의 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 앱 서비스 호출
        reportService.unblockUser(email, blockedUserId);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, null));
    }
}

