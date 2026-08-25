package org.cmarket.cmarket.domain.auth.model;

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
    
    @Column
    private LocalDate birthDate;  // 소셜 로그인 사용자는 null일 수 있음
    
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
    
    @Column(length = 500)
    private String profileImageUrl;  // 프로필 이미지 URL (S3 저장 경로)
    
    @Column(length = 1000)
    private String introduction;  // 소개글 (최대 1000자)

    // ── 약관 동의 (#1088) ────────────────────────────────────────────────
    // ⚠️ **네 칸 다 nullable 로 둔다.** 행이 이미 있는 표에 NOT NULL 을 붙이면 MySQL 이
    //    암묵 기본값을 넣거나 실패한다. 컬럼을 먼저 만들고, 기존 행은 UPDATE 로 채운다.
    //    (ddl-auto=update 는 **컬럼만** 만든다 — 기존 행을 안 채운다)

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TermsAgreementStatus termsAgreementStatus;

    /** 동의한 시각. 약관 도입 전 가입자는 null 이다 — 받은 적이 없으니 적을 시각도 없다. */
    @Column
    private LocalDateTime termsAgreedAt;

    // ⚠️ **판을 두 칸으로 나눈다.** 이용약관과 개인정보처리방침의 시행일이 **이미 다르다.**
    //    한 칸으로 묶으면 어느 판에 동의했는지 못 적는다.
    //    (시각은 하나다 — 지금은 둘을 한 번에 받아 늘 같다)
    @Column(length = 20)
    private String agreedTermsVersion;

    @Column(length = 20)
    private String agreedPrivacyVersion;
    
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
            String socialId,
            TermsAgreementStatus termsAgreementStatus,
            LocalDateTime termsAgreedAt,
            String agreedTermsVersion,
            String agreedPrivacyVersion
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
        // ⚠️ **모르면 「동의 안 함」쪽으로 기운다.** 값을 안 넘긴 자리가 있으면 AGREED 가
        //    아니라 PRE_TERMS 가 되어야 한다 — 동의한 적 없는 사람을 동의했다고 적는 것이
        //    반대보다 훨씬 나쁘다.
        this.termsAgreementStatus =
                termsAgreementStatus != null ? termsAgreementStatus : TermsAgreementStatus.PRE_TERMS;
        this.termsAgreedAt = termsAgreedAt;
        this.agreedTermsVersion = agreedTermsVersion;
        this.agreedPrivacyVersion = agreedPrivacyVersion;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 약관 동의를 기록한다 (#1088 — 소셜 가입 마무리에서 부른다)
     *
     * ⚠️ **이미 AGREED 인 사람은 덮어쓰지 않는다.** 처음 동의한 시각이 증명에 쓰는 값이다.
     *    프로필을 고칠 때마다 시각이 밀리면 「언제 동의했나」를 못 말한다.
     *
     * ⚠️ 소셜 가입은 프로필 수정과 **같은 API** 를 쓴다. 그래서 값이 안 오면
     *    (프로필 수정) 아무것도 안 건드린다.
     */
    public void recordTermsAgreement(String termsVersion, String privacyVersion) {
        if (this.termsAgreementStatus == TermsAgreementStatus.AGREED) {
            return;
        }
        this.termsAgreementStatus = TermsAgreementStatus.AGREED;
        this.termsAgreedAt = LocalDateTime.now();
        this.agreedTermsVersion = termsVersion;
        this.agreedPrivacyVersion = privacyVersion;
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
     *
     * 개인정보 보호법 제21조에 따라 탈퇴 시 회원을 알아볼 수 있는 정보를 지운다.
     * 탈퇴 사유는 남긴다 — UserRepository의 탈퇴 사유 통계가 이 값을 집계하므로
     * 계정을 통째로 지우면 통계가 깨진다.
     *
     * email·nickname은 unique 제약이 걸려 있어 null로 두면 두 번째 탈퇴자가 저장에
     * 실패한다. id를 섞어 겹치지 않는 값으로 바꾼다.
     * name은 nullable = false 라서 null을 넣을 수 없으므로 짧은 고정값으로 둔다.
     * nickname은 length = 10 이므로 "탈퇴" + id 형태로 짧게 유지한다.
     */
    public void softDelete(WithdrawalReasonType withdrawalReason, String withdrawalDetailReason) {
        this.deletedAt = LocalDateTime.now();
        this.withdrawalReason = withdrawalReason;
        this.withdrawalDetailReason = withdrawalDetailReason;

        // 회원을 알아볼 수 있는 정보 제거
        this.email = "deleted_" + this.id + "@cuddlemarket.invalid";
        this.nickname = "탈퇴" + this.id;
        this.name = "탈퇴";
        this.password = null;
        this.birthDate = null;
        this.addressSido = null;
        this.addressGugun = null;
        this.profileImageUrl = null;
        this.introduction = null;
        this.socialId = null;

        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 소프트 삭제 처리 (탈퇴 사유 없이)
     *
     * 사유만 없을 뿐 개인정보를 지우는 처리는 같아야 하므로 위 메서드에 위임한다.
     * 따로 두면 이쪽으로 빠져나가 개인정보가 남는다.
     */
    public void softDelete() {
        softDelete(null, null);
    }
    
    /**
     * 삭제 여부 확인
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }
    
    /**
     * 삭제된 계정 복구 (재가입)
     * 
     * 소셜 로그인으로 탈퇴한 계정을 다시 활성화합니다.
     * deletedAt, withdrawalReason, withdrawalDetailReason을 초기화합니다.
     */
    public void restore() {
        this.deletedAt = null;
        this.withdrawalReason = null;
        this.withdrawalDetailReason = null;
        this.updatedAt = LocalDateTime.now();
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
    
    /**
     * 이름 업데이트
     */
    public void updateName(String name) {
        this.name = name;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 닉네임 업데이트
     */
    public void updateNickname(String nickname) {
        this.nickname = nickname;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 프로필 정보 업데이트
     * 
     * @param nickname 닉네임
     * @param birthDate 생년월일
     * @param addressSido 시/도
     * @param addressGugun 구/군
     * @param profileImageUrl 프로필 이미지 URL
     * @param introduction 소개글
     */
    public void updateProfile(
            String nickname,
            LocalDate birthDate,
            String addressSido,
            String addressGugun,
            String profileImageUrl,
            String introduction
    ) {
        this.nickname = nickname;
        this.birthDate = birthDate;
        this.addressSido = addressSido;
        this.addressGugun = addressGugun;
        this.profileImageUrl = profileImageUrl;
        this.introduction = introduction;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 소셜 계정 연동
     * 
     * 기존 일반 회원(LOCAL)에 소셜 로그인 정보를 연동합니다.
     * 이미 소셜 계정이 연동된 경우에는 업데이트하지 않습니다.
     * 
     * @param provider 소셜 로그인 제공자 (GOOGLE, KAKAO)
     * @param socialId 소셜 서비스의 고유 ID
     */
    public void linkSocialAccount(AuthProvider provider, String socialId) {
        // 이미 소셜 계정이 연동되어 있으면 업데이트하지 않음
        if (this.provider != AuthProvider.LOCAL && this.socialId != null) {
            return;
        }
        this.provider = provider;
        this.socialId = socialId;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 소셜 계정 연동 여부 확인
     */
    public boolean hasSocialAccount() {
        return this.provider != AuthProvider.LOCAL && this.socialId != null;
    }

    /**
     * 역할 변경
     *
     * @param role 변경할 역할 (USER, ADMIN)
     */
    public void changeRole(UserRole role) {
        this.role = role;
        this.updatedAt = LocalDateTime.now();
    }
}

