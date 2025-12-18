# OAuth2 로그인 흐름 상세 가이드

> 반려동물 용품 중고거래 서비스 - **OAuth2 소셜 로그인** 전체 흐름 상세 설명

본 문서는 OAuth2 소셜 로그인(구글, 카카오)의 전체 흐름을 처음 개발하는 개발자도 이해할 수 있도록 단계별로 상세하게 설명합니다.

---

## 목차

- [OAuth2란?](#oauth2란)
- [두 가지 로그인 방식 비교](#두-가지-로그인-방식-비교)
- [방식 1: ID Token 방식 (권장)](#방식-1-id-token-방식-권장)
- [방식 2: Authorization Code Flow](#방식-2-authorization-code-flow)
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

### OAuth2 인증 흐름

본 서비스는 **두 가지 OAuth2 인증 방식**을 지원합니다:

1. **ID Token 방식 (권장)**: 프론트엔드에서 직접 구글 SDK 사용
2. **Authorization Code Flow**: 백엔드가 모든 처리를 담당

---

## 두 가지 로그인 방식 비교

### 비교표

| 항목 | ID Token 방식 (권장) | Authorization Code Flow |
|------|---------------------|------------------------|
| **엔드포인트** | `POST /api/auth/google` | `GET /oauth2/authorization/google` |
| **프론트 작업** | Google Sign-In SDK 사용 | URL 리다이렉트만 |
| **백엔드 작업** | ID Token 검증만 | OAuth2 전체 흐름 처리 |
| **사용자 경험** | 팝업으로 처리 (페이지 이동 없음) | 페이지 리다이렉트 있음 |
| **복잡도** | 낮음 | 높음 |
| **React SPA 적합성** | ✅ 매우 적합 | ⚠️ 가능하지만 UX 제한 |

### 흐름 비교

```
[ID Token 방식 - 권장]
프론트 → 구글(팝업) → 프론트 → 백엔드(/api/auth/google) → 프론트
                      ID Token          JWT 토큰

[Authorization Code Flow]
프론트 → 백엔드 → 구글 → 백엔드 → 프론트
         리다이렉트    콜백      JWT 토큰
```

---

## 방식 1: ID Token 방식 (권장)

> **React SPA에서 권장하는 방식입니다.**  
> 프론트엔드에서 Google Sign-In SDK를 사용하여 ID Token을 받고, 백엔드에서 검증합니다.

### 전체 흐름

```
1. 프론트엔드: Google Sign-In SDK로 로그인 (팝업)
   ↓
2. 구글: ID Token 발급 (프론트엔드로 직접 전달)
   ↓
3. 프론트엔드: POST /api/auth/google (ID Token 전송)
   ↓
4. 백엔드: ID Token 검증 + 사용자 조회/생성 + JWT 토큰 발급
   ↓
5. 프론트엔드: JWT 토큰 수신 및 저장
```

### 프론트엔드 구현 (React)

#### 1. 패키지 설치

```bash
npm install @react-oauth/google
```

#### 2. GoogleOAuthProvider 설정

```javascript
// App.jsx 또는 main.jsx
import { GoogleOAuthProvider } from '@react-oauth/google';

function App() {
  return (
    <GoogleOAuthProvider clientId="YOUR_GOOGLE_CLIENT_ID">
      {/* ... 앱 컴포넌트 */}
    </GoogleOAuthProvider>
  );
}
```

#### 3. 로그인 버튼 구현

```javascript
// LoginPage.jsx
import { GoogleLogin } from '@react-oauth/google';

function LoginPage() {
  const handleGoogleSuccess = async (credentialResponse) => {
    try {
      // 백엔드로 ID Token 전송
      const response = await fetch('http://localhost:8080/api/auth/google', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ 
          idToken: credentialResponse.credential 
        })
      });
      
      const result = await response.json();
      
      if (result.code === 'SUCCESS') {
        // 토큰 저장
        localStorage.setItem('accessToken', result.data.accessToken);
        localStorage.setItem('refreshToken', result.data.refreshToken);
        
        console.log('로그인 성공:', result.data.user);
        // 메인 페이지로 이동
        window.location.href = '/';
      } else {
        console.error('로그인 실패:', result.message);
      }
    } catch (error) {
      console.error('로그인 에러:', error);
    }
  };

  const handleGoogleError = () => {
    console.log('Google 로그인 실패');
  };

  return (
    <div>
      <h1>로그인</h1>
      <GoogleLogin
        onSuccess={handleGoogleSuccess}
        onError={handleGoogleError}
        useOneTap  // 원탭 로그인 (선택사항)
      />
    </div>
  );
}
```

### 백엔드 API 명세

#### 요청

```http
POST /api/auth/google HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "idToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Ij..."
}
```

#### 응답 (성공)

```json
{
  "code": "SUCCESS",
  "message": "Google 로그인 성공",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "user": {
      "email": "user@gmail.com",
      "nickname": "user123",
      "name": "홍길동"
    }
  }
}
```

#### 응답 (실패)

```json
{
  "code": "BAD_REQUEST",
  "message": "유효하지 않은 Google ID Token입니다.",
  "traceId": "abc123..."
}
```

### 백엔드 처리 흐름

```java
// AuthController.java
@PostMapping("/google")
public ResponseEntity<SuccessResponse<LoginResponse>> googleLogin(
        @Valid @RequestBody GoogleLoginRequest request
) {
    // 1. Google ID Token 검증 및 사용자 조회/생성
    User user = googleAuthService.authenticateWithIdToken(request.getIdToken());
    
    // 2. JWT 토큰 생성
    String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getRole().name());
    String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(), user.getRole().name());
    
    // 3. 응답 반환
    return ResponseEntity.ok(new SuccessResponse<>(
            ResponseCode.SUCCESS,
            "Google 로그인 성공",
            LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .user(UserWebDto.from(user))
                    .build()
    ));
}
```

### ID Token 검증 로직

```java
// GoogleIdTokenVerifierService.java
public GoogleUserInfo verify(String idTokenString) {
    // 1. Google의 공개키로 서명 검증
    GoogleIdToken idToken = verifier.verify(idTokenString);
    
    if (idToken == null) {
        return null;  // 유효하지 않은 토큰
    }
    
    // 2. 페이로드에서 사용자 정보 추출
    GoogleIdToken.Payload payload = idToken.getPayload();
    
    return new GoogleUserInfo(
        payload.getSubject(),           // 소셜 ID
        payload.getEmail(),             // 이메일
        (String) payload.get("name"),   // 이름
        (String) payload.get("picture") // 프로필 사진
    );
}
```

### 장점

1. **팝업 로그인**: 페이지 이동 없이 팝업으로 처리
2. **빠른 응답**: 백엔드 왕복 최소화
3. **유연한 UI**: Google Sign-In 버튼 커스터마이징 가능
4. **원탭 로그인**: `useOneTap` 옵션으로 자동 로그인 지원
5. **모바일 앱 연동**: 같은 방식으로 모바일 앱에서도 사용 가능

---

## 방식 2: Authorization Code Flow

> **페이지 리다이렉트 방식입니다.**  
> 백엔드가 OAuth2 전체 흐름을 처리하고, 프론트엔드는 URL 리다이렉트만 합니다.

### 전체 흐름

```
1. 프론트엔드: GET /oauth2/authorization/google 호출
   ↓
2. Spring Security: 구글 인증 페이지로 리다이렉트
   ↓
3. 사용자: 구글에서 로그인 및 동의
   ↓
4. 구글: GET /login/oauth2/code/google?code=xxx&state=xxx 로 리다이렉트
   ↓
5. CustomOAuth2UserService: 사용자 정보 조회/생성
   ↓
6. OAuth2LoginSuccessHandler: JWT 토큰 생성 후 프론트엔드로 리다이렉트
   ↓
7. 프론트엔드: 토큰 수신 및 저장
```

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

### Spring Security 동작

1. **요청 경로 확인**: `/oauth2/authorization/google` 경로를 인식
2. **OAuth2 필터 활성화**: OAuth2 관련 필터들이 자동으로 실행됨
3. **인증 URL 생성**: 구글/카카오 인증 페이지 URL 생성

### SecurityConfig 설정

```java
// SecurityConfig.java
.oauth2Login(oauth2 -> oauth2
    .userInfoEndpoint(userInfo -> userInfo
        .userService(customOAuth2UserService)  // 5단계에서 사용
    )
    .successHandler(oAuth2LoginSuccessHandler)  // 6단계에서 사용
)
```

### 인증 URL 생성 과정

Spring Security는 `application.properties`의 설정을 기반으로 인증 URL을 생성합니다:

**구글의 경우:**
```
https://accounts.google.com/o/oauth2/v2/auth?
  client_id={client-id}&
  redirect_uri={baseUrl}/login/oauth2/code/google&
  response_type=code&
  scope=profile email&
  state={랜덤_문자열}
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

### 리다이렉트

Spring Security는 생성한 인증 URL로 브라우저를 리다이렉트합니다:

```http
HTTP/1.1 302 Found
Location: https://accounts.google.com/o/oauth2/v2/auth?client_id=...
```

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

### OAuth 제공자 동작

사용자가 로그인 및 동의를 완료하면, 구글/카카오는 **인증 코드(Authorization Code)**를 포함하여 우리 서버의 콜백 URL로 리다이렉트합니다.

### 콜백 URL

```
GET /login/oauth2/code/{registrationId}?code={authorization_code}&state={state}
```

**예시:**
```
GET /login/oauth2/code/google?code=4/0AeanS...&state=abc123 HTTP/1.1
Host: localhost:8080
```

### 파라미터 설명

| 파라미터 | 설명 |
|---------|------|
| `code` | 인증 코드 (일회용, 짧은 유효기간) |
| `state` | CSRF 방지를 위한 상태값 (1단계에서 전송한 값과 일치해야 함) |

### Spring Security 동작

1. **state 검증**: 전송한 state와 일치하는지 확인 (CSRF 방지)
2. **인증 코드 교환**: 인증 코드를 Access Token으로 교환
3. **사용자 정보 요청**: Access Token으로 사용자 정보 API 호출

### 인증 코드 → Access Token 교환

Spring Security가 내부적으로 다음 API를 호출합니다:

**구글:**
```http
POST https://oauth2.googleapis.com/token
Content-Type: application/x-www-form-urlencoded

client_id={client-id}&
client_secret={client-secret}&
code={authorization_code}&
grant_type=authorization_code&
redirect_uri={baseUrl}/login/oauth2/code/google
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

### 사용자 정보 요청

Access Token을 받은 후, 사용자 정보를 요청합니다:

**구글:**
```http
GET https://www.googleapis.com/oauth2/v2/userinfo
Authorization: Bearer {access_token}
```

**카카오:**
```http
GET https://kapi.kakao.com/v2/user/me
Authorization: Bearer {access_token}
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

### 호출 시점

`CustomOAuth2UserService`가 `PrincipalDetails`를 반환한 후, `OAuth2LoginSuccessHandler.onAuthenticationSuccess()` 메서드가 자동으로 호출됩니다.

### 코드 흐름

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
    
    // 3. 프론트엔드로 리다이렉트 (토큰을 쿼리 파라미터로 전달)
    String redirectUrl = String.format(
            "%s?accessToken=%s&refreshToken=%s",
            redirectUri,
            accessToken,
            refreshToken
    );
    
    log.info("OAuth2 로그인 성공: email={}, provider={}", user.getEmail(), user.getProvider());
    
    // 4. 리다이렉트
    response.sendRedirect(redirectUrl);
}
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

### ID Token 방식 관련 클래스

#### GoogleIdTokenVerifierService

**역할**: Google ID Token 검증

**주요 메서드:**
- `verify(idToken)`: Google 공개키로 ID Token 서명 검증 및 사용자 정보 추출

**위치**: `web/common/security/GoogleIdTokenVerifierService.java`

#### GoogleAuthService

**역할**: Google ID Token 기반 인증 및 사용자 처리

**주요 메서드:**
- `authenticateWithIdToken(idToken)`: ID Token 검증 후 사용자 조회/생성

**위치**: `web/auth/service/GoogleAuthService.java`

---

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

## 에러 처리

### 1. 이메일 정보 없음

**발생 시점**: 5단계 (CustomOAuth2UserService)

**에러 메시지:**
```
이메일 정보가 필요합니다. 소셜 로그인 시 이메일 제공에 동의해주세요.
```

**원인**: 카카오 로그인 시 이메일 제공에 동의하지 않음

**해결 방법**: 카카오 로그인 시 이메일 제공에 동의해야 함

### 2. 이미 가입된 이메일

**발생 시점**: 5단계 (createNewUser)

**에러 메시지:**
```
이미 가입된 이메일입니다. 일반 로그인을 사용해주세요.
```

**원인**: 일반 회원가입으로 이미 가입된 이메일로 OAuth 로그인 시도

**해결 방법**: 일반 로그인 사용 또는 다른 이메일 사용

### 3. OAuth 제공자 인증 실패

**발생 시점**: 3단계 (구글/카카오 인증 페이지)

**원인:**
- 잘못된 client-id 또는 client-secret
- 리다이렉트 URI 불일치
- OAuth 제공자 서버 오류

**해결 방법**: `application.properties` 설정 확인

### 4. state 불일치

**발생 시점**: 4단계 (콜백 처리)

**원인**: CSRF 공격 시도 또는 세션 만료

**해결 방법**: Spring Security가 자동으로 처리 (요청 거부)

---

## FAQ

### Q1. ID Token 방식과 Authorization Code Flow 중 어떤 것을 사용해야 하나요?

**A**: **React SPA에서는 ID Token 방식을 권장합니다.**

| 상황 | 추천 방식 |
|------|----------|
| React/Vue SPA + 팝업 로그인 원함 | **ID Token 방식** |
| 페이지 리다이렉트 OK | Authorization Code Flow |
| 모바일 앱 연동 예정 | **ID Token 방식** |
| 카카오 로그인도 함께 사용 | Authorization Code Flow (카카오는 ID Token 방식 미지원) |

### Q2. OAuth 로그인 시 별도의 회원가입 API를 호출해야 하나요?

**A**: 아니요. 두 방식 모두 자동 회원가입이 처리됩니다. 
- ID Token 방식: `GoogleAuthService.authenticateWithIdToken()`에서 처리
- Authorization Code Flow: `CustomOAuth2UserService.createNewUser()`에서 처리

### Q3. ID Token 방식에서 프론트엔드가 구글 SDK를 사용하면 client-secret은 어디서 쓰이나요?

**A**: ID Token 방식에서는 **client-secret이 필요 없습니다.** 프론트엔드는 `client-id`만 사용하고, 백엔드는 Google의 공개키로 ID Token 서명을 검증합니다. 이것이 ID Token 방식의 보안적 장점 중 하나입니다.

### Q4. 구글과 카카오 로그인을 동시에 지원하려면?

**A**: 
- **구글**: ID Token 방식 (`POST /api/auth/google`) 또는 Authorization Code Flow 모두 가능
- **카카오**: Authorization Code Flow만 지원 (`/oauth2/authorization/kakao`)

### Q3. OAuth 로그인 사용자는 비밀번호가 없나요?

**A**: 맞습니다. OAuth 로그인 사용자의 `password` 필드는 `null`입니다. 비밀번호 재설정 기능도 사용할 수 없습니다 (일반 회원가입 사용자만 가능).

### Q4. 같은 이메일로 일반 회원가입과 OAuth 로그인을 모두 사용할 수 있나요?

**A**: 아니요. 같은 이메일로는 하나의 Provider만 사용할 수 있습니다. 일반 회원가입으로 가입한 이메일로 OAuth 로그인을 시도하면 에러가 발생합니다.

### Q5. OAuth 로그인 사용자의 닉네임이 중복되면?

**A**: 자동으로 숫자가 추가됩니다. 예: `user`, `user1`, `user2`, ...

### Q6. 프론트엔드에서 토큰을 어떻게 저장해야 하나요?

**A**: `localStorage` 또는 `sessionStorage`에 저장하는 것을 권장합니다. 보안을 위해 `httpOnly` 쿠키를 사용할 수도 있습니다.

### Q7. OAuth 로그인 후 로그아웃은 어떻게 하나요?

**A**: 일반 로그인과 동일하게 `POST /api/auth/logout` API를 호출하면 됩니다. JWT 토큰을 블랙리스트에 등록하여 무효화합니다.

### Q8. 개발 환경에서 OAuth 설정을 테스트하려면?

**A**: 
1. 구글/카카오 개발자 콘솔에서 OAuth 클라이언트 생성
2. 리다이렉트 URI 등록: `http://localhost:8080/login/oauth2/code/{registrationId}`
3. `application.properties`에 client-id와 client-secret 설정

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

### 방식 1: ID Token 방식 (권장 - React SPA)

```
1. 프론트: Google Sign-In SDK로 로그인 (팝업)
2. 프론트: POST /api/auth/google (ID Token 전송)
3. 백엔드: ID Token 검증 → 사용자 조회/생성 → JWT 발급
4. 프론트: JWT 토큰 저장 → 로그인 완료
```

**엔드포인트**: `POST /api/auth/google`

### 방식 2: Authorization Code Flow

```
1. 프론트: /oauth2/authorization/{provider}로 리다이렉트
2. Spring Security: OAuth 제공자 인증 페이지로 리다이렉트
3. 사용자: OAuth 제공자에서 로그인 및 동의
4. OAuth 제공자: 콜백 URL로 인증 코드 전송
5. CustomOAuth2UserService: 사용자 정보 처리 및 User 엔티티 조회/생성
6. OAuth2LoginSuccessHandler: JWT 토큰 생성 및 프론트엔드로 리다이렉트
7. 프론트: 토큰 수신 및 저장
```

**엔드포인트**: `GET /oauth2/authorization/google` 또는 `GET /oauth2/authorization/kakao`

---

두 방식 모두 별도의 회원가입 없이 소셜 계정으로 바로 로그인할 수 있으며, 신규 사용자는 자동으로 회원가입 처리됩니다.

