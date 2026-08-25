package org.cmarket.cmarket.domain.auth.app.service;

import org.cmarket.cmarket.domain.auth.model.AuthProvider;

/**
 * 이메일 발송 서비스 인터페이스
 * 
 * 주요 기능:
 * - 인증코드 발송: 회원가입 및 비밀번호 재설정 시 사용
 * 
 * 구현체는 웹 계층에 위치합니다.
 */
public interface EmailService {
    
    /**
     * 이메일 인증코드 발송
     * 
     * @param to 수신자 이메일 주소
     * @param verificationCode 인증코드 (6자리 숫자)
     */
    void sendVerificationCode(String to, String verificationCode);

    /**
     * 가입 방법 안내 메일 (#849 계정 찾기)
     *
     * ⚠️ **화면이 아니라 메일로 알려주는 것이 핵심이다.** 「이 이메일은 카카오로 가입했다」를
     *    화면이 말하면 남의 이메일을 넣어 본 사람도 알게 된다(계정 열거). 메일함을 여는
     *    사람은 그 이메일의 주인뿐이라, 메일로 보내면 주인에게만 닿는다.
     *
     * ⚠️ 인자를 문자열이 아니라 enum 으로 받는다. 문안이 「이메일로 가입」과 「소셜로 가입」
     *    두 갈래라 displayName() 문자열을 다시 비교해야 하는데, 그러면 그 한글이 바뀔 때
     *    조용히 갈래가 무너진다.
     *
     * @param to       수신자 이메일
     * @param provider 가입 경로 (LOCAL · GOOGLE · KAKAO)
     */
    void sendAccountMethodNotice(String to, AuthProvider provider);
}

