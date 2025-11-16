package org.cmarket.cmarket.domain.profile.repository;

import org.cmarket.cmarket.domain.profile.model.BlockedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * BlockedUser 엔티티 레포지토리 인터페이스
 * 
 * Spring Data JPA가 자동으로 구현체를 생성합니다.
 * 메서드 이름 규칙을 따르면 쿼리가 자동 생성됩니다.
 * 
 * 주요 기능:
 * - 차단 관계 확인
 * - 차단 목록 조회 (페이지네이션)
 * - 차단 해제
 */
public interface BlockedUserRepository extends JpaRepository<BlockedUser, Long> {
    
    /**
     * 차단 목록 조회 (페이지네이션, 최신순 정렬)
     * 
     * @param blockerId 차단한 사용자 ID
     * @param pageable 페이지네이션 정보
     * @return 차단 목록 (최신순 정렬)
     */
    Page<BlockedUser> findByBlockerIdOrderByCreatedAtDesc(Long blockerId, Pageable pageable);
    
    /**
     * 차단 관계 삭제 (차단 해제)
     * 
     * @param blockerId 차단한 사용자 ID
     * @param blockedId 차단당한 사용자 ID
     */
    void deleteByBlockerIdAndBlockedId(Long blockerId, Long blockedId);
    
    /**
     * 특정 사용자 차단 관계 조회
     * 
     * 차단 관계 존재 여부 확인이 필요한 경우, 이 메서드의 결과에 대해 `isPresent()`를 사용하세요.
     * 
     * @param blockerId 차단한 사용자 ID
     * @param blockedId 차단당한 사용자 ID
     * @return 차단 관계 (없으면 Optional.empty())
     */
    Optional<BlockedUser> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId);
}

