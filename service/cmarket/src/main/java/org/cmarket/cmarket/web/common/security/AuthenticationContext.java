package org.cmarket.cmarket.web.common.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 인증 컨텍스트 정보를 담는 클래스
 * 현재 인증된 사용자 정보를 저장
 */
@Getter
@RequiredArgsConstructor
public class AuthenticationContext {
    private final Long userId;
    private final String username;
    // 필요시 추가 필드 확장 가능
}

