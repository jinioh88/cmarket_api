package org.cmarket.cmarket.domain.report.repository;

import org.cmarket.cmarket.domain.report.model.Report;
import org.cmarket.cmarket.domain.report.model.ReportStatus;
import org.cmarket.cmarket.domain.report.model.ReportTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Report 엔티티 레포지토리 인터페이스
 * 
 * Spring Data JPA가 자동으로 구현체를 생성합니다.
 * 메서드 이름 규칙을 따르면 쿼리가 자동 생성됩니다.
 * 
 * 주요 기능:
 * - 신고 중복 확인
 * - 신고 저장
 * - 상태/대상별 신고 목록 조회 (관리자용)
 */
public interface ReportRepository extends JpaRepository<Report, Long> {
    
    /**
     * 신고 중복 확인
     * 
     * 동일한 신고자가 동일한 대상에 대해 이미 신고했는지 확인합니다.
     * 
     * @param reporterId 신고자 ID
     * @param targetType 신고 대상 타입 (USER, PRODUCT, COMMUNITY_POST)
     * @param targetId 신고 대상 ID
     * @return 이미 신고했으면 true, 아니면 false
     */
    boolean existsByReporterIdAndTargetTypeAndTargetId(Long reporterId, ReportTargetType targetType, Long targetId);
    
    /**
     * 대상 타입 및 상태별 신고 목록 조회 (페이지네이션, 최신순 정렬)
     * 
     * @param targetType 신고 대상 타입
     * @param status 신고 상태
     * @param pageable 페이지네이션 정보
     * @return 신고 목록 (최신순 정렬)
     */
    Page<Report> findByTargetTypeAndStatusOrderByCreatedAtDesc(ReportTargetType targetType, ReportStatus status, Pageable pageable);
}

