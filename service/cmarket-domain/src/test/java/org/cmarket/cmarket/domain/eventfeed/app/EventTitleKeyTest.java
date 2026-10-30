package org.cmarket.cmarket.domain.eventfeed.app;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventTitleKeyTest {

    @Test
    void 연도_위치와_괄호만_다르면_같은_열쇠() {
        assertThat(EventTitleKey.normalize("2026 서울 댕냥데이즈")).isEqualTo("서울댕냥데이즈");
        assertThat(EventTitleKey.normalize("서울 댕냥데이즈(2026)")).isEqualTo("서울댕냥데이즈");
    }

    @Test
    void 대괄호_연도_공백_기호가_빠진다() {
        assertThat(EventTitleKey.normalize("[2026년 노원 반려동물 문화축제] 10월 31일(토) 10시 개최"))
                .isEqualTo("노원반려동물문화축제10월31일토10시개최");
    }

    @Test
    void 영문은_소문자로_바꾼다() {
        assertThat(EventTitleKey.normalize("Pet Festa 2026")).isEqualTo("petfesta");
    }

    @Test
    void 길이는_100자_이하() {
        assertThat(EventTitleKey.normalize("가".repeat(150))).hasSize(100);
    }

    // 2026-10-30 실제: 쇼알라에 같은 박람회가 철자만 달리 두 번 올라 있었다

    @Test
    void 철자만_조금_다르면_비슷하다() {
        String a = EventTitleKey.normalize("2026 크리쳐 케어로그 파충류 페스티발 - 대전");
        String b = EventTitleKey.normalize("크리처 케어로그 파충류 페스티벌");
        assertThat(EventTitleKey.isSimilar(a, b)).isTrue();
    }

    @Test
    void 한쪽이_다른_쪽에_들어_있으면_비슷하다() {
        assertThat(EventTitleKey.isSimilar("서울댕냥데이즈", "서울댕냥데이즈반려의하루")).isTrue();
    }

    @Test
    void 다른_행사는_비슷하지_않다() {
        assertThat(EventTitleKey.isSimilar("서울펫쇼", "서울캣쇼")).isFalse();
        assertThat(EventTitleKey.isSimilar("메가주일산시즌2", "케이펫페어부산시즌2")).isFalse();
    }
}
