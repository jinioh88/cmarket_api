package org.cmarket.cmarket.domain.model;

/**
 * 회원 탈퇴 사유 Enum
 * 
 * SERVICE_DISSATISFACTION: 서비스 불만족
 * PRIVACY_CONCERN: 개인정보 우려
 * LOW_USAGE: 사용 빈도 낮음
 * COMPETITOR: 경쟁 서비스 이용
 * OTHER: 기타
 */
public enum WithdrawalReasonType {
    SERVICE_DISSATISFACTION,  // 서비스 불만족
    PRIVACY_CONCERN,           // 개인정보 우려
    LOW_USAGE,                 // 사용 빈도 낮음
    COMPETITOR,                // 경쟁 서비스 이용
    OTHER                      // 기타
}

