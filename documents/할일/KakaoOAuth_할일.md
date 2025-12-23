# 카카오 OAuth 로그인 구현 할일

> 반려동물 용품 중고거래 서비스 - **카카오 OAuth 로그인** 구현 작업 목록

---

## 개요

본 문서는 카카오 OAuth 로그인 기능을 완전히 구현하기 위한 단계별 작업을 정리한 것입니다.
현재 백엔드 코드는 이미 카카오 로그인을 지원하도록 구현되어 있으나, 카카오 개발자 콘솔 설정 및 실제 연동 테스트가 필요합니다.

아키텍처 가이드(`architecture-guide.md`) 원칙을 준수하여 진행합니다.

---

## 구현 순서

### Step 1: 카카오 개발자 콘솔 설정

#### 1-1. 카카오 개발자 계정 생성 및 앱 등록
- **작업 내용**:
  - [카카오 개발자 콘솔](https://developers.kakao.com/) 접속
  - 카카오 계정으로 로그인
  - "내 애플리케이션" > "애플리케이션 추가하기" 클릭
  - 앱 이름, 사업자명 입력 후 생성
- **출력물**: 
  - 카카오 개발자 콘솔에 등록된 애플리케이션

#### 1-2. 카카오 로그인 활성화
- **작업 내용**:
  - 애플리케이션 선택 > "제품 설정" > "카카오 로그인" 활성화
  - "카카오 로그인 활성화" 버튼 클릭
  - 동의 항목 설정:
    - 필수 동의: 닉네임(`profile_nickname`)
    - 선택 동의: 이메일(`account_email`) - **중요**: 일반 웹페이지 로그인에서는 이메일을 필수로 받을 수 없으므로 선택 동의로 설정
  - **주의사항**:
    - 이메일을 필수 동의로 설정하려면 앱을 **비즈니스 앱으로 전환**해야 함
    - 일반 웹페이지 로그인에서는 이메일이 선택 동의이므로, 사용자가 이메일 제공을 거부할 수 있음
    - 이 경우 백엔드에서 이메일이 없을 때 적절한 에러 메시지를 표시해야 함
- **출력물**: 
  - 카카오 로그인이 활성화된 애플리케이션

#### 1-3. Redirect URI 등록
- **작업 내용**:
  - "제품 설정" > "카카오 로그인" > "Redirect URI 등록"
  - 다음 URI들을 등록:
    - 로컬 개발: `http://localhost:8080/login/oauth2/code/kakao`
    - 운영 환경: `https://your-domain.com/login/oauth2/code/kakao`
  - **주의**: 카카오는 정확한 URI 매칭을 요구하므로 오타 없이 정확히 입력
- **출력물**: 
  - 등록된 Redirect URI 목록

#### 1-4. REST API 키 및 Client Secret 확인
- **작업 내용**:
  - "앱 설정" > "앱 키" 메뉴에서 다음 정보 확인:
    - REST API 키 (Client ID로 사용)
    - Client Secret (보안 > Client Secret에서 생성)
  - Client Secret이 없으면 "Client Secret 코드 발급" 클릭하여 생성
  - **보안 주의**: Client Secret은 절대 공개 저장소에 커밋하지 않음
- **출력물**: 
  - REST API 키 (Client ID)
  - Client Secret

#### 1-5. 동의 항목 설정 확인 (참고)
- **작업 내용**:
  - "제품 설정" > "카카오 로그인" > "동의 항목" 메뉴로 이동
  - **개인정보** 섹션에서 다음 항목 확인:
    - **닉네임** (`profile_nickname`): **필수 동의** 또는 **선택 동의**로 설정
  - **참고**: 
    - 일반 웹페이지 로그인에서는 **이메일을 받을 수 없음** (카카오 정책)
    - "카카오 계정(이메일)" 항목은 선택할 수 없음
    - 현재 구현은 이메일이 없어도 로그인 가능 (임시 이메일 자동 생성)
    - scope에서 `account_email`을 제거하여 이메일 요청 자체를 하지 않음
- **출력물**: 
  - 동의 항목 설정 확인 완료

---

### Step 2: 백엔드 설정 파일 업데이트

#### 2-1. application.properties에 카카오 OAuth2 설정 추가/수정
- **작업 내용**:
  - `service/cmarket/src/main/resources/application.properties` 파일 확인
  - 카카오 OAuth2 설정 섹션 확인 및 수정:
    ```properties
    # 카카오 OAuth2
    spring.security.oauth2.client.registration.kakao.client-id=${KAKAO_CLIENT_ID:your-kakao-client-id}
    spring.security.oauth2.client.registration.kakao.client-secret=${KAKAO_CLIENT_SECRET:your-kakao-client-secret}
    spring.security.oauth2.client.registration.kakao.authorization-grant-type=authorization_code
    spring.security.oauth2.client.registration.kakao.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}
    spring.security.oauth2.client.registration.kakao.scope=profile_nickname
    spring.security.oauth2.client.registration.kakao.client-name=Kakao
    spring.security.oauth2.client.provider.kakao.authorization-uri=https://kauth.kakao.com/oauth/authorize
    spring.security.oauth2.client.provider.kakao.token-uri=https://kauth.kakao.com/oauth/token
    spring.security.oauth2.client.provider.kakao.user-info-uri=https://kapi.kakao.com/v2/user/me
    spring.security.oauth2.client.provider.kakao.user-name-attribute=id
    ```
  - **중요**: `scope`에서 `account_email` 제거 (일반 웹페이지 로그인에서는 이메일을 받을 수 없음)
  - 실제 Client ID와 Client Secret을 환경 변수로 설정하거나, 로컬 개발용으로 직접 입력
  - **보안 권장**: 운영 환경에서는 반드시 환경 변수 사용
- **출력물**: 
  - `service/cmarket/src/main/resources/application.properties` (카카오 OAuth2 설정 완료)

#### 2-2. application-local.properties 확인 및 수정
- **작업 내용**:
  - `service/cmarket/src/main/resources/application-local.properties` 파일 확인
  - 카카오 OAuth2 설정이 올바르게 되어 있는지 확인
  - 실제 Client ID와 Client Secret 값이 올바른지 확인
- **출력물**: 
  - `service/cmarket/src/main/resources/application-local.properties` (검증 완료)

---

### Step 3: 백엔드 코드 검증

#### 3-1. CustomOAuth2UserService 카카오 처리 로직 검증
- **작업 내용**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/common/security/CustomOAuth2UserService.java` 파일 확인
  - 카카오 사용자 정보 추출 메서드 검증:
    - `extractEmail()`: 카카오 계정 이메일 추출 로직 확인
    - `extractSocialId()`: 카카오 사용자 ID 추출 로직 확인
    - `extractName()`: 카카오 닉네임 추출 로직 확인
    - `extractNickname()`: 카카오 닉네임 추출 로직 확인
  - 카카오 응답 구조 확인:
    ```json
    {
      "id": 123456789,
      "kakao_account": {
        "email": "user@example.com",  // 선택적 (일반 웹페이지 로그인에서는 없을 수 있음)
        "profile": {
          "nickname": "사용자닉네임"
        }
      }
    }
    ```
  - **이메일이 없는 경우 처리**: 
    - 카카오의 경우 이메일이 없으면 임시 이메일 생성 (`kakao_{socialId}@kakao.local`)
    - 구글은 항상 이메일이 있어야 하므로 예외 발생
  - null 체크 강화 및 타입 안전성 확인
- **출력물**: 
  - 코드 검증 완료 (필요 시 수정)

#### 3-2. SecurityConfig OAuth2 설정 검증
- **작업 내용**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/common/security/SecurityConfig.java` 파일 확인
  - OAuth2 로그인 설정 확인:
    - `.oauth2Login()` 설정이 올바른지 확인
    - `.authorizationEndpoint()` 설정 확인
    - `.redirectionEndpoint()` 설정 확인
    - `.userInfoEndpoint().userService()` 설정 확인
    - `.successHandler()` 설정 확인
    - `.failureHandler()` 설정 확인
  - 카카오 로그인 엔드포인트가 `permitAll()`에 포함되어 있는지 확인
- **출력물**: 
  - 코드 검증 완료 (필요 시 수정)

#### 3-3. OAuth2LoginSuccessHandler 검증
- **작업 내용**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/common/security/OAuth2LoginSuccessHandler.java` 파일 확인
  - JWT 토큰 생성 로직 확인
  - 프론트엔드 리다이렉트 URL 설정 확인
  - `oauth2.redirect-uri` 설정값 확인
- **출력물**: 
  - 코드 검증 완료 (필요 시 수정)

---

### Step 4: 로컬 환경 테스트

#### 4-1. 백엔드 서버 실행 및 로그 확인
- **작업 내용**:
  - Spring Boot 애플리케이션 실행
  - 카카오 OAuth2 설정이 올바르게 로드되는지 확인
  - 에러 로그가 없는지 확인
  - OAuth2 관련 빈이 정상적으로 등록되었는지 확인
- **출력물**: 
  - 정상 실행된 백엔드 서버
  - 에러 없는 로그

#### 4-2. 카카오 로그인 엔드포인트 접근 테스트
- **작업 내용**:
  - 브라우저에서 `http://localhost:8080/oauth2/authorization/kakao` 접근
  - 카카오 로그인 페이지로 리다이렉트되는지 확인
  - **에러 발생 시 (400 에러: "설정하지 않은 동의 항목: account_email")**:
    - `application.properties` 또는 `application-local.properties`에서 scope 확인
    - `scope=profile_nickname`으로 설정되어 있는지 확인 (account_email 제거)
    - 서버 재시작 후 다시 테스트
  - 카카오 계정으로 로그인 시도
  - 이메일 동의 화면이 나타나지 않아야 함 (scope에서 제거했으므로)
  - 동의 후 백엔드로 리다이렉트되는지 확인
- **출력물**: 
  - 카카오 로그인 플로우 정상 동작 확인

#### 4-3. 사용자 정보 저장 및 JWT 토큰 발급 테스트
- **작업 내용**:
  - 카카오 로그인 완료 후 프론트엔드 리다이렉트 URL 확인
  - URL 쿼리 파라미터에 `accessToken`과 `refreshToken`이 포함되어 있는지 확인
  - 데이터베이스에서 사용자 정보가 올바르게 저장되었는지 확인:
    - `provider`가 `KAKAO`인지 확인
    - `socialId`가 올바르게 저장되었는지 확인
    - `email`이 임시 이메일 형식(`kakao_{socialId}@kakao.local`)으로 저장되었는지 확인
    - `nickname`이 올바르게 저장되었는지 확인 (카카오 닉네임 또는 자동 생성된 닉네임)
  - JWT 토큰이 유효한지 확인 (토큰 디코딩하여 내용 확인)
  - **참고**: 이메일이 임시 이메일이어도 로그인 및 인증은 정상 작동함
- **출력물**: 
  - 사용자 정보 저장 확인
  - JWT 토큰 발급 확인

#### 4-4. 기존 사용자 연동 테스트
- **작업 내용**:
  - 동일한 이메일로 일반 회원가입된 계정이 있는 경우
  - 카카오 로그인 시 기존 계정에 소셜 계정이 연동되는지 확인
  - `provider`와 `socialId`가 업데이트되는지 확인
- **출력물**: 
  - 기존 계정 연동 기능 확인

#### 4-5. 에러 케이스 테스트
- **작업 내용**:
  - 이메일 동의를 하지 않은 경우 에러 처리 확인
  - 카카오 로그인 취소 시 에러 처리 확인
  - 잘못된 Redirect URI 설정 시 에러 확인
  - 에러 메시지가 프론트엔드로 올바르게 전달되는지 확인
- **출력물**: 
  - 에러 케이스 처리 확인

---

### Step 5: 프론트엔드 연동 가이드 작성

#### 5-1. 카카오 로그인 버튼 구현 가이드 작성
- **작업 내용**:
  - 프론트엔드에서 카카오 로그인 버튼 클릭 시 동작 설명
  - 리다이렉트 URL: `http://localhost:8080/oauth2/authorization/kakao`
  - 예시 코드 작성:
    ```javascript
    const handleKakaoLogin = () => {
      window.location.href = 'http://localhost:8080/oauth2/authorization/kakao';
    };
    ```
- **출력물**: 
  - 프론트엔드 연동 가이드 문서

#### 5-2. OAuth 리다이렉트 처리 가이드 작성
- **작업 내용**:
  - 프론트엔드에서 OAuth 리다이렉트 URL 처리 방법 설명
  - URL 쿼리 파라미터에서 `accessToken`, `refreshToken` 추출
  - 토큰을 로컬 스토리지 또는 쿠키에 저장
  - 메인 페이지로 리다이렉트
  - 에러 케이스 처리 (쿼리 파라미터에 `error`가 있는 경우)
- **출력물**: 
  - OAuth 리다이렉트 처리 가이드 문서

---

### Step 6: 운영 환경 배포 준비

#### 6-1. 운영 환경 Redirect URI 등록
- **작업 내용**:
  - 카카오 개발자 콘솔에서 운영 환경 Redirect URI 추가
  - 운영 환경 URL: `https://your-domain.com/login/oauth2/code/kakao`
  - **주의**: 도메인 변경 시 반드시 카카오 개발자 콘솔에서도 업데이트 필요
- **출력물**: 
  - 운영 환경 Redirect URI 등록 완료

#### 6-2. 환경 변수 설정
- **작업 내용**:
  - 운영 환경에서 카카오 OAuth2 설정을 환경 변수로 관리
  - `KAKAO_CLIENT_ID` 환경 변수 설정
  - `KAKAO_CLIENT_SECRET` 환경 변수 설정
  - `OAUTH2_REDIRECT_URI` 환경 변수 설정 (프론트엔드 리다이렉트 URL)
  - Docker Compose 또는 배포 환경 설정 파일에 환경 변수 추가
- **출력물**: 
  - 환경 변수 설정 완료
  - 배포 설정 파일 업데이트

#### 6-3. 운영 환경 테스트
- **작업 내용**:
  - 운영 환경에서 카카오 로그인 테스트
  - 모든 기능이 정상 동작하는지 확인
  - 로그 확인하여 에러가 없는지 확인
- **출력물**: 
  - 운영 환경 테스트 완료

---

## 구현 시 주의사항

1. **보안**:
   - Client Secret은 절대 공개 저장소에 커밋하지 않음
   - 환경 변수 또는 시크릿 관리 시스템 사용
   - 운영 환경에서는 반드시 환경 변수로 관리

2. **Redirect URI**:
   - 카카오 개발자 콘솔에 등록한 Redirect URI와 백엔드 설정이 정확히 일치해야 함
   - 로컬 개발과 운영 환경의 URI를 모두 등록
   - URI는 대소문자 구분하며, 경로 끝의 슬래시도 구분함

3. **이메일 동의 (중요)**:
   - 카카오는 이메일 정보 제공에 대한 별도 동의가 필요함
   - **일반 웹페이지 로그인에서는 이메일을 받을 수 없음** (카카오 정책)
   - **해결 방법**: 
     - scope에서 `account_email` 제거 (이미 적용됨)
     - 이메일이 없으면 임시 이메일 생성 (`kakao_{socialId}@kakao.local`)
     - 소셜 ID로 사용자를 식별하므로 이메일이 없어도 로그인 가능
   - **비즈니스 앱으로 전환**하면 이메일을 필수로 받을 수 있지만, 일반 웹페이지 로그인에서는 불가능
   - 현재 구현: 이메일이 없어도 로그인 가능 (임시 이메일 자동 생성)

4. **사용자 정보 추출**:
   - 카카오 API 응답 구조: `kakao_account.email`, `kakao_account.profile.nickname`
   - null 체크 필수
   - 이메일이 없는 경우 예외 처리

5. **기존 계정 연동**:
   - 동일한 이메일로 가입된 계정이 있으면 소셜 계정 연동
   - `linkSocialAccount()` 메서드 사용
   - 사용자에게 연동 사실을 알림

6. **에러 처리**:
   - OAuth2 인증 실패 시 사용자 친화적인 에러 메시지 제공
   - 프론트엔드로 에러 정보 전달
   - 로그에 상세한 에러 정보 기록

---

## 완료 체크리스트

- [ ] Step 1: 카카오 개발자 콘솔 설정
  - [ ] 1-1. 카카오 개발자 계정 생성 및 앱 등록
  - [ ] 1-2. 카카오 로그인 활성화
  - [ ] 1-3. Redirect URI 등록
  - [ ] 1-4. REST API 키 및 Client Secret 확인
  - [ ] 1-5. 동의 항목 설정 확인 및 수정 (중요)
- [ ] Step 2: 백엔드 설정 파일 업데이트
  - [ ] 2-1. application.properties에 카카오 OAuth2 설정 추가/수정
  - [ ] 2-2. application-local.properties 확인 및 수정
- [x] Step 3: 백엔드 코드 검증
  - [x] 3-1. CustomOAuth2UserService 카카오 처리 로직 검증
    - 카카오 사용자 정보 추출 메서드 검증 완료
    - null 체크 강화 및 ClassCastException 방지 로직 추가
    - 카카오 이메일 동의 관련 에러 메시지 개선
    - 소셜 ID 추출 실패 시 예외 처리 추가
  - [x] 3-2. SecurityConfig OAuth2 설정 검증
    - OAuth2 로그인 설정 확인 완료
    - 카카오 로그인 엔드포인트(`/oauth2/**`)가 `permitAll()`에 포함되어 있음
    - 에러 핸들러 설정 확인 완료
  - [x] 3-3. OAuth2LoginSuccessHandler 검증
    - JWT 토큰 생성 로직 확인 완료
    - 프론트엔드 리다이렉트 URL 설정 확인 완료
    - `oauth2.redirect-uri` 설정값 확인 완료
- [ ] Step 4: 로컬 환경 테스트
  - [ ] 4-1. 백엔드 서버 실행 및 로그 확인
  - [ ] 4-2. 카카오 로그인 엔드포인트 접근 테스트
  - [ ] 4-3. 사용자 정보 저장 및 JWT 토큰 발급 테스트
  - [ ] 4-4. 기존 사용자 연동 테스트
  - [ ] 4-5. 에러 케이스 테스트
- [ ] Step 5: 프론트엔드 연동 가이드 작성
  - [ ] 5-1. 카카오 로그인 버튼 구현 가이드 작성
  - [ ] 5-2. OAuth 리다이렉트 처리 가이드 작성
- [ ] Step 6: 운영 환경 배포 준비
  - [ ] 6-1. 운영 환경 Redirect URI 등록
  - [ ] 6-2. 환경 변수 설정
  - [ ] 6-3. 운영 환경 테스트

---

## 참고사항

- 각 Step을 완료한 후 사용자 리뷰를 받고 다음 Step을 진행합니다.
- 아키텍처 가이드의 원칙을 반드시 준수합니다.
- 카카오 개발자 문서를 참고하여 최신 API 변경사항을 확인합니다.
- 테스트는 각 Step 완료 후 작성합니다.

---

## 참고 문서

- [카카오 로그인 REST API 가이드](https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api)
- [카카오 개발자 콘솔](https://developers.kakao.com/)
- [OAuth2 로그인 흐름 문서](./API문서/OAuth2_로그인_흐름.md)
- [아키텍처 가이드](./아키텍처/architecture-guide.md)
