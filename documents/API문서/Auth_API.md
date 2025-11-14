# Auth API 문서

> 반려동물 용품 중고거래 서비스 - **인증(Auth)** API 문서

---

## 목차

- [공통 사항](#공통-사항)
- [이메일 인증코드 발송](#1-이메일-인증코드-발송)
- [이메일 인증코드 검증](#2-이메일-인증코드-검증)
- [회원가입](#3-회원가입)
- [로그인](#4-로그인)

---

## 공통 사항

### Base URL
```
http://localhost:8080
```

### 공통 응답 형식

#### 성공 응답
```json
{
  "code": {
    "code": 200,
    "message": "성공"
  },
  "message": "성공",
  "data": { ... }
}
```

#### 에러 응답
```json
{
  "code": {
    "code": 400,
    "message": "잘못된 요청"
  },
  "message": "에러 메시지",
  "traceId": "e1e4456f40d648c7a24fc7d5cd85e4af",
  "timestamp": "2025-11-14T15:45:00"
}
```

### HTTP 상태 코드

| 코드 | 설명 |
|------|------|
| 200 | 성공 |
| 201 | 생성됨 |
| 400 | 잘못된 요청 (입력값 검증 실패) |
| 401 | 인증 필요 |
| 403 | 권한 없음 |
| 404 | 찾을 수 없음 |
| 409 | 충돌 (중복 등) |
| 500 | 서버 오류 |

### 공통 헤더

| 헤더명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| Content-Type | String | 예 | application/json |
| X-Trace-Id | String | 아니오 | 요청 추적 ID (자동 생성) |

---

## 1. 이메일 인증코드 발송

회원가입 시 이메일 인증을 위해 인증코드를 발송합니다.

### 엔드포인트

```
POST /api/auth/email/verification/send
```

### 설명

- 사용자가 입력한 이메일 주소로 6자리 랜덤 인증코드를 발송합니다.
- 인증코드는 5분간 유효합니다.
- 같은 이메일로 재요청 시 기존 인증코드는 삭제되고 새로운 인증코드가 발송됩니다.
- 개발 환경에서는 실제 이메일 발송 없이 서버 로그에 인증코드가 출력됩니다.

### Request Body

| 필드명 | 타입 | 필수 | 설명 | 제약조건 |
|--------|------|------|------|----------|
| email | String | 예 | 이메일 주소 | 이메일 형식, 최대 100자 |

### Request Body 예시

```json
{
  "email": "user@example.com"
}
```

### Response

#### 성공 응답 (200 OK)

| 필드명 | 타입 | 설명 |
|--------|------|------|
| code | Object | 응답 코드 정보 |
| code.code | Integer | HTTP 상태 코드 (200) |
| code.message | String | 응답 메시지 ("성공") |
| message | String | 응답 메시지 ("인증 번호를 발송했습니다.") |
| data | String | 응답 데이터 (null) |

#### 성공 응답 예시

```json
{
  "code": {
    "code": 200,
    "message": "성공"
  },
  "message": "인증 번호를 발송했습니다.",
  "data": null
}
```

### 에러 응답

#### 400 Bad Request - 입력값 검증 실패

| 필드명 | 타입 | 설명 |
|--------|------|------|
| code | Object | 응답 코드 정보 |
| code.code | Integer | HTTP 상태 코드 (400) |
| code.message | String | 응답 메시지 ("잘못된 요청") |
| message | String | 에러 메시지 |
| traceId | String | 추적 ID |
| timestamp | String | 에러 발생 시간 |

**에러 메시지 예시:**
- `"이메일은 필수입니다."` - email 필드가 비어있을 때
- `"올바른 이메일 형식이 아닙니다."` - 이메일 형식이 올바르지 않을 때

#### 에러 응답 예시

```json
{
  "code": {
    "code": 400,
    "message": "잘못된 요청"
  },
  "message": "이메일은 필수입니다.",
  "traceId": "e1e4456f40d648c7a24fc7d5cd85e4af",
  "timestamp": "2025-11-14T15:45:00"
}
```

### 사용 예시

#### cURL

```bash
curl -X POST http://localhost:8080/api/auth/email/verification/send \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com"
  }'
```

#### JavaScript (Fetch API)

```javascript
const response = await fetch('http://localhost:8080/api/auth/email/verification/send', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    email: 'user@example.com'
  })
});

const data = await response.json();
console.log(data);
```

#### JavaScript (Axios)

```javascript
const response = await axios.post('http://localhost:8080/api/auth/email/verification/send', {
  email: 'user@example.com'
});

console.log(response.data);
```

---

## 2. 이메일 인증코드 검증

사용자가 입력한 인증코드를 검증합니다.

### 엔드포인트

```
POST /api/auth/email/verification/verify
```

### 설명

- 발송된 인증코드가 올바른지 검증합니다.
- 이메일과 인증코드가 일치하고, 만료되지 않았으며, 아직 인증되지 않은 경우에만 성공합니다.
- 검증 성공 시 해당 인증코드는 인증 완료 처리됩니다.
- 검증 실패 시 적절한 에러 메시지를 반환합니다.

### Request Body

| 필드명 | 타입 | 필수 | 설명 | 제약조건 |
|--------|------|------|------|----------|
| email | String | 예 | 이메일 주소 | 이메일 형식, 최대 100자 |
| verificationCode | String | 예 | 인증코드 | 6자리 숫자 (0-9) |

### Request Body 예시

```json
{
  "email": "user@example.com",
  "verificationCode": "123456"
}
```

### Response

#### 성공 응답 (200 OK)

| 필드명 | 타입 | 설명 |
|--------|------|------|
| code | Object | 응답 코드 정보 |
| code.code | Integer | HTTP 상태 코드 (200) |
| code.message | String | 응답 메시지 ("성공") |
| message | String | 응답 메시지 ("인증이 완료되었습니다.") |
| data | String | 응답 데이터 (null) |

#### 성공 응답 예시

```json
{
  "code": {
    "code": 200,
    "message": "성공"
  },
  "message": "인증이 완료되었습니다.",
  "data": null
}
```

### 에러 응답

#### 400 Bad Request - 입력값 검증 실패

| 필드명 | 타입 | 설명 |
|--------|------|------|
| code | Object | 응답 코드 정보 |
| code.code | Integer | HTTP 상태 코드 (400) |
| code.message | String | 응답 메시지 ("잘못된 요청") |
| message | String | 에러 메시지 |
| traceId | String | 추적 ID |
| timestamp | String | 에러 발생 시간 |

**에러 메시지 예시:**
- `"이메일은 필수입니다."` - email 필드가 비어있을 때
- `"올바른 이메일 형식이 아닙니다."` - 이메일 형식이 올바르지 않을 때
- `"인증코드는 필수입니다."` - verificationCode 필드가 비어있을 때
- `"인증코드는 6자리 숫자여야 합니다."` - 인증코드 형식이 올바르지 않을 때
- `"만료된 인증코드입니다. 인증코드 전송 재시도 부탁드립니다."` - 인증코드가 만료되었거나 존재하지 않을 때

#### 에러 응답 예시

```json
{
  "code": {
    "code": 400,
    "message": "잘못된 요청"
  },
  "message": "만료된 인증코드입니다. 인증코드 전송 재시도 부탁드립니다.",
  "traceId": "e1e4456f40d648c7a24fc7d5cd85e4af",
  "timestamp": "2025-11-14T15:45:00"
}
```

### 사용 예시

#### cURL

```bash
curl -X POST http://localhost:8080/api/auth/email/verification/verify \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "verificationCode": "123456"
  }'
```

#### JavaScript (Fetch API)

```javascript
const response = await fetch('http://localhost:8080/api/auth/email/verification/verify', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    email: 'user@example.com',
    verificationCode: '123456'
  })
});

const data = await response.json();
console.log(data);
```

#### JavaScript (Axios)

```javascript
const response = await axios.post('http://localhost:8080/api/auth/email/verification/verify', {
  email: 'user@example.com',
  verificationCode: '123456'
});

console.log(response.data);
```

---

## 3. 회원가입

새로운 사용자 계정을 생성합니다.

### 엔드포인트

```
POST /api/auth/signup
```

### 설명

- 사용자가 입력한 정보로 회원가입을 처리합니다.
- 이메일 인증코드 검증은 프론트엔드에서 회원가입 버튼을 누르기 전에 완료되어야 합니다.
- 회원가입 시 다음 검증이 수행됩니다:
  - 이메일 중복 검증
  - 닉네임 중복 검증
  - 만 14세 이상 검증
  - 비밀번호 암호화 후 저장

### Request Body

| 필드명 | 타입 | 필수 | 설명 | 제약조건 |
|--------|------|------|------|----------|
| email | String | 예 | 이메일 주소 | 이메일 형식, 최대 100자 |
| password | String | 예 | 비밀번호 | 필수 입력 (유효성 검증은 프론트엔드에서 처리) |
| name | String | 예 | 이름 | 최대 10자 |
| nickname | String | 예 | 닉네임 | 최대 10자 |
| birthDate | String | 예 | 생년월일 | ISO 8601 형식 (YYYY-MM-DD) |
| addressSido | String | 아니오 | 시/도 | 최대 50자 |
| addressGugun | String | 아니오 | 구/군 | 최대 50자 |

### Request Body 예시

```json
{
  "email": "user@example.com",
  "password": "MyPassword123!",
  "name": "홍길동",
  "nickname": "길동이",
  "birthDate": "2000-01-01",
  "addressSido": "서울특별시",
  "addressGugun": "강남구"
}
```

### Response

#### 성공 응답 (201 Created)

| 필드명 | 타입 | 설명 |
|--------|------|------|
| code | Object | 응답 코드 정보 |
| code.code | Integer | HTTP 상태 코드 (201) |
| code.message | String | 응답 메시지 ("생성됨") |
| message | String | 응답 메시지 ("생성됨") |
| data | Object | 생성된 사용자 정보 |

**data 필드 구조:**

| 필드명 | 타입 | 설명 |
|--------|------|------|
| id | Long | 사용자 ID |
| email | String | 이메일 주소 |
| name | String | 이름 |
| nickname | String | 닉네임 |
| birthDate | String | 생년월일 (ISO 8601 형식) |
| addressSido | String | 시/도 |
| addressGugun | String | 구/군 |

#### 성공 응답 예시

```json
{
  "code": {
    "code": 201,
    "message": "생성됨"
  },
  "message": "생성됨",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "name": "홍길동",
    "nickname": "길동이",
    "birthDate": "2000-01-01",
    "addressSido": "서울특별시",
    "addressGugun": "강남구"
  }
}
```

### 에러 응답

#### 400 Bad Request - 입력값 검증 실패

**에러 메시지 예시:**
- `"이메일은 필수입니다."` - email 필드가 비어있을 때
- `"올바른 이메일 형식이 아닙니다."` - 이메일 형식이 올바르지 않을 때
- `"비밀번호는 필수입니다."` - password 필드가 비어있을 때
- `"이름은 필수입니다."` - name 필드가 비어있을 때
- `"이름은 최대 10자까지 입력 가능합니다."` - name이 10자를 초과할 때
- `"닉네임은 필수입니다."` - nickname 필드가 비어있을 때
- `"닉네임은 최대 10자까지 입력 가능합니다."` - nickname이 10자를 초과할 때
- `"생년월일은 필수입니다."` - birthDate 필드가 비어있을 때

#### 409 Conflict - 중복 데이터

**에러 메시지 예시:**
- `"이미 사용 중인 이메일입니다."` - 이메일이 이미 등록되어 있을 때
- `"이미 사용 중인 닉네임입니다."` - 닉네임이 이미 등록되어 있을 때

#### 400 Bad Request - 비즈니스 로직 검증 실패

**에러 메시지 예시:**
- `"만 14세 이상만 회원가입이 가능합니다."` - 생년월일 기준 만 14세 미만일 때

#### 에러 응답 예시

```json
{
  "code": {
    "code": 409,
    "message": "충돌"
  },
  "message": "이미 사용 중인 이메일입니다.",
  "traceId": "e1e4456f40d648c7a24fc7d5cd85e4af",
  "timestamp": "2025-11-14T15:45:00"
}
```

### 사용 예시

#### cURL

```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "MyPassword123!",
    "name": "홍길동",
    "nickname": "길동이",
    "birthDate": "2000-01-01",
    "addressSido": "서울특별시",
    "addressGugun": "강남구"
  }'
```

#### JavaScript (Fetch API)

```javascript
const response = await fetch('http://localhost:8080/api/auth/signup', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    email: 'user@example.com',
    password: 'MyPassword123!',
    name: '홍길동',
    nickname: '길동이',
    birthDate: '2000-01-01',
    addressSido: '서울특별시',
    addressGugun: '강남구'
  })
});

const data = await response.json();
console.log(data);
```

#### JavaScript (Axios)

```javascript
const response = await axios.post('http://localhost:8080/api/auth/signup', {
  email: 'user@example.com',
  password: 'MyPassword123!',
  name: '홍길동',
  nickname: '길동이',
  birthDate: '2000-01-01',
  addressSido: '서울특별시',
  addressGugun: '강남구'
});

console.log(response.data);
```

---

## 4. 로그인

이메일과 비밀번호로 로그인합니다.

### 엔드포인트

```
POST /api/auth/login
```

### 설명

- 이메일과 비밀번호를 사용하여 로그인합니다.
- 로그인 성공 시 JWT Access Token과 Refresh Token을 발급합니다.
- 이후 API 요청 시 `Authorization: Bearer <Access Token>` 헤더를 포함해야 합니다.
- Access Token은 짧은 만료 시간(기본 1시간)을 가지며, Refresh Token은 긴 만료 시간(기본 7일)을 가집니다.

### Request Body

| 필드명 | 타입 | 필수 | 설명 | 제약조건 |
|--------|------|------|------|----------|
| email | String | 예 | 이메일 주소 | 이메일 형식, 최대 100자 |
| password | String | 예 | 비밀번호 | 필수 입력 |

### Request Body 예시

```json
{
  "email": "user@example.com",
  "password": "MyPassword123!"
}
```

### Response

#### 성공 응답 (200 OK)

| 필드명 | 타입 | 설명 |
|--------|------|------|
| code | Object | 응답 코드 정보 |
| code.code | Integer | HTTP 상태 코드 (200) |
| code.message | String | 응답 메시지 ("성공") |
| message | String | 응답 메시지 ("성공") |
| data | Object | 로그인 응답 데이터 |

**data 필드 구조:**

| 필드명 | 타입 | 설명 |
|--------|------|------|
| accessToken | String | JWT Access Token (짧은 만료 시간) |
| refreshToken | String | JWT Refresh Token (긴 만료 시간) |
| user | Object | 사용자 정보 |

**user 필드 구조:**

| 필드명 | 타입 | 설명 |
|--------|------|------|
| id | Long | 사용자 ID |
| email | String | 이메일 주소 |
| name | String | 이름 |
| nickname | String | 닉네임 |
| birthDate | String | 생년월일 (ISO 8601 형식) |
| addressSido | String | 시/도 |
| addressGugun | String | 구/군 |

#### 성공 응답 예시

```json
{
  "code": {
    "code": 200,
    "message": "성공"
  },
  "message": "성공",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "email": "user@example.com",
      "name": "홍길동",
      "nickname": "길동이",
      "birthDate": "2000-01-01",
      "addressSido": "서울특별시",
      "addressGugun": "강남구"
    }
  }
}
```

### 에러 응답

#### 400 Bad Request - 입력값 검증 실패

**에러 메시지 예시:**
- `"이메일은 필수입니다."` - email 필드가 비어있을 때
- `"올바른 이메일 형식이 아닙니다."` - 이메일 형식이 올바르지 않을 때
- `"비밀번호는 필수입니다."` - password 필드가 비어있을 때

#### 401 Unauthorized - 인증 실패

**에러 메시지 예시:**
- `"이메일 또는 비밀번호가 일치하지 않습니다."` - 이메일/비밀번호가 일치하지 않거나, 사용자가 존재하지 않을 때

#### 에러 응답 예시

```json
{
  "code": {
    "code": 401,
    "message": "인증 필요"
  },
  "message": "이메일 또는 비밀번호가 일치하지 않습니다.",
  "traceId": "e1e4456f40d648c7a24fc7d5cd85e4af",
  "timestamp": "2025-11-14T15:45:00"
}
```

### 사용 예시

#### cURL

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "MyPassword123!"
  }'
```

#### JavaScript (Fetch API)

```javascript
const response = await fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    email: 'user@example.com',
    password: 'MyPassword123!'
  })
});

