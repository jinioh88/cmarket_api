package org.cmarket.cmarket.domain.admin.app.service;

import org.cmarket.cmarket.domain.admin.app.dto.AdminUserDto;
import org.cmarket.cmarket.domain.auth.app.exception.UserNotFoundException;
import org.cmarket.cmarket.domain.auth.model.User;
import org.cmarket.cmarket.domain.auth.model.UserRole;
import org.cmarket.cmarket.domain.auth.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 어드민 서비스 구현체
 */
@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;

    public AdminServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public AdminUserDto grantAdminRole(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException("대상 회원을 찾을 수 없습니다."));

        user.changeRole(UserRole.ADMIN);

        return toAdminUserDto(user);
    }

    @Override
    @Transactional
    public AdminUserDto restoreWithdrawnUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("탈퇴 회원을 찾을 수 없습니다."));

        if (user.getDeletedAt() == null) {
            throw new UserNotFoundException("해당 회원은 탈퇴한 회원이 아닙니다.");
        }

        user.restore();

        return toAdminUserDto(user);
    }

    private AdminUserDto toAdminUserDto(User user) {
        return AdminUserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .birthDate(user.getBirthDate())
                .addressSido(user.getAddressSido())
                .addressGugun(user.getAddressGugun())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
