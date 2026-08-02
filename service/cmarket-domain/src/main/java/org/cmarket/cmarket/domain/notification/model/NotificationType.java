package org.cmarket.cmarket.domain.notification.model;

/**
 * 알림 타입 Enum
 *
 * CHAT_NEW_ROOM: 새로운 채팅이 생성되었을 때
 * CHAT_NEW_MESSAGE: 새로운 메시지가 도착했을 때
 * PRODUCT_FAVORITE_STATUS_CHANGED: 찜한 상품 거래 상태 변경
 * PRODUCT_FAVORITE_PRICE_CHANGED: 찜한 상품 가격 변동
 * ADMIN_SANCTION: 어드민 제재 알림 (보존)
 * POST_DELETED: 내 게시글이 삭제 당했을 때
 * COMMENT_REPLY: 내가 작성한 댓글에 댓글이 달렸을 경우
 * POST_COMMENT: 내가 작성한 게시글에 댓글이 달렸을 경우
 *
 * autoDeletable: 자동 삭제 정책 대상 여부.
 *   - true: 시간 기준으로 자동 삭제 (NotificationCleanupScheduler)
 *   - false: 거래/이력성 알림이라 영구 보존
 */
public enum NotificationType {
    CHAT_NEW_ROOM(true),
    CHAT_NEW_MESSAGE(true),
    // 찜한 상품의 상태 변경도 자동 삭제 대상이다.
    //
    // 예전에는 false(영구 보존)였다. 「거래/이력성」이라는 이유였는데, 이건 **내가 한
    // 거래가 아니라 남이 자기 물건 상태를 바꾼 것**이다. 내 이력이 아니다.
    // 바로 아래 PRODUCT_FAVORITE_PRICE_CHANGED와도 어긋났다 — 둘 다 「찜한 물건에
    // 뭔가 바뀜」인데 한쪽만 영원히 남았다.
    //
    // 실제로 상품 하나를 예약 걸었다 풀기를 반복하면 그 기록이 그대로 쌓여
    // 알림 목록이 그 물건 하나로 채워졌다 (2026-08-02 실기기에서 다섯 줄).
    PRODUCT_FAVORITE_STATUS_CHANGED(true),
    PRODUCT_FAVORITE_PRICE_CHANGED(true),
    ADMIN_SANCTION(false),
    POST_DELETED(true),
    COMMENT_REPLY(true),
    POST_COMMENT(true);

    private final boolean autoDeletable;

    NotificationType(boolean autoDeletable) {
        this.autoDeletable = autoDeletable;
    }

    public boolean isAutoDeletable() {
        return autoDeletable;
    }
}
