# 알림 API 문서

> 반려동물 용품 중고거래 서비스 – **알림** API

본 문서는 알림 관련 API에 대한 상세 설명을 제공합니다.  
프론트엔드 개발자가 클라이언트 기능을 구현할 때 필요한 요청/응답 규격과 Enum 목록, SSE 연동 방법을 모두 포함합니다.

---

## 목차

1. [공통 사항](#공통-사항)
2. [Enum 정의](#enum-정의)
3. [REST API](#rest-api)
   - [3-1. 알림 목록 조회](#3-1-알림-목록-조회-get-apinotifications)
   - [3-2. 안 읽은 알림 개수 조회](#3-2-안-읽은-알림-개수-조회-get-apinotificationsunread-count)
   - [3-3. 알림 읽음 처리](#3-3-알림-읽음-처리-patch-apinotificationsnotificationidread)
   - [3-4. 모든 알림 읽음 처리](#3-4-모든-알림-읽음-처리-patch-apinotificationsread-all)
4. [SSE API](#sse-api)
   - [4-1. SSE 연결](#4-1-sse-연결-get-apinotificationsstream)
   - [4-2. 이벤트 타입](#4-2-이벤트-타입)
   - [4-3. 전체 연결 예시](#4-3-전체-연결-예시-javascript-eventSource)
5. [에러 코드](#에러-코드)
6. [FAQ 및 참고](#faq-및-참고)

---

## 공통 사항

### Base URL

```
http://localhost:8080
```

### 공통 헤더 (REST API)

| 헤더명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| Authorization | String | 예 | `Bearer <Access Token>` |
| Content-Type | String | 조건 | JSON 요청: `application/json` |

> 모든 알림 API는 인증이 필수입니다.

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

### NotificationType (알림 타입)

| 값 | 설명 | 사용 예시 |
|----|------|----------|
| `CHAT_NEW_ROOM` | 새로운 채팅방 생성 | 새로운 채팅이 생성되었을 때 |
| `CHAT_NEW_MESSAGE` | 새로운 메시지 도착 | 새로운 메시지가 도착했을 때 |
| `PRODUCT_FAVORITE_STATUS_CHANGED` | 찜한 상품 거래 상태 변경 | 찜한 상품의 거래 상태가 변경되었을 때 |
| `PRODUCT_FAVORITE_PRICE_CHANGED` | 찜한 상품 가격 변동 | 찜한 상품의 가격이 변동되었을 때 |
| `ADMIN_SANCTION` | 어드민 재제 알림 | 관리자에 의해 제재를 받았을 때 |
| `POST_DELETED` | 게시글 삭제 알림 | 내가 작성한 게시글이 삭제 당했을 때 |
| `COMMENT_REPLY` | 댓글에 댓글 달림 | 내가 작성한 댓글에 댓글이 달렸을 경우 |
| `POST_COMMENT` | 게시글에 댓글 달림 | 내가 작성한 게시글에 댓글이 달렸을 경우 |

---

## REST API

### 3-1. 알림 목록 조회 (GET /api/notifications)

- **인증 필요**: 예 (`Bearer` 토큰)
- **설명**: 현재 로그인한 사용자의 알림 목록을 조회합니다. 최신순으로 정렬되며 페이지네이션을 지원합니다.

#### 요청

**URL**: `/api/notifications`

**Method**: `GET`

**Query Parameters**

| 파라미터명 | 타입 | 필수 | 기본값 | 설명 |
|-----------|------|------|--------|------|
| page | Integer | 아니오 | 0 | 페이지 번호 (0부터 시작) |
| size | Integer | 아니오 | 20 | 페이지 크기 |

**예시**

```http
GET /api/notifications?page=0&size=20
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### 응답

**성공 (200 OK)**

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": {
    "page": 0,
    "size": 20,
    "total": 45,
    "content": [
      {
        "notificationId": 1,
        "notificationType": "CHAT_NEW_MESSAGE",
        "title": "새로운 메시지가 도착했습니다",
        "content": "안녕하세요, 상품 문의드립니다.",
        "relatedEntityType": "CHAT_ROOM",
        "relatedEntityId": 123,
        "isRead": false,
        "readAt": null,
        "createdAt": "2025-01-15T10:30:00"
      }
    ],
    "totalPages": 3,
    "hasNext": true,
    "hasPrevious": false,
    "totalElements": 45,
    "numberOfElements": 20
  }
}
```

**응답 필드**

##### PageResultResponse<NotificationListResponse>

| 필드명 | 타입 | 설명 |
|--------|------|------|
| page | Integer | 현재 페이지 번호 (0부터 시작) |
| size | Integer | 페이지 크기 |
| total | Long | 전체 항목 수 |
| content | Array<NotificationListResponse> | 알림 목록 |
| totalPages | Integer | 전체 페이지 수 |
| hasNext | Boolean | 다음 페이지 존재 여부 |
| hasPrevious | Boolean | 이전 페이지 존재 여부 |
| totalElements | Long | 전체 항목 수 (total과 동일) |
| numberOfElements | Long | 현재 페이지의 항목 수 |

##### NotificationListResponse

| 필드명 | 타입 | 설명 |
|--------|------|------|
| notificationId | Long | 알림 ID |
| notificationType | String | 알림 타입 (NotificationType enum 값) |
| title | String | 알림 제목 |
| content | String | 알림 내용 |
| relatedEntityType | String | 관련 엔티티 타입 (예: "CHAT_ROOM", "PRODUCT", "POST" 등) |
| relatedEntityId | Long | 관련 엔티티 ID |
| isRead | Boolean | 읽음 여부 |
| readAt | String (ISO 8601) | 읽은 시간 (null 가능) |
| createdAt | String (ISO 8601) | 알림 생성 시간 |

---

### 3-2. 안 읽은 알림 개수 조회 (GET /api/notifications/unread-count)

- **인증 필요**: 예 (`Bearer` 토큰)
- **설명**: 현재 로그인한 사용자의 안 읽은 알림 개수를 조회합니다.

#### 요청

**URL**: `/api/notifications/unread-count`

**Method**: `GET`

**예시**

```http
GET /api/notifications/unread-count
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### 응답

**성공 (200 OK)**

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": {
    "unreadCount": 5
  }
}
```

**응답 필드**

##### NotificationUnreadCountResponse

| 필드명 | 타입 | 설명 |
|--------|------|------|
| unreadCount | Long | 안 읽은 알림 개수 |

---

### 3-3. 알림 읽음 처리 (PATCH /api/notifications/{notificationId}/read)

- **인증 필요**: 예 (`Bearer` 토큰)
- **설명**: 특정 알림을 읽음 상태로 변경합니다.

#### 요청

**URL**: `/api/notifications/{notificationId}/read`

**Method**: `PATCH`

**Path Parameters**

| 파라미터명 | 타입 | 필수 | 설명 |
|-----------|------|------|------|
| notificationId | Long | 예 | 알림 ID |

**예시**

```http
PATCH /api/notifications/1/read
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### 응답

**성공 (200 OK)**

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": null
}
```

**에러 응답**

- **404 NOT_FOUND**: 알림을 찾을 수 없음
- **403 FORBIDDEN**: 다른 사용자의 알림에 접근 시도

---

### 3-4. 모든 알림 읽음 처리 (PATCH /api/notifications/read-all)

- **인증 필요**: 예 (`Bearer` 토큰)
- **설명**: 현재 로그인한 사용자의 모든 안 읽은 알림을 읽음 상태로 변경합니다.

#### 요청

**URL**: `/api/notifications/read-all`

**Method**: `PATCH`

**예시**

```http
PATCH /api/notifications/read-all
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### 응답

**성공 (200 OK)**

```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": null
}
```

---

## SSE API

### 4-1. SSE 연결 (GET /api/notifications/stream)

- **인증 필요**: 예 (`Bearer` 토큰)
- **설명**: Server-Sent Events를 통한 실시간 알림 수신을 위한 스트림 연결을 생성합니다. 프론트엔드에서는 로그인 직후와 페이지 새로고침 시 이 엔드포인트에 연결해야 합니다.

#### 요청

**URL**: `/api/notifications/stream`

**Method**: `GET`

**Headers**

| 헤더명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| Authorization | String | 예 | `Bearer <Access Token>` |
| Accept | String | 예 | `text/event-stream` |

**예시**

```http
GET /api/notifications/stream
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Accept: text/event-stream
Cache-Control: no-cache
```

#### 응답

**성공 (200 OK)**

- **Content-Type**: `text/event-stream`
- **Connection**: `keep-alive`
- **Cache-Control**: `no-cache`

연결이 성공하면 실시간으로 이벤트를 수신할 수 있습니다.

---

### 4-2. 이벤트 타입

SSE 연결을 통해 수신할 수 있는 이벤트 타입은 다음과 같습니다.

#### connect (연결 성공)

연결 직후 전송되는 초기 이벤트입니다.

**이벤트 형식**

```
event: connect
data: Connect Success
```

#### notification (알림 수신)

새로운 알림이 생성되었을 때 전송되는 이벤트입니다.

**이벤트 형식**

```
event: notification
data: {"notificationId":1,"notificationType":"CHAT_NEW_MESSAGE","title":"새로운 메시지가 도착했습니다","content":"안녕하세요, 상품 문의드립니다.","relatedEntityType":"CHAT_ROOM","relatedEntityId":123,"isRead":false,"readAt":null,"createdAt":"2025-01-15T10:30:00"}
```

**데이터 필드 (NotificationDto)**

| 필드명 | 타입 | 설명 |
|--------|------|------|
| notificationId | Long | 알림 ID |
| notificationType | String | 알림 타입 (NotificationType enum 값) |
| title | String | 알림 제목 |
| content | String | 알림 내용 |
| relatedEntityType | String | 관련 엔티티 타입 (예: "CHAT_ROOM", "PRODUCT", "POST" 등) |
| relatedEntityId | Long | 관련 엔티티 ID |
| isRead | Boolean | 읽음 여부 (항상 false) |
| readAt | String (ISO 8601) | 읽은 시간 (항상 null) |
| createdAt | String (ISO 8601) | 알림 생성 시간 |

> **참고**: `notificationType`은 enum 값이지만 JSON에서는 문자열로 전송됩니다. 예: `"CHAT_NEW_MESSAGE"`

#### ping (Heartbeat)

연결 유지를 위한 주기적 ping 이벤트입니다. 약 15초마다 전송됩니다.

**이벤트 형식**

```
event: ping
data: ping
```

---

### 4-3. 전체 연결 예시 (JavaScript EventSource)

```javascript
// SSE 연결 생성
const token = 'your-access-token';
const eventSource = new EventSource(`http://localhost:8080/api/notifications/stream`, {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

// EventSource는 기본적으로 Authorization 헤더를 지원하지 않으므로
// URL에 토큰을 포함하거나, fetch API를 사용해야 합니다.

// fetch API를 사용한 SSE 연결 (권장)
async function connectSSE() {
  const token = localStorage.getItem('accessToken');
  
  const response = await fetch('http://localhost:8080/api/notifications/stream', {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Accept': 'text/event-stream'
    }
  });

  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = '';

  while (true) {
    const { done, value } = await reader.read();
    
    if (done) {
      console.log('SSE 연결 종료');
      break;
    }

    buffer += decoder.decode(value, { stream: true });
    const lines = buffer.split('\n');
    buffer = lines.pop() || '';

    let eventType = null;
    let data = null;

    for (const line of lines) {
      if (line.startsWith('event:')) {
        eventType = line.substring(6).trim();
      } else if (line.startsWith('data:')) {
        data = line.substring(5).trim();
      }
    }

    if (eventType && data) {
      handleSSEEvent(eventType, data);
    }
  }
}

// 이벤트 처리 함수
function handleSSEEvent(eventType, data) {
  switch (eventType) {
    case 'connect':
      console.log('SSE 연결 성공:', data);
      break;
      
    case 'notification':
      try {
        const notification = JSON.parse(data);
        console.log('새 알림 수신:', notification);
        // 알림 UI 업데이트
        updateNotificationUI(notification);
        // 안 읽은 알림 개수 업데이트
        updateUnreadCount();
      } catch (e) {
        console.error('알림 파싱 오류:', e);
      }
      break;
      
    case 'ping':
      console.log('Heartbeat:', data);
      break;
      
    default:
      console.log('알 수 없는 이벤트:', eventType, data);
  }
}

// 연결 시작
connectSSEEvent();

// 페이지 언로드 시 연결 종료
window.addEventListener('beforeunload', () => {
  eventSource?.close();
});
```

**React 예시**

```typescript
import { useEffect, useRef } from 'react';

function useNotificationSSE(token: string) {
  const eventSourceRef = useRef<EventSource | null>(null);

  useEffect(() => {
    if (!token) return;

    // EventSource는 Authorization 헤더를 지원하지 않으므로
    // fetch API를 사용하거나 URL에 토큰을 포함해야 합니다.
    
    // 방법 1: fetch API 사용 (권장)
    const connectSSE = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/notifications/stream', {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Accept': 'text/event-stream',
          },
        });

        const reader = response.body?.getReader();
        if (!reader) return;

        const decoder = new TextDecoder();
        let buffer = '';

        while (true) {
          const { done, value } = await reader.read();
          if (done) break;

          buffer += decoder.decode(value, { stream: true });
          const lines = buffer.split('\n');
          buffer = lines.pop() || '';

          let eventType: string | null = null;
          let data: string | null = null;

          for (const line of lines) {
            if (line.startsWith('event:')) {
              eventType = line.substring(6).trim();
            } else if (line.startsWith('data:')) {
              data = line.substring(5).trim();
            }
          }

          if (eventType && data) {
            handleEvent(eventType, data);
          }
        }
      } catch (error) {
        console.error('SSE 연결 오류:', error);
      }
    };

    connectSSE();

    return () => {
      // 정리 작업
    };
  }, [token]);

  const handleEvent = (eventType: string, data: string) => {
    switch (eventType) {
      case 'connect':
        console.log('SSE 연결 성공');
        break;
      case 'notification':
        try {
          const notification = JSON.parse(data);
          // 알림 처리 로직
          console.log('새 알림:', notification);
        } catch (e) {
          console.error('알림 파싱 오류:', e);
        }
        break;
      case 'ping':
        // Heartbeat 무시
        break;
    }
  };
}
```

---

## 에러 코드

| 코드 | HTTP 상태 | 설명 |
|------|-----------|------|
| `SUCCESS` | 200 | 성공 |
| `BAD_REQUEST` | 400 | 잘못된 요청 |
| `UNAUTHORIZED` | 401 | 인증 필요 |
| `FORBIDDEN` | 403 | 권한 없음 (다른 사용자의 알림에 접근) |
| `NOT_FOUND` | 404 | 알림을 찾을 수 없음 |
| `INTERNAL_SERVER_ERROR` | 500 | 서버 오류 |

---

## FAQ 및 참고

### Q1. SSE 연결이 자주 끊어집니다.

**A**: SSE 연결은 30초 타임아웃이 설정되어 있습니다. 서버에서 15초마다 `ping` 이벤트를 전송하므로, 클라이언트에서 연결이 끊어지면 자동으로 재연결을 시도해야 합니다.

### Q2. EventSource에서 Authorization 헤더를 어떻게 전달하나요?

**A**: 네이티브 `EventSource`는 커스텀 헤더를 지원하지 않습니다. 다음 방법을 사용하세요:

1. **fetch API 사용** (권장): 위 예시 코드 참조
2. **URL에 토큰 포함**: `?token=xxx` (보안상 권장하지 않음)
3. **쿠키 사용**: 서버에서 쿠키 기반 인증을 지원하는 경우

### Q3. 알림을 받았을 때 어떻게 처리하나요?

**A**: 
1. SSE로 `notification` 이벤트를 수신
2. 알림 목록 API를 호출하여 최신 알림 목록 갱신
3. 안 읽은 알림 개수 API를 호출하여 배지 업데이트
4. 필요시 사용자에게 토스트/팝업 표시

### Q4. 페이지를 새로고침하면 알림이 사라지나요?

**A**: 아니요. 알림은 데이터베이스에 저장되므로 페이지를 새로고침해도 유지됩니다. 페이지 로드 시 알림 목록 API를 호출하여 기존 알림을 표시할 수 있습니다.

### Q5. 알림 타입별로 다른 UI를 표시하고 싶습니다.

**A**: `notificationType` 필드를 확인하여 각 타입에 맞는 UI를 표시하세요. 예:
- `CHAT_NEW_MESSAGE`: 채팅 아이콘 + 채팅방으로 이동
- `PRODUCT_FAVORITE_PRICE_CHANGED`: 상품 아이콘 + 상품 상세 페이지로 이동
- `POST_COMMENT`: 댓글 아이콘 + 게시글 상세 페이지로 이동

### Q6. `relatedEntityType`과 `relatedEntityId`는 어떻게 사용하나요?

**A**: 알림을 클릭했을 때 해당 엔티티로 이동하기 위해 사용합니다. 예:
- `relatedEntityType: "CHAT_ROOM"`, `relatedEntityId: 123` → `/chat/rooms/123`으로 이동
- `relatedEntityType: "PRODUCT"`, `relatedEntityId: 456` → `/products/456`으로 이동
- `relatedEntityType: "POST"`, `relatedEntityId: 789` → `/posts/789`로 이동

---

**문서 버전**: 1.0  
**최종 업데이트**: 2025-01-15
