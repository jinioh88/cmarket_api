package org.cmarket.cmarket.domain.report.app.service;

/**
 * 사용자 차단 조회 서비스.
 * 
 * 교차 도메인에서 차단 여부를 확인할 때 사용합니다.
 */
public interface UserBlockQueryService {

    /**
     * 차단 여부 확인 (단방향).
     *
     * @param blockerId 차단한 사용자 ID
     * @param targetUserId 상대 사용자 ID
     * @return 차단되어 있으면 true
     */
    boolean isBlocked(Long blockerId, Long targetUserId);

    /**
     * 양방향 차단 여부 확인.
     * 
     * A가 B를 차단했거나, B가 A를 차단한 경우 true를 반환합니다.
     * 서로의 콘텐츠(게시글/댓글)에 대한 상호작용을 차단할 때 사용합니다.
     *
     * @param userId1 사용자 ID 1
     * @param userId2 사용자 ID 2
     * @return 양방향 중 하나라도 차단되어 있으면 true
     */
    boolean isBlockedBidirectional(Long userId1, Long userId2);
}

