package org.cmarket.cmarket.domain.eventfeed.repository;

import org.cmarket.cmarket.domain.eventfeed.model.EventIngestRecord;
import org.cmarket.cmarket.domain.eventfeed.model.EventIngestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * 행사 수집 기록 Repository
 */
public interface EventIngestRecordRepository extends JpaRepository<EventIngestRecord, Long> {

    List<EventIngestRecord> findBySourceCodeAndExternalIdIn(String sourceCode, Collection<String> externalIds);

    List<EventIngestRecord> findByStatus(EventIngestStatus status);

    List<EventIngestRecord> findByStatusAndEventStartDate(EventIngestStatus status, LocalDate eventStartDate);

    List<EventIngestRecord> findByStatusAndPostIdIn(EventIngestStatus status, Collection<Long> postIds);

    boolean existsBySourceCodeAndExternalId(String sourceCode, String externalId);
}
