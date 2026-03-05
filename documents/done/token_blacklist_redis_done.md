# 토큰 블랙리스트 Redis 캐시 적용 완료

## 목적
- **문제**: HikariCP 커넥션 풀 고갈 (`Connection is not available, request timed out after 60000ms`)
- **원인**: `JwtAuthenticationFilter`가 인증된 **모든 요청**마다 `TokenBlacklistRepository.existsByToken()` DB 조회 수행
- **해결**: 토큰 블랙리스트 조회를 Redis로 이전하여 매 요청 DB 호출 제거

---

## 구현 내역

### 1. TokenBlacklistCache 인터페이스 (domain)
- **경로**: `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/auth/app/service/TokenBlacklistCache.java`
- **역할**: 토큰 블랙리스트 캐시 추상화
- **메서드**:
  - `addToBlacklist(token, expiresAt)`: 블랙리스트 등록
  - `isBlacklisted(token)`: 블랙리스트 여부 조회

### 2. TokenBlacklistCacheService (web)
- **경로**: `service/cmarket/src/main/java/org/cmarket/cmarket/web/common/security/TokenBlacklistCacheService.java`
- **역할**: Redis 기반 블랙리스트 캐시 구현
- **동작**:
  - Redis 키: `auth:blacklist:{SHA256(token)}` (토큰 해시로 키 길이 고정)
  - TTL: 토큰 만료 시점까지 (자동 삭제)
  - Redis 장애 시: DB(`TokenBlacklistRepository`)로 폴백

### 3. AuthServiceImpl 수정
- **변경**: `logout()` 시 DB 저장 후 `tokenBlacklistCache.addToBlacklist()` 호출
- **흐름**: DB(감사/복구용) + Redis(빠른 조회용) 이중 저장

### 4. JwtAuthenticationFilter 수정
- **변경**: `TokenBlacklistRepository.existsByToken()` → `TokenBlacklistCache.isBlacklisted()`
- **효과**: 인증된 요청마다 DB 조회 제거, Redis만 조회

### 5. SecurityConfig 수정
- **변경**: `JwtAuthenticationFilter`에 `TokenBlacklistRepository` 대신 `TokenBlacklistCache` 주입

---

## 기대 효과
- 인증된 API 요청 시 DB 커넥션 사용량 감소
- HikariCP 풀 고갈 및 504 타임아웃 발생 가능성 감소

---

## 테스트
- `./gradlew :service:cmarket:compileJava :service:cmarket-domain:compileJava` 성공
