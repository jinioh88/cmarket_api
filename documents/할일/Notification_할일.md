# Notification 기능 구현 할일

> 반려동물 용품 중고거래 서비스 - **Notification** 영역 구현 작업 목록

---

## 개요

본 문서는 `07_Notification.md` 요구사항 정의서를 기반으로 구현할 작업을 순차적으로 정리한 것입니다.
아키텍처 가이드(`architecture-guide.md`) 원칙을 준수하여 구현합니다.

### 기술 스택

| 구분 | 기술 | 설명 |
|------|------|------|
| 실시간 통신 | SSE (Server-Sent Events) | 단방향 실시간 푸시 알림 |
| 이벤트 버스 | Spring Event (@EventListener) | 서비스 로직과 알림 로직의 결합도 낮추기 |
| DB | RDB (H2/MySQL) | 알림 내역 영구 저장 (읽음/안 읽음 상태 관리) |
| 메모리 관리 | ConcurrentHashMap | 접속한 사용자들의 SseEmitter 안전하게 보관 |
| 캐시 | Caffeine Cache | 알림 이력 캐싱 (성능 최적화) |
| 비동기 처리 | Spring @Async | 알림 생성 및 전송 비동기 처리 |

### 아키텍처 흐름

```
① 알림 생성 (Event Source)
  - Chat Service: 새로운 메시지 도착
  - Product Service: 찜한 상품 가격 변동/상태 변경
  - Admin/Community Service: 게시글 삭제, 댓글 등록

② 이벤트 버스 (Spring Event)
  - ApplicationEventPublisher를 통한 이벤트 발행
  - @EventListener를 통한 이벤트 구독 및 처리
  - 서비스 로직과 알림 로직의 결합도 낮춤

③ 저장 및 전송 (Persistence & Push)
  - RDB: 알림 내역 저장 (읽음/안 읽음 상태)
  - SseEmitter: 실시간 푸시 전송 (ConcurrentHashMap으로 관리)
```

### 핵심 구현 포인트

1. **추상화 전략**
   - `NotificationService` 인터페이스 생성
   - `LocalNotificationService` 구현 (서버 내부 메모리 기반)
   - 향후 Redis 필요 시 `RedisNotificationService`로 교체 가능하도록 구조 설계

2. **연결 관리 (Connection Management)**
   - SSE 연결 타임아웃 설정 (60초)
   - 연결 직후 "Connect Success" 더미 데이터 전송 (503 에러 방지)
   - 클라이언트 재연결 유도
   - `ConcurrentHashMap<Long, SseEmitter>`로 연결 관리

3. **비동기 처리 (@Async)**
   - 알림 생성 및 전송은 별도 쓰레드에서 비동기 처리
   - 메인 비즈니스 로직 블로킹 방지

4. **결합도 낮추기**
   - Spring Event를 통한 이벤트 발행/구독
   - 서비스 로직에서 직접 알림 서비스 호출하지 않고 이벤트만 발행

5. **캐시 관리 및 데이터 정합성**
   - Caffeine Cache를 통한 알림 이력 캐싱 (성능 최적화)
   - 캐시 키: 사용자별 알림 목록, 안 읽은 알림 개수 등
   - **데이터 정합성 보장**: 알림 읽음 처리 시 해당 사용자의 캐시를 삭제(evict)
   - 읽음 처리 로직(`markAsRead`, `markAllAsRead`) 실행 시 캐시 무효화 필수

---

## 구현 순서

### Step 1: 도메인 모델 생성

