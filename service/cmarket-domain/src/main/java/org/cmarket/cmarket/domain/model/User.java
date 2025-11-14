package org.cmarket.cmarket.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 사용자(회원) 엔티티
 * 
 * 회원 정보를 저장하는 도메인 모델입니다.
 * - 일반 회원가입과 소셜 로그인을 모두 지원
 * - 소프트 삭제 지원 (deletedAt)
 * - 이메일과 닉네임은 unique 제약조건
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(length = 255)
    private String password;  // 소셜 로그인 사용자는 null일 수 있음
    
    @Column(nullable = false, length = 10)
    private String name;
    
    @Column(nullable = false, unique = true, length = 10)
    private String nickname;
    
    @Column(nullable = false)
    private LocalDate birthDate;
    
    @Column(length = 50)
    private String addressSido;  // 시/도
    
    @Column(length = 50)
    private String addressGugun;  // 구/군
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;  // 가입 경로: LOCAL, GOOGLE, KAKAO
    
    @Column(length = 100)
    private String socialId;  // 소셜 로그인 ID (provider가 LOCAL이면 null)
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column
    private LocalDateTime deletedAt;  // 소프트 삭제용 (null이면 활성, 값이 있으면 삭제됨)
    
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private WithdrawalReasonType withdrawalReason;  // 탈퇴 사유 (탈퇴 시에만 값이 있음)
    
    @Column(length = 500)
    private String withdrawalDetailReason;  // 탈퇴 상세 사유 (선택, 2~500자)
    
    @Builder
    public User(
            String email,
            String password,
            String name,
            String nickname,
            LocalDate birthDate,
            String addressSido,
            String addressGugun,
            UserRole role,
            AuthProvider provider,
            String socialId
    ) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.birthDate = birthDate;
        this.addressSido = addressSido;
        this.addressGugun = addressGugun;
        this.role = role != null ? role : UserRole.USER;
        this.provider = provider;
        this.socialId = socialId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 비밀번호 변경
     */
    public void changePassword(String newPassword) {
        this.password = newPassword;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 소프트 삭제 처리 (탈퇴 사유 포함)
     */
    public void softDelete(WithdrawalReasonType withdrawalReason, String withdrawalDetailReason) {
        this.deletedAt = LocalDateTime.now();
        this.withdrawalReason = withdrawalReason;
        this.withdrawalDetailReason = withdrawalDetailReason;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 소프트 삭제 처리 (탈퇴 사유 없이)
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 삭제 여부 확인
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }
    
    /**
     * 정보 업데이트
     */
    public void updateInfo(String name, String nickname, String addressSido, String addressGugun) {
        this.name = name;
        this.nickname = nickname;
        this.addressSido = addressSido;
        this.addressGugun = addressGugun;
        this.updatedAt = LocalDateTime.now();
    }
}

