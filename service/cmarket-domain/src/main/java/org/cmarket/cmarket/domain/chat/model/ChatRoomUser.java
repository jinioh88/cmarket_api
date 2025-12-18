package org.cmarket.cmarket.domain.chat.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 채팅방 참여자 엔티티
 * 
 * 채팅방의 참여자 정보를 저장하는 도메인 모델입니다.
 * - 사용자 정보는 참여 시점의 스냅샷으로 저장
 * - 채팅방 나가기는 소프트 삭제 (isActive = false)
 * - 나간 사용자의 채팅방 목록에서는 제외됨
 * - 최근 메시지 정보 비정규화 저장 (목록 조회 성능 최적화)
 */
@Entity
@Table(
    name = "chat_room_users",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_chat_room_user",
            columnNames = {"chat_room_id", "user_id"}
        )
    },
    indexes = {
        @Index(name = "idx_chat_room_user_chat_room_id", columnList = "chat_room_id"),
        @Index(name = "idx_chat_room_user_user_id", columnList = "user_id"),
        @Index(name = "idx_chat_room_user_last_message_at", columnList = "last_message_at")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomUser {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, name = "chat_room_id")
    private Long chatRoomId;  // 채팅방 ID (ChatRoom 참조)
    
    @Column(nullable = false, name = "user_id")
    private Long userId;  // 사용자 ID (User 참조)
    
    @Column(nullable = false, name = "user_nickname", length = 10)
    private String userNickname;  // 사용자 닉네임 (참여 시점 스냅샷)
    
    @Column(name = "user_profile_image_url", length = 500)
    private String userProfileImageUrl;  // 사용자 프로필 이미지 (참여 시점 스냅샷)
    
    @Column(nullable = false, name = "is_active")
    private Boolean isActive = true;  // 활성 여부 (기본값 true)
    
    @Column(name = "left_at")
    private LocalDateTime leftAt;  // 나간 시간 (채팅방 나갔을 때 설정)
    
    // 최근 메시지 정보 (비정규화 - 목록 조회 성능 최적화)
    @Column(name = "last_message_content", length = 100)
    private String lastMessageContent;  // 최근 메시지 내용 (100자 미리보기)
    
    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;  // 최근 메시지 시간
    
    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Builder
    public ChatRoomUser(
            Long chatRoomId,
            Long userId,
            String userNickname,
            String userProfileImageUrl
    ) {
        this.chatRoomId = chatRoomId;
        this.userId = userId;
        this.userNickname = userNickname;
        this.userProfileImageUrl = userProfileImageUrl;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 채팅방 나가기
     * 
     * isActive를 false로 설정하고 leftAt에 현재 시간을 기록합니다.
     * 나간 사용자의 채팅방 목록에서는 해당 채팅방이 제외됩니다.
     */
    public void leave() {
        this.isActive = false;
        this.leftAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 나간 여부 확인
     * 
     * @return 채팅방을 나갔으면 true, 아니면 false
     */
    public boolean isLeft() {
        return !this.isActive && this.leftAt != null;
    }
    
    /**
     * 업데이트 시간 갱신
     */
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 최근 메시지 정보 업데이트
     * 
     * 메시지 전송 시 호출하여 채팅방의 최근 메시지 정보를 업데이트합니다.
     * 차단된 메시지는 "[차단된 메시지]"로 표시됩니다.
     * 
     * @param content 메시지 내용 (100자 초과 시 자동 절삭)
     * @param messageTime 메시지 시간
     * @param isBlocked 차단 여부
     */
    public void updateLastMessage(String content, LocalDateTime messageTime, boolean isBlocked) {
        if (isBlocked) {
            this.lastMessageContent = "[차단된 메시지]";
        } else {
            // 100자 초과 시 절삭
            this.lastMessageContent = content != null && content.length() > 100 
                    ? content.substring(0, 100) 
                    : content;
        }
        this.lastMessageAt = messageTime;
        this.updatedAt = LocalDateTime.now();
    }
}
