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
}
