# Chat 기능 구현 할일

> 반려동물 용품 중고거래 서비스 - **Chat** 영역 구현 작업 목록

---

## 개요

본 문서는 `06_Chat.md` 요구사항 정의서를 기반으로 구현할 작업을 순차적으로 정리한 것입니다.
아키텍처 가이드(`architecture-guide.md`) 원칙을 준수하여 구현합니다.

### 기술 스택

| 구분 | 기술 | 설명 |
|------|------|------|
| 통신 | Spring Boot WebSocket + STOMP | Simple Broker 활용 |
| DB | RDB (MySQL/PostgreSQL) | 채팅방, 참여자, 메시지 내역 저장 |
| 캐시 | Redis | 읽음 상태 관리 및 세션 정보 처리 |

---

## 구현 순서

### Step 1: 도메인 모델 생성

#### 1-1. ChatRoom 엔티티 생성
- **작업 내용**:
  - 채팅방 정보를 저장하는 엔티티 생성
  - 필드:
    - id (PK)
    - productId (상품 ID, Product 참조)
    - productTitle (상품 제목, 스냅샷)
    - productPrice (상품 가격, 스냅샷)
    - productImageUrl (상품 대표 이미지, 스냅샷)
    - createdAt, updatedAt
  - 인덱스: productId, createdAt
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/chat/model/ChatRoom.java`

#### 1-2. ChatRoomUser 엔티티 생성
- **작업 내용**:
  - 채팅방 참여자 정보를 저장하는 엔티티 생성
  - 필드:
    - id (PK)
    - chatRoomId (채팅방 ID, ChatRoom 참조)
    - userId (사용자 ID, User 참조)
    - userNickname (사용자 닉네임, 스냅샷)
    - userProfileImageUrl (사용자 프로필 이미지, 스냅샷)
    - isActive (활성 여부, 기본값 true)
    - leftAt (나간 시간, nullable - 채팅방 나갔을 때 설정)
    - createdAt, updatedAt
  - 복합 유니크 제약: (chatRoomId, userId)
  - 인덱스: chatRoomId, userId
  - 비즈니스 메서드:
    - `leave()`: 채팅방 나가기 (isActive = false, leftAt 설정)
    - `isLeft()`: 나간 여부 확인
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/chat/model/ChatRoomUser.java`

#### 1-3. ChatMessage 엔티티 생성
- **작업 내용**:
  - 채팅 메시지 정보를 저장하는 엔티티 생성
  - 필드:
    - id (PK)
    - chatRoomId (채팅방 ID, ChatRoom 참조)
    - senderId (발신자 ID, User 참조)
    - senderNickname (발신자 닉네임, 스냅샷)
    - messageType (메시지 타입: TEXT, IMAGE, SYSTEM)
    - content (메시지 내용, 최대 1000자)
    - imageUrl (이미지 URL, nullable)
    - isRead (읽음 여부, 기본값 false) ← **RDB 영구 저장용**
    - isBlocked (차단 여부, 개인정보 포함 메시지)
    - blockReason (차단 사유, nullable)
    - createdAt
  - 인덱스: chatRoomId, senderId, createdAt, isRead
  - 비즈니스 메서드:
    - `markAsRead()`: 읽음 처리 (isRead = true)
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/chat/model/ChatMessage.java`

#### 1-4. MessageType Enum 생성
- **작업 내용**:
  - 메시지 타입을 정의하는 Enum 생성
  - 값:
    - TEXT: 텍스트 메시지
    - IMAGE: 이미지 메시지
    - SYSTEM: 시스템 메시지 (입장/퇴장 알림 등)
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/chat/model/MessageType.java`

---

### Step 2: 도메인 레포지토리 인터페이스 생성

