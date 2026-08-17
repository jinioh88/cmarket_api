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
     *    서버 JVM이 UTC로 돈다(운영 로그·저장된 createdAt이 모두 UTC다).
     *    시간대를 안 박으면 하루가 바뀌는 순간이 <b>한국 시간 아침 9시</b>가 된다.
     *
     *      아침 8시(한국)에 봄  → 서버 날짜 어제  → 조회 +1
     *      아침 10시(한국)에 봄 → 서버 날짜 오늘  → 조회 +1   ← 같은 날인데 두 번 세진다
     *
     *    출퇴근 시간대라 가장 많이 걸릴 자리여서 명시한다.
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
