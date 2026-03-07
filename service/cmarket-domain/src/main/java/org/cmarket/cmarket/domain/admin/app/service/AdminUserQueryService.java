package org.cmarket.cmarket.domain.admin.app.service;

import org.cmarket.cmarket.domain.admin.app.dto.AdminUserListItemDto;
import org.cmarket.cmarket.domain.auth.model.User;
import org.cmarket.cmarket.domain.auth.model.UserRole;
import org.cmarket.cmarket.domain.auth.repository.UserRepository;
import org.cmarket.cmarket.domain.profile.app.dto.PageResult;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 어드민 유저 조회 서비스
 */
@Service
public class AdminUserQueryService {

    private final UserRepository userRepository;

    public AdminUserQueryService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PageResult<AdminUserListItemDto> getUsersForAdmin(
            String keyword,
            String statusFilter,
            UserRole roleFilter,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        var userPage = userRepository.searchUsersForAdmin(keyword, statusFilter, roleFilter, pageable);

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

    private AdminUserListItemDto toListItemDto(User user) {
        String status = user.getDeletedAt() != null ? "WITHDRAWN" : "ACTIVE";
        return AdminUserListItemDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .birthDate(user.getBirthDate())
                .addressSido(user.getAddressSido())
                .addressGugun(user.getAddressGugun())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .status(status)
                .build();
    }
}