#### 1-1. NotificationType Enum 생성
- **작업 내용**:
  - 알림 타입을 정의하는 Enum 생성
  - 값:
    - `CHAT_NEW_ROOM`: 새로운 채팅이 생성되었을 때
    - `CHAT_NEW_MESSAGE`: 새로운 메시지가 도착했을 때
    - `PRODUCT_FAVORITE_STATUS_CHANGED`: 찜한 상품 거래 상태 변경
    - `PRODUCT_FAVORITE_PRICE_CHANGED`: 찜한 상품 가격 변동
    - `ADMIN_SANCTION`: 어드민 재제 알림
    - `POST_DELETED`: 내 게시글이 삭제 당했을 때
    - `COMMENT_REPLY`: 내가 작성한 댓글에 댓글이 달렸을 경우
    - `POST_COMMENT`: 내가 작성한 게시글에 댓글이 달렸을 경우
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/notification/model/NotificationType.java`

#### 1-2. Notification 엔티티 생성
- **작업 내용**:
  - 알림 정보를 저장하는 엔티티 생성
  - 필드:
    - id (PK)
    - userId (수신자 ID, User 참조)
    - notificationType (알림 타입, NotificationType Enum)
    - title (알림 제목)
    - content (알림 내용)
    - relatedEntityType (관련 엔티티 타입, 예: "CHAT_ROOM", "PRODUCT", "POST", "COMMENT")
    - relatedEntityId (관련 엔티티 ID, nullable)
    - isRead (읽음 여부, 기본값 false)
    - readAt (읽은 시간, nullable)
    - createdAt
  - 인덱스: userId, isRead, createdAt, notificationType
  - 비즈니스 메서드:
    - `markAsRead()`: 읽음 처리 (isRead = true, readAt 설정)
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/notification/model/Notification.java`

---

### Step 2: 도메인 레포지토리 인터페이스 생성

#### 2-1. NotificationRepository 인터페이스 생성
- **작업 내용**:
  - `JpaRepository<Notification, Long>` 상속
  - 사용자별 알림 목록 조회 (페이지네이션): `Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable)`
  - 사용자별 안 읽은 알림 개수: `long countByUserIdAndIsReadFalse(Long userId)`
  - 사용자별 안 읽은 알림 목록: `List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId)`
  - 알림 읽음 처리: `@Modifying @Query` 사용하여 `int markAsRead(Long notificationId, Long userId)` 구현
  - 사용자별 모든 알림 읽음 처리: `@Modifying @Query` 사용하여 `int markAllAsRead(Long userId)` 구현
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/notification/repository/NotificationRepository.java`

---

### Step 3: 애플리케이션 서비스 생성

#### 3-1. NotificationService 인터페이스 생성
- **작업 내용**:
  - 알림 관련 비즈니스 로직 인터페이스 정의
  - 추상화를 통해 향후 Redis 기반 구현으로 교체 가능하도록 설계
  - 메서드:
    - `createNotification(NotificationCreateCommand)`: 알림 생성 (RDB 저장 + 실시간 전송)
    - `getNotificationList(String email, Pageable)`: 알림 목록 조회
    - `getUnreadCount(String email)`: 안 읽은 알림 개수 조회
    - `markAsRead(String email, Long notificationId)`: 알림 읽음 처리
    - `markAllAsRead(String email)`: 모든 알림 읽음 처리
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/notification/app/service/NotificationService.java`

#### 3-2. NotificationCreateCommand DTO 생성
- **작업 내용**:
  - 알림 생성 명령 DTO
  - 필드:
    - userId (수신자 ID)
    - notificationType (알림 타입)
    - title (알림 제목)
    - content (알림 내용)
    - relatedEntityType (관련 엔티티 타입, nullable)
    - relatedEntityId (관련 엔티티 ID, nullable)
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/notification/app/dto/NotificationCreateCommand.java`

#### 3-3. NotificationDto DTO 생성
- **작업 내용**:
  - 알림 정보 응답 DTO
  - 필드:
    - notificationId
    - notificationType
    - title
    - content
    - relatedEntityType
    - relatedEntityId
    - isRead
    - readAt
    - createdAt
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/notification/app/dto/NotificationDto.java`

