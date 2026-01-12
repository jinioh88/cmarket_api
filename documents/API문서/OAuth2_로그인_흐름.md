# OAuth2 로그인 흐름 상세 가이드

> 반려동물 용품 중고거래 서비스 - **OAuth2 소셜 로그인** 전체 흐름 상세 설명

본 문서는 OAuth2 소셜 로그인(구글, 카카오)의 전체 흐름을 처음 개발하는 개발자도 이해할 수 있도록 단계별로 상세하게 설명합니다.

---

## 목차

- [OAuth2란?](#oauth2란)
- [Authorization Code Flow (권장)](#authorization-code-flow-권장) - **패키지 설치 불필요!**
- [백엔드 상세 구현](#백엔드-상세-구현-authorization-code-flow)
- [설정 파일 설명](#설정-파일-설명)
- [주요 클래스 설명](#주요-클래스-설명)
- [에러 처리](#에러-처리)
- [FAQ](#faq)

---

## OAuth2란?

OAuth2(Open Authorization 2.0)는 **제3자 서비스(구글, 카카오 등)의 계정을 사용하여 우리 서비스에 로그인할 수 있게 해주는 인증 프로토콜**입니다.

### OAuth2의 장점

1. **사용자 편의성**: 별도의 회원가입 없이 소셜 계정으로 바로 로그인 가능
2. **보안성**: 비밀번호를 우리 서버에 저장하지 않아도 됨
3. **빠른 온보딩**: 회원가입 절차 없이 즉시 서비스 이용 가능

---

## Authorization Code Flow (권장)


> **패키지 설치 없이 가장 간단하게 구현할 수 있는 방식입니다.**  
> 프론트엔드는 URL 리다이렉트만 하면 되고, 백엔드가 모든 OAuth2 처리를 담당합니다.  
> `client_secret`이 백엔드에만 있어 보안상 안전합니다.

### 전체 흐름

```
1. 프론트엔드: "구글 로그인" 버튼 클릭
   ↓
2. 프론트엔드: window.location.href로 백엔드 OAuth2 엔드포인트 호출
   ↓
3. 백엔드 → 구글: 인증 페이지로 리다이렉트
   ↓
4. 사용자: 구글에서 로그인 및 동의
   ↓
5. 구글 → 백엔드: 인가 코드 전달 (콜백)
   ↓
6. 백엔드: 사용자 조회/생성 + JWT 토큰 발급 + 프론트엔드로 리다이렉트
   ↓
7. 프론트엔드: URL에서 토큰 추출 및 저장
```

### 구글 클라우드 콘솔 설정

#### 1. OAuth 2.0 클라이언트 ID 생성

1. **Google Cloud Console 접속**
   - https://console.cloud.google.com 접속
   - 프로젝트 선택 또는 새 프로젝트 생성

2. **API 및 서비스 > 사용자 인증 정보**
   - 좌측 메뉴에서 "API 및 서비스" > "사용자 인증 정보" 선택
   - 상단의 "+ 사용자 인증 정보 만들기" > "OAuth 클라이언트 ID" 선택

3. **OAuth 동의 화면 설정** (최초 1회)
   - 사용자 유형: 외부 선택
   - 앱 정보 입력:
     - 앱 이름: "커들마켓" (또는 원하는 이름)
     - 사용자 지원 이메일: 본인 이메일
     - 개발자 연락처 정보: 본인 이메일

4. **OAuth 클라이언트 ID 생성**
   - 애플리케이션 유형: **웹 애플리케이션** 선택
   - 이름: "커들마켓" (또는 원하는 이름)

#### 2. 승인된 JavaScript 원본 설정

**설정 위치**: OAuth 2.0 클라이언트 ID 편집 화면 > "승인된 JavaScript 원본"

**설명**: 브라우저에서 OAuth 요청을 시작할 수 있는 도메인 목록입니다.

**등록해야 할 URI**:
```
http://localhost:3000
http://localhost:8080
https://cuddle-market-fe.vercel.app
```

**⚠️ 중요 사항**:
- 백엔드 도메인(`cmarket-api.duckdns.org`)은 **등록하지 않습니다**
- JavaScript 원본은 프론트엔드 도메인만 등록합니다
- HTTP와 HTTPS를 구분하여 등록합니다

#### 3. 승인된 리디렉션 URI 설정

**설정 위치**: OAuth 2.0 클라이언트 ID 편집 화면 > "승인된 리디렉션 URI"

**설명**: 구글이 인가 코드를 전달할 백엔드 콜백 URL 목록입니다.

**등록해야 할 URI**:
```
http://localhost:8080/login/oauth2/code/google
https://cmarket-api.duckdns.org/login/oauth2/code/google
```

**⚠️ 중요 사항**:
- 프론트엔드 도메인(`cuddle-market-fe.vercel.app`)은 **등록하지 않습니다**
- 리디렉션 URI는 백엔드 도메인만 등록합니다
- 프로덕션 환경에서는 HTTPS를 사용합니다
- 경로는 정확히 `/login/oauth2/code/google`이어야 합니다

#### 4. 클라이언트 ID 및 클라이언트 보안 비밀번호 확인

생성 후 다음 정보를 확인합니다:
- **클라이언트 ID**: `13269763480-xxxxx.apps.googleusercontent.com` 형식
- **클라이언트 보안 비밀번호**: `GOCSPX-xxxxx` 형식

이 정보는 백엔드 설정에 사용됩니다.

#### 5. 최종 설정 확인

**승인된 JavaScript 원본**:
```
✅ http://localhost:3000
✅ http://localhost:8080
✅ https://cuddle-market-fe.vercel.app
❌ http://cmarket-api.duckdns.org (등록하지 않음)
```

**승인된 리디렉션 URI**:
```
✅ http://localhost:8080/login/oauth2/code/google
✅ https://cmarket-api.duckdns.org/login/oauth2/code/google
❌ https://cuddle-market-fe.vercel.app/login/oauth2/code/google (등록하지 않음)
```

### 프론트엔드 구현 (React) - 패키지 설치 불필요!

> **⚡ 단 2개의 코드만 작성하면 됩니다!**

#### 1. 환경 변수 설정

프론트엔드 프로젝트 루트에 `.env` 파일 생성:

```bash
# .env (로컬 개발)
VITE_API_BASE_URL=http://localhost:8080

# .env.production (프로덕션)
VITE_API_BASE_URL=https://cmarket-api.duckdns.org
```

**Vercel 배포 시**: Vercel 대시보드 > 프로젝트 설정 > Environment Variables에서 설정

#### 2. 구글 로그인 버튼 (로그인 페이지)

```typescript
// components/SocialLoginButtons.tsx
import { Button } from '@src/components/commons/button/Button'
import google from '@assets/images/google.svg'

export function SocialLoginButtons() {
  // 백엔드 API URL을 환경 변수로 관리
  const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'https://cmarket-api.duckdns.org'

  const handleGoogleLogin = () => {
    // 백엔드 OAuth2 엔드포인트로 리다이렉트
    window.location.href = `${API_BASE_URL}/oauth2/authorization/google`
  }

  return (
    <div className="flex w-full flex-col gap-2">
      <Button 
        iconSrc={google} 
        size="md" 
        className="w-full cursor-pointer bg-[#F2F2F2]" 
        onClick={handleGoogleLogin}
      >
        구글 간편 로그인
      </Button>
    </div>
  )
}
```

**⚠️ 중요 사항**:
- `window.location.href`를 사용하여 전체 페이지 리다이렉트를 수행합니다
- `fetch()`나 `axios`를 사용하지 않습니다 (OAuth2는 브라우저 리다이렉트 방식)
- 백엔드 도메인으로 요청을 보냅니다 (프론트엔드 도메인이 아님)

#### 3. OAuth 리다이렉트 페이지 (토큰 수신)

```typescript
// pages/OAuthRedirect.tsx
import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'

export default function OAuthRedirect() {
  const navigate = useNavigate()

  useEffect(() => {
    // URL에서 토큰 추출
    const params = new URLSearchParams(window.location.search)
    const accessToken = params.get('accessToken')
    const refreshToken = params.get('refreshToken')

    if (accessToken && refreshToken) {
      // 토큰 저장
      localStorage.setItem('accessToken', accessToken)
      localStorage.setItem('refreshToken', refreshToken)
      
      console.log('구글 로그인 성공!')
      
      // 메인 페이지로 이동
      navigate('/')
    } else {
      // 토큰이 없으면 로그인 페이지로
      console.error('로그인 실패: 토큰이 없습니다.')
      navigate('/login')
    }
  }, [navigate])

  return (
    <div style={{ textAlign: 'center', marginTop: '100px' }}>
      <h2>로그인 처리 중...</h2>
      <p>잠시만 기다려주세요.</p>
    </div>
  )
}
```

#### 4. 라우터 설정

```typescript
// App.tsx 또는 router 설정 파일
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import OAuthRedirect from './pages/OAuthRedirect'
import LoginPage from './pages/LoginPage'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<MainPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/oauth-redirect" element={<OAuthRedirect />} />
        {/* ... 기타 라우트 */}
      </Routes>
    </BrowserRouter>
  )
}
```

**⚠️ 중요 사항**:
- `/oauth-redirect` 라우트는 반드시 설정해야 합니다
- 이 경로가 없으면 백엔드에서 리다이렉트할 때 404 에러가 발생합니다

### 로그인 성공 시 리다이렉트 URL 형식

구글 로그인이 성공하면, 백엔드에서 아래 URL로 리다이렉트합니다:

```
http://localhost:3000/oauth-redirect?accessToken=eyJhbGciOiJIUzUxMiJ9...&refreshToken=eyJhbGciOiJIUzUxMiJ9...
```

### 장점

1. **패키지 설치 불필요**: `@react-oauth/google` 등 추가 패키지 없이 구현 가능
2. **최소한의 코드**: 로그인 버튼 + 리다이렉트 페이지 2개만 작성
3. **보안**: `client_secret`이 백엔드에만 있어 안전
4. **안정성**: Spring Security의 검증된 OAuth2 구현 사용
5. **자동 회원가입**: 신규 사용자는 자동으로 회원가입 처리

### 주의사항

- 프론트엔드 서버가 `http://localhost:3000`에서 실행 중이어야 합니다
- `/oauth-redirect` 라우트가 설정되어 있어야 합니다
- `fetch()`나 `axios`로 호출하면 안 됩니다 (반드시 `window.location.href` 사용)
- 백엔드 도메인으로 요청을 보내야 합니다 (프론트엔드 도메인이 아님)

### 백엔드 설정

#### application-prod.properties 설정

```properties
# Google OAuth2
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID:your-google-client-id}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET:your-google-client-secret}
spring.security.oauth2.client.registration.google.scope=profile,email
spring.security.oauth2.client.registration.google.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}

# OAuth2 리다이렉트 URL (프론트엔드)
oauth2.redirect-uri=${OAUTH2_REDIRECT_URI:https://cuddle-market-fe.vercel.app/oauth-redirect}
```

**설정 항목 설명**:

| 설정 항목 | 설명 | 예시 |
|----------|------|------|
| `client-id` | 구글 클라우드 콘솔에서 발급받은 클라이언트 ID | `13269763480-xxxxx.apps.googleusercontent.com` |
| `client-secret` | 구글 클라우드 콘솔에서 발급받은 클라이언트 보안 비밀번호 | `GOCSPX-xxxxx` |
| `scope` | 요청할 사용자 정보 범위 | `profile,email` |
| `redirect-uri` | 구글이 인가 코드를 전달할 백엔드 콜백 URL | `{baseUrl}/login/oauth2/code/google` |
| `oauth2.redirect-uri` | 백엔드가 최종적으로 프론트엔드로 리다이렉트할 URL | `https://cuddle-market-fe.vercel.app/oauth-redirect` |

**⚠️ 중요 사항**:
- `client-id`와 `client-secret`은 환경 변수로 관리해야 합니다 (보안)
- `redirect-uri`는 Spring Security가 자동으로 `{baseUrl}`을 현재 서버 URL로 치환합니다
- `oauth2.redirect-uri`는 프론트엔드 도메인을 사용합니다

#### 환경 변수 설정 (프로덕션)

EC2 또는 Docker 배포 시 환경 변수 설정:

```bash
export GOOGLE_CLIENT_ID="13269763480-xxxxx.apps.googleusercontent.com"
export GOOGLE_CLIENT_SECRET="GOCSPX-xxxxx"
export OAUTH2_REDIRECT_URI="https://cuddle-market-fe.vercel.app/oauth-redirect"
```

---

## 백엔드 상세 구현 (Authorization Code Flow)

> 아래는 백엔드에서 Authorization Code Flow를 처리하는 상세 내용입니다.

### 1단계: 프론트엔드에서 OAuth 로그인 시작

#### 사용자 동작

사용자가 프론트엔드에서 "구글 로그인" 또는 "카카오 로그인" 버튼을 클릭합니다.

#### 프론트엔드 코드 예시

```javascript
// React 예시
const handleGoogleLogin = () => {
  // Spring Security가 자동으로 제공하는 OAuth2 엔드포인트로 리다이렉트
  window.location.href = 'http://localhost:8080/oauth2/authorization/google';
};

// 카카오 로그인
const handleKakaoLogin = () => {
  window.location.href = 'http://localhost:8080/oauth2/authorization/kakao';
};
```

#### HTTP 요청

```http
GET /oauth2/authorization/google HTTP/1.1
Host: localhost:8080
```

#### 중요 사항

- **이 엔드포인트는 컨트롤러에 정의할 필요가 없습니다**
- Spring Security가 `.oauth2Login()` 설정을 통해 자동으로 제공합니다
- `registrationId`(google, kakao)에 따라 엔드포인트가 자동 생성됩니다

#### 엔드포인트 형식

```
/oauth2/authorization/{registrationId}
```

- `{registrationId}`: `application.properties`에 등록된 OAuth2 클라이언트 이름
  - 구글: `google`
  - 카카오: `kakao`

### 2단계: Spring Security OAuth2 필터 처리

#### Spring Security 동작

1. **요청 경로 확인**: `/oauth2/authorization/google` 경로를 인식
2. **OAuth2 필터 활성화**: OAuth2 관련 필터들이 자동으로 실행됨
3. **인증 URL 생성**: 구글/카카오 인증 페이지 URL 생성

#### SecurityConfig 설정

```java
// SecurityConfig.java
.oauth2Login(oauth2 -> oauth2
    .authorizationEndpoint(authorization -> authorization
        .baseUri("/oauth2/authorization")
        .authorizationRequestRepository(cookieAuthorizationRequestRepository)
    )
    .redirectionEndpoint(redirection -> redirection
        .baseUri("/login/oauth2/code/*")
    )
    .tokenEndpoint(token -> token
        .accessTokenResponseClient(customOAuth2AuthorizationCodeTokenResponseClient)
    )
    .userInfoEndpoint(userInfo -> userInfo
        .userService(customOAuth2UserService)  // 5단계에서 사용
    )
    .successHandler(oAuth2LoginSuccessHandler)  // 6단계에서 사용
    .failureHandler(...)
)
```

**설정 항목 설명**:

| 설정 항목 | 설명 |
|----------|------|
| `authorizationEndpoint.baseUri` | OAuth2 인증 시작 엔드포인트 (`/oauth2/authorization/{registrationId}`) |
| `authorizationRequestRepository` | 인증 요청 정보를 쿠키에 저장 (세션 대신) |
| `redirectionEndpoint.baseUri` | 구글이 콜백할 엔드포인트 (`/login/oauth2/code/*`) |
| `accessTokenResponseClient` | 인가 코드로 액세스 토큰을 교환하는 클라이언트 |
| `userService` | 사용자 정보를 처리하는 서비스 |
| `successHandler` | 로그인 성공 시 실행되는 핸들러 |
| `failureHandler` | 로그인 실패 시 실행되는 핸들러 |

#### 인증 URL 생성 과정

Spring Security는 `application.properties`의 설정을 기반으로 인증 URL을 생성합니다:

**구글의 경우:**
```
https://accounts.google.com/o/oauth2/v2/auth?
  client_id=13269763480-xxxxx.apps.googleusercontent.com&
  redirect_uri=https://cmarket-api.duckdns.org/login/oauth2/code/google&
  response_type=code&
  scope=profile email&
  state={랜덤_CSRF_토큰}
```

**카카오의 경우:**
```
https://kauth.kakao.com/oauth/authorize?
  client_id={client-id}&
  redirect_uri={baseUrl}/login/oauth2/code/kakao&
  response_type=code&
  scope=profile_nickname account_email&
  state={랜덤_문자열}
```

#### 리다이렉트

Spring Security는 생성한 인증 URL로 브라우저를 리다이렉트합니다:

```http
HTTP/1.1 302 Found
Location: https://accounts.google.com/o/oauth2/v2/auth?client_id=...
Set-Cookie: oauth2_auth_request_state=...; HttpOnly; Secure; SameSite=Lax
```

**인증 요청 정보 저장**: 
- 인증 요청 정보(state, redirect_uri 등)는 쿠키에 저장됩니다
- 세션 대신 쿠키를 사용하여 STATELESS 세션 정책을 지원합니다

### 3단계: OAuth 제공자(구글/카카오) 인증

### 사용자 동작

1. 브라우저가 구글/카카오 인증 페이지로 이동
2. 사용자가 로그인 (이미 로그인되어 있으면 생략 가능)
3. 사용자가 권한 동의 (최초 1회만 필요)

### 구글 인증 페이지 예시

```
┌─────────────────────────────────────┐
│  Google 계정으로 로그인              │
├─────────────────────────────────────┤
│  이메일: user@gmail.com             │
│  비밀번호: ********                  │
│                                     │
│  [로그인]                           │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│  권한 요청                           │
├─────────────────────────────────────┤
│  cmarket 서비스가 다음 정보에       │
│  액세스하려고 합니다:               │
│                                     │
│  ✓ 이메일 주소                      │
│  ✓ 기본 프로필 정보                 │
│                                     │
│  [허용] [거부]                      │
└─────────────────────────────────────┘
```

### 중요 사항

- **이메일 동의 필수**: 카카오의 경우 이메일 제공에 동의해야 합니다
- **최초 1회만 동의**: 이후 로그인 시 자동으로 처리됩니다
- **state 파라미터**: CSRF 공격 방지를 위한 랜덤 문자열 (Spring Security가 자동 생성)

### 4단계: OAuth 제공자에서 콜백 처리

#### OAuth 제공자 동작

사용자가 로그인 및 동의를 완료하면, 구글/카카오는 **인증 코드(Authorization Code)**를 포함하여 우리 서버의 콜백 URL로 리다이렉트합니다.

#### 콜백 URL

```
GET /login/oauth2/code/{registrationId}?code={authorization_code}&state={state}
```

**예시:**
```http
GET /login/oauth2/code/google?code=4/0AeanS...&state=abc123 HTTP/1.1
Host: cmarket-api.duckdns.org
Cookie: oauth2_auth_request_state=...
```

#### 파라미터 설명

| 파라미터 | 설명 |
|---------|------|
| `code` | 인증 코드 (일회용, 짧은 유효기간) |
| `state` | CSRF 방지를 위한 상태값 (1단계에서 전송한 값과 일치해야 함) |

#### Spring Security 동작

1. **OAuth2LoginAuthenticationFilter**가 요청을 가로챕니다
2. 쿠키에서 인증 요청 정보를 확인하고 CSRF 토큰 검증
3. **인증 코드 교환**: 인증 코드를 Access Token으로 교환
4. **사용자 정보 요청**: Access Token으로 사용자 정보 API 호출

#### 인증 코드 → Access Token 교환

**CustomOAuth2AuthorizationCodeTokenResponseClient**가 인가 코드로 액세스 토큰을 교환합니다:

**구글:**
```http
POST https://oauth2.googleapis.com/token HTTP/1.1
Content-Type: application/x-www-form-urlencoded

code=4/0AeanS...&
client_id=13269763480-xxxxx&
client_secret=GOCSPX-xxxxx&
redirect_uri=https://cmarket-api.duckdns.org/login/oauth2/code/google&
grant_type=authorization_code
```

**카카오:**
```http
POST https://kauth.kakao.com/oauth/token
Content-Type: application/x-www-form-urlencoded

client_id={client-id}&
client_secret={client-secret}&
code={authorization_code}&
grant_type=authorization_code&
redirect_uri={baseUrl}/login/oauth2/code/kakao
```

**응답 예시 (구글):**
```json
{
  "access_token": "ya29.a0AfH6SMB...",
  "expires_in": 3599,
  "id_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "scope": "openid https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile",
  "token_type": "Bearer"
}
```

#### 사용자 정보 요청

**CustomOAuth2UserService**가 액세스 토큰으로 사용자 정보를 조회합니다:

**구글:**
```http
GET https://www.googleapis.com/oauth2/v2/userinfo HTTP/1.1
Authorization: Bearer {access_token}
```

**카카오:**
```http
GET https://kapi.kakao.com/v2/user/me
Authorization: Bearer {access_token}
```

**구글 사용자 정보 응답 예시:**
```json
{
  "sub": "1234567890",
  "email": "user@gmail.com",
  "name": "홍길동",
  "picture": "https://..."
}
```

### 5단계: CustomOAuth2UserService 실행

### 호출 시점

Spring Security가 OAuth 제공자로부터 사용자 정보를 받아온 직후, `CustomOAuth2UserService.loadUser()` 메서드가 자동으로 호출됩니다.

### 코드 흐름

```java
// CustomOAuth2UserService.java
@Override
public OAuth2User loadUser(OAuth2UserRequest userRequest) {
    // 1. 기본 OAuth2UserService로 사용자 정보 가져오기
    OAuth2User oAuth2User = super.loadUser(userRequest);
    
    // 2. Provider 정보 추출 (google, kakao)
    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());
    
    // 3. 사용자 정보 추출
    Map<String, Object> attributes = oAuth2User.getAttributes();
    String email = extractEmail(attributes, provider);
    String socialId = extractSocialId(attributes, provider);
    String name = extractName(attributes, provider);
    String nickname = extractNickname(attributes, provider);
    
    // 4. 이메일 검증
    if (email == null || email.isEmpty()) {
        throw new OAuth2AuthenticationException("이메일 정보가 필요합니다.");
    }
    
    // 5. 기존 사용자 조회
    Optional<User> existingUser = userRepository.findByProviderAndSocialId(provider, socialId);
    
    User user;
    if (existingUser.isPresent()) {
        // 6-1. 기존 사용자: 정보 업데이트
        user = existingUser.get();
        updateUserInfo(user, name, nickname);
    } else {
        // 6-2. 신규 사용자: 자동 회원가입
        user = createNewUser(email, socialId, name, nickname, provider);
    }
    
    // 7. PrincipalDetails 생성 및 반환
    return new PrincipalDetails(user, attributes);
}
```

### 단계별 상세 설명

#### 3-1. 사용자 정보 추출

**구글의 경우:**
```json
{
  "sub": "1234567890",           // 소셜 ID
  "email": "user@gmail.com",     // 이메일
  "name": "홍길동",              // 이름
  "picture": "https://..."       // 프로필 이미지
}
```

**카카오의 경우:**
```json
{
  "id": 1234567890,              // 소셜 ID
  "kakao_account": {
    "email": "user@kakao.com",   // 이메일
    "profile": {
      "nickname": "홍길동"       // 닉네임
    }
  }
}
```

#### 3-2. 이메일 검증

```java
if (email == null || email.isEmpty()) {
    throw new OAuth2AuthenticationException("이메일 정보가 필요합니다. 소셜 로그인 시 이메일 제공에 동의해주세요.");
}
```

- **구글**: 항상 이메일 제공
- **카카오**: 사용자가 이메일 제공에 동의해야 함 (동의하지 않으면 예외 발생)

#### 5. 기존 사용자 조회

```java
Optional<User> existingUser = userRepository.findByProviderAndSocialId(provider, socialId);
```

- `provider`와 `socialId` 조합으로 조회
- 예: `provider=GOOGLE, socialId=1234567890`

#### 6-1. 기존 사용자 정보 업데이트

```java
private void updateUserInfo(User user, String name, String nickname) {
    if (name != null && !name.isEmpty() && user.getName() == null) {
        user.updateName(name);
    }
    if (nickname != null && !nickname.isEmpty() && user.getNickname() == null) {
        user.updateNickname(nickname);
    }
    userRepository.save(user);
}
```

- 이름이나 닉네임이 비어있을 때만 업데이트
- 기존 값이 있으면 유지

#### 6-2. 신규 사용자 자동 회원가입

```java
private User createNewUser(String email, String socialId, String name, String nickname, AuthProvider provider) {
    // 1. 이메일 중복 확인 (다른 Provider로 가입한 경우)
    Optional<User> existingUserByEmail = userRepository.findByEmailAndDeletedAtIsNull(email);
    if (existingUserByEmail.isPresent()) {
        throw new OAuth2AuthenticationException("이미 가입된 이메일입니다. 일반 로그인을 사용해주세요.");
    }
    
    // 2. 닉네임이 없으면 이메일 앞부분 사용
    if (nickname == null || nickname.isEmpty()) {
        nickname = email.split("@")[0];
    }
    
    // 3. 닉네임 중복 확인 및 처리
    String finalNickname = nickname;
    int suffix = 1;
    while (userRepository.existsByNickname(finalNickname)) {
        finalNickname = nickname + suffix;
        suffix++;
    }
    
    // 4. User 엔티티 생성
    User user = User.builder()
            .email(email)
            .password(null)  // 소셜 로그인은 비밀번호 없음
            .name(name != null ? name : "")
            .nickname(finalNickname)
            .birthDate(null)  // 소셜 로그인은 생년월일 정보 없음
            .addressSido(null)
            .addressGugun(null)
            .role(UserRole.USER)
            .provider(provider)  // GOOGLE 또는 KAKAO
            .socialId(socialId)
            .build();
    
    return userRepository.save(user);
}
```

**자동 회원가입 처리 내용:**
1. 이메일 중복 확인: 일반 회원가입으로 이미 가입된 이메일이면 예외 발생
2. 닉네임 자동 생성: 없으면 이메일 앞부분 사용 (예: `user@gmail.com` → `user`)
3. 닉네임 중복 처리: 중복이면 숫자 추가 (예: `user`, `user1`, `user2`, ...)
4. User 엔티티 생성: 비밀번호는 `null`, `provider`와 `socialId` 설정

#### 7. PrincipalDetails 생성

```java
return new PrincipalDetails(user, attributes);
```

- `PrincipalDetails`: Spring Security의 `UserDetails`와 `OAuth2User`를 모두 구현
- 이후 인증 정보로 사용됨

### 6단계: OAuth2LoginSuccessHandler 실행

#### 호출 시점

`CustomOAuth2UserService`가 `PrincipalDetails`를 반환한 후, `OAuth2LoginSuccessHandler.onAuthenticationSuccess()` 메서드가 자동으로 호출됩니다.

#### 코드 흐름

```java
// OAuth2LoginSuccessHandler.java
@Override
public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
) throws IOException {
    // 1. PrincipalDetails에서 User 정보 추출
    PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
    User user = principalDetails.getUser();
    
    // 2. JWT 토큰 생성
    String accessToken = jwtTokenProvider.createAccessToken(
            user.getEmail(),
            user.getRole().name()
    );
    String refreshToken = jwtTokenProvider.createRefreshToken(
            user.getEmail(),
            user.getRole().name()
    );
    
    // 3. OAuth2 인증 관련 쿠키 삭제
    cookieAuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    
    // 4. 프론트엔드로 리다이렉트 (토큰을 쿼리 파라미터로 전달)
    String redirectUrl = String.format(
            "%s?accessToken=%s&refreshToken=%s",
            redirectUri,  // application-prod.properties의 oauth2.redirect-uri
            accessToken,
            refreshToken
    );
    
    log.info("OAuth2 로그인 성공: email={}, provider={}, redirectUri={}, redirectUrl={}", 
            user.getEmail(), user.getProvider(), redirectUri, redirectUrl);
    
    // 5. 리다이렉트
    response.sendRedirect(redirectUrl);
}
```

#### HTTP 응답

```http
HTTP/1.1 302 Found
Location: https://cuddle-market-fe.vercel.app/oauth-redirect?accessToken=eyJhbGci...&refreshToken=eyJhbGci...
```

### 단계별 상세 설명

#### 1. User 정보 추출

```java
PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
User user = principalDetails.getUser();
```

- `authentication.getPrincipal()`: 5단계에서 반환한 `PrincipalDetails` 객체
- `PrincipalDetails.getUser()`: 우리 DB에 저장된 `User` 엔티티

#### 2. JWT 토큰 생성

```java
String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getRole().name());
String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(), user.getRole().name());
```

- **Access Token**: API 요청 시 사용 (기본 1시간 유효)
- **Refresh Token**: Access Token 갱신 시 사용 (기본 7일 유효)
- 토큰에는 `email`과 `role` 정보가 포함됨

#### 3. 프론트엔드 리다이렉트 URL 생성

```java
String redirectUrl = String.format(
        "%s?accessToken=%s&refreshToken=%s",
        redirectUri,  // application.properties의 oauth2.redirect-uri
        accessToken,
        refreshToken
);
```

**예시:**
```
http://localhost:3000/oauth-redirect?accessToken=eyJhbGc...&refreshToken=eyJhbGc...
```

#### 4. 리다이렉트

```java
response.sendRedirect(redirectUrl);
```

- HTTP 302 리다이렉트 응답
- 브라우저가 자동으로 프론트엔드 URL로 이동

### 7단계: 프론트엔드에서 토큰 수신

### 프론트엔드 동작

브라우저가 리다이렉트된 URL에서 토큰을 추출하고 저장합니다.

### 프론트엔드 코드 예시 (React)

```javascript
// oauth-redirect 페이지
import { useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';

function OAuthRedirect() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  useEffect(() => {
    // URL에서 토큰 추출
    const accessToken = searchParams.get('accessToken');
    const refreshToken = searchParams.get('refreshToken');

    if (accessToken && refreshToken) {
      // 로컬 스토리지에 토큰 저장
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);

      // 메인 페이지로 이동
      navigate('/');
    } else {
      // 토큰이 없으면 에러 처리
      navigate('/login?error=oauth_failed');
    }
  }, [searchParams, navigate]);

  return <div>로그인 처리 중...</div>;
}
```

### 이후 API 요청

프론트엔드는 저장한 Access Token을 사용하여 API를 요청합니다:

```javascript
// API 요청 예시
fetch('http://localhost:8080/api/profile/me', {
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
  }
});
```

---

## 설정 파일 설명

### application.properties

```properties
# OAuth2 클라이언트 설정 (구글)
spring.security.oauth2.client.registration.google.client-id=your-google-client-id
spring.security.oauth2.client.registration.google.client-secret=your-google-client-secret
spring.security.oauth2.client.registration.google.scope=profile,email

# OAuth2 클라이언트 설정 (카카오)
spring.security.oauth2.client.registration.kakao.client-id=your-kakao-client-id
spring.security.oauth2.client.registration.kakao.client-secret=your-kakao-client-secret
spring.security.oauth2.client.registration.kakao.scope=profile_nickname,account_email
spring.security.oauth2.client.provider.kakao.authorization-uri=https://kauth.kakao.com/oauth/authorize
spring.security.oauth2.client.provider.kakao.token-uri=https://kauth.kakao.com/oauth/token
spring.security.oauth2.client.provider.kakao.user-info-uri=https://kapi.kakao.com/v2/user/me

# OAuth2 리다이렉트 URL (프론트엔드)
oauth2.redirect-uri=http://localhost:3000/oauth-redirect
```

### 설정 항목 설명

| 설정 항목 | 설명 |
|---------|------|
| `client-id` | OAuth 제공자에서 발급받은 클라이언트 ID |
| `client-secret` | OAuth 제공자에서 발급받은 클라이언트 시크릿 |
| `scope` | 요청할 사용자 정보 범위 (이메일, 프로필 등) |
| `redirect-uri` | OAuth 로그인 성공 후 리다이렉트할 프론트엔드 URL |

### SecurityConfig 설정

```java
.oauth2Login(oauth2 -> oauth2
    .userInfoEndpoint(userInfo -> userInfo
        .userService(customOAuth2UserService)  // 사용자 정보 처리
    )
    .successHandler(oAuth2LoginSuccessHandler)  // 로그인 성공 후 처리
)
```

---

## 주요 클래스 설명

### Authorization Code Flow 관련 클래스

#### CustomOAuth2UserService

**역할**: OAuth 제공자로부터 받은 사용자 정보를 처리하여 우리 DB의 User와 매핑

**주요 메서드:**
- `loadUser()`: OAuth 사용자 정보를 받아 User 엔티티 조회/생성
- `extractEmail()`: Provider별로 이메일 추출
- `extractSocialId()`: Provider별로 소셜 ID 추출
- `createNewUser()`: 신규 사용자 자동 회원가입

**위치**: `web/common/security/CustomOAuth2UserService.java`

#### OAuth2LoginSuccessHandler

**역할**: OAuth 로그인 성공 후 JWT 토큰 생성 및 프론트엔드로 리다이렉트

**주요 메서드:**
- `onAuthenticationSuccess()`: 로그인 성공 시 호출, JWT 토큰 생성 및 리다이렉트

**위치**: `web/common/security/OAuth2LoginSuccessHandler.java`

#### HttpCookieOAuth2AuthorizationRequestRepository

**역할**: STATELESS 세션 정책에서 OAuth2 인증 요청을 쿠키에 저장

**주요 메서드:**
- `saveAuthorizationRequest()`: 인증 요청을 쿠키에 저장
- `loadAuthorizationRequest()`: 쿠키에서 인증 요청 로드
- `removeAuthorizationRequestCookies()`: 인증 관련 쿠키 삭제

**위치**: `web/common/security/HttpCookieOAuth2AuthorizationRequestRepository.java`

---

### 공통 클래스

#### PrincipalDetails

**역할**: Spring Security의 인증 정보를 담는 객체

**특징:**
- `UserDetails`와 `OAuth2User`를 모두 구현
- 일반 로그인과 OAuth 로그인 모두 지원

**위치**: `web/common/security/PrincipalDetails.java`

---

## 에러 처리 및 트러블슈팅

### 일반적인 에러

#### 1. 404 Not Found - `/oauth-redirect` 경로 없음

**증상**: 
```
GET https://cuddle-market-fe.vercel.app/oauth-redirect?accessToken=... 404 Not Found
```

**원인**: 프론트엔드에 `/oauth-redirect` 라우트가 없음

**해결**: 프론트엔드 라우터에 `/oauth-redirect` 라우트 추가

#### 2. redirect_uri_mismatch

**증상**: 구글에서 "redirect_uri_mismatch" 에러 발생

**원인**: 구글 클라우드 콘솔에 등록된 리디렉션 URI와 실제 요청 URI가 일치하지 않음

**해결**: 
- 구글 클라우드 콘솔에서 정확한 URI 등록 확인
- 프로토콜(HTTP/HTTPS) 확인
- 경로 정확성 확인 (`/login/oauth2/code/google`)

#### 3. invalid_client

**증상**: "invalid_client" 에러 발생

**원인**: 클라이언트 ID 또는 클라이언트 보안 비밀번호가 잘못됨

**해결**: 
- `application-prod.properties`의 `client-id`와 `client-secret` 확인
- 환경 변수 설정 확인

#### 4. 이메일 정보 없음

**발생 시점**: 5단계 (CustomOAuth2UserService)

**에러 메시지:**
```
이메일 정보가 필요합니다. 소셜 로그인 시 이메일 제공에 동의해주세요.
```

**원인**: 
- 구글 로그인 시 이메일 제공에 동의하지 않음 (구글은 항상 이메일 제공)
- 카카오 로그인 시 이메일 제공에 동의하지 않음

**해결 방법**: 소셜 로그인 시 이메일 제공에 동의해야 함

#### 5. 이미 가입된 이메일

**발생 시점**: 5단계 (createNewUser)

**원인**: 일반 회원가입으로 이미 가입된 이메일로 OAuth 로그인 시도

**해결 방법**: 
- 일반 로그인 사용
- 또는 기존 계정에 소셜 계정 연동 (자동 처리됨)

#### 6. OAuth 제공자 인증 실패

**발생 시점**: 3단계 (구글/카카오 인증 페이지)

**원인:**
- 잘못된 client-id 또는 client-secret
- 리다이렉트 URI 불일치
- OAuth 제공자 서버 오류

**해결 방법**: `application.properties` 설정 확인

#### 7. state 불일치

**발생 시점**: 4단계 (콜백 처리)

**원인**: CSRF 공격 시도 또는 세션 만료

**해결 방법**: Spring Security가 자동으로 처리 (요청 거부)

### 디버깅 팁

1. **백엔드 로그 확인**:
   - `OAuth2LoginSuccessHandler`의 로그 확인
   - `CustomOAuth2UserService`의 로그 확인

2. **네트워크 탭 확인**:
   - 브라우저 개발자 도구 > Network 탭
   - `/oauth2/authorization/google` 요청 확인
   - `/login/oauth2/code/google` 요청 확인
   - `/oauth-redirect` 요청 확인

3. **쿠키 확인**:
   - 브라우저 개발자 도구 > Application > Cookies
   - `oauth2_auth_request_state` 쿠키 확인

---

## FAQ

### Q1. OAuth 로그인 시 별도의 회원가입 API를 호출해야 하나요?

**A**: 아니요. 자동 회원가입이 처리됩니다.
- Authorization Code Flow: `CustomOAuth2UserService.createNewUser()`에서 처리

### Q2. OAuth 로그인 사용자는 비밀번호가 없나요?

**A**: 맞습니다. OAuth 로그인 사용자의 `password` 필드는 `null`입니다. 비밀번호 재설정 기능도 사용할 수 없습니다 (일반 회원가입 사용자만 가능).

### Q3. 같은 이메일로 일반 회원가입과 OAuth 로그인을 모두 사용할 수 있나요?

**A**: 자동으로 소셜 계정 연동이 처리됩니다. 일반 회원가입으로 가입한 이메일로 OAuth 로그인을 시도하면, `CustomOAuth2UserService.createNewUser()` 메서드에서 기존 계정에 소셜 계정을 연동합니다.

### Q4. 구글 클라우드 콘솔에 프론트엔드 도메인을 등록해야 하나요?

**A**: 
- **승인된 JavaScript 원본**: 프론트엔드 도메인 등록 필요 (`https://cuddle-market-fe.vercel.app`)
- **승인된 리디렉션 URI**: 프론트엔드 도메인 등록 불필요 (백엔드 도메인만 등록: `https://cmarket-api.duckdns.org/login/oauth2/code/google`)

### Q5. 왜 프론트엔드 도메인으로 리다이렉트하나요?

**A**: 백엔드가 OAuth2 인증을 완료한 후, 사용자를 프론트엔드 애플리케이션으로 돌려보내야 하기 때문입니다. 토큰을 쿼리 파라미터로 전달하여 프론트엔드에서 저장하도록 합니다.

### Q6. 로컬 개발 환경과 프로덕션 환경을 어떻게 구분하나요?

**A**: 
- 환경 변수로 관리: `VITE_API_BASE_URL` (프론트엔드), `OAUTH2_REDIRECT_URI` (백엔드)
- 구글 클라우드 콘솔에 로컬과 프로덕션 URI 모두 등록

### Q7. OAuth 로그인 사용자의 닉네임이 중복되면?

**A**: 자동으로 숫자가 추가됩니다. 예: `user`, `user1`, `user2`, ...

### Q8. 프론트엔드에서 토큰을 어떻게 저장해야 하나요?

**A**: `localStorage` 또는 `sessionStorage`에 저장하는 것을 권장합니다. 보안을 위해 `httpOnly` 쿠키를 사용할 수도 있습니다.

### Q9. OAuth 로그인 후 로그아웃은 어떻게 하나요?

**A**: 일반 로그인과 동일하게 `POST /api/auth/logout` API를 호출하면 됩니다. JWT 토큰을 블랙리스트에 등록하여 무효화합니다.

### Q10. 개발 환경에서 OAuth 설정을 테스트하려면?

**A**: 
1. 구글/카카오 개발자 콘솔에서 OAuth 클라이언트 생성
2. 리다이렉트 URI 등록: `http://localhost:8080/login/oauth2/code/{registrationId}`
3. `application.properties`에 client-id와 client-secret 설정

### Q11. 보안상 문제는 없나요?

**A**: 
- `client_secret`은 백엔드에만 있어 안전합니다
- JWT 토큰은 HTTPS로 전달됩니다
- CSRF 토큰으로 요청 변조를 방지합니다

---

## 전체 흐름 다이어그램

```
┌─────────────┐
│  프론트엔드  │
│  (React)    │
└──────┬──────┘
       │ 1. GET /oauth2/authorization/google
       ↓
┌─────────────────────────────────────┐
│  Spring Security OAuth2 필터        │
│  - 인증 URL 생성                    │
│  - 구글 인증 페이지로 리다이렉트    │
└──────┬──────────────────────────────┘
       │ 2. HTTP 302 Redirect
       ↓
┌─────────────┐
│   구글      │
│  인증 페이지 │
└──────┬──────┘
       │ 3. 사용자 로그인 및 동의
       ↓
┌─────────────┐
│   구글      │
│  서버       │
└──────┬──────┘
       │ 4. GET /login/oauth2/code/google?code=xxx
       ↓
┌─────────────────────────────────────┐
│  Spring Security OAuth2 필터        │
│  - 인증 코드 → Access Token 교환    │
│  - 사용자 정보 API 호출             │
└──────┬──────────────────────────────┘
       │ 5. CustomOAuth2UserService.loadUser()
       ↓
┌─────────────────────────────────────┐
│  CustomOAuth2UserService             │
│  - 사용자 정보 추출                 │
│  - 기존 사용자 조회 또는 신규 생성  │
│  - PrincipalDetails 반환            │
└──────┬──────────────────────────────┘
       │ 6. OAuth2LoginSuccessHandler.onAuthenticationSuccess()
       ↓
┌─────────────────────────────────────┐
│  OAuth2LoginSuccessHandler          │
│  - JWT 토큰 생성                   │
│  - 프론트엔드로 리다이렉트          │
└──────┬──────────────────────────────┘
       │ 7. HTTP 302 Redirect
       │    Location: http://localhost:3000/oauth-redirect?accessToken=xxx&refreshToken=xxx
       ↓
┌─────────────┐
│  프론트엔드  │
│  (React)    │
│  - 토큰 추출 및 저장                │
│  - 메인 페이지로 이동               │
└─────────────┘
```

---

## 참고 자료

- [Spring Security OAuth2 공식 문서](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)
- [구글 OAuth2 문서](https://developers.google.com/identity/protocols/oauth2)
- [카카오 OAuth2 문서](https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api)

---

## 요약

### Authorization Code Flow (권장 - 패키지 설치 불필요)

```
1. 프론트: window.location.href로 백엔드 OAuth2 엔드포인트 호출
2. 백엔드 → 구글: 인증 페이지로 리다이렉트
3. 사용자: 구글에서 로그인 및 동의
4. 구글 → 백엔드: 인가 코드 전달
5. 백엔드: 사용자 조회/생성 → JWT 발급 → 프론트로 리다이렉트
6. 프론트: URL에서 토큰 추출 및 저장
```

**엔드포인트**: `GET /oauth2/authorization/google`  
**프론트 필요 코드**: 로그인 버튼 + `/oauth-redirect` 페이지 (총 2개)  
**패키지 설치**: ❌ 불필요

---

**💡 권장사항**: 패키지 설치 없이 간단하게 구현할 수 있는 **Authorization Code Flow**를 사용합니다.

별도의 회원가입 없이 소셜 계정으로 바로 로그인할 수 있으며, 신규 사용자는 자동으로 회원가입 처리됩니다.

