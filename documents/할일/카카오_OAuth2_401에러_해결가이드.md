# 카카오 OAuth2 401 에러 해결 가이드

## 에러 내용
```
[invalid_token_response] An error occurred while attempting to retrieve the OAuth 2.0 Access Token Response: 401 : [no body]
```

이 에러는 카카오 서버에 토큰 교환 요청을 보냈을 때 401 Unauthorized 응답을 받았다는 의미입니다.

## ⚠️ 중요: Spring Boot 3.x 버전 사용 시 필수 설정

**Spring Boot 3.x 버전을 사용하는 경우**, 카카오 OAuth2 클라이언트 인증 방식을 명시적으로 설정해야 합니다:

```properties
spring.security.oauth2.client.registration.kakao.client-authentication-method=client_secret_post
```

이 설정이 없으면 Spring Security가 기본적으로 HTTP Basic 인증(`client_secret_basic`)을 사용하려고 시도하는데, 카카오는 POST body에 `client_secret`을 포함하는 방식을 요구하므로 401 에러가 발생합니다.

---

## 원인 체크리스트

### 1. 카카오 개발자 콘솔 설정 확인

#### 1-1. Redirect URI 확인 (가장 중요!)
카카오 개발자 콘솔에서 다음 URI가 **정확히** 등록되어 있는지 확인:

**로컬 개발 환경:**
```
http://localhost:8080/login/oauth2/code/kakao
```

**프로덕션 환경:**
실제 백엔드 서버의 URL을 사용합니다. 예를 들어:
```
https://cmarket-api.duckdns.org/login/oauth2/code/kakao
```

**⚠️ 중요:**
- Spring Security의 `{baseUrl}`은 **실제 백엔드 서버의 URL**을 사용합니다
- 프론트엔드 도메인(`https://cuddle-market.duckdns.org`)이 아니라 **백엔드 서버 도메인**(`https://cmarket-api.duckdns.org`)을 사용해야 합니다
- URI는 대소문자, 경로, 슬래시까지 **정확히** 일치해야 합니다
- 끝에 슬래시(`/`)가 있으면 안 됩니다
- `http`와 `https`도 구분됩니다