#### 3-4. LocalNotificationService 구현
- **작업 내용**:
  - `NotificationService` 인터페이스의 로컬 메모리 기반 구현체
  - 서버 내부 메모리(ConcurrentHashMap)를 사용하여 SSE 연결 관리
  - `NotificationSseService` 의존성 주입
  - `NotificationRepository` 의존성 주입
  - Caffeine Cache 의존성 주입:
    - `Cache<Long, Page<NotificationDto>>` (알림 목록 캐시)
    - `Cache<Long, Long>` (안 읽은 알림 개수 캐시)
  - `createNotification()`: 
    - RDB에 알림 저장 (`NotificationRepository.save()`)
    - 저장된 알림을 `NotificationDto`로 변환
    - 해당 사용자의 캐시 무효화 (알림 목록, 안 읽은 개수)
    - `NotificationSseService.sendNotification()` 호출하여 실시간 전송 (메모리 기반)
    - `@Async` 적용하여 비동기 처리
  - `getNotificationList()`: 
    - 캐시에서 먼저 조회 (Key: userId)
    - 캐시 미스 시 RDB 조회 후 캐시에 저장
    - 사용자별 알림 목록 조회 (페이지네이션)
  - `getUnreadCount()`: 
    - 캐시에서 먼저 조회 (Key: userId)
    - 캐시 미스 시 RDB 조회 후 캐시에 저장
    - 안 읽은 알림 개수 조회
  - `markAsRead()`: 
    - RDB에서 알림 읽음 처리
    - **캐시 무효화 필수**: 해당 사용자의 알림 목록 캐시 및 안 읽은 개수 캐시 삭제 (evict)
    - 데이터 정합성 보장
  - `markAllAsRead()`: 
    - RDB에서 모든 알림 읽음 처리
    - **캐시 무효화 필수**: 해당 사용자의 알림 목록 캐시 및 안 읽은 개수 캐시 삭제 (evict)
    - 데이터 정합성 보장
  - `@Service` 또는 `@Component`로 빈 등록
  - `@Primary` 어노테이션 추가 (기본 구현체로 사용)
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/notification/service/LocalNotificationService.java`

---

### Step 4: Caffeine Cache 설정

#### 4-1. Caffeine Cache 의존성 추가
- **작업 내용**:
  - `build.gradle`에 Caffeine Cache 의존성 추가
  - `implementation 'com.github.ben-manes.caffeine:caffeine'`
- **출력물**:
  - 기존 파일 수정:
    - `service/cmarket/build.gradle`

#### 4-2. NotificationCacheConfig 설정 클래스 생성
- **작업 내용**:
  - Caffeine Cache 설정 클래스 생성
  - 캐시 빈 생성:
    - `Cache<Long, Page<NotificationDto>>`: 사용자별 알림 목록 캐시
      - Key: userId
      - Value: Page<NotificationDto>
      - 최대 크기: 1000
      - TTL: 5분
    - `Cache<Long, Long>`: 사용자별 안 읽은 알림 개수 캐시
      - Key: userId
      - Value: unreadCount
      - 최대 크기: 5000
      - TTL: 1분
  - `@Configuration` 어노테이션 추가
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/notification/config/NotificationCacheConfig.java`

---

### Step 5: Spring Event 이벤트 정의

#### 4-1. NotificationCreatedEvent 생성
- **작업 내용**:
  - 알림 생성 요청 이벤트 클래스
  - `ApplicationEvent` 상속
  - 필드:
    - userId (수신자 ID)
    - command (NotificationCreateCommand)
  - Spring Event를 통한 이벤트 발행/구독 구조
  - 생성자에서 userId와 command 받아서 저장
  - 서비스 로직에서 알림 생성 요청을 이벤트로 발행
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/notification/app/event/NotificationCreatedEvent.java`

#### 5-2. NotificationEventListener 생성
- **작업 내용**:
  - Spring Event 리스너
  - `@EventListener` 어노테이션 사용
  - `NotificationCreatedEvent` 이벤트 구독
  - 이벤트 수신 시:
    - 이벤트에서 `NotificationCreateCommand` 추출
    - `NotificationService.createNotification()` 호출 (인터페이스 주입)
    - `LocalNotificationService.createNotification()` 실행:
      - RDB에 알림 저장
      - 캐시 무효화 (해당 사용자의 알림 목록, 안 읽은 개수)
      - `NotificationSseService.sendNotification()` 호출하여 실시간 전송
  - `@Async` 적용하여 비동기 처리
  - 서비스 로직과 알림 로직의 결합도 낮춤
  - `NotificationService` 의존성 주입 (인터페이스)
  - `@Component`로 빈 등록
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/notification/service/NotificationEventListener.java`

---

### Step 6: SSE 컨트롤러 및 연결 관리

