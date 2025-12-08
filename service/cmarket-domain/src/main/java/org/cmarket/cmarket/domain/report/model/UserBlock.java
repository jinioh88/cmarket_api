package org.cmarket.cmarket.domain.report.model;

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
 * 사용자 차단 관계.
 */
@Entity
@Table(
    name = "user_blocks",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_blocker_blocked_user",
            columnNames = {"blocker_id", "blocked_user_id"}
        )
    },
    indexes = {
        @Index(name = "idx_blocker_id", columnList = "blocker_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "blocker_id")
    private Long blockerId;

    @Column(nullable = false, name = "blocked_user_id")
    private Long blockedUserId;

    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Builder
    public UserBlock(Long blockerId, Long blockedUserId) {
        this.blockerId = blockerId;
        this.blockedUserId = blockedUserId;
        this.createdAt = LocalDateTime.now();
    }
}

