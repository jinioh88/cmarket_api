# 아키텍처 가이드 — **핵심 실무 요약(클린 아키텍처)**

## 0) 목적
- 도메인 독립성 확보와 변경 용이성.
- 계층 간 **의존 방향 단방향화(웹 → 앱 → 도메인)** 및 인프라 격리.

---

## 1) 핵심 원칙 (Must)
1. **의존 방향 고정**: 웹 → 애플리케이션 → 도메인. (반대 참조 금지)
2. **도메인 순수성**: 프레임워크/외부 인프라 무의존. 비즈니스 규칙만 보유.
3. **유스케이스 중심**: 트랜잭션 경계와 조합 로직은 **애플리케이션 서비스**에 위치.
4. **웹 계층의 역할**: HTTP 처리, 인증/인가, **외부 인프라 호출(S3/외부 API)** 담당.
5. **DTO 경계**: 컨트롤러는 **도메인 모델 직접 노출/조작 금지**, 입·출력은 DTO만.
6. **타입 명시성**: `var` 금지, 의미 있는 타입/이름 사용.

> 요약: _도메인은 규칙만, 앱은 유스케이스, 웹은 I/O와 인프라._

---

## 2) 금지/권장 패턴

### 금지 (Don’t)
- 컨트롤러에서 도메인 엔티티 직접 사용/반환.
- 도메인/애플리케이션에서 S3/외부 API/환경설정 값 직접 사용.
- 애플리케이션/도메인에서 웹 계층 의존.
- **정상 흐름 로깅(시작/완료/요청 파라미터 debug/trace)**.

### 권장 (Do)
- **컨트롤러 ⇄ 앱 서비스** 간에는 **웹 DTO** ↔ **앱 DTO** 변환.
  - 웹 DTO 내부에 `toAppRequest()`/`toCommand()` 등 변환 메서드를 두어 App DTO를 만들고, 반환된 App DTO만 서비스에 전달하는 패턴은 허용된다.
  - 단, 웹 DTO(또는 요청 객체) 자체를 서비스에 직접 넘기거나 도메인 모델을 웹 계층에서 생성하지 않는다.
- 외부 인프라 호출은 컨트롤러(or 인프라 어댑터)에서 수행 후 **결과만** 앱 서비스로 전달.
- **facilityId 등 컨텍스트 키**는 요청 DTO에 **항상 포함**(POST/PATCH/PUT), GET/DELETE는 `@RequestParam`.

---

## 3) 표준 컨트롤러 패턴

### 기본 패턴 (Path Variable 사용)

```java
@PostMapping("/employees/{employeeId}/allowance-setting")
public ResponseEntity<SuccessResponse<AllowanceSettingWebDto>> create(
    @PathVariable Long employeeId,
    @Valid @RequestBody AllowanceSettingCreateWebRequest req
) {
  visitCareFacilityService.validateVisitCareServiceAvailable(req.getFacilityId());

  AllowanceSettingCreateRequest cmd = AllowanceSettingCreateRequest.builder()
      .employeeId(employeeId)
      .payRequestType(PayRequestType.valueOf(req.getPayRequestType()))
      .build();

  AllowanceSettingDto result = allowanceSettingService.createAllowanceSetting(cmd);
  return ResponseEntity.status(HttpStatus.CREATED)
      .body(new SuccessResponse<>(ResponseCode.CREATED, AllowanceSettingWebDto.fromDto(result)));
}
```

### 현재 로그인한 사용자 정보 사용 패턴

인증이 필요한 API에서 현재 로그인한 사용자 정보를 사용해야 할 때는 `SecurityUtils`를 사용합니다.

