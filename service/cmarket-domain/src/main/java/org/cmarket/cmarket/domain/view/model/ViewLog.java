package org.cmarket.cmarket.domain.view.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 조회 기록 엔티티
 *
 * 「누가 · 무엇을 · 어느 날 봤는지」를 남깁니다.
 * 같은 사람이 같은 것을 같은 날 다시 봐도 조회수를 다시 올리지 않기 위한 표입니다.
 *
 * - viewerId와 targetType, targetId, viewDate의 복합 unique 제약조건으로 하루 한 번만 기록
 * - 상품과 커뮤니티 글을 표 하나에 담습니다 (신고 엔티티와 같은 방식)
 *
 * ⚠️ viewDate는 한국 시간(Asia/Seoul) 기준 날짜입니다.
 *    서버 JVM이 UTC로 돌기 때문에, 시간대를 못 박지 않으면 하루가 바뀌는 순간이
 *    한국 시간 아침 9시가 되어 출근 시간대에 같은 사람이 두 번 세집니다.
 *    날짜를 만드는 곳은 ViewLogServiceImpl 한 곳뿐이니 그쪽을 함께 보세요.
 */
@Entity
@Table(
    name = "view_logs",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_viewer_target_date",
            columnNames = {"viewer_id", "target_type", "target_id", "view_date"}
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ViewLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "viewer_id")
    private Long viewerId;  // 본 사람의 사용자 ID

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "target_type", length = 30)
    private ViewTargetType targetType;  // 무엇을 봤는지 (상품인지 글인지)

    @Column(nullable = false, name = "target_id")
    private Long targetId;  // 본 대상의 ID

    @Column(nullable = false, name = "view_date")
    private LocalDate viewDate;  // 본 날짜 (한국 시간 기준)

    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    // 인덱스를 따로 달지 않습니다.
    // 조회는 existsBy...(viewerId, targetType, targetId, viewDate) 하나뿐이고,
    // 위의 unique 제약조건이 그 네 컬럼을 그대로 덮는 인덱스라 이미 충분합니다.
    // 인덱스를 더 달면 상세 조회마다 일어나는 INSERT만 느려집니다.

    @Builder
    public ViewLog(
            Long viewerId,
            ViewTargetType targetType,
            Long targetId,
            LocalDate viewDate
    ) {
        this.viewerId = viewerId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.viewDate = viewDate;
        this.createdAt = LocalDateTime.now();
    }
}
