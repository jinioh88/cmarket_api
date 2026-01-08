package org.cmarket.cmarket.domain.profile.app.service;

import lombok.RequiredArgsConstructor;
import org.cmarket.cmarket.domain.auth.app.dto.UserDto;
import org.cmarket.cmarket.domain.auth.app.exception.NicknameAlreadyExistsException;
import org.cmarket.cmarket.domain.auth.app.exception.UserNotFoundException;
import org.cmarket.cmarket.domain.auth.model.User;
import org.cmarket.cmarket.domain.auth.repository.UserRepository;
import org.cmarket.cmarket.domain.profile.app.dto.BlockedUserDto;
import org.cmarket.cmarket.domain.profile.app.dto.BlockedUserListDto;
import org.cmarket.cmarket.domain.profile.app.dto.MyPageDto;
import org.cmarket.cmarket.domain.profile.app.dto.PageResult;
import org.cmarket.cmarket.domain.profile.app.dto.ProfileUpdateCommand;
import org.cmarket.cmarket.domain.profile.app.dto.UserProfileDto;
import org.cmarket.cmarket.domain.report.app.service.UserBlockQueryService;
import org.cmarket.cmarket.domain.report.model.ReportTargetType;
import org.cmarket.cmarket.domain.report.model.UserBlock;
import org.cmarket.cmarket.domain.report.repository.ReportRepository;
import org.cmarket.cmarket.domain.report.repository.UserBlockRepository;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 프로필 서비스 구현체
 * 
 * 프로필 관련 비즈니스 로직을 구현합니다.
 */
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
    
    private final UserRepository userRepository;
    private final UserBlockRepository userBlockRepository;
    private final UserBlockQueryService userBlockQueryService;
    private final ReportRepository reportRepository;
    
    @Override
    public MyPageDto getUserInfo(String email) {
        // 1. 사용자 조회
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
        
        // 2. 사용자 정보만 반환 (상품 목록은 빈 리스트)
        return MyPageDto.builder()
                .id(user.getId())
                .profileImageUrl(user.getProfileImageUrl())
                .nickname(user.getNickname())
                .name(user.getName())
                .introduction(user.getIntroduction())
                .birthDate(user.getBirthDate())
                .email(user.getEmail())
                .addressSido(user.getAddressSido())
                .addressGugun(user.getAddressGugun())
                .createdAt(user.getCreatedAt())
                .favoriteProducts(Collections.emptyList())
                .myProducts(Collections.emptyList())
                .purchaseRequests(Collections.emptyList())
                .blockedUsers(Collections.emptyList())
                .build();
    }
    
    @Override
    @Transactional
    public UserDto updateProfile(ProfileUpdateCommand command) {
        // 1. 사용자 조회
        User user = userRepository.findByEmailAndDeletedAtIsNull(command.getEmail())
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
        
        // 2. 닉네임 중복 검증 (본인 닉네임 제외)
        if (!user.getNickname().equals(command.getNickname()) 
                && userRepository.existsByNickname(command.getNickname())) {
            throw new NicknameAlreadyExistsException("이미 사용 중인 닉네임입니다.");
        }
        
        // 3. 프로필 정보 업데이트
        user.updateProfile(
                command.getNickname(),
                command.getAddressSido(),
                command.getAddressGugun(),
                command.getProfileImageUrl(),
                command.getIntroduction()
        );
        
        // 4. UserDto 생성 및 반환
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .birthDate(user.getBirthDate())
                .addressSido(user.getAddressSido())
                .addressGugun(user.getAddressGugun())
                .build();
    }
    
    @Override
    public UserProfileDto getUserProfile(Long userId, String currentUserEmail) {
        // 1. 사용자 조회 (소프트 삭제된 사용자 제외)
        User user = userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
        
        // 2. 차단 여부 및 신고 여부 확인 (현재 사용자가 로그인한 경우에만)
        Boolean isBlocked = null;
        Boolean isReported = null;
        if (currentUserEmail != null) {
            User currentUser = userRepository.findByEmailAndDeletedAtIsNull(currentUserEmail)
                    .orElse(null);
            if (currentUser != null) {
                isBlocked = userBlockQueryService.isBlocked(currentUser.getId(), userId);
                isReported = reportRepository.existsByReporterIdAndTargetTypeAndTargetId(
                        currentUser.getId(), ReportTargetType.USER, userId);
            }
        }
        
        // 3. UserProfileDto 생성 및 반환
        // 등록한 상품 목록은 향후 Product 도메인에서 구현 예정
        return UserProfileDto.builder()
                .id(user.getId())
                .profileImageUrl(user.getProfileImageUrl())
                .addressSido(user.getAddressSido())
                .addressGugun(user.getAddressGugun())
                .nickname(user.getNickname())
                .createdAt(user.getCreatedAt())
                .introduction(user.getIntroduction())
                .name(user.getName())
                .birthDate(user.getBirthDate())
                .email(user.getEmail())
                .isBlocked(isBlocked)
                .isReported(isReported)
                .products(Collections.emptyList())  // todo: 향후 Product 도메인에서 구현
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public BlockedUserListDto getBlockedUsers(String email, org.springframework.data.domain.Pageable pageable) {
        // 1. 사용자 조회
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        // 2. 차단 목록 조회 (페이지네이션, 최신순 정렬)
        org.springframework.data.domain.Page<UserBlock> userBlockPage = userBlockRepository
                .findByBlockerIdOrderByCreatedAtDesc(user.getId(), pageable);

        // 3. 차단당한 사용자 정보 조회 및 DTO 변환
        List<BlockedUserDto> blockedUserDtos = userBlockPage.getContent().stream()
                .map(userBlock -> {
                    User blockedUserEntity = userRepository.findById(userBlock.getBlockedUserId())
                            .orElse(null);

                    if (blockedUserEntity == null || blockedUserEntity.isDeleted()) {
                        return null;  // 삭제된 사용자는 제외
                    }

                    return BlockedUserDto.builder()
                            .blockedUserId(blockedUserEntity.getId())
                            .nickname(blockedUserEntity.getNickname())
                            .profileImageUrl(blockedUserEntity.getProfileImageUrl())
                            .build();
                })
                .filter(blockedUserDto -> blockedUserDto != null)
                .collect(Collectors.toList());

        // 4. PageResult 생성 (삭제된 사용자를 제외한 실제 개수로 조정)
        // Spring Data Page를 PageResult로 변환
        org.springframework.data.domain.Page<BlockedUserDto> blockedUserDtoPage =
                new PageImpl<>(
                        blockedUserDtos,
                        pageable,
                        userBlockPage.getTotalElements()  // 전체 개수는 원본 페이지에서 가져옴
                );

        PageResult<BlockedUserDto> pageResult = PageResult.fromPage(blockedUserDtoPage);

        // 5. BlockedUserListDto 생성 및 반환
        return BlockedUserListDto.builder()
                .blockedUsers(pageResult)
                .build();
    }
    
}