#### 6-1. AsyncConfig 설정 클래스 생성
- **작업 내용**:
  - `@EnableAsync` 설정
  - `ThreadPoolTaskExecutor` 빈 생성
  - 알림 전송 전용 쓰레드 풀 설정 (corePoolSize: 5, maxPoolSize: 10, queueCapacity: 100)
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/notification/config/AsyncConfig.java`

#### 6-2. NotificationSseService 생성
- **작업 내용**:
  - SSE 연결 관리 서비스
  - 메서드:
    - `connect(Long userId)`: SSE 연결 생성 및 관리
      - `SseEmitter` 생성 (timeout: 60초)
      - 연결 직후 "Connect Success" 더미 데이터 전송 (503 에러 방지)
      - 연결 해제 시 정리 작업
    - `sendNotification(Long userId, NotificationDto notificationDto)`: 특정 사용자에게 알림 전송
    - `disconnect(Long userId)`: SSE 연결 해제
    - `isConnected(Long userId)`: 사용자 연결 여부 확인
  - 연결 관리: `ConcurrentHashMap<Long, SseEmitter>` 사용 (스레드 안전)
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/notification/service/NotificationSseService.java`

#### 6-3. NotificationSseController 생성
- **작업 내용**:
  - SSE 엔드포인트 컨트롤러
  - 엔드포인트: `GET /api/notifications/stream`
  - 인증: `@PreAuthorize("isAuthenticated()")` 적용
  - 현재 로그인한 사용자 정보 추출 (`SecurityUtils.getCurrentUserEmail()`)
  - `NotificationSseService.connect()` 호출
  - 응답 타입: `text/event-stream`
  - 헤더 설정: `Cache-Control: no-cache`, `Connection: keep-alive`
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/notification/controller/NotificationSseController.java`


---

### Step 6: 웹 DTO 및 REST 컨트롤러

#### 7-1. NotificationListResponse 웹 DTO 생성
- **작업 내용**:
  - 알림 목록 응답 DTO
  - `NotificationDto`를 받아 변환하는 `fromDto()` 메서드 포함
  - 필드:
    - notificationId
    - notificationType
    - title
    - content
    - relatedEntityType
    - relatedEntityId
    - isRead
    - readAt
    - createdAt
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/notification/dto/NotificationListResponse.java`

#### 7-2. NotificationUnreadCountResponse 웹 DTO 생성
- **작업 내용**:
  - 안 읽은 알림 개수 응답 DTO
  - 필드:
    - unreadCount (Long)
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/notification/dto/NotificationUnreadCountResponse.java`

#### 7-3. NotificationController 생성
- **작업 내용**:
  - 알림 관련 REST API 컨트롤러
  - 엔드포인트:
    - `GET /api/notifications`: 알림 목록 조회 (페이지네이션)
      - Query Parameter: `page`, `size`, `sort`
      - 인증: `@PreAuthorize("isAuthenticated()")`
      - 현재 로그인한 사용자 정보 추출 (`SecurityUtils.getCurrentUserEmail()`)
      - `NotificationService.getNotificationList()` 호출
      - `PageResult<NotificationListResponse>` 반환
    - `GET /api/notifications/unread-count`: 안 읽은 알림 개수 조회
      - 인증: `@PreAuthorize("isAuthenticated()")`
      - 현재 로그인한 사용자 정보 추출
      - `NotificationService.getUnreadCount()` 호출
      - `NotificationUnreadCountResponse` 반환
    - `PATCH /api/notifications/{notificationId}/read`: 알림 읽음 처리
      - Path Variable: `notificationId`
      - 인증: `@PreAuthorize("isAuthenticated()")`
      - 현재 로그인한 사용자 정보 추출
      - `NotificationService.markAsRead()` 호출
      - `SuccessResponse<Void>` 반환
    - `PATCH /api/notifications/read-all`: 모든 알림 읽음 처리
      - 인증: `@PreAuthorize("isAuthenticated()")`
      - 현재 로그인한 사용자 정보 추출
      - `NotificationService.markAllAsRead()` 호출
      - `SuccessResponse<Void>` 반환
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/notification/controller/NotificationController.java`

---

### Step 8: 이벤트 발행 통합 (다양한 서비스에서 알림 생성)

