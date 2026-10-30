package org.cmarket.cmarket.web.eventfeed;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 수집 전용 키 검사
 * 
 * 수집 주소는 로그인 토큰이 아니라 X-Ingest-Key 머리글로 연다.
 * 관리자 비밀번호를 GitHub에 두지 않으려는 선택이다.
 * 비교는 MessageDigest.isEqual로 해서 글자 수에 따라 걸리는 시간이 달라지지 않게 한다.
 */
@Component
public class IngestKeyVerifier {

    private final byte[] expected;

    public IngestKeyVerifier(@Value("${ingest.api-key:}") String apiKey) {
        this.expected = apiKey == null ? new byte[0] : apiKey.getBytes(StandardCharsets.UTF_8);
    }

    public boolean isConfigured() {
        return expected.length > 0;
    }

    public boolean verify(String presented) {
        if (!isConfigured() || presented == null || presented.isEmpty()) {
            return false;
        }
        return MessageDigest.isEqual(expected, presented.getBytes(StandardCharsets.UTF_8));
    }
}