#### 2-1. ChatRoomRepository 인터페이스 생성
- **작업 내용**:
  - `JpaRepository<ChatRoom, Long>` 상속
  - 상품별 채팅방 조회: `Optional<ChatRoom> findByProductId(Long productId)`
  - 채팅방 존재 확인: `boolean existsByProductIdAndId(Long productId, Long chatRoomId)`
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/chat/repository/ChatRoomRepository.java`

#### 2-2. ChatRoomUserRepository 인터페이스 생성
- **작업 내용**:
  - `JpaRepository<ChatRoomUser, Long>` 상속
  - 사용자의 활성 채팅방 목록 조회: `List<ChatRoomUser> findByUserIdAndIsActiveTrueOrderByUpdatedAtDesc(Long userId)`
  - 채팅방의 참여자 조회: `List<ChatRoomUser> findByChatRoomId(Long chatRoomId)`
  - 특정 사용자의 채팅방 참여 정보 조회: `Optional<ChatRoomUser> findByChatRoomIdAndUserId(Long chatRoomId, Long userId)`
  - 채팅방 참여 여부 확인: `boolean existsByChatRoomIdAndUserIdAndIsActiveTrue(Long chatRoomId, Long userId)`
  - 기존 채팅방 조회 (상품 + 두 사용자): `Optional<ChatRoomUser> findByProductIdAndUsers(Long productId, Long userId1, Long userId2)` (QueryDSL)
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/chat/repository/ChatRoomUserRepository.java`

#### 2-3. ChatRoomUserRepositoryCustom 인터페이스 및 구현체 생성
- **작업 내용**:
  - QueryDSL을 사용한 복잡한 채팅방 조회 메서드 정의
  - `ChatRoomUserRepositoryCustom` 인터페이스 생성
  - `ChatRoomUserRepositoryCustomImpl` 구현체 생성
  - 메서드:
    - 사용자의 채팅방 목록 조회 (최근 메시지 포함): `List<ChatRoomListItemDto> findChatRoomListByUserId(Long userId)`
    - 기존 채팅방 조회 (상품 + 두 사용자 기준): `Optional<ChatRoom> findExistingChatRoom(Long productId, Long buyerId, Long sellerId)`
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/chat/repository/ChatRoomUserRepositoryCustom.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/chat/repository/ChatRoomUserRepositoryCustomImpl.java`

#### 2-4. ChatMessageRepository 인터페이스 생성
- **작업 내용**:
  - `JpaRepository<ChatMessage, Long>` 상속
  - 채팅방의 메시지 목록 조회 (페이지네이션): `Page<ChatMessage> findByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId, Pageable pageable)`
  - 채팅방의 최근 메시지 조회: `Optional<ChatMessage> findTopByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId)`
  - 특정 시점 이후 메시지 조회: `List<ChatMessage> findByChatRoomIdAndCreatedAtAfterOrderByCreatedAtAsc(Long chatRoomId, LocalDateTime after)`
  - **읽음 상태 일괄 업데이트 (Redis → RDB Sync용)**:
    - `@Modifying @Query` 사용
    - `int markMessagesAsRead(Long chatRoomId, Long userId, LocalDateTime beforeTime)`: 특정 시점 이전의 안 읽은 메시지 일괄 읽음 처리
    - `int countUnreadMessages(Long chatRoomId, Long userId)`: 안 읽은 메시지 개수 조회 (RDB 기준)
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/chat/repository/ChatMessageRepository.java`

---

### Step 3: WebSocket + STOMP 설정

#### 3-1. WebSocket 설정 클래스 생성
- **작업 내용**:
  - `@EnableWebSocketMessageBroker` 설정
  - STOMP 엔드포인트 설정: `/ws-stomp`
  - Application Destination Prefix: `/app`
  - Simple Broker: `/topic`, `/queue`
  - SockJS 지원 설정
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/chat/config/WebSocketConfig.java`

#### 3-2. WebSocket 인터셉터 생성
- **작업 내용**:
  - `ChannelInterceptor` 구현
  - CONNECT 시 JWT 토큰 검증
  - 사용자 인증 정보를 StompHeaderAccessor에 설정
  - 인증 실패 시 연결 거부
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/chat/config/StompChannelInterceptor.java`

