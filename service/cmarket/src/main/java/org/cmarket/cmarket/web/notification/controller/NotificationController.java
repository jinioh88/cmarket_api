package org.cmarket.cmarket.web.notification.controller;

import lombok.RequiredArgsConstructor;
import org.cmarket.cmarket.domain.notification.app.service.NotificationService;
import org.cmarket.cmarket.domain.profile.app.dto.PageResult;
import org.cmarket.cmarket.web.common.response.ResponseCode;
import org.cmarket.cmarket.web.common.response.SuccessResponse;
import org.cmarket.cmarket.web.common.security.SecurityUtils;
import org.cmarket.cmarket.web.notification.dto.NotificationListResponse;
import org.cmarket.cmarket.web.notification.dto.NotificationUnreadCountResponse;
import org.cmarket.cmarket.web.profile.dto.PageResultResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 알림 REST API 컨트롤러
 * 
 * 알림 관련 REST API를 제공합니다.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    
    /**
     * 알림 목록 조회
     * 
     * GET /api/notifications
     * 
     * 현재 로그인한 사용자의 알림 목록을 조회합니다.
     * - 최신순 정렬
     * - 페이지네이션 지원 (기본값: page=0, size=20)
     * 
     * @param pageable 페이지네이션 정보 (기본값: page=0, size=20)
     * @return 알림 목록 (페이지네이션 포함)
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<PageResultResponse<NotificationListResponse>>> getNotificationList(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        // 현재 로그인한 사용자의 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 앱 서비스 호출
        PageResult<org.cmarket.cmarket.domain.notification.app.dto.NotificationDto> pageResult = 
                notificationService.getNotificationList(email, pageable);
        
        // 앱 DTO → 웹 DTO 변환
        PageResult<NotificationListResponse> webPageResult = pageResult.map(NotificationListResponse::fromDto);
        PageResultResponse<NotificationListResponse> response = PageResultResponse.fromPageResult(webPageResult);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }
    
    /**
     * 안 읽은 알림 개수 조회
     * 
     * GET /api/notifications/unread-count
     * 
     * 현재 로그인한 사용자의 안 읽은 알림 개수를 조회합니다.
     * 
     * @return 안 읽은 알림 개수
     */
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<NotificationUnreadCountResponse>> getUnreadCount() {
        // 현재 로그인한 사용자의 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 앱 서비스 호출
        Long unreadCount = notificationService.getUnreadCount(email);
        
        // 웹 DTO 생성
        NotificationUnreadCountResponse response = new NotificationUnreadCountResponse(unreadCount);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }
    
    /**
     * 알림 읽음 처리
     * 
     * PATCH /api/notifications/{notificationId}/read
     * 
     * 특정 알림을 읽음 상태로 변경합니다.
     * 
     * @param notificationId 알림 ID
     * @return 성공 응답
     */
    @PatchMapping("/{notificationId}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<Void>> markAsRead(@PathVariable Long notificationId) {
        // 현재 로그인한 사용자의 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 앱 서비스 호출
        notificationService.markAsRead(email, notificationId);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, null));
    }
    
    /**
     * 모든 알림 읽음 처리
     * 
     * PATCH /api/notifications/read-all
     * 
     * 현재 로그인한 사용자의 모든 안 읽은 알림을 읽음 상태로 변경합니다.
     * 
     * @return 성공 응답
     */
    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<Void>> markAllAsRead() {
        // 현재 로그인한 사용자의 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 앱 서비스 호출
        notificationService.markAllAsRead(email);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, null));
    }
}
