package org.cmarket.cmarket.domain.auth.model;

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
    KAKAO;

    /**
     * 사용자에게 보여줄 이름.
     *
     * 안내 문구에 쓴다 — 「소셜 로그인 사용자는…」처럼 뭉뚱그리면 화면이 「카카오·구글로
     * 가입한 계정이에요」라고밖에 못 쓴다. 정작 그 사람이 알아야 할 것은 **어느 쪽으로
     * 가입했는가**다. 기억이 안 나서 비밀번호 찾기까지 온 사람에게 「아, 카카오로 했었지」를
     * 되살려 주는 것이 그 화면의 일이다.
     */
    public String displayName() {
        switch (this) {
            case GOOGLE:
                return "구글";
            case KAKAO:
                return "카카오";
            default:
                return "이메일";
        }
    }
}

