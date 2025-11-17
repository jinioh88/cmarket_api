package org.cmarket.cmarket.domain.product.model;

/**
 * 거래 상태 Enum
 * 
 * 판매 상품의 경우:
 * - SELLING: 판매중
 * - RESERVED: 예약중
 * - COMPLETED: 거래완료
 * 
 * 판매 요청의 경우:
 * - BUYING: 삽니다
 * - RESERVED: 예약중
 * - COMPLETED: 거래완료
 */
public enum TradeStatus {
    SELLING,    // 판매중 (판매 상품용)
    BUYING,     // 삽니다 (판매 요청용)
    RESERVED,   // 예약중 (공통)
    COMPLETED   // 거래완료 (공통)
}

