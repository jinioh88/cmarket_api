package org.cmarket.cmarket.domain.chat.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 채팅 메시지 엔티티
 * 
 * 채팅 메시지 정보를 저장하는 도메인 모델입니다.
 * - 발신자 정보는 전송 시점의 스냅샷으로 저장
 * - 개인정보 포함 메시지는 차단 처리 (isBlocked = true)
 * - 읽음 상태는 RDB에 영구 저장 (Redis와 동기화)
 */
@Entity
@Table(
    name = "chat_messages",
    indexes = {
        @Index(name = "idx_chat_message_chat_room_id", columnList = "chat_room_id"),
        @Index(name = "idx_chat_message_sender_id", columnList = "sender_id"),
        @Index(name = "idx_chat_message_created_at", columnList = "created_at"),
        @Index(name = "idx_chat_message_is_read", columnList = "is_read")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, name = "chat_room_id")
    private Long chatRoomId;  // 채팅방 ID (ChatRoom 참조)
    
    @Column(nullable = false, name = "sender_id")
    private Long senderId;  // 발신자 ID (User 참조)
    
    @Column(nullable = false, name = "sender_nickname", length = 10)
    private String senderNickname;  // 발신자 닉네임 (전송 시점 스냅샷)
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "message_type", length = 20)
    private MessageType messageType;  // 메시지 타입 (TEXT, IMAGE, SYSTEM)
    
    @Column(nullable = false, length = 1000)
    private String content;  // 메시지 내용 (최대 1000자)
    
    @Column(name = "image_url", length = 500)
    private String imageUrl;  // 이미지 URL (이미지 메시지의 경우)
    
    @Column(nullable = false, name = "is_read")
    private Boolean isRead = false;  // 읽음 여부 (기본값 false, RDB 영구 저장용)
    
    @Column(nullable = false, name = "is_blocked")
    private Boolean isBlocked = false;  // 차단 여부 (개인정보 포함 메시지)
    
    @Column(name = "block_reason", length = 200)
    private String blockReason;  // 차단 사유 (개인정보 유형 등)
    
    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;
    
    @Builder
    public ChatMessage(
            Long chatRoomId,
            Long senderId,
            String senderNickname,
            MessageType messageType,
            String content,
            String imageUrl,
            Boolean isBlocked,
            String blockReason
    ) {
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.senderNickname = senderNickname;
        this.messageType = messageType;
        this.content = content;
        this.imageUrl = imageUrl;
        this.isRead = false;
        this.isBlocked = isBlocked != null ? isBlocked : false;
        this.blockReason = blockReason;
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * 읽음 처리
     * 
     * 메시지를 읽음 상태로 변경합니다.
     * Redis에서 관리하는 읽음 상태와 동기화할 때 사용합니다.
     */
    public void markAsRead() {
        this.isRead = true;
    }
}
