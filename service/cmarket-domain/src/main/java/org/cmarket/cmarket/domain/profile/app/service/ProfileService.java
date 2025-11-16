package org.cmarket.cmarket.domain.profile.app.service;

import org.cmarket.cmarket.domain.auth.app.dto.UserDto;
import org.cmarket.cmarket.domain.profile.app.dto.BlockedUserListDto;
import org.cmarket.cmarket.domain.profile.app.dto.MyPageDto;
import org.cmarket.cmarket.domain.profile.app.dto.ProfileUpdateCommand;
import org.cmarket.cmarket.domain.profile.app.dto.UserProfileDto;

/**
 * 프로필 서비스 인터페이스
 * 
 * 프로필 관련 비즈니스 로직을 담당합니다.
 */
public interface ProfileService {
    
    /**
     * 마이페이지 조회
     * 
     * 현재 로그인한 사용자의 마이페이지 정보를 조회합니다.
     * 
     * @param email 사용자 이메일
     * @return 마이페이지 정보
     * @throws org.cmarket.cmarket.domain.auth.app.exception.UserNotFoundException 사용자가 존재하지 않을 때
     */
    MyPageDto getMyPage(String email);
    
    /**
     * 프로필 정보 수정
     * 
     * 닉네임, 지역, 프로필 이미지, 소개글을 수정합니다.
     * 
     * @param command 프로필 수정 명령
     * @return 수정된 사용자 정보
     * @throws org.cmarket.cmarket.domain.auth.app.exception.UserNotFoundException 사용자가 존재하지 않을 때
     * @throws org.cmarket.cmarket.domain.auth.app.exception.NicknameAlreadyExistsException 닉네임이 이미 사용 중일 때
     */
    UserDto updateProfile(ProfileUpdateCommand command);
    
    /**
     * 유저 프로필 조회
     * 
     * 특정 사용자의 프로필 정보를 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 유저 프로필 정보
     * @throws org.cmarket.cmarket.domain.auth.app.exception.UserNotFoundException 사용자가 존재하지 않을 때
     */
    UserProfileDto getUserProfile(Long userId);
    
    /**
     * 차단한 유저 목록 조회
     * 
     * 현재 로그인한 사용자가 차단한 유저 목록을 조회합니다.
     * 
     * @param email 사용자 이메일
     * @param pageable 페이지네이션 정보
     * @return 차단한 유저 목록 (페이지네이션 포함)
     * @throws org.cmarket.cmarket.domain.auth.app.exception.UserNotFoundException 사용자가 존재하지 않을 때
     */
    BlockedUserListDto getBlockedUsers(String email, org.springframework.data.domain.Pageable pageable);
    
    /**
     * 유저 차단 해제
     * 
     * 현재 로그인한 사용자가 차단한 유저를 차단 해제합니다.
     * 차단 관계가 존재하지 않는 경우에도 성공으로 처리합니다 (idempotent).
     * 
     * @param email 사용자 이메일 (차단한 사용자)
     * @param blockedUserId 차단 해제할 사용자 ID
     * @throws org.cmarket.cmarket.domain.auth.app.exception.UserNotFoundException 사용자가 존재하지 않을 때
     */
    void unblockUser(String email, Long blockedUserId);
}

