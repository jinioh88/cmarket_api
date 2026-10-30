package org.cmarket.cmarket.domain.eventfeed.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 웹의 상태 칩(src/lib/utils/eventStatus.ts)과 같은 경계여야 한다.
 * 다르면 「진행 중」으로 걸렀는데 「종료」 칩이 섞여 보인다.
 */
class EventPeriodStatusTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 30);

    private static LocalDate d(int month, int day) {
        return LocalDate.of(2026, month, day);
    }

    @Test
    void 시작_전이면_예정() {
        assertThat(EventPeriodStatus.of(d(10, 31), d(11, 2), TODAY)).isEqualTo(EventPeriodStatus.UPCOMING);
    }

    @Test
    void 시작일부터_종료일까지_진행_중() {
        assertThat(EventPeriodStatus.of(d(10, 30), d(11, 2), TODAY)).isEqualTo(EventPeriodStatus.ONGOING);
        assertThat(EventPeriodStatus.of(d(10, 25), d(10, 30), TODAY)).isEqualTo(EventPeriodStatus.ONGOING);
    }

    @Test
    void 종료일이_없으면_시작일_하루만_진행_중() {
        assertThat(EventPeriodStatus.of(d(10, 30), null, TODAY)).isEqualTo(EventPeriodStatus.ONGOING);
        assertThat(EventPeriodStatus.of(d(10, 29), null, TODAY)).isEqualTo(EventPeriodStatus.ENDED);
    }

    @Test
    void 종료일이_지나면_종료() {
        assertThat(EventPeriodStatus.of(d(10, 20), d(10, 29), TODAY)).isEqualTo(EventPeriodStatus.ENDED);
    }

    @Test
    void 시작일이_없으면_상태를_정할_수_없다() {
        assertThat(EventPeriodStatus.of(null, null, TODAY)).isNull();
    }
}
