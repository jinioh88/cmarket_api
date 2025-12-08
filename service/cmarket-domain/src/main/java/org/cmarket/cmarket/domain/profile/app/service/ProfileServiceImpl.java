package org.cmarket.cmarket.domain.profile.app.service;

import lombok.RequiredArgsConstructor;
import org.cmarket.cmarket.domain.auth.app.dto.UserDto;
import org.cmarket.cmarket.domain.auth.app.exception.NicknameAlreadyExistsException;
import org.cmarket.cmarket.domain.auth.app.exception.UserNotFoundException;
import org.cmarket.cmarket.domain.auth.model.User;
import org.cmarket.cmarket.domain.auth.repository.UserRepository;
import org.cmarket.cmarket.domain.product.app.dto.FavoriteItemDto;
import org.cmarket.cmarket.domain.product.app.dto.MyProductListItemDto;
import org.cmarket.cmarket.domain.product.model.Favorite;
import org.cmarket.cmarket.domain.product.model.Product;
import org.cmarket.cmarket.domain.product.model.ProductType;
import org.cmarket.cmarket.domain.product.repository.FavoriteRepository;
import org.cmarket.cmarket.domain.product.repository.ProductRepository;
import org.cmarket.cmarket.domain.profile.app.dto.BlockedUserDto;
import org.cmarket.cmarket.domain.profile.app.dto.BlockedUserListDto;
import org.cmarket.cmarket.domain.profile.app.dto.MyPageDto;
import org.cmarket.cmarket.domain.profile.app.dto.PageResult;
import org.cmarket.cmarket.domain.profile.app.dto.ProfileUpdateCommand;
import org.cmarket.cmarket.domain.profile.app.dto.UserProfileDto;
import org.cmarket.cmarket.domain.profile.model.BlockedUser;
import org.cmarket.cmarket.domain.profile.repository.BlockedUserRepository;
import org.cmarket.cmarket.domain.report.app.service.UserBlockQueryService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    private final BlockedUserRepository blockedUserRepository;
    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    private final UserBlockQueryService userBlockQueryService;
    
    @Override
    public MyPageDto getMyPage(String email) {
        // 1. 사용자 조회
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
        
        // 2. 차단한 유저 목록 조회 (최대 100개로 제한)
        Pageable pageable = PageRequest.of(0, 100);
        List<BlockedUser> blockedUsers = blockedUserRepository
                .findByBlockerIdOrderByCreatedAtDesc(user.getId(), pageable)
                .getContent();
        
        // 3. 차단한 유저 정보 조회
        List<BlockedUserDto> blockedUserDtos = blockedUsers.stream()
                .map(blockedUser -> {
                    User blockedUserEntity = userRepository.findById(blockedUser.getBlockedId())
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
        
        // 4. 찜한 상품 목록 조회 (최신 5개)
        Pageable favoritePageable = PageRequest.of(0, 5);
        org.springframework.data.domain.Page<Favorite> favoritePage = favoriteRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId(), favoritePageable);
        
        List<Long> favoriteProductIds = favoritePage.getContent().stream()
                .map(Favorite::getProductId)
                .collect(Collectors.toList());
        
        List<FavoriteItemDto> favoriteProducts = Collections.emptyList();
        if (!favoriteProductIds.isEmpty()) {
            List<Product> favoriteProductsList = productRepository.findAllById(favoriteProductIds).stream()
                    .filter(product -> product.getDeletedAt() == null)
                    .collect(Collectors.toList());
            
            Map<Long, Product> productMap = favoriteProductsList.stream()
                    .collect(Collectors.toMap(Product::getId, product -> product));
            
            favoriteProducts = favoritePage.getContent().stream()
                    .map(favorite -> {
                        Product product = productMap.get(favorite.getProductId());
                        if (product == null) {
                            return null;
                        }
                        return FavoriteItemDto.builder()
                                .id(product.getId())
                                .mainImageUrl(product.getMainImageUrl())
                                .title(product.getTitle())
                                .price(product.getPrice())
                                .viewCount(product.getViewCount())
                                .tradeStatus(product.getTradeStatus())
                                .build();
                    })
                    .filter(item -> item != null)
                    .collect(Collectors.toList());
        }
        
        // 5. 등록한 상품 목록 조회 (판매 상품만, 최신 5개)
        Pageable myProductPageable = PageRequest.of(0, 5);
        org.springframework.data.domain.Page<Product> myProductPage = productRepository
                .findBySellerIdAndProductTypeAndDeletedAtIsNullOrderByCreatedAtDesc(
                        user.getId(), ProductType.SELL, myProductPageable);
        
        List<MyProductListItemDto> myProducts = myProductPage.getContent().stream()
                .map(MyProductListItemDto::fromEntity)
                .collect(Collectors.toList());
        
        // 6. 판매 요청 목록 조회 (최신 5개)
        Pageable purchaseRequestPageable = PageRequest.of(0, 5);
        org.springframework.data.domain.Page<Product> purchaseRequestPage = productRepository
                .findBySellerIdAndProductTypeAndDeletedAtIsNullOrderByCreatedAtDesc(
                        user.getId(), ProductType.REQUEST, purchaseRequestPageable);
        
        List<MyProductListItemDto> purchaseRequests = purchaseRequestPage.getContent().stream()
                .map(MyProductListItemDto::fromEntity)
                .collect(Collectors.toList());
        
        // 7. MyPageDto 생성 및 반환
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
                .favoriteProducts(favoriteProducts.stream().map(item -> (Object) item).collect(Collectors.toList()))
                .myProducts(myProducts.stream().map(item -> (Object) item).collect(Collectors.toList()))
                .purchaseRequests(purchaseRequests.stream().map(item -> (Object) item).collect(Collectors.toList()))
                .blockedUsers(blockedUserDtos)
                .build();
    }
    
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
        
        // 2. 차단 여부 확인 (현재 사용자가 로그인한 경우에만)
        Boolean isBlocked = null;
        if (currentUserEmail != null) {
            User currentUser = userRepository.findByEmailAndDeletedAtIsNull(currentUserEmail)
                    .orElse(null);
            if (currentUser != null) {
                isBlocked = userBlockQueryService.isBlocked(currentUser.getId(), userId);
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
                .products(Collections.emptyList())  // todo: 향후 Product 도메인에서 구현
                .build();
    }
    
    @Override
    public BlockedUserListDto getBlockedUsers(String email, org.springframework.data.domain.Pageable pageable) {
        // 1. 사용자 조회
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
        
        // 2. 차단 목록 조회 (페이지네이션, 최신순 정렬)
        org.springframework.data.domain.Page<BlockedUser> blockedUserPage = blockedUserRepository
                .findByBlockerIdOrderByCreatedAtDesc(user.getId(), pageable);
        
        // 3. 차단당한 사용자 정보 조회 및 DTO 변환
        List<BlockedUserDto> blockedUserDtos = blockedUserPage.getContent().stream()
                .map(blockedUser -> {
                    User blockedUserEntity = userRepository.findById(blockedUser.getBlockedId())
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
                new org.springframework.data.domain.PageImpl<>(
                        blockedUserDtos,
                        pageable,
                        blockedUserPage.getTotalElements()  // 전체 개수는 원본 페이지에서 가져옴
                );
        
        PageResult<BlockedUserDto> pageResult = PageResult.fromPage(blockedUserDtoPage);
        
        // 5. BlockedUserListDto 생성 및 반환
        return BlockedUserListDto.builder()
                .blockedUsers(pageResult)
                .build();
    }
    
    @Override
    @Transactional
    public void unblockUser(String email, Long blockedUserId) {
        // 1. 사용자 조회
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
        
        // 2. 차단 관계 삭제 (차단 관계가 없어도 예외 발생하지 않음 - idempotent)
        blockedUserRepository.deleteByBlockerIdAndBlockedId(user.getId(), blockedUserId);
    }
}