#### 3-3. WebSocket 이벤트 리스너 생성
- **작업 내용**:
  - `@EventListener` 활용
  - 연결/해제 이벤트 처리
  - 연결 시: 사용자 세션 정보 Redis에 저장
  - 해제 시: 사용자 세션 정보 Redis에서 제거
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/chat/config/WebSocketEventListener.java`

---

### Step 4: Redis 설정 (읽음 상태 관리)

#### 4-1. Redis 설정 클래스 생성
- **작업 내용**:
  - `RedisTemplate<String, Object>` 설정
  - 직렬화 설정 (StringRedisSerializer, GenericJackson2JsonRedisSerializer)
  - 연결 풀 설정
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/chat/config/RedisConfig.java`

#### 4-2. ChatReadStatusService 생성
- **작업 내용**:
  - Redis를 활용한 **실시간 읽음 상태 관리** 서비스
  - **핵심 전략**: 
    - 실시간 안 읽은 개수는 Redis에서 처리 (성능 최적화)
    - 유저가 채팅방 진입 시 Redis 상태를 RDB에 일괄 Sync
  - 메서드:
    - **Redis 실시간 처리**:
      - `incrementUnreadCount(Long chatRoomId, Long recipientId)`: 안 읽은 메시지 개수 증가 (메시지 전송 시 호출)
      - `getUnreadCount(Long chatRoomId, Long userId)`: 안 읽은 메시지 개수 조회 (Redis 기준, 실시간)
      - `resetUnreadCount(Long chatRoomId, Long userId)`: 안 읽은 메시지 개수 초기화 (채팅방 진입 시)
      - `getLastReadTime(Long chatRoomId, Long userId)`: 마지막 읽은 시간 조회
      - `updateLastReadTime(Long chatRoomId, Long userId)`: 마지막 읽은 시간 업데이트
    - **Redis → RDB Sync**:
      - `syncReadStatusToRdb(Long chatRoomId, Long userId)`: 채팅방 진입 시 Redis 상태를 RDB에 일괄 반영
        - Redis의 unreadCount를 0으로 리셋
        - RDB의 해당 채팅방 메시지 중 본인이 받은 메시지를 일괄 isRead = true로 업데이트
        - `@Transactional` 처리
  - Redis Key 구조:
    - 안 읽은 개수: `chat:unread:{chatRoomId}:{userId}` (Integer)
    - 마지막 읽은 시간: `chat:lastread:{chatRoomId}:{userId}` (Timestamp)
  - TTL: 30일
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/chat/app/service/ChatReadStatusService.java`
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/chat/service/ChatReadStatusServiceImpl.java`

#### 4-3. ChatSessionService 생성
- **작업 내용**:
  - Redis를 활용한 사용자 세션 정보 관리
  - 메서드:
    - `addUserSession(Long userId, String sessionId)`: 사용자 세션 등록
    - `removeUserSession(Long userId, String sessionId)`: 사용자 세션 제거
    - `isUserOnline(Long userId)`: 사용자 온라인 여부 확인
    - `getUserCurrentChatRoom(Long userId)`: 현재 접속 중인 채팅방 조회
    - `setUserCurrentChatRoom(Long userId, Long chatRoomId)`: 현재 채팅방 설정
  - Redis Key 구조: 
    - 세션: `chat:session:{userId}`
    - 현재 채팅방: `chat:current:{userId}`
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/chat/app/service/ChatSessionService.java`
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/chat/service/ChatSessionServiceImpl.java`

---

### Step 5: 채팅방 생성 구현 (FR-025)