#### 8-1. Chat Service에서 알림 이벤트 발행
- **작업 내용**:
  - `ApplicationEventPublisher` 의존성 주입
  - 새로운 채팅방 생성 시 알림 이벤트 발행
    - 위치: `ChatService.createChatRoom()` 또는 `ChatController.createChatRoom()`
    - `NotificationCreateCommand` 생성
    - `ApplicationEventPublisher.publishEvent(new NotificationCreatedEvent(userId, command))` 호출
    - 알림 타입: `CHAT_NEW_ROOM`
    - 수신자: 상대방 사용자
    - 서비스 로직과 알림 로직의 결합도 낮춤 (직접 호출 대신 이벤트 발행)
  - 새로운 메시지 도착 시 알림 이벤트 발행
    - 위치: `ChatService.sendMessage()` 또는 `ChatWebSocketController.sendMessage()`
    - `NotificationCreateCommand` 생성
    - `ApplicationEventPublisher.publishEvent(new NotificationCreatedEvent(userId, command))` 호출
    - 알림 타입: `CHAT_NEW_MESSAGE`
    - 수신자: 메시지 수신자 (본인 제외)
- **출력물**:
  - 기존 파일 수정:
    - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/chat/app/service/ChatService.java` (또는 구현체)
    - `service/cmarket/src/main/java/org/cmarket/cmarket/web/chat/controller/ChatWebSocketController.java` (또는 ChatController)

#### 8-2. Product Service에서 알림 이벤트 발행
- **작업 내용**:
  - `ApplicationEventPublisher` 의존성 주입
  - 찜한 상품의 가격 변동 시 알림 이벤트 발행
    - 위치: `ProductService.updateProduct()` 또는 가격 변경 로직
    - 해당 상품을 찜한 모든 사용자 조회
    - 각 사용자별로 `NotificationCreateCommand` 생성
    - `ApplicationEventPublisher.publishEvent(new NotificationCreatedEvent(userId, command))` 반복 호출
    - 알림 타입: `PRODUCT_FAVORITE_PRICE_CHANGED`
    - 수신자: 해당 상품을 찜한 모든 사용자
  - 찜한 상품의 거래 상태 변경 시 알림 이벤트 발행
    - 위치: `ProductService.updateProductStatus()` 또는 상태 변경 로직
    - 해당 상품을 찜한 모든 사용자 조회
    - 각 사용자별로 `NotificationCreateCommand` 생성
    - `ApplicationEventPublisher.publishEvent(new NotificationCreatedEvent(userId, command))` 반복 호출
    - 알림 타입: `PRODUCT_FAVORITE_STATUS_CHANGED`
    - 수신자: 해당 상품을 찜한 모든 사용자
- **출력물**:
  - 기존 파일 수정:
    - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/product/app/service/ProductService.java` (또는 구현체)

#### 8-3. Community Service에서 알림 이벤트 발행
- **작업 내용**:
  - `ApplicationEventPublisher` 의존성 주입
  - 게시글 삭제 시 알림 이벤트 발행
    - 위치: `PostService.deletePost()` 또는 삭제 로직
    - `NotificationCreateCommand` 생성
    - `ApplicationEventPublisher.publishEvent(new NotificationCreatedEvent(userId, command))` 호출
    - 알림 타입: `POST_DELETED`
    - 수신자: 게시글 작성자
  - 댓글 등록 시 알림 이벤트 발행
    - 위치: `CommentService.createComment()` 또는 댓글 생성 로직
    - `NotificationCreateCommand` 생성
    - `ApplicationEventPublisher.publishEvent(new NotificationCreatedEvent(userId, command))` 호출
    - 알림 타입: `COMMENT_REPLY` (댓글에 댓글) 또는 `POST_COMMENT` (게시글에 댓글)
    - 수신자: 댓글 대상 작성자 (본인 제외)
- **출력물**:
  - 기존 파일 수정:
    - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/community/app/service/PostService.java` (또는 구현체)
    - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/community/app/service/CommentService.java` (또는 구현체)

<!-- #### 8-4. Admin Service에서 알림 이벤트 발행
- **작업 내용**:
  - `ApplicationEventPublisher` 의존성 주입
  - 어드민 재제 시 알림 이벤트 발행
    - 위치: `AdminService.applySanction()` 또는 재제 로직
    - `NotificationCreateCommand` 생성
    - `ApplicationEventPublisher.publishEvent(new NotificationCreatedEvent(userId, command))` 호출
    - 알림 타입: `ADMIN_SANCTION`
    - 수신자: 재제 대상 사용자