```java
@PreAuthorize("isAuthenticated()")
@GetMapping("/api/profile/me")
public ResponseEntity<SuccessResponse<MyPageResponse>> getMyPage() {
  // 현재 로그인한 사용자의 이메일 추출
  String email = SecurityUtils.getCurrentUserEmail();
  
  // 앱 서비스 호출 (email을 기반으로 사용자 정보 조회)
  MyPageDto myPageDto = profileService.getMyPage(email);
  
  // 앱 DTO → 웹 DTO 변환
  MyPageResponse response = MyPageResponse.fromDto(myPageDto);
  
  return ResponseEntity.status(HttpStatus.OK)
      .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
}

@PreAuthorize("isAuthenticated()")
@PatchMapping("/api/profile/me")
public ResponseEntity<SuccessResponse<UserWebDto>> updateProfile(
    @Valid @RequestBody ProfileUpdateRequest req
) {
  // 현재 로그인한 사용자의 이메일 추출
  String email = SecurityUtils.getCurrentUserEmail();
  
  // 웹 DTO → 앱 DTO 변환
  ProfileUpdateCommand cmd = ProfileUpdateCommand.builder()
      .email(email)
      .nickname(req.getNickname())
      .addressSido(req.getAddressSido())
      .addressGugun(req.getAddressGugun())
      .profileImageUrl(req.getProfileImageUrl())
      .introduction(req.getIntroduction())
      .build();
  
  // 앱 서비스 호출
  UserDto userDto = profileService.updateProfile(cmd);
  
  // 앱 DTO → 웹 DTO 변환
  UserWebDto userWebDto = UserWebDto.fromDto(userDto);
  
  return ResponseEntity.status(HttpStatus.OK)
      .body(new SuccessResponse<>(ResponseCode.SUCCESS, userWebDto));
}
```

**SecurityUtils 주요 메서드:**
- `SecurityUtils.getCurrentUserEmail()`: 현재 로그인한 사용자의 이메일 반환
- `SecurityUtils.getCurrentAuthentication()`: 현재 Authentication 객체 반환
- `SecurityUtils.isAuthenticated()`: 인증 여부 확인

> GET/PATCH/DELETE도 동일 패턴 적용 (인증 → 현재 사용자 정보 추출 → 앱 서비스 호출 → 웹 DTO 변환 → 표준 응답).

---

## 4) 로깅 · 예외 · 응답 (**예외 전용 로깅**)

- **도메인**: 로깅 금지 (순수 비즈니스 규칙만 유지).
- **애플리케이션 서비스**: **예외 상황에서만** `warn/error` 로깅. 시작/완료/정상 파라미터 로깅 없음.
- **웹 계층(Controller)**: 정상 요청/응답 로깅 제거. **예외 시에만** `warn/error` 로깅.
- **전역 예외 핸들러**: 모든 예외를 `error`로 기록하고, 표준 에러 응답 반환.
- **추적 ID**: 예외 로깅에 `traceId`(MDC) 반드시 포함.
- **응답 규격**: 
  - 성공 → `SuccessResponse<T>`  
  - 실패 → `ErrorResponse(code, message, traceId, timestamp)`

---

## 5) 페이징 처리

- Spring Data `Page` **직노출 금지**. `PageResult<T>` 전용 타입 사용.
- 정렬 필드는 **화이트리스트 검증**으로 인젝션 방지.

```java
public record PageResult<T>(
  int page, int size, long total, java.util.List<T> content,
  int totalPages, boolean hasNext, boolean hasPrevious,
  long totalElements, long numberOfElements
) {}
```

---

## 6) 데이터 접근 기술

- **Spring Data JPA**를 기본 데이터 접근 기술로 사용합니다.
- 도메인 레포지토리는 **인터페이스**로 정의하며, `JpaRepository`를 상속받습니다.
- 레포지토리 구현체는 Spring Data JPA가 자동 생성하며, 도메인 계층에서는 인터페이스만 의존합니다.
- 복잡한 쿼리는 `@Query` 어노테이션을 사용하거나, QueryDSL을 활용합니다.
- **트랜잭션 경계**: 애플리케이션 서비스 레벨에서 `@Transactional`을 관리합니다.

```java
// 도메인 레포지토리 인터페이스 예시
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId);
    
    @Query("SELECT o FROM Order o WHERE o.status = :status")
    List<Order> findByStatus(@Param("status") OrderStatus status);
}
```

---

## 7) 패키지 트리

```
market-api/
├─ service/market-domain/  # 도메인
│  └─ domain/
│     ├─ model/        # 엔티티/값객체/enums
│     ├─ app/          # 유스케이스 서비스, DTO, 응답
│     └─ repository/   # 도메인 레포지토리 인터페이스
├─ service/market/         # 웹
│  └─ web/
│     ├─ controller/   # REST
│     ├─ dto/          # 웹 DTO
│     ├─ response/     # 웹 응답
│     └─ filter/       # MDC 등
```

---

## 8) 코딩 스타일

- `var` 금지, **명시적 타입**.
- 의미 있는 이름, 약어 지양, `camelCase`.
- 컨트롤러 입·출력은 반드시 DTO.

---

## 9) Lombok

- Lombok 적극 사용(`@Getter`, `@Builder`, `@RequiredArgsConstructor` 등).
- 수동 getter/setter 지양.

---
