package org.cmarket.cmarket.domain.report.repository;

import org.cmarket.cmarket.domain.report.model.UserBlock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * UserBlock 엔티티 레포지토리 인터페이스
 * 
 * Spring Data JPA가 자동으로 구현체를 생성합니다.
 * 메서드 이름 규칙을 따르면 쿼리가 자동 생성됩니다.
 * 
 * 주요 기능:
 * - 차단 관계 확인
 * - 차단 저장
 * - 차단 목록 조회
 * - 차단 해제
 */
public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {
    
    /**
     * 차단 관계 존재 확인
     * 
     * 특정 사용자가 다른 사용자를 차단했는지 확인합니다.
     * 
     * @param blockerId 차단한 사용자 ID
     * @param blockedUserId 차단당한 사용자 ID
     * @return 차단 관계가 존재하면 true, 아니면 false
     */
    boolean existsByBlockerIdAndBlockedUserId(Long blockerId, Long blockedUserId);
    
    /**
     * 차단 관계 조회
     * 
     * @param blockerId 차단한 사용자 ID
     * @param blockedUserId 차단당한 사용자 ID
     * @return 차단 관계 (없으면 Optional.empty())
     */
    Optional<UserBlock> findByBlockerIdAndBlockedUserId(Long blockerId, Long blockedUserId);
    
    /**
     * 차단 목록 조회 (페이지네이션, 최신순 정렬)
     * 
     * @param blockerId 차단한 사용자 ID
     * @param pageable 페이지네이션 정보
     * @return 차단 목록 (최신순 정렬)
     */
    Page<UserBlock> findByBlockerIdOrderByCreatedAtDesc(Long blockerId, Pageable pageable);
    
    /**
     * 차단 해제 (차단 관계 삭제)
     * 
     * @param blockerId 차단한 사용자 ID
     * @param blockedUserId 차단당한 사용자 ID
     */
    void deleteByBlockerIdAndBlockedUserId(Long blockerId, Long blockedUserId);
    
    /**
     * 내가 차단한 사용자 ID만 뽑는다 (페이지 없이 전부)
     * 
     * 상품 목록에서 차단한 사람의 상품을 빼는 데 쓴다. 목록 한 페이지를 그릴 때마다
     * 필요하므로 엔티티 전체가 아니라 ID만 가져온다.
     * 
     * @param blockerId 차단한 사용자 ID
     * @return 차단당한 사용자 ID 목록 (없으면 빈 목록)
     */
    @Query("SELECT ub.blockedUserId FROM UserBlock ub WHERE ub.blockerId = :blockerId")
    List<Long> findBlockedUserIdsByBlockerId(@Param("blockerId") Long blockerId);
}

