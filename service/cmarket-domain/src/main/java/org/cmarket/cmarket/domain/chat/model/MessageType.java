package org.cmarket.cmarket.domain.chat.model;

/**
 * 채팅 메시지 타입
 * 
 * 메시지의 종류를 구분하는 열거형입니다.
 * - TEXT: 텍스트 메시지
 * - IMAGE: 이미지 메시지
 * - SYSTEM: 시스템 메시지 (입장/퇴장 알림 등)
 */
public enum MessageType {
    
    TEXT("텍스트"),
    IMAGE("이미지"),
    SYSTEM("시스템");
    
    private final String description;
    
    MessageType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