#### 5-1. 채팅방 생성 웹 DTO 및 앱 DTO 생성
- **작업 내용**:
  - 웹 DTO: `ChatRoomCreateRequest` (productId)
  - 검증 어노테이션:
    - productId: @NotNull
  - 앱 DTO: `ChatRoomCreateCommand`
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/chat/dto/ChatRoomCreateRequest.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/chat/app/dto/ChatRoomCreateCommand.java`

#### 5-2. 채팅방 생성 앱 서비스 구현
- **작업 내용**:
  - `ChatService.createChatRoom()` 메서드 구현
  - 로직:
    1. 상품 존재 여부 확인 (ProductRepository 사용)
    2. 판매자 본인과의 채팅 방지 (본인 상품에는 채팅 불가)
    3. 기존 채팅방 존재 시 기존 채팅방 반환 (상품 + 구매자 + 판매자 기준)
    4. 채팅방이 없으면 새로 생성:
       - ChatRoom 생성 (상품 정보 스냅샷 저장)
       - ChatRoomUser 2개 생성 (구매자, 판매자)
    5. ChatRoomDto 반환
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/chat/app/service/ChatService.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/chat/app/service/ChatServiceImpl.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/chat/app/dto/ChatRoomDto.java`

#### 5-3. 채팅방 생성 컨트롤러 구현
- **작업 내용**:
  - `POST /api/chat/rooms` 엔드포인트
  - 인증 필수 (`@PreAuthorize("isAuthenticated()")`)
  - 현재 로그인한 사용자 정보 추출 (SecurityUtils 사용)
  - 웹 DTO → 앱 DTO 변환
  - 앱 서비스 호출
  - `SuccessResponse<ChatRoomResponse>` 반환 (HTTP 201)
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/chat/controller/ChatController.java`
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/chat/dto/ChatRoomResponse.java`

---

### Step 6: 채팅방 목록 조회 구현 (FR-024)

#### 6-1. 채팅방 목록 조회 웹 DTO 및 앱 DTO 생성
- **작업 내용**:
  - 웹 DTO: `ChatRoomListResponse` (채팅방 목록)
  - 채팅방 정보:
    - chatRoomId (채팅방 ID)
    - productId (상품 ID)
    - productTitle (상품 제목)
    - productPrice (상품 가격)
    - productImageUrl (상품 대표 이미지)
    - opponentId (상대방 ID)
    - opponentNickname (상대방 닉네임)
    - opponentProfileImageUrl (상대방 프로필 이미지)
    - lastMessage (최근 메시지 내용)
    - lastMessageTime (최근 메시지 시간)
    - hasUnread (읽지 않은 메시지 존재 여부)
    - unreadCount (읽지 않은 메시지 개수)
  - 앱 DTO: `ChatRoomListDto`, `ChatRoomListItemDto`
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/chat/dto/ChatRoomListResponse.java`
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/chat/dto/ChatRoomListItemResponse.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/chat/app/dto/ChatRoomListDto.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/chat/app/dto/ChatRoomListItemDto.java`

#### 6-2. 채팅방 목록 조회 앱 서비스 구현
- **작업 내용**:
  - `ChatService.getChatRoomList()` 메서드 구현
  - 로직:
    1. 사용자의 활성 채팅방 목록 조회 (isActive = true)
    2. 각 채팅방의 최근 메시지 조회
    3. **Redis에서 각 채팅방의 안 읽은 개수 조회** (`getUnreadCount`) - 실시간 성능
    4. 최근 메시지 시간 기준 정렬 (내림차순)
    5. ChatRoomListDto 반환 (unreadCount 포함)
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/chat/app/service/ChatService.java` (메서드 추가)

#### 6-3. 채팅방 목록 조회 컨트롤러 구현
- **작업 내용**:
  - `GET /api/chat/rooms` 엔드포인트
  - 인증 필수 (`@PreAuthorize("isAuthenticated()")`)
  - 현재 로그인한 사용자 정보 추출
  - 앱 서비스 호출
  - 앱 DTO → 웹 DTO 변환
  - `SuccessResponse<ChatRoomListResponse>` 반환
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/chat/controller/ChatController.java` (메서드 추가)

