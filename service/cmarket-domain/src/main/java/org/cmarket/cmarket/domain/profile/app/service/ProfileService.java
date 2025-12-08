package org.cmarket.cmarket.domain.profile.app.service;

import org.cmarket.cmarket.domain.auth.app.dto.UserDto;
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
     * 사용자 정보 조회
     * 
     * 현재 로그인한 사용자의 기본 정보만 조회합니다.
     * 
     * @param email 사용자 이메일
     * @return 사용자 정보
     * @throws org.cmarket.cmarket.domain.auth.app.exception.UserNotFoundException 사용자가 존재하지 않을 때
     */
    MyPageDto getUserInfo(String email);
    
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
     * @param currentUserEmail 현재 로그인한 사용자 이메일 (차단 여부 확인용, null 가능)
     * @return 유저 프로필 정보
     * @throws org.cmarket.cmarket.domain.auth.app.exception.UserNotFoundException 사용자가 존재하지 않을 때
     */
    UserProfileDto getUserProfile(Long userId, String currentUserEmail);
    
}

