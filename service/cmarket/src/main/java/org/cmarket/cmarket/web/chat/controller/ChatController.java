package org.cmarket.cmarket.web.chat.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cmarket.cmarket.domain.chat.app.dto.ChatMessageDto;
import org.cmarket.cmarket.domain.chat.app.dto.ChatMessageListDto;
import org.cmarket.cmarket.domain.chat.app.dto.ChatRoomDto;
import org.cmarket.cmarket.domain.chat.app.dto.ChatRoomListDto;
import org.cmarket.cmarket.domain.chat.app.service.ChatService;
import org.cmarket.cmarket.web.chat.dto.ChatMessageListResponse;
import org.cmarket.cmarket.web.chat.dto.ChatRoomCreateRequest;
import org.cmarket.cmarket.web.chat.dto.ChatRoomListResponse;
import org.cmarket.cmarket.web.chat.dto.ChatRoomResponse;
import org.cmarket.cmarket.web.common.response.ResponseCode;
import org.cmarket.cmarket.web.common.response.SuccessResponse;
import org.cmarket.cmarket.web.common.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 채팅 관련 컨트롤러
 * 
 * 채팅방 생성, 목록 조회, 메시지 조회 등 채팅 관련 REST API를 제공합니다.
 * 실시간 메시지 전송은 WebSocket(ChatWebSocketController)에서 처리합니다.
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    
    private final ChatService chatService;
    private final ChatWebSocketController chatWebSocketController;
    
    /**
     * 채팅방 생성 (FR-025)
     * 
     * POST /api/chat/rooms
     * 
     * 상품에 대한 채팅방을 생성합니다.
     * - 구매자가 상품 상세 페이지에서 "채팅하기" 버튼을 누를 때 호출
     * - 기존 채팅방이 있으면 기존 채팅방 반환
     * - 본인 상품에는 채팅 불가
     * 
     * @param request 채팅방 생성 요청 (productId)
     * @return 생성된 채팅방 정보
     */
    @PostMapping("/rooms")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<ChatRoomResponse>> createChatRoom(
            @Valid @RequestBody ChatRoomCreateRequest request
    ) {
        // 현재 로그인한 사용자의 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 앱 서비스 호출
        ChatRoomDto chatRoomDto = chatService.createChatRoom(email, request.toCommand());
        
        // 앱 DTO → 웹 DTO 변환
        ChatRoomResponse response = ChatRoomResponse.fromDto(chatRoomDto);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse<>(ResponseCode.CREATED, response));
    }
    
    /**
     * 채팅방 목록 조회 (FR-024)
     * 
     * GET /api/chat/rooms
     * 
     * 현재 로그인한 사용자의 채팅방 목록을 조회합니다.
     * - 활성 상태인 채팅방만 조회
     * - 최근 메시지 시간 기준 내림차순 정렬
     * - 각 채팅방의 안 읽은 메시지 개수 포함
     * - 페이지네이션 지원
     * 
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 20)
     * @return 채팅방 목록
     */
    @GetMapping("/rooms")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<ChatRoomListResponse>> getChatRoomList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        // 현재 로그인한 사용자의 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 앱 서비스 호출
        ChatRoomListDto chatRoomListDto = chatService.getChatRoomList(email, page, size);
        
        // 앱 DTO → 웹 DTO 변환
        ChatRoomListResponse response = ChatRoomListResponse.fromDto(chatRoomListDto);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }
    
    /**
     * 채팅 내역 조회 (FR-028)
     * 
     * GET /api/chat/rooms/{chatRoomId}/messages
     * 
     * 채팅방의 메시지 내역을 조회합니다.
     * - 페이지네이션 지원
     * - 첫 페이지 조회 시 안 읽은 메시지 읽음 처리
     * - 차단된 메시지는 발신자 본인에게만 표시
     * 
     * @param chatRoomId 채팅방 ID
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 50)
     * @return 메시지 목록
     */
    @GetMapping("/rooms/{chatRoomId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<ChatMessageListResponse>> getChatMessages(
            @PathVariable Long chatRoomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        // 현재 로그인한 사용자의 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 앱 서비스 호출
        ChatMessageListDto messageListDto = chatService.getChatMessages(email, chatRoomId, page, size);
        
        // 앱 DTO → 웹 DTO 변환
        ChatMessageListResponse response = ChatMessageListResponse.from(messageListDto);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }
    
    /**
     * 채팅방 나가기 (FR-027)
     * 
     * DELETE /api/chat/rooms/{chatRoomId}
     * 
     * 채팅방에서 나갑니다 (소프트 삭제).
     * - 나가기 처리 후 상대방에게 시스템 메시지 전송
     * - 나간 채팅방은 목록에서 제외됨
     * - 나간 후에는 메시지 전송 불가
     * 
     * @param chatRoomId 채팅방 ID
     * @return 성공 응답
     */
    @DeleteMapping("/rooms/{chatRoomId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<Void>> leaveChatRoom(
            @PathVariable Long chatRoomId
    ) {
        // 현재 로그인한 사용자의 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 앱 서비스 호출 (나가기 처리 + 시스템 메시지 저장)
        ChatMessageDto systemMessage = chatService.leaveChatRoom(email, chatRoomId);
        
        // 상대방에게 시스템 메시지 WebSocket 전송
        chatWebSocketController.sendSystemMessage(chatRoomId, systemMessage.getContent());
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, null));
    }
}