---

### Step 7: 채팅 전송 구현 (FR-026)

#### 7-1. 채팅 메시지 DTO 생성
- **작업 내용**:
  - 웹 DTO: `ChatMessageRequest` (chatRoomId, content, messageType, imageUrl)
  - 검증:
    - content: @Size(max=1000) - 최대 1000자
    - messageType: @NotNull
  - 응답 DTO: `ChatMessageResponse`
  - 앱 DTO: `ChatMessageCommand`, `ChatMessageDto`
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/chat/dto/ChatMessageRequest.java`
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/chat/dto/ChatMessageResponse.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/chat/app/dto/ChatMessageCommand.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/chat/app/dto/ChatMessageDto.java`

#### 7-2. 개인정보 필터링 서비스 구현
- **작업 내용**:
  - `PrivacyFilterService` 구현
  - 개인정보 패턴 검사:
    - 전화번호 (010-XXXX-XXXX, 01X XXXX XXXX 등)
    - 이메일 주소
    - 계좌번호
    - 주민등록번호
  - 메서드:
    - `containsPrivateInfo(String content)`: 개인정보 포함 여부
    - `getBlockReason(String content)`: 차단 사유 반환
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/chat/app/service/PrivacyFilterService.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/chat/app/service/PrivacyFilterServiceImpl.java`

#### 7-3. 채팅 전송 앱 서비스 구현
- **작업 내용**:
  - `ChatService.sendMessage()` 메서드 구현
  - 로직:
    1. 채팅방 존재 및 참여 여부 확인
    2. 상대방이 채팅방을 나갔는지 확인 (나갔으면 전송 불가)
    3. 개인정보 필터링:
       - 개인정보 포함 시: isBlocked = true, blockReason 설정
       - 메시지는 저장하되 발신자에게만 표시
    4. ChatMessage 엔티티 생성 및 저장 (isRead = false)
    5. **Redis에 상대방의 안 읽은 메시지 개수 증가** (`incrementUnreadCount`)
       - 상대방이 현재 해당 채팅방에 접속 중이면 증가하지 않음 (ChatSessionService로 확인)
    6. ChatRoomUser의 updatedAt 갱신
    7. ChatMessageDto 반환
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/chat/app/service/ChatService.java` (메서드 추가)

#### 7-4. WebSocket 메시지 컨트롤러 구현
- **작업 내용**:
  - `@MessageMapping` 사용
  - 엔드포인트: `/app/chat/message`
  - 구독: `/topic/chat/{chatRoomId}`
  - 로직:
    1. 메시지 저장 (앱 서비스 호출)
    2. 상대방에게 메시지 전송 (SimpMessagingTemplate 사용)
    3. 개인정보 차단 메시지는 발신자에게만 전송 (`/queue/chat/{userId}`)
  - 시스템 메시지 전송 (채팅방 나가기 시)
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/chat/controller/ChatWebSocketController.java`

#### 7-5. 이미지 메시지 전송 구현
- **작업 내용**:
  - 이미지 업로드는 기존 ImageController 활용
  - 업로드된 이미지 URL을 imageUrl에 포함하여 메시지 전송
  - messageType = IMAGE로 설정
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/chat/controller/ChatWebSocketController.java` (수정)

---

### Step 8: 채팅 내역 조회 구현 (FR-028)

#### 8-1. 채팅 내역 조회 웹 DTO 및 앱 DTO 생성
- **작업 내용**:
  - 웹 DTO: `ChatMessageListResponse` (메시지 목록, 페이지네이션)
  - 메시지 정보:
    - messageId (메시지 ID)
    - senderId (발신자 ID)
    - senderNickname (발신자 닉네임)
    - messageType (메시지 타입)
    - content (메시지 내용)
    - imageUrl (이미지 URL)
    - isBlocked (차단 여부)
    - blockReason (차단 사유)
    - createdAt (전송 시간)
    - isMine (내가 보낸 메시지 여부)
  - 앱 DTO: `ChatMessageListDto`, `ChatMessageListItemDto`
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/chat/dto/ChatMessageListResponse.java`
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/chat/dto/ChatMessageListItemResponse.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/chat/app/dto/ChatMessageListDto.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/chat/app/dto/ChatMessageListItemDto.java`

