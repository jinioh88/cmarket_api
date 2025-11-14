package org.cmarket.cmarket.domain.repository;

import org.cmarket.cmarket.domain.model.AuthProvider;
import org.cmarket.cmarket.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * User 엔티티 레포지토리 인터페이스
 * 
 * Spring Data JPA가 자동으로 구현체를 생성합니다.
 * 메서드 이름 규칙을 따르면 쿼리가 자동 생성됩니다.
 * 
 * 주요 기능:
 * - 이메일로 사용자 조회 (소프트 삭제 제외)
 * - 닉네임/이메일 중복 확인
 * - 소셜 로그인용 사용자 조회
 */
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 이메일로 사용자 조회 (소프트 삭제된 사용자 제외)
     * 
     * @param email 이메일 주소
     * @return 사용자 (없으면 Optional.empty())
     */
    Optional<User> findByEmailAndDeletedAtIsNull(String email);
    
    /**
     * 닉네임 중복 확인
     * 
     * @param nickname 닉네임
     * @return 중복이면 true, 아니면 false
     */
    boolean existsByNickname(String nickname);
    
    /**
     * 이메일 중복 확인
     * 
     * @param email 이메일 주소
     * @return 중복이면 true, 아니면 false
     */
    boolean existsByEmail(String email);
    
    /**
     * 소셜 로그인용 사용자 조회 (provider와 socialId로 조회)
     * 
     * @param provider 인증 제공자 (GOOGLE, KAKAO)
     * @param socialId 소셜 로그인 ID
     * @return 사용자 (없으면 Optional.empty())
     */
    Optional<User> findByProviderAndSocialId(AuthProvider provider, String socialId);
}

