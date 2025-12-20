package org.cmarket.cmarket.domain.notification.model;

/**
 * 알림 타입 Enum
 * 
 * CHAT_NEW_ROOM: 새로운 채팅이 생성되었을 때
 * CHAT_NEW_MESSAGE: 새로운 메시지가 도착했을 때
 * PRODUCT_FAVORITE_STATUS_CHANGED: 찜한 상품 거래 상태 변경
 * PRODUCT_FAVORITE_PRICE_CHANGED: 찜한 상품 가격 변동
 * ADMIN_SANCTION: 어드민 재제 알림
 * POST_DELETED: 내 게시글이 삭제 당했을 때
 * COMMENT_REPLY: 내가 작성한 댓글에 댓글이 달렸을 경우
 * POST_COMMENT: 내가 작성한 게시글에 댓글이 달렸을 경우
 */
public enum NotificationType {
    CHAT_NEW_ROOM,
    CHAT_NEW_MESSAGE,
    PRODUCT_FAVORITE_STATUS_CHANGED,
    PRODUCT_FAVORITE_PRICE_CHANGED,
    ADMIN_SANCTION,
    POST_DELETED,
    COMMENT_REPLY,
    POST_COMMENT
}
