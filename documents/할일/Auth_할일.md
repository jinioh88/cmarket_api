# Auth 기능 구현 할일

> 반려동물 용품 중고거래 서비스 - **Auth** 영역 구현 작업 목록

---

## 개요

본 문서는 `01_Auth.md` 요구사항 정의서를 기반으로 구현할 작업을 순차적으로 정리한 것입니다.
아키텍처 가이드(`architecture-guide.md`) 원칙을 준수하여 구현합니다.

---

## 구현 순서

### Step 1: 기본 인프라 및 공통 설정

#### 1-1. JWT 토큰 관리 라이브러리 추가 및 설정
- **작업 내용**:
  - `cmarket` 모듈에 JWT 의존성 추가 (jjwt-api, jjwt-impl, jjwt-jackson 또는 auth0-java-jwt)
  - JWT 토큰 생성/검증 유틸리티 클래스 생성 (`web/security/JwtTokenProvider`)
  - 필수 메서드 구현:
    - `createAccessToken(String email, String role)`: Access Token 생성
    - `createRefreshToken(String email, String role)`: Refresh Token 생성
    - `validateToken(String token)`: 토큰 유효성 검증
    - `getAuthentication(String token)`: 토큰에서 인증 정보 추출 (Authentication 객체 반환)
  - JWT 설정값 (secret key, access token expiration time, refresh token expiration time) application.properties에 추가
- **출력물**: 
  - `service/cmarket/build.gradle` (의존성 추가)
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/security/JwtTokenProvider.java`
  - `service/cmarket/src/main/resources/application.properties` (JWT 설정)

#### 1-2. 이메일 발송 기능 구현
- **작업 내용**:
  - `cmarket` 모듈에 Spring Mail 의존성 추가
  - 이메일 발송 서비스 인터페이스 및 구현체 생성 (`web/service/EmailService`)
  - 이메일 템플릿 작성 (인증코드 발송용)
  - application.properties에 이메일 서버 설정 추가
- **출력물**:
  - `service/cmarket/build.gradle` (의존성 추가)
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/service/EmailService.java`
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/service/EmailServiceImpl.java`
  - `service/cmarket/src/main/resources/application.properties` (이메일 설정)

#### 1-3. 비밀번호 암호화 설정
- **작업 내용**:
  - Spring Security의 BCryptPasswordEncoder 빈 등록
  - PasswordEncoderConfig에서 @Bean으로 등록
  - 비밀번호 암호화/검증 유틸리티 생성
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/security/PasswordEncoderConfig.java`

#### 1-4. Spring Security 핵심 설정 (STATELESS)
- **작업 내용**:
  - SecurityConfig에서 SecurityFilterChain 빈 등록
  - JWT 기반 설정 (필수):
    - 세션 비활성화: `.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)`
    - 기본 로그인 폼 비활성화: `.formLogin().disable()`
    - HTTP Basic 비활성화: `.httpBasic().disable()`
    - CSRF 비활성화: `.csrf().disable()` (JWT 방식은 세션을 사용하지 않으므로)
  - CORS 설정: 프론트엔드와 통신을 위해 CORS 설정 추가
  - 필터 등록: JwtAuthenticationFilter를 스프링 시큐리티 필터 체인에 등록 (`.addFilterBefore(...)`)
  - 접근 권한 설정:
    - `/api/auth/signup`, `/api/auth/login`, `/api/auth/email/**` 등은 `permitAll()` (모두 허용)
    - 나머지 모든 요청(`anyRequest()`)은 `authenticated()` (인증 필요)
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/security/SecurityConfig.java` (전체 재작성)

---

### Step 2: 도메인 모델 생성

#### 2-1. User 엔티티 생성
- **작업 내용**:
  - `cmarket-domain` 모듈에 User 엔티티 생성
  - 필드: id, email, password, name, nickname, birthDate, addressSido, addressGugun, 
    role (권한, 예: USER, ADMIN), provider (가입 경로: LOCAL, GOOGLE, KAKAO), 
    socialId (소셜 로그인 ID), createdAt, updatedAt, deletedAt
  - 이메일, 닉네임 unique 제약조건
  - 소프트 삭제 지원 (deletedAt)
  - **참고**: provider는 가입 경로를 나타내며, 일반 회원가입은 "LOCAL", 소셜 로그인은 "GOOGLE" 또는 "KAKAO"
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/model/User.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/model/UserRole.java` (enum: USER, ADMIN)
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/model/AuthProvider.java` (enum: LOCAL, GOOGLE, KAKAO)

#### 2-2. EmailVerification 엔티티 생성
- **작업 내용**:
  - 이메일 인증코드 저장용 엔티티 생성
  - 필드: id, email, verificationCode, expiresAt, verifiedAt, createdAt
  - 만료 시간 관리 (예: 5분)
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/model/EmailVerification.java`