#### 8-2. 채팅 내역 조회 앱 서비스 구현
- **작업 내용**:
  - `ChatService.getChatMessages()` 메서드 구현
  - 로직:
    1. 채팅방 존재 및 참여 여부 확인
    2. **Redis → RDB Sync 실행** (`syncReadStatusToRdb`)
       - Redis의 안 읽은 개수 0으로 리셋
       - RDB의 메시지 isRead 일괄 업데이트 (본인이 받은 메시지 중 isRead = false인 것들)
       - 첫 페이지 조회 시에만 실행 (page == 0)
    3. 메시지 목록 조회 (페이지네이션, 최신순 → 오래된순 정렬하여 반환)
    4. 차단된 메시지는 발신자 본인에게만 표시
    5. Redis에 마지막 읽은 시간 업데이트
    6. ChatMessageListDto 반환
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/chat/app/service/ChatService.java` (메서드 추가)

#### 8-3. 채팅 내역 조회 컨트롤러 구현
- **작업 내용**:
  - `GET /api/chat/rooms/{chatRoomId}/messages` 엔드포인트
  - 인증 필수 (`@PreAuthorize("isAuthenticated()")`)
  - 쿼리 파라미터: page (기본값: 0), size (기본값: 50)
  - 앱 서비스 호출
  - 앱 DTO → 웹 DTO 변환
  - `SuccessResponse<ChatMessageListResponse>` 반환
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/chat/controller/ChatController.java` (메서드 추가)

---

### Step 9: 채팅방 삭제 (나가기) 구현 (FR-027)

#### 9-1. 채팅방 삭제 앱 서비스 구현
- **작업 내용**:
  - `ChatService.leaveChatRoom()` 메서드 구현
  - 로직:
    1. 채팅방 존재 및 참여 여부 확인
    2. ChatRoomUser의 `leave()` 메서드 호출 (isActive = false, leftAt 설정)
    3. 시스템 메시지 생성 ("ㅇㅇ님이 채팅방을 나가셨습니다.")
    4. 상대방에게 WebSocket으로 시스템 메시지 전송
    5. Redis에서 해당 사용자의 읽음 정보 삭제
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/chat/app/service/ChatService.java` (메서드 추가)

#### 9-2. 채팅방 삭제 컨트롤러 구현
- **작업 내용**:
  - `DELETE /api/chat/rooms/{chatRoomId}` 엔드포인트
  - 인증 필수 (`@PreAuthorize("isAuthenticated()")`)
  - 현재 로그인한 사용자 정보 추출
  - 앱 서비스 호출
  - `SuccessResponse` 반환 (data: null)
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/chat/controller/ChatController.java` (메서드 추가)

---

### Step 10: 커스텀 예외 클래스 생성

#### 10-1. Chat 관련 예외 클래스 생성
- **작업 내용**:
  - `ChatRoomNotFoundException`: 채팅방 없음
  - `ChatRoomAccessDeniedException`: 채팅방 접근 권한 없음
  - `ChatRoomAlreadyExistsException`: 이미 존재하는 채팅방
  - `SelfChatNotAllowedException`: 본인과의 채팅 불가
  - `ChatRoomUserLeftException`: 상대방이 채팅방을 나간 경우
  - `MessageTooLongException`: 메시지 길이 초과
  - `PrivacyViolationException`: 개인정보 포함 메시지 (내부 처리용)
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/chat/app/exception/` 패키지 내 예외 클래스들

#### 10-2. ErrorCode에 Chat 관련 에러 코드 추가
- **작업 내용**:
  - ErrorCode Enum에 Chat 관련 에러 코드 추가:
    - `CHAT_ROOM_NOT_FOUND(404, "채팅방을 찾을 수 없습니다.")`
    - `CHAT_ROOM_ACCESS_DENIED(403, "채팅방에 대한 접근 권한이 없습니다.")`
    - `SELF_CHAT_NOT_ALLOWED(400, "본인과의 채팅은 불가능합니다.")`
    - `CHAT_ROOM_USER_LEFT(400, "상대방이 채팅방을 나갔습니다.")`
    - `MESSAGE_TOO_LONG(400, "메시지는 1000자를 초과할 수 없습니다.")`
    - `PRIVACY_VIOLATION(400, "개인정보가 포함된 메시지는 전송할 수 없습니다.")`
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/exception/ErrorCode.java` (수정)

