package org.cmarket.cmarket.domain.view.app.service;

import lombok.RequiredArgsConstructor;
import org.cmarket.cmarket.domain.view.model.ViewLog;
import org.cmarket.cmarket.domain.view.model.ViewTargetType;
import org.cmarket.cmarket.domain.view.repository.ViewLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Transactional
public class ViewLogServiceImpl implements ViewLogService {

    /**
     * 하루의 경계를 한국 시간으로 못 박는다.
     *
     * ⚠️ 이 저장소의 다른 곳은 LocalDate.now()를 시간대 없이 쓴다. 여기만 다른 이유가 있다.
     *    서버 JVM이 UTC로 돈다(운영 로그가 UTC다).
     *    시간대를 안 박으면 하루가 바뀌는 순간이 <b>한국 시간 아침 9시</b>가 된다.
     *
     *      아침 8시(한국)에 봄  → 서버 날짜 어제  → 조회 +1
     *      아침 10시(한국)에 봄 → 서버 날짜 오늘  → 조회 +1   ← 같은 날인데 두 번 세진다
     *
     *    출퇴근 시간대라 가장 많이 걸릴 자리여서 명시한다.
     *
     * ⚠️ <b>저장된 createdAt은 UTC가 아니라 KST다.</b> 예전에 이 주석이 「운영 로그·저장된
     *    createdAt이 모두 UTC다」라고 적고 있었는데 <b>뒤쪽이 틀렸다</b>
     *    (2026-08-23에 운영 DB를 직접 조회해 갈랐다).
     *    JDBC URL의 serverTimezone=Asia/Seoul 때문에 드라이버가 옮겨 담는다.
     *
     *      로그          2026-08-23T04:35:52Z   ← UTC
     *      DB createdAt  2026-08-23 13:35:10    ← 같은 순간인데 +9. KST다
     *
     *    그래서 <b>로그와 DB createdAt을 나란히 놓고 디버깅하면 9시간 어긋난다.</b>
     *    실제로 그것 때문에 멀쩡한 것을 버그로 볼 뻔했다.
     *    ⚠️ LocalDate인 viewDate는 날짜뿐이라 <b>안 옮겨진다</b> — 아래 계산은 그대로 옳다.
     */
    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private final ViewLogRepository viewLogRepository;

    @Override
    public boolean markViewedToday(Long viewerId, ViewTargetType targetType, Long targetId) {
        if (viewerId == null || targetType == null || targetId == null) {
            return false;
        }

        LocalDate today = LocalDate.now(KOREA_ZONE);

        // 오늘 이미 본 기록이 있으면 조회수를 올리지 않는다
        if (viewLogRepository.existsByViewerIdAndTargetTypeAndTargetIdAndViewDate(
                viewerId, targetType, targetId, today)) {
            return false;
        }

        viewLogRepository.save(
                ViewLog.builder()
                        .viewerId(viewerId)
                        .targetType(targetType)
                        .targetId(targetId)
                        .viewDate(today)
                        .build()
        );

        return true;
    }
}
