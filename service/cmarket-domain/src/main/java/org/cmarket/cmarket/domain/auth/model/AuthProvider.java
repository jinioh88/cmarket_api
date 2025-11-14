package org.cmarket.cmarket.domain.model;

/**
 * 인증 제공자 Enum (가입 경로)
 * 
 * LOCAL: 일반 회원가입 (이메일/비밀번호)
 * GOOGLE: 구글 소셜 로그인
 * KAKAO: 카카오 소셜 로그인
 */
public enum AuthProvider {
    LOCAL,
    GOOGLE,
    KAKAO
}

