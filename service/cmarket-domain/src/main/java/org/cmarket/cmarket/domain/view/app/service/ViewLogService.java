package org.cmarket.cmarket.domain.view.app.service;

import org.cmarket.cmarket.domain.view.model.ViewTargetType;

/**
 * 조회 기록 서비스.
 *
 * 교차 도메인(상품·커뮤니티)에서 「오늘 처음 보는 것인가」를 판단할 때 사용합니다.
 */
public interface ViewLogService {

    /**
     * 오늘 처음 본 것이면 기록을 남기고 true를 돌려준다.
     *
     * 부르는 쪽은 true일 때만 조회수를 올리면 됩니다.
     * 이미 오늘 본 기록이 있으면 아무것도 하지 않고 false를 돌려줍니다.
     *
     * ⚠️ 기록을 남기는 일과 조회수를 올리는 일은 <b>같은 트랜잭션</b>에서 일어나야 합니다.
     *    따로 떼면 「기록은 남았는데 조회수는 안 올랐다」가 되어,
     *    그 사람은 그날 다시는 세지지 않습니다. 그래서 여기서 트랜잭션을 새로 열지 않고
     *    부르는 쪽의 트랜잭션에 얹힙니다(기본 전파 REQUIRED).
     *
     * @param viewerId 본 사람의 사용자 ID (비로그인이면 null)
     * @param targetType 대상 종류 (상품인지 글인지)
     * @param targetId 대상 ID
     * @return 오늘 처음이면 true, 이미 봤으면 false
     */
    boolean markViewedToday(Long viewerId, ViewTargetType targetType, Long targetId);
}