const data = await response.json();
console.log(data);

// 토큰 저장
if (data.data && data.data.accessToken) {
  localStorage.setItem('accessToken', data.data.accessToken);
  localStorage.setItem('refreshToken', data.data.refreshToken);
}
```

#### JavaScript (Axios)

```javascript
const response = await axios.post('http://localhost:8080/api/auth/login', {
  email: 'user@example.com',
  password: 'MyPassword123!'
});

console.log(response.data);

// 토큰 저장
if (response.data.data && response.data.data.accessToken) {
  localStorage.setItem('accessToken', response.data.data.accessToken);
  localStorage.setItem('refreshToken', response.data.data.refreshToken);
}
```

### 인증된 요청 예시

로그인 후 받은 Access Token을 사용하여 인증이 필요한 API를 호출합니다.

#### cURL

```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### JavaScript (Fetch API)

```javascript
const accessToken = localStorage.getItem('accessToken');

const response = await fetch('http://localhost:8080/api/auth/me', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${accessToken}`
  }
});

const data = await response.json();
console.log(data);
```

#### JavaScript (Axios)

```javascript
const accessToken = localStorage.getItem('accessToken');

const response = await axios.get('http://localhost:8080/api/auth/me', {
  headers: {
    'Authorization': `Bearer ${accessToken}`
  }
});

