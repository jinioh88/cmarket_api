package org.cmarket.cmarket.domain.eventfeed.model;

/**
 * 행사 수집 기록 상태
 * 
 * 게시했거나, 왜 건너뛰었는지를 남긴다. 건너뛴 글도 기록해야 다음 날 다시 묻지 않는다.
 */
public enum EventIngestStatus {
    POSTED,
    SKIPPED_NOT_EVENT,
    SKIPPED_ENDED,
    SKIPPED_NO_DATE,
    SKIPPED_NO_TEXT,
    SKIPPED_DUPLICATE
}
