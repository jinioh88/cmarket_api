# 504 Bad Gateway / Connection is not available (60000ms timeout) 원인 및 조치

## 증상
- EC2 API에서 **504 Bad Gateway** 발생
- 로그: **"Connection is not available, request timed out after 60000ms."**
- HikariCP에서 커넥션 풀에서 커넥션을 가져오기 위해 60초 대기 후 실패한 상황

---

## 원인 요약

### 1. 트랜잭션 안에서 외부 HTTP 호출 (가장 유력)
- **CustomOAuth2UserService**: 클래스에 `@Transactional`이 걸려 있는데, `loadUser()` 진입 시 트랜잭션이 시작되고 **DB 커넥션을 풀에서 가져옴**. 그 다음 `super.loadUser(userRequest)`가 **Google/Kakao 사용자 정보 API(HTTP)** 를 호출함.
- 이 동안 **DB 커넥션을 붙잡은 채로** 외부 HTTP 응답(수 초~수십 초)을 기다리게 됨.
- 동시 로그인/요청이 많아지면 풀(최대 20개)이 모두 이런 식으로 점유되고, 새 요청은 60초 동안 커넥션을 기다리다가 타임아웃 → 504로 이어짐.
- **GoogleAuthService**: 클래스에 `@Transactional`이 걸려 있고, `authenticateWithIdToken()`에서 `googleIdTokenVerifierService.verify(idToken)`를 호출함. 이 검증 과정에서 **Google JWKS 등 HTTP 호출**이 발생할 수 있어, 같은 패턴으로 커넥션을 오래 붙잡을 수 있음.

### 2. spring.jpa.open-in-view 기본값 (true)
- `application-prod.properties`에 `spring.jpa.open-in-view` 설정이 없어 **기본값 true** 적용.
- true이면 요청 처리 동안 **영속성 컨텍스트(및 DB 커넥션)가 HTTP 요청 전체 구간** 동안 유지될 수 있음.
- 응답이 느린 API나 lazy 로딩이 많은 경로가 있으면 커넥션 점유 시간이 길어져, 풀 고갈에 기여할 수 있음.

### 3. HikariCP 설정
- `connection-timeout=60000`: 풀에서 커넥션을 **기다리는 최대 시간**이 60초라, 위와 같은 상황에서 60초 후에 해당 로그가 발생.
- `maximum-pool-size=20`: 동시에 커넥션을 오래 붙잡는 요청이 20개를 넘으면 나머지는 대기 → 타임아웃 가능.

### 4. MySQL/RDS 측
- JDBC URL에 `socketTimeout`, `connectTimeout` 등이 없으면, DB/네트워크 지연이나 RDS의 idle 종료 정책과 맞물려 끊긴 커넥션을 풀이 그대로 쓰려다 실패할 수 있음. (보조 요인)

---

## 조치 사항

### A. 코드 수정 (필수)

1. **CustomOAuth2UserService**
   - 클래스 레벨 `@Transactional` 제거.
   - `loadUser()`에서는 **먼저** `super.loadUser(userRequest)`만 호출(트랜잭션 없음, HTTP만 수행).
   - 사용자 정보 추출 후, **DB 접근만 하는 메서드**를 별도로 두고 그 메서드에만 `@Transactional` 적용.
   - 결과: 외부 HTTP 호출 중에는 DB 커넥션을 쓰지 않음.

2. **GoogleAuthService**
   - 클래스 레벨 `@Transactional` 제거.
   - `authenticateWithIdToken()`에서는 **먼저** `googleIdTokenVerifierService.verify(idToken)` 호출(트랜잭션 없음).
   - 검증 결과로 **DB 조회/저장만 하는 메서드**를 별도로 두고 그 메서드에만 `@Transactional` 적용.
   - 결과: Google 검증(HTTP) 중에는 DB 커넥션을 쓰지 않음.

### B. 설정 수정 (권장)

1. **application-prod.properties**
   - `spring.jpa.open-in-view=false` 추가.
   - lazy 로딩을 컨트롤러/뷰에서 쓰지 않도록 하고, 필요한 곳은 `@Transactional(readOnly = true)` 서비스나 fetch join/DTO로 처리.

2. **HikariCP (선택)**
   - 트래픽이 많은 경우: `maximum-pool-size`를 30~50 수준으로 올리는 것 검토 (DB/RDS max_connections 한도 내에서).
   - `connection-timeout`: 60초 유지해도 되지만, “빨리 실패”하려면 20000~30000 정도로 줄이는 것도 가능.
   - MySQL URL에 `socketTimeout`, `connectTimeout` 추가로 끊긴 커넥션 재사용 방지 (예: `socketTimeout=30000&connectTimeout=10000`).

3. **커넥션 검증**
   - 이미 `connection-test-query=SELECT 1` 사용 중이면 유지.
   - RDS와 EC2 간 네트워크가 불안정하면 `keepaliveTime`(HikariCP) 또는 MySQL 쪽 keepalive 설정 검토.

---

## 수정 후 확인 사항
- OAuth 로그인(Google/Kakao) 및 Google ID Token 로그인 플로우가 정상 동작하는지 테스트.
- `open-in-view=false` 적용 후 lazy 로딩 관련 LazyInitializationException이 나는 API가 있는지 확인 후, 해당 경로만 서비스 레이어에서 fetch join 또는 DTO 매핑으로 보완.

위 조치로 “트랜잭션 안에서의 외부 HTTP”로 인한 커넥션 장시간 점유가 제거되고, open-in-view로 인한 불필요한 커넥션 유지가 줄어들어 504 및 "Connection is not available, request timed out after 60000ms" 재발을 줄일 수 있습니다.
