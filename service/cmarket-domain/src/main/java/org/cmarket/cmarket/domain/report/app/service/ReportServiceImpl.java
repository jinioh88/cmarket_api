package org.cmarket.cmarket.domain.report.app.service;

import lombok.RequiredArgsConstructor;
import org.cmarket.cmarket.domain.auth.app.exception.UserNotFoundException;
import org.cmarket.cmarket.domain.auth.model.User;
import org.cmarket.cmarket.domain.auth.repository.UserRepository;
import org.cmarket.cmarket.domain.profile.app.dto.PageResult;
import org.cmarket.cmarket.domain.report.app.dto.BlockedUserDto;
import org.cmarket.cmarket.domain.report.app.dto.BlockedUserListDto;
import org.cmarket.cmarket.domain.report.app.dto.ReportCreateCommand;
import org.cmarket.cmarket.domain.report.app.dto.ReportDto;
import org.cmarket.cmarket.domain.report.app.dto.ReportReviewCommand;
import org.cmarket.cmarket.domain.report.app.dto.UserBlockCreateCommand;
import org.cmarket.cmarket.domain.report.app.dto.UserBlockDto;
import org.cmarket.cmarket.domain.report.app.exception.AlreadyReportedException;
import org.cmarket.cmarket.domain.report.app.exception.BlockSelfNotAllowedException;
import org.cmarket.cmarket.domain.report.app.exception.ReportNotFoundException;
import org.cmarket.cmarket.domain.report.app.exception.ReportStatusInvalidException;
import org.cmarket.cmarket.domain.report.app.exception.UserAlreadyBlockedException;
import org.cmarket.cmarket.domain.report.model.CommunityReportReason;
import org.cmarket.cmarket.domain.report.model.ProductReportReason;
import org.cmarket.cmarket.domain.report.model.Report;
import org.cmarket.cmarket.domain.report.model.ReportStatus;
import org.cmarket.cmarket.domain.report.model.ReportTargetType;
import org.cmarket.cmarket.domain.report.model.UserBlock;
import org.cmarket.cmarket.domain.report.model.UserReportReason;
import org.cmarket.cmarket.domain.report.repository.ReportRepository;
import org.cmarket.cmarket.domain.report.repository.UserBlockRepository;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final UserRepository userRepository;
    private final UserBlockRepository userBlockRepository;
    private final ReportRepository reportRepository;

    @Override
    public UserBlockDto blockUser(String email, UserBlockCreateCommand command) {
        User blocker = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(UserNotFoundException::new);

        User blockedUser = userRepository.findById(command.getBlockedUserId())
                .filter(user -> user.getDeletedAt() == null)
                .orElseThrow(UserNotFoundException::new);

        if (blocker.getId().equals(blockedUser.getId())) {
            throw new BlockSelfNotAllowedException();
        }

        if (userBlockRepository.existsByBlockerIdAndBlockedUserId(blocker.getId(), blockedUser.getId())) {
            throw new UserAlreadyBlockedException();
        }

        UserBlock userBlock = UserBlock.builder()
                .blockerId(blocker.getId())
                .blockedUserId(blockedUser.getId())
                .build();

        UserBlock saved = userBlockRepository.save(userBlock);

        return UserBlockDto.builder()
                .blockerId(saved.getBlockerId())
                .blockedUserId(saved.getBlockedUserId())
                .blockedNickname(blockedUser.getNickname())
                .blockedProfileImageUrl(blockedUser.getProfileImageUrl())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void unblockUser(String email, Long blockedUserId) {
        // 1. 사용자 조회
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        // 2. 차단 관계 삭제 (차단 관계가 없어도 예외 발생하지 않음 - idempotent)
        userBlockRepository.deleteByBlockerIdAndBlockedUserId(user.getId(), blockedUserId);
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

    @Override
    public ReportDto createReport(String email, ReportCreateCommand command) {
        // 신고자 조회
        User reporter = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(UserNotFoundException::new);

        // 중복 신고 확인
        if (reportRepository.existsByReporterIdAndTargetTypeAndTargetId(
                reporter.getId(), command.getTargetType(), command.getTargetId())) {
            String targetName = getTargetName(command.getTargetType());
            throw new AlreadyReportedException("이미 신고된 " + targetName + "입니다.");
        }

        // 신고 사유 검증 (최소 1개 선택)
        if (command.getReasonCodes() == null || command.getReasonCodes().isEmpty()) {
            throw new IllegalArgumentException("신고 사유를 최소 1개 이상 선택해야 합니다.");
        }

        // targetType별 reason Enum 검증
        validateReasonCodes(command.getTargetType(), command.getReasonCodes());

        // Report 엔티티 생성
        Report report = Report.builder()
                .reporterId(reporter.getId())
                .targetType(command.getTargetType())
                .targetId(command.getTargetId())
                .reasonCodes(command.getReasonCodes())
                .detailReason(command.getDetailReason())
                .imageUrls(command.getImageUrls())
                .build();

        // 저장
        Report savedReport = reportRepository.save(report);

        // DTO로 변환하여 반환
        return ReportDto.fromEntity(savedReport);
    }

    @Override
    public ReportDto reviewReport(String email, ReportReviewCommand command) {
        // 관리자 여부는 컨트롤러/시큐리티에서 검증한다고 가정
        Report report = reportRepository.findById(command.getReportId())
                .orElseThrow(ReportNotFoundException::new);

        // 상태 전환 검증: PENDING에서만 변경 허용
        if (report.getStatus() != ReportStatus.PENDING) {
            throw new ReportStatusInvalidException("신고가 이미 처리되었습니다.");
        }

        report.review(command.getStatus(), command.getRejectedReason());
        // JPA 더티 체킹으로 자동 반영되므로 save() 불필요
        return ReportDto.fromEntity(report);
    }

    /**
     * targetType별 reason 코드 검증
     */
    private void validateReasonCodes(ReportTargetType targetType, List<String> reasonCodes) {
        Set<String> validCodes;
        
        switch (targetType) {
            case USER:
                validCodes = Stream.of(UserReportReason.values())
                        .map(Enum::name)
                        .collect(Collectors.toSet());
                break;
            case PRODUCT:
                validCodes = Stream.of(ProductReportReason.values())
                        .map(Enum::name)
                        .collect(Collectors.toSet());
                break;
            case COMMUNITY_POST:
                validCodes = Stream.of(CommunityReportReason.values())
                        .map(Enum::name)
                        .collect(Collectors.toSet());
                break;
            default:
                throw new IllegalArgumentException("지원하지 않는 신고 대상 타입입니다: " + targetType);
        }

        for (String code : reasonCodes) {
            if (!validCodes.contains(code)) {
                throw new IllegalArgumentException("유효하지 않은 신고 사유 코드입니다: " + code);
            }
        }
    }

    /**
     * 신고 대상 타입에 따른 이름 반환
     */
    private String getTargetName(ReportTargetType targetType) {
        switch (targetType) {
            case USER:
                return "유저";
            case PRODUCT:
                return "상품";
            case COMMUNITY_POST:
                return "게시글";
            default:
                return "대상";
        }
    }
}