console.log(response.data);
```

---

## 주의사항

### 인증코드 유효성

1. **만료 시간**: 인증코드는 발송 후 5분간만 유효합니다.
2. **일회성**: 인증코드는 한 번만 사용 가능합니다. 검증 성공 후에는 재사용할 수 없습니다.
3. **재발송**: 같은 이메일로 재발송 시 기존 인증코드는 무효화되고 새로운 인증코드가 발송됩니다.

### 개발 환경

- 개발 환경에서는 실제 이메일 발송 없이 서버 콘솔 로그에 인증코드가 출력됩니다.
- 서버 로그에서 `=== 이메일 인증코드 (개발 모드) ===` 부분을 확인하여 인증코드를 확인할 수 있습니다.

### 에러 처리

- 모든 에러는 `ErrorResponse` 형식으로 반환됩니다.
- `traceId`를 포함하여 서버 로그에서 해당 요청을 추적할 수 있습니다.
- 클라이언트는 `traceId`를 사용자에게 표시하여 고객 지원 시 문제 해결에 도움을 줄 수 있습니다.

---

## 업데이트 이력

| 날짜 | 버전 | 변경 내용 |
|------|------|----------|
| 2025-11-14 | 1.0.0 | 초기 문서 작성 |
| 2025-11-14 | 1.1.0 | 회원가입 API 추가 |
| 2025-11-14 | 1.2.0 | 로그인 API 추가 |