#### 2-3. TokenBlacklist 엔티티 생성
- **작업 내용**:
  - 로그아웃된 토큰 관리용 엔티티 생성
  - 필드: id, token, expiresAt, createdAt
  - 토큰 만료 시간과 함께 저장
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/model/TokenBlacklist.java`

#### 2-4. WithdrawalReason 엔티티 생성 (회원 탈퇴 사유)
- **작업 내용**:
  - 회원 탈퇴 사유 저장용 엔티티 생성
  - 필드: id, userId, reason (enum), detailReason, createdAt
  - reason enum: SERVICE_DISSATISFACTION, PRIVACY_CONCERN, LOW_USAGE, COMPETITOR, OTHER
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/model/WithdrawalReason.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/model/WithdrawalReasonType.java` (enum)

---

### Step 3: 도메인 레포지토리 인터페이스 생성

#### 3-1. UserRepository 인터페이스 생성
- **작업 내용**:
  - `JpaRepository<User, Long>` 상속
  - 이메일로 사용자 조회 메서드: `Optional<User> findByEmail(String email)`
  - 닉네임 중복 확인 메서드: `boolean existsByNickname(String nickname)`
  - 이메일 중복 확인 메서드: `boolean existsByEmail(String email)`
  - 소셜 로그인용 조회: `Optional<User> findByProviderAndSocialId(AuthProvider provider, String socialId)`
  - 소프트 삭제된 사용자 제외 조회: `Optional<User> findByEmailAndDeletedAtIsNull(String email)`
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/repository/UserRepository.java`

#### 3-2. EmailVerificationRepository 인터페이스 생성
- **작업 내용**:
  - `JpaRepository<EmailVerification, Long>` 상속
  - 이메일과 인증코드로 조회: `Optional<EmailVerification> findByEmailAndVerificationCode(String email, String code)`
  - 만료된 인증코드 삭제용: `void deleteByExpiresAtBefore(LocalDateTime now)`
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/repository/EmailVerificationRepository.java`

#### 3-3. TokenBlacklistRepository 인터페이스 생성
- **작업 내용**:
  - `JpaRepository<TokenBlacklist, Long>` 상속
  - 토큰 존재 확인: `boolean existsByToken(String token)`
  - 만료된 토큰 삭제: `void deleteByExpiresAtBefore(LocalDateTime now)`
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/repository/TokenBlacklistRepository.java`

#### 3-4. WithdrawalReasonRepository 인터페이스 생성
- **작업 내용**:
  - `JpaRepository<WithdrawalReason, Long>` 상속
  - 기본 CRUD 제공
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/repository/WithdrawalReasonRepository.java`

---

### Step 4: 이메일 인증 기능 구현

#### 4-1. 이메일 인증코드 발송 API
- **작업 내용**:
  - 컨트롤러: `POST /api/auth/email/verification/send`
  - 웹 DTO: `EmailVerificationSendRequest` (email 필드)
  - 앱 서비스: 이메일 형식 검증 → 6자리 랜덤 인증코드 생성 → EmailVerification 저장 → 이메일 발송
  - 응답: `SuccessResponse` (메시지: "인증 번호를 발송했습니다.")
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/controller/AuthController.java` (일부)
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/dto/EmailVerificationSendRequest.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/app/dto/EmailVerificationSendCommand.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/app/service/EmailVerificationService.java`