- **출력물**:
  - 기존 파일 수정:
    - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/admin/app/service/AdminService.java` (또는 구현체) -->

---

### Step 9: ErrorCode 및 예외 처리

#### 9-1. Notification 관련 ErrorCode 추가
- **작업 내용**:
  - `ErrorCode` Enum에 알림 관련 에러 코드 추가
  - 예:
    - `NOTIFICATION_NOT_FOUND(404, "알림을 찾을 수 없습니다.")`
    - `NOTIFICATION_ACCESS_DENIED(403, "알림에 대한 접근 권한이 없습니다.")`
- **출력물**:
  - 기존 파일 수정:
    - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/exception/ErrorCode.java`

#### 9-2. NotificationNotFoundException 생성
- **작업 내용**:
  - 알림을 찾을 수 없을 때 발생하는 예외
  - `BusinessException` 상속
  - `ErrorCode.NOTIFICATION_NOT_FOUND` 사용
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/notification/app/exception/NotificationNotFoundException.java`

#### 9-3. NotificationAccessDeniedException 생성
- **작업 내용**:
  - 알림에 대한 접근 권한이 없을 때 발생하는 예외
  - `BusinessException` 상속
  - `ErrorCode.NOTIFICATION_ACCESS_DENIED` 사용
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/notification/app/exception/NotificationAccessDeniedException.java`

---

## 참고 사항

### 추상화 전략
- `NotificationService` 인터페이스를 통한 추상화
- 현재는 `LocalNotificationService` 구현 (서버 내부 메모리 기반)
- 향후 Redis 필요 시 `RedisNotificationService` 구현으로 교체 가능
- 구현체는 `@Primary` 또는 `@Qualifier`로 선택

### SSE 연결 관리 전략
- 타임아웃: 60초 (클라이언트가 주기적으로 재연결)
- 연결 직후 더미 데이터 전송 필수 (503 에러 방지)
- `ConcurrentHashMap<Long, SseEmitter>`로 연결 관리 (스레드 안전)
- 연결 해제 시 리소스 정리

### 비동기 처리 전략
- 알림 생성 및 전송은 모두 `@Async` 적용
- 메인 비즈니스 로직 블로킹 방지
- 쓰레드 풀 크기 조정 가능 (AsyncConfig에서 설정)

### Spring Event 활용
- `ApplicationEventPublisher`를 통한 이벤트 발행
- `@EventListener`를 통한 이벤트 구독
- 서비스 로직과 알림 로직의 결합도 낮춤
- 이벤트 기반 아키텍처로 확장성 확보

### Caffeine Cache 관리 및 데이터 정합성
- **캐시 구조**:
  - 알림 목록 캐시: `Cache<Long, Page<NotificationDto>>` (Key: userId)
  - 안 읽은 개수 캐시: `Cache<Long, Long>` (Key: userId)
- **캐시 전략**:
  - Cache-Aside 패턴 사용
  - 조회 시: 캐시 먼저 확인 → 미스 시 RDB 조회 → 캐시 저장
  - 쓰기 시: RDB 저장 → 캐시 무효화 (evict)
- **데이터 정합성 보장**:
  - 알림 읽음 처리(`markAsRead`, `markAllAsRead`) 시 **반드시 캐시 무효화**
  - 알림 생성 시에도 해당 사용자의 캐시 무효화
  - 캐시와 RDB 간 데이터 불일치 방지
- **캐시 설정**:
  - 알림 목록: 최대 1000개, TTL 5분
  - 안 읽은 개수: 최대 5000개, TTL 1분
  - 메모리 사용량과 성능의 균형 고려

### 알림 타입별 관련 엔티티 정보
- `relatedEntityType`: "CHAT_ROOM", "PRODUCT", "POST", "COMMENT" 등
- `relatedEntityId`: 해당 엔티티의 ID
- 프론트엔드에서 상세 페이지 이동 시 활용

### 향후 확장성
- 서버 확장 시 Redis 기반 구현으로 전환 가능
- `LocalNotificationService` → `RedisNotificationService` 교체만으로 확장
- 인터페이스 기반 설계로 코드 변경 최소화

---
