package org.cmarket.cmarket.domain.view.model;

/**
 * 조회 기록 대상 종류.
 *
 * 신고(ReportTargetType)와 같은 결로, 대상 종류와 대상 id를 함께 두어
 * 상품과 커뮤니티 글을 표 하나에 담습니다.
 */
public enum ViewTargetType {
    PRODUCT,
    COMMUNITY_POST
}