#### 4-2. 이메일 인증코드 검증 API
- **작업 내용**:
  - 컨트롤러: `POST /api/auth/email/verification/verify`
  - 웹 DTO: `EmailVerificationVerifyRequest` (email, verificationCode)
  - 앱 서비스: 인증코드 조회 → 만료 여부 확인 → 검증 완료 처리
  - 응답: `SuccessResponse` 또는 만료 시 에러 메시지
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/controller/AuthController.java` (일부)
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/dto/EmailVerificationVerifyRequest.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/app/dto/EmailVerificationVerifyCommand.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/app/service/EmailVerificationService.java` (메서드 추가)

---

### Step 5: 일반 회원가입 구현 (FR-001)

#### 5-1. 회원가입 웹 DTO 및 앱 DTO 생성
- **작업 내용**:
  - 웹 DTO: `SignUpRequest` (email, emailVerificationCode, password, name, nickname, birthDate, addressSido, addressGugun)
  - 검증 어노테이션 추가 (@Email, @Size, @Pattern 등)
  - 앱 DTO: `SignUpCommand`
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/dto/SignUpRequest.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/app/dto/SignUpCommand.java`

#### 5-2. 회원가입 앱 서비스 구현
- **작업 내용**:
  - `AuthService.signUp()` 메서드 구현
  - 이메일 중복 검증
  - 이메일 인증코드 검증
  - 닉네임 중복 검증
  - 비밀번호 유효성 검증 (10-30자, 대소문자/숫자/특수문자 포함)
  - 생년월일로 만 14세 이상 검증
  - 비밀번호 암호화 후 User 엔티티 저장
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/app/service/AuthService.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/app/dto/UserDto.java` (응답용)

#### 5-3. 회원가입 컨트롤러 구현
- **작업 내용**:
  - `POST /api/auth/signup` 엔드포인트
  - 웹 DTO → 앱 DTO 변환
  - 앱 서비스 호출
  - `SuccessResponse<UserDto>` 반환
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/controller/AuthController.java` (일부)
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/dto/UserWebDto.java`

---

### Step 6: 일반 로그인 구현 (FR-002)

#### 6-1. 로그인 웹 DTO 및 앱 DTO 생성
- **작업 내용**:
  - 웹 DTO: `LoginRequest` (email, password)
  - 앱 DTO: `LoginCommand`
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/dto/LoginRequest.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/app/dto/LoginCommand.java`

#### 6-2. 로그인 앱 서비스 구현
- **작업 내용**:
  - `AuthService.login()` 메서드 구현
  - 이메일로 사용자 조회 (소프트 삭제된 사용자 제외)
  - 비밀번호 검증 (PasswordEncoder 사용)
  - 로그인 응답 DTO 반환 (user 정보만, 토큰은 컨트롤러에서 생성)
  - **참고**: AuthenticationManager는 컨트롤러에서 사용하여 인증 처리
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/app/service/AuthService.java` (메서드 추가)
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/app/dto/LoginResponse.java`

#### 6-3. 로그인 컨트롤러 구현
- **작업 내용**:
  - `POST /api/auth/login` 엔드포인트
  - 웹 DTO → 앱 DTO 변환
  - AuthenticationManager를 사용하여 사용자 인증 (UsernamePasswordAuthenticationToken 생성 후 authenticate 호출)
  - 인증 성공 시: JwtTokenProvider를 사용해 Access Token과 Refresh Token 생성
  - LoginResponse에 accessToken, refreshToken, user 정보 포함
  - `SuccessResponse<LoginResponse>` 반환
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/controller/AuthController.java` (일부)
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/dto/LoginResponse.java` (accessToken, refreshToken 필드 포함)

#### 6-4. JWT 인증 필터 구현
- **작업 내용**:
  - JWT 토큰 검증 필터 생성 (`OncePerRequestFilter` 상속)
  - 역할: 클라이언트가 API 요청 시 헤더에 `Authorization: Bearer <TOKEN>`을 담아 보내면, 이 필터가 토큰을 가로채 검증
  - 동작: 토큰이 유효하면, SecurityContextHolder에 인증 정보(Authentication)를 저장
  - 블랙리스트 토큰 검증 (TokenBlacklistRepository 사용)
  - 토큰이 없거나 유효하지 않으면 필터 통과 (인증 실패는 SecurityConfig에서 처리)
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/security/JwtAuthenticationFilter.java`

