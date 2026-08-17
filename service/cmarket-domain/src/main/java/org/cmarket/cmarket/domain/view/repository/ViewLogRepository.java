package org.cmarket.cmarket.domain.view.repository;

import org.cmarket.cmarket.domain.view.model.ViewLog;
import org.cmarket.cmarket.domain.view.model.ViewTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

/**
 * ViewLog 엔티티 레포지토리 인터페이스
 *
 * Spring Data JPA가 자동으로 구현체를 생성합니다.
 * 메서드 이름 규칙을 따르면 쿼리가 자동 생성됩니다.
 *
 * 주요 기능:
 * - 오늘 이미 본 기록이 있는지 확인
 */
public interface ViewLogRepository extends JpaRepository<ViewLog, Long> {

    /**
     * 오늘 이미 본 기록이 있는지 확인
     *
     * 네 컬럼이 ViewLog의 unique 제약조건(uk_viewer_target_date)과 같은 순서라
     * 그 인덱스를 그대로 탑니다.
     *
     * @param viewerId 본 사람의 사용자 ID
     * @param targetType 대상 종류 (상품인지 글인지)
     * @param targetId 대상 ID
     * @param viewDate 본 날짜 (한국 시간 기준)
     * @return 기록이 있으면 true, 없으면 false
     */
    boolean existsByViewerIdAndTargetTypeAndTargetIdAndViewDate(
            Long viewerId,
            ViewTargetType targetType,
            Long targetId,
            LocalDate viewDate
    );
}