**확인 방법:**
1. [카카오 개발자 콘솔](https://developers.kakao.com/) 접속
2. 내 애플리케이션 > 앱 선택
3. 제품 설정 > 카카오 로그인 > Redirect URI 등록
4. 위 URI가 정확히 등록되어 있는지 확인

#### 1-2. Client ID 확인
**확인 위치:**
- 카카오 개발자 콘솔 > 앱 설정 > 앱 키 > **REST API 키**

**현재 설정 파일 값:**
```properties
spring.security.oauth2.client.registration.kakao.client-id=633c2808dacc3bac334abd53ed4aed69
```

**확인 사항:**
- 개발자 콘솔의 REST API 키와 위 값이 **정확히** 일치하는지 확인
- 복사/붙여넣기 시 공백이나 특수문자가 포함되지 않았는지 확인

#### 1-3. Client Secret 확인 (가장 흔한 원인!)
**확인 위치:**
- 카카오 개발자 콘솔 > 앱 설정 > 보안 > **Client Secret**

**현재 설정 파일 값:**
```properties
spring.security.oauth2.client.registration.kakao.client-secret=vVvp0gxP0V9VmoYTTjNL5kcZ730h0sum
```

**확인 사항:**
- Client Secret이 **생성되어 있는지** 확인 (없으면 생성 필요)
- 개발자 콘솔의 Client Secret과 위 값이 **정확히** 일치하는지 확인
- Client Secret은 **재생성하면 이전 값이 무효화**되므로, 재생성했다면 설정 파일도 업데이트 필요

#### 1-4. 카카오 로그인 활성화 확인
**확인 위치:**
- 카카오 개발자 콘솔 > 제품 설정 > 카카오 로그인

**확인 사항:**
- 카카오 로그인이 **활성화**되어 있는지 확인
- 앱 상태가 **정상**인지 확인 (정지 또는 제한 상태가 아닌지)

---

## 해결 방법

### 방법 1: Redirect URI 추가/수정 (프로덕션 환경)
**프로덕션 환경에서 가장 흔한 문제입니다!**

1. 카카오 개발자 콘솔 접속
2. REST API 키 수정 페이지로 이동
3. "카카오 로그인 리다이렉트 URI" 섹션 확인
4. **실제 백엔드 서버 URL**의 Redirect URI가 등록되어 있는지 확인:
   ```
   https://cmarket-api.duckdns.org/login/oauth2/code/kakao
   ```
5. 등록되어 있지 않으면 **'+' 버튼**을 클릭하여 추가
6. 서버 재시작 (필요한 경우)
7. 카카오 로그인 재시도

**참고:**
- 로컬 개발용: `http://localhost:8080/login/oauth2/code/kakao`
- 프로덕션용: `https://실제백엔드서버도메인/login/oauth2/code/kakao`
- 두 URI를 모두 등록해도 됩니다

### 방법 2: client-authentication-method 설정 추가 (Spring Boot 3.x 필수!)

**Spring Boot 3.x 버전을 사용하는 경우 필수입니다!**

1. `application.properties` 또는 `application-prod.properties` 파일 열기
2. 카카오 OAuth2 설정 섹션에 다음 줄 추가:
   ```properties
   spring.security.oauth2.client.registration.kakao.client-authentication-method=client_secret_post
   ```
3. 전체 설정 예시:
   ```properties
   spring.security.oauth2.client.registration.kakao.client-id=${KAKAO_CLIENT_ID:your-kakao-client-id}
   spring.security.oauth2.client.registration.kakao.client-secret=${KAKAO_CLIENT_SECRET:your-kakao-client-secret}
   spring.security.oauth2.client.registration.kakao.authorization-grant-type=authorization_code
   spring.security.oauth2.client.registration.kakao.client-authentication-method=client_secret_post
   spring.security.oauth2.client.registration.kakao.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}
   spring.security.oauth2.client.registration.kakao.scope=profile_nickname
   ```
4. 서버 재시작
5. 카카오 로그인 재시도

### 방법 3: Client Secret 재생성
1. 카카오 개발자 콘솔 접속
2. 앱 설정 > 보안 > Client Secret
3. "Client Secret 코드 발급" 클릭 (재생성)
4. 새로 생성된 Client Secret 복사
5. `application-local.properties` 파일 업데이트:
   ```properties
   spring.security.oauth2.client.registration.kakao.client-secret=새로운_Client_Secret_값
   ```
6. 서버 재시작
7. 카카오 로그인 재시도

### 방법 3: Client ID 확인 및 수정
1. 카카오 개발자 콘솔 접속
2. 앱 설정 > 앱 키 > REST API 키 확인
3. `application-local.properties` 파일의 `client-id` 값과 일치하는지 확인
4. 불일치하면 수정 후 서버 재시작

---

## 디버깅 팁

### 서버 로그 확인
서버를 재시작한 후 카카오 로그인을 시도하면 더 자세한 로그가 출력됩니다:
- 요청 URI
- OAuth2 Error Code
- OAuth2 Error Description
- HTTP 상태 코드
- HTTP 응답 본문

이 정보를 통해 정확한 원인을 파악할 수 있습니다.

### 카카오 개발자 콘솔 로그 확인
1. 카카오 개발자 콘솔 > 내 애플리케이션 > 앱 선택
2. 통계 > 오류 로그 메뉴에서 최근 에러 확인
3. 에러 메시지를 통해 원인 파악

---

## 일반적인 원인 순위

1. **Redirect URI 불일치** (가장 흔함)
   - 카카오 개발자 콘솔에 등록된 URI와 실제 사용되는 URI가 다름
   - 해결: URI를 정확히 일치시키기

2. **Client Secret 불일치**
   - 설정 파일의 Client Secret이 잘못되었거나 만료됨
   - 해결: Client Secret 재생성 후 설정 파일 업데이트

3. **Client ID 불일치**
   - 설정 파일의 Client ID가 잘못됨
   - 해결: 개발자 콘솔의 REST API 키와 일치시키기

4. **카카오 로그인 비활성화**
   - 제품 설정에서 카카오 로그인이 비활성화됨
   - 해결: 카카오 로그인 활성화

---

## 확인 완료 후 다음 단계

1. 위 체크리스트를 모두 확인
2. 필요한 수정 사항 적용
3. **서버 재시작** (중요!)
4. 카카오 로그인 재시도
5. 서버 로그에서 상세 에러 정보 확인
6. 여전히 문제가 있으면 로그 내용을 공유하여 추가 지원 요청

---

## 참고 자료

- [카카오 로그인 REST API 가이드](https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api)
- [카카오 개발자 콘솔](https://developers.kakao.com/)
- [OAuth2 로그인 흐름 문서](./API문서/OAuth2_로그인_흐름.md)