---

## 구현 시 주의사항

1. **아키텍처 원칙 준수**:
   - 웹 → 앱 → 도메인 의존 방향 준수
   - 도메인 모델은 웹 계층에서 직접 사용 금지
   - DTO 변환 필수

2. **WebSocket + STOMP**:
   - STOMP 프로토콜을 활용한 실시간 통신
   - 구독/발행 패턴 사용
   - 인증은 JWT 토큰 기반 (HandshakeInterceptor에서 처리)

3. **Redis 활용 및 읽음 상태 관리**:
   - **핵심 전략**: 실시간 성능을 위해 Redis에서 안 읽은 개수 관리, 채팅방 진입 시 RDB에 일괄 Sync
   - Redis Key 구조:
     - 안 읽은 개수: `chat:unread:{chatRoomId}:{userId}`
     - 마지막 읽은 시간: `chat:lastread:{chatRoomId}:{userId}`
     - 사용자 세션: `chat:session:{userId}`
     - 현재 채팅방: `chat:current:{userId}`
   - TTL: 30일 (메모리 관리)
   - **읽음 상태 플로우**:
     1. 메시지 전송 시: Redis의 상대방 unreadCount 증가 (상대방이 해당 채팅방에 없을 때만)
     2. 채팅방 목록 조회 시: Redis에서 각 채팅방의 unreadCount 조회 (실시간)
     3. 채팅방 진입 시 (첫 페이지 조회): Redis unreadCount 리셋 + RDB isRead 일괄 업데이트
   - **RDB isRead 필드**: 영구 저장용, Redis 장애 시 복구 기준

4. **메시지 길이 제한**:
   - 최대 1000자 (띄어쓰기 포함)
   - 초과 시 에러 반환

5. **개인정보 필터링**:
   - 전화번호, 이메일, 계좌번호, 주민등록번호 패턴 검사
   - 차단된 메시지는 저장하되 발신자에게만 표시
   - 안내 메시지와 함께 차단 사유 전달

6. **채팅방 삭제 정책**:
   - 실제 데이터 삭제 아님 (ChatRoomUser.isActive = false)
   - 삭제한 사용자의 목록에서만 제거
   - 상대방은 이전 메시지 확인 가능
   - 상대방에게 시스템 메시지 전송
   - 나간 후에는 상대방이 메시지 전송 불가

7. **상품 정보 스냅샷**:
   - 채팅방 생성 시점의 상품 정보 저장
   - 상품 정보 변경되어도 채팅방의 정보는 유지

8. **사용자 정보 스냅샷**:
   - 채팅방 참여 시점의 사용자 정보 저장
   - 닉네임/프로필 이미지 변경 시에도 기존 채팅방 정보 유지

9. **페이지네이션**:
   - 메시지 조회: 50개씩 (무한 스크롤)
   - PageResult 사용 (아키텍처 가이드 준수)

10. **예외 처리**:
    - 모든 예외는 BusinessException 상속
    - GlobalExceptionHandler에서 공통 처리
    - traceId 포함하여 로깅

11. **인증/인가**:
    - 모든 채팅 API는 인증 필수
    - 본인이 참여한 채팅방만 접근 가능
    - WebSocket 연결 시에도 JWT 검증

