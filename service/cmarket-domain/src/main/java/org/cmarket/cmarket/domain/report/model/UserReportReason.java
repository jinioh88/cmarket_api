package org.cmarket.cmarket.domain.report.model;

/**
 * 사용자 신고 사유.
 */
public enum UserReportReason {
    HARASSMENT,              // 욕설, 비방, 괴롭힘
    FRAUD,                   // 사기, 허위 거래 시도
    INAPPROPRIATE_CONTENT,   // 음란물 또는 불건전 행위
    SPAM,                    // 스팸/광고성 메시지
    OFFENSIVE_PROFILE,       // 불괘한 사용자 정보 내용
    UNDERAGE,                // 만 14세 미만 유저입니다
    OTHER                    // 기타
}

