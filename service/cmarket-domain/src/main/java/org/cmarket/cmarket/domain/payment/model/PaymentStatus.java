package org.cmarket.cmarket.domain.payment.model;

/**
 * 결제 기록 상태 (#1126)
 */
public enum PaymentStatus {
    READY,   // 주문만 만들었다 (결제창을 열기 전)
    DONE,    // 토스가 승인했다
    FAILED   // 승인이 거절됐다
}
