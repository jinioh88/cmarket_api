package org.cmarket.cmarket.domain.eventfeed.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 행사 수집 기록 엔티티
 * 
 * 크롤러가 본 출처 글 하나당 한 줄. (출처 코드, 출처 글 번호)는 겹치지 않는다.
 * 행사명 열쇠와 시작일은 출처가 달라도 같은 행사를 한 번만 올리려고 둔다.
 */
@Entity
@Table(
    name = "event_ingest_record",
    uniqueConstraints = @UniqueConstraint(
            name = "uk_event_ingest_source_external",
            columnNames = {"source_code", "external_id"}
    ),
    indexes = @Index(name = "idx_event_ingest_title_start", columnList = "title_key, event_start_date")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventIngestRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "source_code", length = 30)
    private String sourceCode;

    @Column(nullable = false, name = "external_id", length = 100)
    private String externalId;

    // ⚠️ columnDefinition 으로 문자열 칸을 못 박는다. 하이버네이트 6 은 MySQL 에서
    // enum 을 ENUM('…') 칸으로 만들어, 값을 더하면 운영에서 "Data truncated" 가 난다
    // (scripts/alter-pet-type-columns.sql 이 그 흔적이다).
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30, columnDefinition = "varchar(30)")
    private EventIngestStatus status;

    @Column(name = "post_id")
    private Long postId;

    @Column(nullable = false, name = "source_url", length = 500)
    private String sourceUrl;

    @Column(name = "title_key", length = 100)
    private String titleKey;

    @Column(name = "event_start_date")
    private LocalDate eventStartDate;

    @Column(name = "event_end_date")
    private LocalDate eventEndDate;

    // 목록에 보이는 넓은 지역(서울, 제주 등). 상세에는 장소와 같이 보인다
    @Column(length = 20)
    private String region;

    @Column(length = 100)
    private String place;

    @Column(length = 100)
    private String organizer;

    // 한 줄 소개 (AI가 쓴다. 없을 수 있다)
    @Column(length = 100)
    private String summary;

    @Column(nullable = false, name = "collected_at")
    private LocalDateTime collectedAt;

    @Builder
    public EventIngestRecord(
            String sourceCode,
            String externalId,
            EventIngestStatus status,
            Long postId,
            String sourceUrl,
            String titleKey,
            LocalDate eventStartDate,
            LocalDate eventEndDate,
            String region,
            String place,
            String organizer,
            String summary
    ) {
        this.sourceCode = sourceCode;
        this.externalId = externalId;
        this.status = status;
        this.postId = postId;
        this.sourceUrl = sourceUrl;
        this.titleKey = titleKey;
        this.eventStartDate = eventStartDate;
        this.eventEndDate = eventEndDate;
        this.region = region;
        this.place = place;
        this.organizer = organizer;
        this.summary = summary;
        this.collectedAt = LocalDateTime.now();
    }
}
