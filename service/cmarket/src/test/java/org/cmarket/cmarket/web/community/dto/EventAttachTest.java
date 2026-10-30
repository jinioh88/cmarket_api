package org.cmarket.cmarket.web.community.dto;

import org.cmarket.cmarket.domain.community.model.BoardType;
import org.cmarket.cmarket.domain.eventfeed.app.dto.EventInfoDto;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 커뮤니티 목록·상세 응답에 행사 칸(event)을 붙인다. 행사 글이 아니면 null로 둔다.
 */
class EventAttachTest {

    private final EventInfoDto info = new EventInfoDto(
            LocalDate.of(2026, 10, 31), LocalDate.of(2026, 11, 2),
            "서울", "문화비축기지", "서울특별시", "반려견과 함께 즐기는 가을 축제",
            "반려견", "무료", List.of("놀이터"), List.of("목줄"),
            "https://www.seoul.go.kr/news", "https://www.seoul.go.kr/a.jpg");

    private PostListItemResponse item(long id, BoardType boardType) {
        PostListItemResponse item = new PostListItemResponse();
        ReflectionTestUtils.setField(item, "id", id);
        ReflectionTestUtils.setField(item, "boardType", boardType);
        return item;
    }

    @Test
    void 목록에서_행사_글에만_행사_칸이_붙는다() {
        PostListResponse list = new PostListResponse();
        ReflectionTestUtils.setField(list, "content", List.of(item(1L, BoardType.EVENT), item(2L, BoardType.INFO)));

        assertThat(list.getPostIds()).containsExactly(1L, 2L);
        list.attachEvents(Map.of(1L, info));

        EventInfoResponse event = list.getContent().get(0).getEvent();
        assertThat(event.getStartDate()).isEqualTo(LocalDate.of(2026, 10, 31));
        assertThat(event.getEndDate()).isEqualTo(LocalDate.of(2026, 11, 2));
        assertThat(event.getRegion()).isEqualTo("서울");
        assertThat(event.getPlace()).isEqualTo("문화비축기지");
        assertThat(event.getOrganizer()).isEqualTo("서울특별시");
        assertThat(event.getSummary()).isEqualTo("반려견과 함께 즐기는 가을 축제");
        assertThat(event.getTarget()).isEqualTo("반려견");
        assertThat(event.getFee()).isEqualTo("무료");
        assertThat(event.getActivities()).containsExactly("놀이터");
        assertThat(event.getChecks()).containsExactly("목줄");
        assertThat(event.getSourceUrl()).isEqualTo("https://www.seoul.go.kr/news");
        assertThat(event.getImageUrl()).isEqualTo("https://www.seoul.go.kr/a.jpg");
        assertThat(list.getContent().get(1).getEvent()).isNull();
    }

    @Test
    void 상세에서_기록이_있으면_붙고_없으면_null() {
        PostDetailResponse detail = new PostDetailResponse();
        detail.attachEvent(info);
        assertThat(detail.getEvent().getOrganizer()).isEqualTo("서울특별시");

        PostDetailResponse plain = new PostDetailResponse();
        plain.attachEvent(null);
        assertThat(plain.getEvent()).isNull();
    }
}
