package org.cmarket.cmarket.domain.eventfeed.app;

/**
 * 행사명 비교 열쇠
 * 
 * 출처마다 같은 행사를 "2026 서울 댕냥데이즈", "서울 댕냥데이즈(2026)"처럼 다르게 쓴다.
 * 연도, 띄어쓰기, 기호를 빼고 비교해 같은 행사로 본다.
 */
public final class EventTitleKey {

    private static final int MAX_LENGTH = 100;

    private EventTitleKey() {
    }

    public static String normalize(String title) {
        if (title == null) {
            return "";
        }
        String key = title
                .replaceAll("[\\[(（]\\s*20\\d\\d\\s*년?\\s*[\\])）]", "")  // (2026), [2026년]
                .replaceAll("20\\d\\d\\s*년?", "")                       // 2026, 2026년
                .replaceAll("[^가-힣a-zA-Z0-9]", "")
                .toLowerCase();
        return key.length() > MAX_LENGTH ? key.substring(0, MAX_LENGTH) : key;
    }
}