---

### Step 7: 로그아웃 구현 (FR-004)

#### 7-1. 로그아웃 컨트롤러 구현
- **작업 내용**:
  - `GET /api/auth/logout` 엔드포인트
  - 인증된 사용자의 토큰을 TokenBlacklist에 저장
  - 토큰 만료 시간 계산하여 저장
  - `SuccessResponse` 반환
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/controller/AuthController.java` (일부)
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/app/service/AuthService.java` (logout 메서드 추가)

---

### Step 8: 소셜 로그인 구현 (FR-003)

#### 8-1. OAuth2 의존성 및 설정 추가
- **작업 내용**:
  - Spring Security OAuth2 Client 의존성 추가 (`spring-boot-starter-oauth2-client`)
  - application.properties에 구글, 카카오 OAuth2 설정 추가
  - 구글과 카카오에서 발급받은 client-id, client-secret 등록
- **출력물**:
  - `service/cmarket/build.gradle` (의존성 추가)
  - `service/cmarket/src/main/resources/application.properties` (OAuth2 설정)

#### 8-2. CustomOAuth2UserService 구현
- **작업 내용**:
  - `DefaultOAuth2UserService`를 상속받아 `CustomOAuth2UserService` 생성
  - `loadUser` 메서드를 오버라이드
  - 역할: 구글/카카오로부터 사용자 정보를 받아온 직후 호출
  - 동작:
    - 받아온 정보(이메일, provider ID 등)로 UserRepository를 조회
    - 회원이 아니면: 이 정보를 바탕으로 User를 생성(자동 회원가입)하고 DB에 저장 (Provider는 "GOOGLE" 또는 "KAKAO")
    - 회원이면: 정보를 업데이트할 수 있음
    - DB에 저장된 User 정보(또는 새로 생성된 User 정보)를 담아 PrincipalDetails 같은 커스텀 객체를 반환
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/security/CustomOAuth2UserService.java`
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/security/PrincipalDetails.java` (커스텀 UserDetails 구현체)

#### 8-3. OAuth2LoginSuccessHandler 구현
- **작업 내용**:
  - `AuthenticationSuccessHandler`를 구현
  - 역할: OAuth 로그인이 최종 성공했을 때 호출
  - 동작:
    - CustomOAuth2UserService가 반환한 User 정보를 가져옴 (PrincipalDetails에서 추출)
    - 이 User 정보를 기반으로 우리 서비스의 JWT(Access/Refresh Token)를 생성 (JwtTokenProvider 사용)
    - 이 토큰을 프론트엔드에게 전달 (보통 프론트엔드 URL로 리다이렉트시키면서, URL 쿼리 파라미터에 토큰을 담아 보냄)
    - 예: `https://frontend.com/oauth-redirect?accessToken=...&refreshToken=...`
  - **참고**: OAuth2 로그인은 별도의 컨트롤러가 필요 없으며, 핸들러에서 모든 처리를 완료
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/security/OAuth2LoginSuccessHandler.java`

#### 8-4. SecurityConfig에 OAuth2 설정 추가
- **작업 내용**:
  - SecurityConfig에 `.oauth2Login()` 설정 추가
  - 로그인 성공 시: `.successHandler(myOAuth2LoginSuccessHandler)`
  - 사용자 정보 처리 시: `.userInfoEndpoint().userService(myCustomOAuth2UserService)`
  - OAuth2 로그인 엔드포인트도 `permitAll()`에 추가
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/security/SecurityConfig.java` (수정)

