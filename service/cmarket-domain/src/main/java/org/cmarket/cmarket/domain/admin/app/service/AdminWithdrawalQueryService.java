package org.cmarket.cmarket.domain.admin.app.service;

import org.cmarket.cmarket.domain.admin.app.dto.WithdrawalDetailDto;
import org.cmarket.cmarket.domain.admin.app.dto.WithdrawalListItemDto;
import org.cmarket.cmarket.domain.auth.app.exception.UserNotFoundException;
import org.cmarket.cmarket.domain.auth.model.User;
import org.cmarket.cmarket.domain.auth.repository.UserRepository;
import org.cmarket.cmarket.domain.profile.app.dto.PageResult;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 어드민 탈퇴 관리 조회 서비스
 */
@Service
public class AdminWithdrawalQueryService {

    private final UserRepository userRepository;

    public AdminWithdrawalQueryService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PageResult<WithdrawalListItemDto> getWithdrawnUsers(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        var userPage = userRepository.findWithdrawnUsers(keyword, pageable);

        var content = userPage.getContent().stream()
                .map(this::toListItemDto)
                .toList();

        return PageResult.fromPage(
                new org.springframework.data.domain.PageImpl<>(
                        content,
                        pageable,
                        userPage.getTotalElements()
                )
        );
    }

    @Transactional(readOnly = true)
    public WithdrawalDetailDto getWithdrawnUserDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("탈퇴 회원을 찾을 수 없습니다."));

        if (user.getDeletedAt() == null) {
            throw new UserNotFoundException("해당 회원은 탈퇴한 회원이 아닙니다.");
        }

        return WithdrawalDetailDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .name(user.getName())
                .role(user.getRole())
                .addressSido(user.getAddressSido())
                .addressGugun(user.getAddressGugun())
                .birthDate(user.getBirthDate())
                .createdAt(user.getCreatedAt())
                .profileImageUrl(user.getProfileImageUrl())
                .withdrawalReason(user.getWithdrawalReason())
                .withdrawalDetailReason(user.getWithdrawalDetailReason())
                .deletedAt(user.getDeletedAt())
                .build();
    }

    private WithdrawalListItemDto toListItemDto(User user) {
        return WithdrawalListItemDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .authorNickname(user.getNickname())
                .title(user.getWithdrawalDetailReason() != null ? user.getWithdrawalDetailReason() : "")
                .name(user.getName())
                .role(user.getRole())
                .addressSido(user.getAddressSido())
                .addressGugun(user.getAddressGugun())
                .birthDate(user.getBirthDate())
                .withdrawalReason(user.getWithdrawalReason())
                .withdrawalDetailReason(user.getWithdrawalDetailReason())
                .deletedAt(user.getDeletedAt())
                .build();
    }
}
