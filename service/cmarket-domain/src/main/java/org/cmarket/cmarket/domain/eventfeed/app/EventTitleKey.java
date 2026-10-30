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

    /**
     * 같은 행사를 철자만 달리 쓴 것인가 (2026-10-30 「크리쳐 … 페스티발 - 대전」 / 「크리처 … 페스티벌」)
     * 
     * 한쪽이 다른 쪽에 들어 있거나, 같은 글자 비율(2 × 공통부분 길이 / 두 길이 합)이 0.8 이상이면 비슷하다.
     * 호출하는 쪽이 시작일과 지역이 같은 것끼리만 견준다(다른 행사가 합쳐지지 않게).
     */
    public static boolean isSimilar(String a, String b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) {
            return false;
        }
        if (a.contains(b) || b.contains(a)) {
            return true;
        }
        return 2.0 * longestCommonSubsequence(a, b) / (a.length() + b.length()) >= SIMILAR_RATIO;
    }

    private static final double SIMILAR_RATIO = 0.8;

    private static int longestCommonSubsequence(String a, String b) {
        int[][] table = new int[a.length() + 1][b.length() + 1];
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                table[i][j] = a.charAt(i - 1) == b.charAt(j - 1)
                        ? table[i - 1][j - 1] + 1
                        : Math.max(table[i - 1][j], table[i][j - 1]);
            }
        }
        return table[a.length()][b.length()];
    }
}
