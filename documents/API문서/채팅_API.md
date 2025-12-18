# 채팅 API 문서

> 반려동물 용품 중고거래 서비스 – **채팅** API

본 문서는 1:1 채팅 관련 API에 대한 상세 설명을 제공합니다.  
프론트엔드 개발자가 클라이언트 기능을 구현할 때 필요한 요청/응답 규격과 Enum 목록, WebSocket 연동 방법을 모두 포함합니다.

---

## 목차

1. [공통 사항](#공통-사항)
2. [Enum 정의](#enum-정의)
3. [REST API](#rest-api)
   - [3-1. 채팅방 생성](#3-1-채팅방-생성-post-apichatrooms)
   - [3-2. 채팅방 목록 조회](#3-2-채팅방-목록-조회-get-apichatrooms)
   - [3-3. 채팅 내역 조회](#3-3-채팅-내역-조회-get-apichatroomschatroomidmessages)
   - [3-4. 채팅방 나가기](#3-4-채팅방-나가기-delete-apichatroomschatroomid)
4. [WebSocket API](#websocket-api)
   - [4-1. WebSocket 연결](#4-1-websocket-연결)
   - [4-2. 구독 (Subscribe)](#4-2-구독-subscribe---메시지-수신-경로)
   - [4-3. 발행 (Publish)](#4-3-발행-publish---메시지-전송-경로)
   - [4-4. 전체 연결 예시](#4-4-전체-연결-예시-javascript--sockjs--stompjs)
   - [4-5. 메시지 수신 형식](#4-5-메시지-수신-형식)
   - [4-6. 에러 수신](#4-6-에러-수신)
5. [에러 코드](#에러-코드)
6. [FAQ 및 참고](#faq-및-참고)

---

## 공통 사항

### Base URL

```
http://localhost:8080
```

### WebSocket URL

| 방식 | URL | 설명 |
|------|-----|------|
| **SockJS** (권장) | `http://localhost:8080/ws-stomp` | SockJS 라이브러리 사용 시 (`http://` 또는 `https://`) |
| **Pure WebSocket** | `ws://localhost:8080/ws-stomp` | 네이티브 WebSocket 사용 시 (`ws://` 또는 `wss://`) |

> **참고**: SockJS는 WebSocket을 지원하지 않는 브라우저에서 폴백(fallback)을 제공합니다.  
> 프로덕션 환경에서는 `https://` 또는 `wss://`를 사용하세요.

### 공통 헤더 (REST API)

| 헤더명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| Authorization | String | 예 | `Bearer <Access Token>` |
| Content-Type | String | 조건 | JSON 요청: `application/json` |

> 모든 채팅 API는 인증이 필수입니다.

### 공통 응답 형식

#### 성공

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": { ... }
}
```

#### 에러

```json
{
  "code": "BAD_REQUEST",
  "message": "에러 메시지",
  "traceId": "e1e4-...",
  "timestamp": "2025-01-15T10:30:00"
}
```

---

## Enum 정의

### MessageType (메시지 타입)

| 값 | 설명 | 사용 예시 |
|----|------|----------|
| `TEXT` | 텍스트 메시지 | 일반 채팅 메시지 |
| `IMAGE` | 이미지 메시지 | 사진 전송 |
| `SYSTEM` | 시스템 메시지 | 입장/퇴장 알림 |

---

## REST API

### 3-1. 채팅방 생성 (POST /api/chat/rooms)

- **인증 필요**: 예 (`Bearer` 토큰)
- **설명**: 상품에 대한 채팅방을 생성합니다. 기존 채팅방이 있으면 기존 채팅방을 반환합니다.

#### Request Body

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| productId | Long | 예 | 채팅할 상품 ID |

#### Request 예시

```json
{
  "productId": 123
}
```

#### Response Body

| 필드 | 타입 | 설명 |
|------|------|------|
| chatRoomId | Long | 채팅방 ID |
| productId | Long | 상품 ID |
| productTitle | String | 상품 제목 (스냅샷) |
| productPrice | Long | 상품 가격 (스냅샷) |
| productImageUrl | String | 상품 대표 이미지 URL (스냅샷) |
| createdAt | String | 생성일시 (ISO 8601) |

#### Response 예시

```json
{
  "code": "CREATED",
  "message": "성공",
  "data": {
    "chatRoomId": 1,
    "productId": 123,
    "productTitle": "귀여운 강아지 옷",
    "productPrice": 15000,
    "productImageUrl": "https://example.com/image.jpg",
    "createdAt": "2025-01-15T10:30:00"
  }
}
```

#### 에러 응답

| 상태 코드 | 에러 코드 | 설명 |
|----------|----------|------|
| 400 | SELF_CHAT_NOT_ALLOWED | 본인 상품에는 채팅할 수 없습니다 |
| 404 | PRODUCT_NOT_FOUND | 상품을 찾을 수 없습니다 |
| 404 | USER_NOT_FOUND | 사용자를 찾을 수 없습니다 |

---

### 3-2. 채팅방 목록 조회 (GET /api/chat/rooms)

- **인증 필요**: 예 (`Bearer` 토큰)
- **설명**: 현재 로그인한 사용자의 채팅방 목록을 조회합니다. 최근 메시지 시간 기준 내림차순 정렬됩니다.

#### Request Parameters

없음

#### Response Body

| 필드 | 타입 | 설명 |
|------|------|------|
| chatRooms | Array | 채팅방 목록 |
| totalCount | Integer | 전체 채팅방 개수 |

#### chatRooms 배열 항목

| 필드 | 타입 | 설명 |
|------|------|------|
| chatRoomId | Long | 채팅방 ID |
| productId | Long | 상품 ID |
| productTitle | String | 상품 제목 (스냅샷) |
| productPrice | Long | 상품 가격 (스냅샷) |
| productImageUrl | String \| null | 상품 대표 이미지 URL (스냅샷) |
| opponentId | Long \| null | 상대방 사용자 ID (탈퇴 시 null) |
| opponentNickname | String | 상대방 닉네임 (탈퇴 시 "알 수 없는 사용자") |
| opponentProfileImageUrl | String \| null | 상대방 프로필 이미지 URL |
| lastMessage | String \| null | 최근 메시지 내용 (100자 미리보기) |
| lastMessageTime | String \| null | 최근 메시지 시간 (ISO 8601) |
| hasUnread | Boolean | 안 읽은 메시지 여부 |
| unreadCount | Integer | 안 읽은 메시지 개수 |

#### Response 예시

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": {
    "chatRooms": [
      {
        "chatRoomId": 1,
        "productId": 123,
        "productTitle": "귀여운 강아지 옷",
        "productPrice": 15000,
        "productImageUrl": "https://example.com/product.jpg",
        "opponentId": 20,
        "opponentNickname": "판매자닉네임",
        "opponentProfileImageUrl": "https://example.com/profile.jpg",
        "lastMessage": "안녕하세요! 아직 판매중인가요?",
        "lastMessageTime": "2025-01-15T10:30:00",
        "hasUnread": true,
        "unreadCount": 3
      },
      {
        "chatRoomId": 2,
        "productId": 456,
        "productTitle": "고양이 장난감 세트",
        "productPrice": 8000,
        "productImageUrl": null,
        "opponentId": null,
        "opponentNickname": "알 수 없는 사용자",
        "opponentProfileImageUrl": null,
        "lastMessage": "[차단된 메시지]",
        "lastMessageTime": "2025-01-14T15:20:00",
        "hasUnread": false,
        "unreadCount": 0
      }
    ],
    "totalCount": 2
  }
}
```

---

### 3-3. 채팅 내역 조회 (GET /api/chat/rooms/{chatRoomId}/messages)

- **인증 필요**: 예 (`Bearer` 토큰)
- **설명**: 채팅방의 메시지 내역을 조회합니다. 첫 페이지 조회 시 안 읽은 메시지가 자동으로 읽음 처리됩니다.

#### Path Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| chatRoomId | Long | 예 | 채팅방 ID |

#### Query Parameters

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| page | Integer | 아니오 | 0 | 페이지 번호 (0부터 시작) |
| size | Integer | 아니오 | 50 | 페이지 크기 |

#### Response Body

| 필드 | 타입 | 설명 |
|------|------|------|
| messages | Array | 메시지 목록 (오래된순 정렬) |
| currentPage | Integer | 현재 페이지 번호 |
| totalPages | Integer | 전체 페이지 수 |
| totalElements | Long | 전체 메시지 개수 |
| hasNext | Boolean | 다음 페이지 존재 여부 |
| hasPrevious | Boolean | 이전 페이지 존재 여부 |

#### messages 배열 항목

| 필드 | 타입 | 설명 |
|------|------|------|
| messageId | Long | 메시지 ID |
| senderId | Long | 발신자 사용자 ID |
| senderNickname | String | 발신자 닉네임 (스냅샷) |
| messageType | String | 메시지 타입 (`TEXT`, `IMAGE`, `SYSTEM`) |
| content | String \| null | 메시지 내용 |
| imageUrl | String \| null | 이미지 URL (messageType이 IMAGE인 경우) |
| isBlocked | Boolean | 차단 여부 (개인정보 포함) |
| blockReason | String \| null | 차단 사유 (차단된 경우에만) |
| createdAt | String | 전송 시간 (ISO 8601) |
| isMine | Boolean | 내가 보낸 메시지 여부 |

> **차단된 메시지**: `isBlocked=true`인 메시지는 발신자 본인에게만 표시됩니다. 다른 사용자에게는 목록에서 제외됩니다.

#### Response 예시

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": {
    "messages": [
      {
        "messageId": 1,
        "senderId": 10,
        "senderNickname": "구매자",
        "messageType": "TEXT",
        "content": "안녕하세요! 아직 판매중인가요?",
        "imageUrl": null,
        "isBlocked": false,
        "blockReason": null,
        "createdAt": "2025-01-15T10:30:00",
        "isMine": true
      },
      {
        "messageId": 2,
        "senderId": 20,
        "senderNickname": "판매자",
        "messageType": "TEXT",
        "content": "네! 판매중입니다. 직거래 가능하세요?",
        "imageUrl": null,
        "isBlocked": false,
        "blockReason": null,
        "createdAt": "2025-01-15T10:31:00",
        "isMine": false
      },
      {
        "messageId": 3,
        "senderId": 10,
        "senderNickname": "구매자",
        "messageType": "IMAGE",
        "content": null,
        "imageUrl": "https://example.com/chat-image.jpg",
        "isBlocked": false,
        "blockReason": null,
        "createdAt": "2025-01-15T10:32:00",
        "isMine": true
      },
      {
        "messageId": 4,
        "senderId": 10,
        "senderNickname": "구매자",
        "messageType": "TEXT",
        "content": "제 연락처는 010-1234-5678 입니다",
        "imageUrl": null,
        "isBlocked": true,
        "blockReason": "전화번호 포함",
        "createdAt": "2025-01-15T10:33:00",
        "isMine": true
      }
    ],
    "currentPage": 0,
    "totalPages": 1,
    "totalElements": 4,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

#### 에러 응답

| 상태 코드 | 에러 코드 | 설명 |
|----------|----------|------|
| 403 | CHAT_ROOM_ACCESS_DENIED | 채팅방에 대한 접근 권한이 없습니다 |

---

### 3-4. 채팅방 나가기 (DELETE /api/chat/rooms/{chatRoomId})

- **인증 필요**: 예 (`Bearer` 토큰)
- **설명**: 채팅방에서 나갑니다 (소프트 삭제). 나간 후에는 채팅방 목록에서 제외되며 메시지 전송이 불가합니다.

#### Path Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| chatRoomId | Long | 예 | 채팅방 ID |

#### Response Body

없음 (`data: null`)

#### Response 예시

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": null
}
```

#### 에러 응답

| 상태 코드 | 에러 코드 | 설명 |
|----------|----------|------|
| 400 | CHAT_ROOM_USER_LEFT | 이미 나간 채팅방입니다 |
| 403 | CHAT_ROOM_ACCESS_DENIED | 채팅방에 대한 접근 권한이 없습니다 |

> 나가기 시 상대방에게 시스템 메시지("ㅇㅇ님이 채팅방을 나가셨습니다.")가 WebSocket으로 전송됩니다.

---

## WebSocket API

> **STOMP 프로토콜**을 사용합니다. 연결 → 구독 → 발행 순서로 진행합니다.

### 4-1. WebSocket 연결

#### 연결 URL

| 방식 | URL | 설명 |
|------|-----|------|
| **SockJS** (권장) | `http://localhost:8080/ws-stomp` | SockJS 라이브러리 사용 시 |
| **Pure WebSocket** | `ws://localhost:8080/ws-stomp` | 네이티브 WebSocket 사용 시 |

#### 연결 헤더

| 헤더명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| Authorization | String | 예 | `Bearer <Access Token>` |

#### 연결 에러

| 에러 코드 | 설명 |
|----------|------|
| AUTH_REQUIRED | 인증 토큰이 필요합니다 |
| INVALID_TOKEN | 유효하지 않은 토큰입니다 |

---

### 4-2. 구독 (Subscribe) - 메시지 수신 경로

> **연결 성공 후** 아래 경로들을 구독해야 메시지를 수신할 수 있습니다.

#### 구독 경로 목록

| 경로 | 변수 | 대상 | 설명 |
|------|------|------|------|
| `/topic/chat/{chatRoomId}` | `{chatRoomId}` = 채팅방 ID (Long) | **해당 채팅방 참여자 전체** | 일반 메시지, 시스템 메시지 수신 |
| `/user/queue/chat` | 없음 | **나에게만** | 차단된 메시지 수신 (발신자 본인 확인용) |
| `/user/queue/errors` | 없음 | **나에게만** | 에러 메시지 수신 (디버깅 필수) |

> **중요**: `/user/` 로 시작하는 경로는 **로그인한 본인에게만** 메시지가 전달됩니다.  
> Spring이 내부적으로 세션 ID를 기반으로 라우팅합니다.

#### 구독 예시

```javascript
// 1. 에러 메시지 구독 (필수 - 디버깅용)
// → 나에게만 오는 에러 메시지
client.subscribe('/user/queue/errors', (message) => {
  const error = JSON.parse(message.body);
  console.error('[에러]', error.code, error.message);
});

// 2. 개인 메시지 구독 (차단된 메시지 확인용)
// → 나에게만 오는 차단 메시지 (발신자 본인만 확인 가능)
client.subscribe('/user/queue/chat', (message) => {
  const msg = JSON.parse(message.body);
  if (msg.isBlocked) {
    console.warn('[차단됨]', msg.blockReason);
  }
});

// 3. 채팅방 구독 (채팅방 진입 시)
// → {chatRoomId}에 실제 채팅방 ID를 넣어주세요
const chatRoomId = 123;  // 예: REST API로 조회한 채팅방 ID
client.subscribe(`/topic/chat/${chatRoomId}`, (message) => {
  const msg = JSON.parse(message.body);
  console.log('[메시지]', msg);
});
```

---

### 4-3. 발행 (Publish) - 메시지 전송 경로

> 메시지를 **서버로 전송**할 때 사용합니다.

#### 발행 경로

| 경로 | 설명 |
|------|------|
| `/app/chat/message` | 채팅 메시지 전송 |

#### Request Body (JSON)

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| chatRoomId | Long | **예** | 메시지를 보낼 채팅방 ID |
| content | String | 조건 | 메시지 내용 (최대 1000자, `TEXT` 타입 시 필수) |
| messageType | String | **예** | `TEXT` 또는 `IMAGE` |
| imageUrl | String | 조건 | 이미지 URL (`IMAGE` 타입 시 필수) |

#### 발행 예시 (텍스트 메시지)

```javascript
// 텍스트 메시지 전송
client.publish({
  destination: '/app/chat/message',
  body: JSON.stringify({
    chatRoomId: 123,
    content: '안녕하세요! 아직 판매중인가요?',
    messageType: 'TEXT',
    imageUrl: null
  })
});
```

#### 발행 예시 (이미지 메시지)

```javascript
// 1. 먼저 이미지 업로드 API 호출
// POST /api/images/upload → imageUrl 응답 받음

// 2. 업로드된 URL로 이미지 메시지 전송
client.publish({
  destination: '/app/chat/message',
  body: JSON.stringify({
    chatRoomId: 123,
    content: null,
    messageType: 'IMAGE',
    imageUrl: 'https://cdn.example.com/chat/image123.jpg'
  })
});
```

#### 전송 성공 시 동작

1. 메시지가 DB에 저장됩니다.
2. **일반 메시지**: `/topic/chat/{chatRoomId}` 구독자 전체에게 브로드캐스트됩니다.
3. **차단된 메시지**: `/user/queue/chat`으로 발신자에게만 전송됩니다 (상대방에게는 전송 안 됨).

#### 전송 에러

| 에러 코드 | 설명 |
|----------|------|
| ACCESS_DENIED | 해당 채팅방에 메시지를 보낼 권한이 없습니다 |
| INVALID_MESSAGE | 잘못된 메시지 형식입니다 |
| MESSAGE_SEND_FAILED | 메시지 전송에 실패했습니다 |

> 에러 발생 시 `/user/queue/errors`로 에러 메시지가 전송됩니다.

---

### 4-4. 전체 연결 예시 (JavaScript + SockJS + StompJS)

```javascript
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

// 1. STOMP 클라이언트 생성
const client = new Client({
  // SockJS 사용 시 http:// (Pure WebSocket은 ws://)
  webSocketFactory: () => new SockJS('http://localhost:8080/ws-stomp'),
  
  // 인증 토큰 (필수)
  connectHeaders: {
    Authorization: 'Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...'
  },
  
  // 자동 재연결 설정
  reconnectDelay: 5000,
  heartbeatIncoming: 120000,
  heartbeatOutgoing: 120000,
  
  // 연결 성공 콜백
  onConnect: (frame) => {
    console.log('✅ WebSocket 연결 성공');
    
    // [필수] 에러 구독 - 디버깅에 필수!
    client.subscribe('/user/queue/errors', (message) => {
      const error = JSON.parse(message.body);
      alert(`에러: ${error.message}`);
    });
    
    // [선택] 개인 메시지 구독 - 차단 메시지 확인용
    client.subscribe('/user/queue/chat', (message) => {
      const msg = JSON.parse(message.body);
      if (msg.isBlocked) {
        showBlockedMessage(msg);  // UI에 차단 메시지 표시
      }
    });
  },
  
  // STOMP 에러 콜백
  onStompError: (frame) => {
    console.error('❌ STOMP 에러:', frame.headers['message']);
  },
  
  // WebSocket 연결 해제 콜백
  onWebSocketClose: () => {
    console.warn('⚠️ WebSocket 연결 해제됨');
  }
});

// 2. 연결 시작
client.activate();

// 3. 채팅방 입장 함수
function enterChatRoom(chatRoomId) {
  client.subscribe(`/topic/chat/${chatRoomId}`, (message) => {
    const msg = JSON.parse(message.body);
    renderMessage(msg);  // UI에 메시지 렌더링
  });
}

// 4. 메시지 전송 함수
function sendTextMessage(chatRoomId, content) {
  client.publish({
    destination: '/app/chat/message',
    body: JSON.stringify({
      chatRoomId: chatRoomId,
      content: content,
      messageType: 'TEXT',
      imageUrl: null
    })
  });
}

// 5. 연결 해제 함수
function disconnect() {
  client.deactivate();
}
```

---

### 4-5. 메시지 수신 형식

#### Response Body (ChatMessageResponse)

| 필드 | 타입 | 설명 |
|------|------|------|
| messageId | Long \| null | 메시지 ID (시스템 메시지는 null일 수 있음) |
| chatRoomId | Long | 채팅방 ID |
| senderId | Long \| null | 발신자 사용자 ID |
| senderNickname | String \| null | 발신자 닉네임 |
| messageType | String | 메시지 타입 (`TEXT`, `IMAGE`, `SYSTEM`) |
| content | String \| null | 메시지 내용 |
| imageUrl | String \| null | 이미지 URL |
| isBlocked | Boolean \| null | 차단 여부 |
| blockReason | String \| null | 차단 사유 |
| createdAt | String | 전송 시간 (ISO 8601) |

#### Response 예시 (일반 메시지)

```json
{
  "messageId": 10,
  "chatRoomId": 1,
  "senderId": 20,
  "senderNickname": "판매자",
  "messageType": "TEXT",
  "content": "네, 가능합니다!",
  "imageUrl": null,
  "isBlocked": false,
  "blockReason": null,
  "createdAt": "2025-01-15T10:35:00"
}
```

#### Response 예시 (시스템 메시지)

```json
{
  "messageId": null,
  "chatRoomId": 1,
  "senderId": null,
  "senderNickname": null,
  "messageType": "SYSTEM",
  "content": "구매자님이 채팅방을 나가셨습니다.",
  "imageUrl": null,
  "isBlocked": null,
  "blockReason": null,
  "createdAt": "2025-01-15T11:00:00"
}
```

#### Response 예시 (차단된 메시지 - 발신자에게만 /user/queue/chat으로 전송)

```json
{
  "messageId": 11,
  "chatRoomId": 1,
  "senderId": 10,
  "senderNickname": "구매자",
  "messageType": "TEXT",
  "content": "제 연락처는 010-1234-5678입니다",
  "imageUrl": null,
  "isBlocked": true,
  "blockReason": "전화번호 포함",
  "createdAt": "2025-01-15T10:36:00"
}
```

---

### 4-6. 에러 수신

> `/user/queue/errors` 구독을 통해 에러 메시지를 수신합니다.  
> **디버깅에 필수**이므로 반드시 구독하세요.

#### Response Body

| 필드 | 타입 | 설명 |
|------|------|------|
| code | String | 에러 코드 |
| message | String | 에러 메시지 |
| timestamp | Long | 에러 발생 시간 (Unix timestamp) |

#### Response 예시

```json
{
  "code": "ACCESS_DENIED",
  "message": "해당 채팅방에 대한 접근 권한이 없습니다.",
  "timestamp": 1705312200000
}
```

---

## 에러 코드

### REST API 에러 코드

| HTTP 상태 | 에러 코드 | 메시지 |
|----------|----------|--------|
| 400 | SELF_CHAT_NOT_ALLOWED | 본인과의 채팅은 불가능합니다 |
| 400 | CHAT_ROOM_USER_LEFT | 상대방이 채팅방을 나갔습니다 |
| 400 | MESSAGE_TOO_LONG | 메시지는 1000자를 초과할 수 없습니다 |
| 400 | PRIVACY_VIOLATION | 개인정보가 포함된 메시지는 전송할 수 없습니다 |
| 403 | CHAT_ROOM_ACCESS_DENIED | 채팅방에 대한 접근 권한이 없습니다 |
| 404 | CHAT_ROOM_NOT_FOUND | 채팅방을 찾을 수 없습니다 |
| 404 | USER_NOT_FOUND | 사용자를 찾을 수 없습니다 |
| 404 | PRODUCT_NOT_FOUND | 상품을 찾을 수 없습니다 |

### WebSocket 에러 코드

| 에러 코드 | 설명 |
|----------|------|
| AUTH_REQUIRED | 인증 토큰이 필요합니다 |
| INVALID_TOKEN | 유효하지 않은 토큰입니다 |
| ACCESS_DENIED | 해당 채팅방에 대한 접근 권한이 없습니다 |
| INVALID_CHAT_ROOM | 잘못된 채팅방 ID입니다 |
| INVALID_MESSAGE | 잘못된 메시지 형식입니다 |
| MESSAGE_SEND_FAILED | 메시지 전송에 실패했습니다 |

---

## FAQ 및 참고

### Q1. 개인정보 필터링은 어떻게 동작하나요?

전화번호, 이메일, 계좌번호, 주민등록번호가 포함된 메시지는 자동으로 차단됩니다.
- 차단된 메시지는 저장되지만 `isBlocked=true`로 표시됩니다.
- **발신자**에게는 `/user/queue/chat`으로 차단된 메시지가 전송됩니다.
- **수신자**에게는 메시지가 전송되지 않습니다.

### Q2. 안 읽은 메시지 개수는 언제 업데이트되나요?

- **증가**: 상대방이 메시지를 보낼 때 (내가 해당 채팅방에 접속 중이 아닌 경우)
- **초기화**: 채팅 내역 조회 API(`GET /api/chat/rooms/{chatRoomId}/messages`)의 첫 페이지를 조회할 때

### Q3. 상대방이 나간 채팅방에 메시지를 보낼 수 있나요?

아니요. 상대방이 채팅방을 나가면 `CHAT_ROOM_USER_LEFT` 에러가 발생합니다.

### Q4. 채팅방 목록에서 상대방이 "알 수 없는 사용자"로 표시되는 경우는?

상대방이 회원 탈퇴한 경우입니다. 이 경우 `opponentId`가 `null`로 반환됩니다.

### Q5. WebSocket 연결이 끊어지면 어떻게 하나요?

STOMP 클라이언트의 `reconnectDelay` 옵션을 설정하여 자동 재연결을 구현할 수 있습니다:

```javascript
const client = new Client({
  webSocketFactory: () => new SockJS('http://localhost:8080/ws-stomp'),
  reconnectDelay: 5000,  // 5초 후 재연결 시도
  heartbeatIncoming: 120000,  // 서버 heartbeat
  heartbeatOutgoing: 120000,  // 클라이언트 heartbeat
  // ...
});
```

### Q6. 이미지 메시지는 어떻게 전송하나요?

1. 먼저 이미지 업로드 API(`POST /api/images/upload`)로 이미지를 업로드합니다.
2. 반환된 이미지 URL을 `imageUrl` 필드에 담아 `messageType: "IMAGE"`로 전송합니다.

---

## 변경 이력

| 버전 | 날짜 | 내용 |
|------|------|------|
| 1.0.0 | 2025-01-15 | 최초 작성 |
