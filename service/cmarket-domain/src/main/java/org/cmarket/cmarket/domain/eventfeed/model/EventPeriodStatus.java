package org.cmarket.cmarket.domain.eventfeed.model;

import java.time.LocalDate;

/**
 * 행사 진행 상태 (예정, 진행 중, 종료)
 * 
 * 저장하지 않고 조회할 때 「오늘」과 기간을 비교해 정한다. 저장하면 날마다 바꿔 줘야 한다.
 * ⚠️ 웹 상태 칩(cuddle-market src/lib/utils/eventStatus.ts)과 경계가 같아야 한다.
 */
public enum EventPeriodStatus {
    UPCOMING,
    ONGOING,
    ENDED;

    /**
     * 시작일이 없으면 null (상태를 정할 수 없다). 종료일이 없으면 시작일 하루짜리로 본다.
     */
    public static EventPeriodStatus of(LocalDate startDate, LocalDate endDate, LocalDate today) {
        if (startDate == null) {
            return null;
        }
        LocalDate end = endDate != null ? endDate : startDate;
        if (today.isBefore(startDate)) {
            return UPCOMING;
        }
        if (!today.isAfter(end)) {
            return ONGOING;
        }
        return ENDED;
    }
}
