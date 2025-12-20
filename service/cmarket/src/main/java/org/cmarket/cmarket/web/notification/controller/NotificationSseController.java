package org.cmarket.cmarket.web.notification.controller;

import lombok.RequiredArgsConstructor;
import org.cmarket.cmarket.domain.auth.model.User;
import org.cmarket.cmarket.domain.auth.repository.UserRepository;
import org.cmarket.cmarket.web.common.security.SecurityUtils;
import org.cmarket.cmarket.web.notification.service.NotificationSseService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE 알림 스트림 컨트롤러
 * 프론트에서 로그인 직후와 페이지 새로고침 시 호출
 * 
 * Server-Sent Events를 통한 실시간 알림 전송을 제공합니다.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationSseController {
    
    private final NotificationSseService notificationSseService;
    private final UserRepository userRepository;
    
    /**
     * SSE 알림 스트림 연결
     * 
     * 클라이언트가 이 엔드포인트에 연결하면 실시간으로 알림을 받을 수 있습니다.
     * 
     * @return SseEmitter
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> stream() {
        // 현재 로그인한 사용자 정보 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 사용자 조회
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));
        Long userId = user.getId();
        
        // SSE 연결 생성
        SseEmitter emitter = notificationSseService.connect(userId);
        
        // 응답 헤더 설정
        return ResponseEntity.ok()
                .header("Cache-Control", "no-cache")
                .header("Connection", "keep-alive")
                .body(emitter);
    }
}
