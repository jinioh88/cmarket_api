package org.cmarket.cmarket.domain.report.app.service;

import lombok.RequiredArgsConstructor;
import org.cmarket.cmarket.domain.report.repository.UserBlockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserBlockQueryServiceImpl implements UserBlockQueryService {

    private final UserBlockRepository userBlockRepository;

    @Override
    public boolean isBlocked(Long blockerId, Long targetUserId) {
        if (blockerId == null || targetUserId == null) {
            return false;
        }
        return userBlockRepository.existsByBlockerIdAndBlockedUserId(blockerId, targetUserId);
    }

    @Override
    public boolean isBlockedBidirectional(Long userId1, Long userId2) {
        if (userId1 == null || userId2 == null) {
            return false;
        }
        // A가 B를 차단했거나, B가 A를 차단한 경우
        return userBlockRepository.existsByBlockerIdAndBlockedUserId(userId1, userId2)
                || userBlockRepository.existsByBlockerIdAndBlockedUserId(userId2, userId1);
    }
}