---

## 완료 체크리스트

- [ ] Step 1: 도메인 모델 생성
- [ ] Step 2: 도메인 레포지토리 인터페이스 생성
- [ ] Step 3: WebSocket + STOMP 설정
- [ ] Step 4: Redis 설정 (읽음 상태 관리)
- [ ] Step 5: 채팅방 생성 구현 (FR-025)
- [ ] Step 6: 채팅방 목록 조회 구현 (FR-024)
- [ ] Step 7: 채팅 전송 구현 (FR-026)
- [ ] Step 8: 채팅 내역 조회 구현 (FR-028)
- [ ] Step 9: 채팅방 삭제 (나가기) 구현 (FR-027)
- [ ] Step 10: 커스텀 예외 클래스 생성

---

## 참고사항

- 각 Step을 완료한 후 사용자 리뷰를 받고 다음 Step을 진행합니다.
- 아키텍처 가이드의 원칙을 반드시 준수합니다.
- 테스트는 각 Step 완료 후 작성합니다.
- WebSocket 연결은 SockJS를 통해 폴백 지원합니다.
- 메시지 데이터는 RDB에 영구 저장됩니다.

### 읽음 상태 관리 아키텍처

```
┌─────────────────────────────────────────────────────────────────┐
│                        읽음 상태 플로우                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  [메시지 전송]                                                   │
│       │                                                         │
│       ▼                                                         │
│  ┌─────────┐    isRead=false    ┌─────────┐                    │
│  │   RDB   │◄───────────────────│ Message │                    │
│  └─────────┘                    └─────────┘                    │
│       │                              │                          │
│       │                              ▼                          │
│       │                    ┌──────────────────┐                │
│       │                    │ Redis unreadCount│ +1             │
│       │                    │ (상대방이 채팅방  │                │
│       │                    │  밖에 있을 때만) │                │
│       │                    └──────────────────┘                │
│       │                                                         │
│  [채팅방 목록 조회]                                              │
│       │                                                         │
│       ▼                                                         │
│  ┌──────────────────┐                                          │
│  │ Redis unreadCount│ 조회 (실시간, 빠름)                       │
│  └──────────────────┘                                          │
│                                                                 │
│  [채팅방 진입] (첫 페이지 조회 시)                                │
│       │                                                         │
│       ▼                                                         │
│  ┌──────────────────┐         ┌─────────┐                      │
│  │ Redis unreadCount│ = 0  +  │   RDB   │ isRead 일괄 UPDATE   │
│  └──────────────────┘         └─────────┘                      │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

- **Redis**: 실시간 안 읽은 개수 관리 (성능 최적화)
- **RDB isRead**: 영구 저장, Redis 장애 시 복구 기준
- **Sync 타이밍**: 채팅방 진입 시 일괄 처리 (매 메시지마다 RDB 업데이트 방지)

---

## API 엔드포인트 요약

| 메서드 | 엔드포인트 | 설명 | 인증 |
|--------|-----------|------|------|
| POST | `/api/chat/rooms` | 채팅방 생성 | 필수 |
| GET | `/api/chat/rooms` | 채팅방 목록 조회 | 필수 |
| GET | `/api/chat/rooms/{chatRoomId}/messages` | 채팅 내역 조회 | 필수 |
| DELETE | `/api/chat/rooms/{chatRoomId}` | 채팅방 나가기 | 필수 |

### WebSocket 엔드포인트

| 타입 | 엔드포인트 | 설명 |
|------|-----------|------|
| STOMP | `/ws-stomp` | WebSocket 연결 엔드포인트 |
| 발행 | `/app/chat/message` | 메시지 전송 |
| 구독 | `/topic/chat/{chatRoomId}` | 채팅방 메시지 수신 |
| 구독 | `/queue/chat/{userId}` | 개인 메시지 수신 (차단 메시지 등) |

