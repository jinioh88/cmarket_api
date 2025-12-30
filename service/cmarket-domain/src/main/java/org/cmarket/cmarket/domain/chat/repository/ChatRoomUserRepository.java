package org.cmarket.cmarket.domain.chat.repository;

import org.cmarket.cmarket.domain.chat.model.ChatRoomUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * ChatRoomUser 엔티티 레포지토리 인터페이스
 * 
 * Spring Data JPA가 자동으로 구현체를 생성합니다.
 * 
 * 주요 기능:
 * - 사용자의 활성 채팅방 목록 조회
 * - 채팅방의 참여자 조회
 * - 채팅방 참여 여부 확인
 */
public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser, Long> {
    
    /**
     * 사용자의 활성 채팅방 목록 조회 (최근 메시지 시간 기준 내림차순)
     * 
     * lastMessageAt이 null인 경우 createdAt 기준으로 정렬합니다.
     * 
     * @param userId 사용자 ID
     * @return 활성 채팅방 참여 정보 목록 (최근 메시지 시간 순)
     */
    @Query("SELECT cru FROM ChatRoomUser cru " +
           "WHERE cru.userId = :userId AND cru.isActive = true " +
           "ORDER BY COALESCE(cru.lastMessageAt, cru.createdAt) DESC")
    List<ChatRoomUser> findActiveByUserIdOrderByLastMessageAtDesc(@Param("userId") Long userId);
    
    /**
     * 사용자의 활성 채팅방 목록 조회 (페이지네이션 지원)
     * 
     * lastMessageAt이 null인 경우 createdAt 기준으로 정렬합니다.
     * 
     * @param userId 사용자 ID
     * @param pageable 페이지 정보
     * @return 활성 채팅방 참여 정보 페이지 (최근 메시지 시간 순)
     */
    @Query("SELECT cru FROM ChatRoomUser cru " +
           "WHERE cru.userId = :userId AND cru.isActive = true " +
           "ORDER BY COALESCE(cru.lastMessageAt, cru.createdAt) DESC")
    Page<ChatRoomUser> findActiveByUserIdOrderByLastMessageAtDesc(
            @Param("userId") Long userId,
            Pageable pageable
    );
    
    /**
     * 채팅방의 모든 참여자 조회
     * 
     * @param chatRoomId 채팅방 ID
     * @return 채팅방 참여자 목록
     */
    List<ChatRoomUser> findByChatRoomId(Long chatRoomId);
    
    /**
     * 특정 사용자의 채팅방 참여 정보 조회
     * 
     * @param chatRoomId 채팅방 ID
     * @param userId 사용자 ID
     * @return 채팅방 참여 정보 (없으면 Optional.empty())
     */
    Optional<ChatRoomUser> findByChatRoomIdAndUserId(Long chatRoomId, Long userId);
    
    /**
     * 채팅방 참여 여부 확인 (활성 상태만)
     * 
     * @param chatRoomId 채팅방 ID
     * @param userId 사용자 ID
     * @return 활성 참여 중이면 true, 아니면 false
     */
    boolean existsByChatRoomIdAndUserIdAndIsActiveTrue(Long chatRoomId, Long userId);
    
    /**
     * 특정 채팅방에 참여한 사용자 수 조회
     * 
     * 기존 채팅방에 두 사용자(구매자, 판매자)가 모두 참여했는지 확인할 때 사용합니다.
     * 결과가 2이면 두 사용자 모두 참여한 것입니다.
     * 
     * @param chatRoomId 채팅방 ID
     * @param userIds 확인할 사용자 ID 목록 (구매자, 판매자)
     * @return 해당 채팅방에 참여한 사용자 수
     */
    long countByChatRoomIdAndUserIdIn(Long chatRoomId, List<Long> userIds);
    
    /**
     * 채팅방의 상대방 조회
     * 
     * 1:1 채팅에서 나를 제외한 상대방 정보를 직접 조회합니다.
     * N+1 문제 방지를 위해 사용합니다.
     * 
     * @param chatRoomId 채팅방 ID
     * @param myUserId 나의 사용자 ID
     * @return 상대방 참여 정보 (없으면 Optional.empty())
     */
    @Query("SELECT cru FROM ChatRoomUser cru " +
           "WHERE cru.chatRoomId = :chatRoomId AND cru.userId != :myUserId")
    Optional<ChatRoomUser> findOpponentByChatRoomIdAndMyUserId(
            @Param("chatRoomId") Long chatRoomId,
            @Param("myUserId") Long myUserId
    );
    
    /**
     * 여러 채팅방의 상대방 목록 일괄 조회
     * 
     * N+1 문제 방지를 위해 IN 절로 한 번에 조회합니다.
     * 
     * @param chatRoomIds 채팅방 ID 목록
     * @param myUserId 나의 사용자 ID
     * @return 상대방 참여 정보 목록
     */
    @Query("SELECT cru FROM ChatRoomUser cru " +
           "WHERE cru.chatRoomId IN :chatRoomIds AND cru.userId != :myUserId")
    List<ChatRoomUser> findOpponentsByChatRoomIdsAndMyUserId(
            @Param("chatRoomIds") List<Long> chatRoomIds,
            @Param("myUserId") Long myUserId
    );
}
