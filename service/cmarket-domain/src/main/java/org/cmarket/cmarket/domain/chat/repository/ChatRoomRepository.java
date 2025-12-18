package org.cmarket.cmarket.domain.chat.repository;

import org.cmarket.cmarket.domain.chat.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

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
     * 상품별 채팅방 조회
     * 
     * @param productId 상품 ID
     * @return 채팅방 (없으면 Optional.empty())
     */
    Optional<ChatRoom> findByProductId(Long productId);
    
    /**
     * 채팅방 존재 확인 (상품 ID와 채팅방 ID로)
     * 
     * @param productId 상품 ID
     * @param chatRoomId 채팅방 ID
     * @return 존재하면 true, 없으면 false
     */
    boolean existsByProductIdAndId(Long productId, Long chatRoomId);
}
