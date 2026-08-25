package org.cmarket.cmarket.domain.profile.app.service;

import lombok.RequiredArgsConstructor;
import org.cmarket.cmarket.domain.auth.app.dto.UserDto;
import org.cmarket.cmarket.domain.auth.app.exception.NicknameAlreadyExistsException;
import org.cmarket.cmarket.domain.auth.app.exception.UserNotFoundException;
import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;
import org.cmarket.cmarket.domain.auth.model.User;
import org.cmarket.cmarket.domain.auth.model.ConsentVersions;
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

import java.time.LocalDate;
import java.time.Period;
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

    /** 가입·이용 가능한 최소 나이 (AuthServiceImpl과 같은 값) */
    private static final int MINIMUM_AGE = 14;

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
                .userRole(user.getRole())
                .provider(user.getProvider())
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
        
        // 3. 만 14세 이상 검증
        //
        // 소셜 가입 완료(웹 SocialSignUpForm)와 프로필 수정이 둘 다 이 API(PATCH /profile/me)를 쓴다.
        // 나이 검사는 이메일 가입(AuthServiceImpl.signUp)에만 있어서, 소셜 가입은 나이 확인 없이
        // 통과했고 이메일 가입자도 나중에 생년월일을 바꾸면 검사를 피할 수 있었다.
        validateAge(command.getBirthDate());

        // 4. 프로필 정보 업데이트
        user.updateProfile(
                command.getNickname(),
                command.getBirthDate(),
                command.getAddressSido(),
                command.getAddressGugun(),
                command.getProfileImageUrl(),
                command.getIntroduction()
        );

        // 5. 약관 동의 기록 (#1088 — 소셜 가입 마무리에서만 온다)
        //
        // ⚠️ **소셜 가입과 프로필 수정이 같은 API 를 쓴다.** 그래서 값이 온 경우에만 적는다 —
        //    프로필을 고칠 때는 null 로 와서 아무것도 안 건드린다.
        //
        // ⚠️ **여기서 「동의 안 했으면 막기」를 하지 않는다.** 그러면 프로필 수정이 막힌다.
        //    소셜 가입에서 동의를 강제하는 것은 화면 몫이다(단추 끄기 + 제출 직전 검사).
        if (Boolean.TRUE.equals(command.getTermsAgreed())
                && Boolean.TRUE.equals(command.getPrivacyAgreed())) {
            user.recordTermsAgreement(ConsentVersions.TERMS, ConsentVersions.PRIVACY);
        }
        
        // 5. UserDto 생성 및 반환
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
        //
        // ⚠️ UserNotFoundException 이 아니라 PROFILE_USER_NOT_FOUND(404) 를 던진다.
        //    UserNotFoundException 은 값이 401 이라(ErrorCode 주석 참고) 앱이 「로그인이 풀렸다」와
        //    구분하지 못했다. 그래서 탈퇴한 사람의 프로필을 열면 「네트워크를 확인하라」는
        //    엉뚱한 안내가 떴다(#995). 여기는 「그 사람이 이제 없다」이므로 404 가 맞다.
        User user = userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_USER_NOT_FOUND));
        
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
                .userRole(user.getRole())
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
                            .blockedAt(userBlock.getCreatedAt())
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

    /**
     * 만 14세 이상 검증
     *
     * 생년월일이 null이면 검사하지 않는다. 프로필 수정에서 생년월일을 건드리지 않는
     * 경우(소개글만 고치는 등)에 null이 들어오는데, 여기서 막으면 그런 수정까지 막힌다.
     * 소셜 가입 완료 화면은 생년월일을 반드시 채워 보내므로 그 경로는 검사된다.
     *
     * @param birthDate 생년월일 (null이면 검사하지 않음)
     * @throws IllegalArgumentException 만 14세 미만일 때
     */
    private void validateAge(LocalDate birthDate) {
        if (birthDate == null) {
            return;
        }

        int age = Period.between(birthDate, LocalDate.now()).getYears();

        if (age < MINIMUM_AGE) {
            throw new IllegalArgumentException("만 14세 이상만 이용할 수 있습니다.");
        }
    }

}