---

### Step 9: 비밀번호 찾기 구현

#### 9-1. 비밀번호 재설정 인증코드 발송 API
- **작업 내용**:
  - 컨트롤러: `POST /api/auth/password/reset/send`
  - 웹 DTO: `PasswordResetSendRequest` (email)
  - 앱 서비스: 이메일로 사용자 조회 → 인증코드 생성 및 발송
  - 응답: `SuccessResponse` (메시지: "인증 번호를 발송했습니다.")
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/controller/AuthController.java` (일부)
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/dto/PasswordResetSendRequest.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/app/service/AuthService.java` (메서드 추가)

#### 9-2. 비밀번호 재설정 API
- **작업 내용**:
  - 컨트롤러: `PATCH /api/auth/password/reset`
  - 웹 DTO: `PasswordResetRequest` (email, verificationCode, newPassword, confirmPassword)
  - 앱 서비스: 인증코드 검증 → 비밀번호 유효성 검증 → 비밀번호 일치 확인 → 비밀번호 변경
  - 응답: `SuccessResponse`
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/controller/AuthController.java` (일부)
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/dto/PasswordResetRequest.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/app/service/AuthService.java` (메서드 추가)

---

### Step 10: 회원 탈퇴 구현 (FR-007)

#### 10-1. 회원 탈퇴 웹 DTO 및 앱 DTO 생성
- **작업 내용**:
  - 웹 DTO: `WithdrawalRequest` (reason, detailReason)
  - 검증: reason 필수, detailReason 2-500자 (선택)
  - 앱 DTO: `WithdrawalCommand`
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/dto/WithdrawalRequest.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/app/dto/WithdrawalCommand.java`

#### 10-2. 회원 탈퇴 앱 서비스 구현
- **작업 내용**:
  - `AuthService.withdraw()` 메서드 구현
  - 진행 중인 거래 확인 (향후 구현 예정이면 주석 처리)
  - 탈퇴 사유 저장 (WithdrawalReason)
  - 사용자 소프트 삭제 (deletedAt 설정)
  - 소셜 로그인인 경우 소셜 연결 끊기 (향후 구현 예정이면 주석 처리)
  - 관련 데이터 삭제 (게시글, 댓글 등 - 향후 구현 예정이면 주석 처리)
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/app/service/AuthService.java` (메서드 추가)

#### 10-3. 회원 탈퇴 컨트롤러 구현
- **작업 내용**:
  - `DELETE /api/auth/withdraw` 엔드포인트
  - 인증 필수 (`@PreAuthorize("isAuthenticated()")`)
  - 현재 로그인한 사용자 정보 추출
  - 앱 서비스 호출
  - 로그아웃 처리 (토큰 블랙리스트 추가)
  - `SuccessResponse` 반환
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/controller/AuthController.java` (일부)

---

### Step 11: 추가 기능 및 개선

#### 11-1. 닉네임 중복 확인 API
- **작업 내용**:
  - 컨트롤러: `GET /api/auth/nickname/check?nickname={nickname}`
  - 앱 서비스: 닉네임 중복 확인
  - 응답: `SuccessResponse<Boolean>` (true: 사용 가능, false: 중복)
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/controller/AuthController.java` (일부)
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/app/service/AuthService.java` (메서드 추가)

#### 11-2. 만료된 인증코드 정리 스케줄러 (선택)
- **작업 내용**:
  - 주기적으로 만료된 EmailVerification, TokenBlacklist 삭제
  - `@Scheduled` 어노테이션 사용
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/scheduler/CleanupScheduler.java`

