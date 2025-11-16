package org.cmarket.cmarket.domain.profile.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.time.LocalDateTime;

/**
 * 사용자 차단 관계 엔티티
 * 
 * 사용자가 다른 사용자를 차단한 관계를 저장합니다.
 * - blockerId: 차단한 사용자 ID
 * - blockedId: 차단당한 사용자 ID
 * - 동일 사용자 중복 차단 방지 (복합 unique 제약조건)
 * - 차단 목록 조회 성능 향상을 위해 blockerId에 인덱스 추가
 */
@Entity
@Table(
    name = "blocked_users",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_blocker_blocked",
            columnNames = {"blocker_id", "blocked_id"}
        )
    },
    indexes = {
        @Index(name = "idx_blocker_id", columnList = "blocker_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlockedUser {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, name = "blocker_id")
    private Long blockerId;  // 차단한 사용자 ID
    
    @Column(nullable = false, name = "blocked_id")
    private Long blockedId;  // 차단당한 사용자 ID
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Builder
    public BlockedUser(
            Long blockerId,
            Long blockedId
    ) {
        this.blockerId = blockerId;
        this.blockedId = blockedId;
        this.createdAt = LocalDateTime.now();
    }
}

