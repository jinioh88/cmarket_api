package org.cmarket.cmarket.domain.chat.repository;

import org.cmarket.cmarket.domain.chat.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * ChatRoom 엔티티 레포지토리 인터페이스
 * 
 * Spring Data JPA가 자동으로 구현체를 생성합니다.
 * 
 * 주요 기능:
 * - 상품별 채팅방 조회
 * - 채팅방 존재 확인
 */
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    
    /**
     * 상품별 채팅방 목록 조회
     * 
     * 같은 상품에 대해 여러 채팅방이 존재할 수 있으므로 List를 반환합니다.
     * 
     * @param productId 상품 ID
     * @return 채팅방 목록
     */
    List<ChatRoom> findAllByProductId(Long productId);
    
    /**
     * 상품 ID와 두 사용자 ID로 기존 채팅방 조회
     * 
     * 특정 상품에 대해 두 사용자(구매자, 판매자)가 모두 참여한 채팅방을 조회합니다.
     * 
     * @param productId 상품 ID
     * @param userId1 첫 번째 사용자 ID (구매자 또는 판매자)
     * @param userId2 두 번째 사용자 ID (구매자 또는 판매자)
     * @return 기존 채팅방 (없으면 Optional.empty())
     */
    @Query("SELECT DISTINCT cr FROM ChatRoom cr " +
           "JOIN ChatRoomUser cru1 ON cru1.chatRoomId = cr.id AND cru1.userId = :userId1 AND cru1.isActive = true " +
           "JOIN ChatRoomUser cru2 ON cru2.chatRoomId = cr.id AND cru2.userId = :userId2 AND cru2.isActive = true " +
           "WHERE cr.productId = :productId")
    Optional<ChatRoom> findByProductIdAndUsers(
            @Param("productId") Long productId,
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2
    );
    
    /**
     * 채팅방 존재 확인 (상품 ID와 채팅방 ID로)
     * 
     * @param productId 상품 ID
     * @param chatRoomId 채팅방 ID
     * @return 존재하면 true, 없으면 false
     */
    boolean existsByProductIdAndId(Long productId, Long chatRoomId);
}