#### 11-3. 커스텀 예외 클래스 생성
- **작업 내용**:
  - `EmailAlreadyExistsException`
  - `NicknameAlreadyExistsException`
  - `InvalidVerificationCodeException`
  - `ExpiredVerificationCodeException`
  - `InvalidPasswordException`
  - `UserNotFoundException`
  - `AuthenticationFailedException`
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/app/exception/` 패키지 내 예외 클래스들

#### 11-4. GlobalExceptionHandler에 커스텀 예외 처리 추가
- **작업 내용**:
  - 각 커스텀 예외에 대한 핸들러 메서드 추가
  - 적절한 HTTP 상태 코드 및 에러 메시지 반환
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/exception/GlobalExceptionHandler.java` (수정)

---

## 구현 시 주의사항

1. **아키텍처 원칙 준수**:
   - 웹 → 앱 → 도메인 의존 방향 준수
   - 도메인 모델은 웹 계층에서 직접 사용 금지
   - DTO 변환 필수

2. **비밀번호 검증**:
   - 10-30자, 대소문자/숫자/특수문자 포함
   - 정규식 패턴: `^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{10,30}$`

3. **이메일 인증코드**:
   - 6자리 랜덤 숫자
   - 만료 시간: 5분
   - 인증 완료 후 삭제 또는 만료 처리

4. **JWT 토큰**:
   - Access Token과 Refresh Token 모두 생성
   - Access Token: 짧은 만료 시간 (예: 1시간)
   - Refresh Token: 긴 만료 시간 (예: 7일)
   - 토큰 만료 시간 설정 (application.properties)
   - 블랙리스트로 로그아웃 처리
   - JwtTokenProvider에서 getAuthentication 메서드로 토큰에서 인증 정보 추출

5. **소프트 삭제**:
   - User 엔티티는 deletedAt으로 소프트 삭제
   - 조회 시 deletedAt이 null인 것만 조회

6. **예외 처리**:
   - 모든 예외는 GlobalExceptionHandler에서 처리
   - traceId 포함하여 로깅
   - 사용자 친화적인 에러 메시지 반환

7. **Spring Security 설정**:
   - STATELESS 세션 정책 필수 (JWT 사용 시)
   - formLogin, httpBasic, CSRF 비활성화
   - CORS 설정 필수 (프론트엔드와 통신)
   - JwtAuthenticationFilter를 필터 체인에 등록
   - 인증이 필요한 엔드포인트와 허용 엔드포인트 명확히 구분

8. **OAuth2 로그인**:
   - CustomOAuth2UserService에서 소셜 사용자 정보를 받아 DB에 저장/업데이트
   - OAuth2LoginSuccessHandler에서 JWT 토큰 생성 후 프론트엔드로 리다이렉트
   - OAuth2 로그인 엔드포인트는 permitAll()에 포함

9. **AuthenticationManager 사용**:
   - 일반 로그인 시 AuthenticationManager를 사용하여 인증 처리
   - UsernamePasswordAuthenticationToken 생성 후 authenticate() 호출

---

## 완료 체크리스트

- [ ] Step 1: 기본 인프라 및 공통 설정
- [ ] Step 2: 도메인 모델 생성
- [ ] Step 3: 도메인 레포지토리 인터페이스 생성
- [ ] Step 4: 이메일 인증 기능 구현
- [ ] Step 5: 일반 회원가입 구현 (FR-001)
- [ ] Step 6: 일반 로그인 구현 (FR-002)
- [ ] Step 7: 로그아웃 구현 (FR-004)
- [ ] Step 8: 소셜 로그인 구현 (FR-003)
- [ ] Step 9: 비밀번호 찾기 구현
- [ ] Step 10: 회원 탈퇴 구현 (FR-007)
- [ ] Step 11: 추가 기능 및 개선

---

## 참고사항

- 각 Step을 완료한 후 사용자 리뷰를 받고 다음 Step을 진행합니다.
- 아키텍처 가이드의 원칙을 반드시 준수합니다.
- 테스트는 각 Step 완료 후 작성합니다.

