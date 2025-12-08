package org.cmarket.cmarket.domain.profile.app.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 유저 프로필 정보 DTO
 * 
 * 앱 서비스에서 사용하는 유저 프로필 정보 DTO입니다.
 */
@Getter
@Builder
public class UserProfileDto {
    private Long id;
    private String profileImageUrl;
    private String addressSido;
    private String addressGugun;
    private String nickname;
    private LocalDateTime createdAt;
    private String introduction;
    private String name;
    private LocalDate birthDate;
    private String email;
    private Boolean isBlocked;
    private Boolean isReported;
    
    // todo: 등록한 상품 목록 (향후 Product 도메인에서 구현 예정)
    private List<Object> products;  // 빈 리스트로 반환
}

