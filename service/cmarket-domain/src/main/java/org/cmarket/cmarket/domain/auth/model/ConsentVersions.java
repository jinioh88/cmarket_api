package org.cmarket.cmarket.domain.auth.model;

/**
 * 동의를 받은 문서의 판 (#1088)
 *
 * ⚠️ **판 표기를 시행일로 쓴다.** 문서 화면에 실제로 적히는 것이 시행일이라
 *    (「이 약관은 2026년 9월 1일부터 적용합니다」), 사람이 화면을 보고 대조할 수 있다.
 *    "v1" 같은 표기는 그 대조를 못 한다.
 *
 * ⚠️ **문서를 고치면 여기도 같이 고쳐야 한다.** 안 고치면 옛 판에 동의한 것으로 기록된다.
 *    두 문서의 시행일이 **이미 다르므로** 칸도 따로 둔다.
 */
public final class ConsentVersions {

    /** 이용약관 시행일 — src/app/(main)/terms/page.tsx 의 EFFECTIVE_DATE */
    public static final String TERMS = "2026-08-25";

    /** 개인정보처리방침 시행일 */
    public static final String PRIVACY = "2026-07-30";

    private ConsentVersions() {
    }
}
