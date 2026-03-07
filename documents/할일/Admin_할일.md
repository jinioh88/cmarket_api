# Admin 기능 구현 할일 (1차: ROLE 및 역할 부여 API)

> 반려동물 용품 중고거래 서비스 - **Admin** 영역 선행 작업
> 
> 어드민 API 구현에 앞서 ADMIN ROLE 확인 및 회원에게 어드민 역할 부여 API를 구현합니다.

---

## 개요

본 문서는 어드민 API 구현을 위한 **선행 작업**을 정의합니다.
- `09_Admin.md` 요구사항 정의서 및 이미지에 나온 어드민 API 부족분을 기반으로 합니다.
- 아키텍처 가이드(`architecture-guide.md`) 원칙을 준수하여 구현합니다.

---

## 사전 확인 결과

### ADMIN ROLE 현황 (확인 완료)

| 항목 | 위치 | 상태 |
|------|------|------|
| UserRole enum | `domain/auth/model/UserRole.java` | ✅ USER, ADMIN 존재 |
| User 엔티티 role 필드 | `domain/auth/model/User.java` | ✅ UserRole 타입 사용 |
| Spring Security 권한 | `PrincipalDetails`, `CustomUserDetailsService`, `JwtTokenProvider` | ✅ `ROLE_` + role.name() 적용 |
| Admin API 보호 | `AdminReportController` | ✅ `@PreAuthorize("hasRole('ADMIN')")` 사용 |

**결론**: ADMIN ROLE은 이미 정의되어 있으며, Spring Security와 연동되어 있습니다. **추가 작업 불필요**.

---

## 구현 순서

### Step 1: ADMIN ROLE 최종 검증 (선택)

- **작업 내용**:
  - `UserRole` enum에 `ADMIN` 값이 있는지 코드 리뷰로 확인
  - `AdminReportController` 등 기존 어드민 API가 `@PreAuthorize("hasRole('ADMIN')")`로 보호되는지 확인
  - 필요 시 단위 테스트로 ADMIN 권한 검증 로직 추가
- **출력물**:
  - 검증 완료 확인 (코드 리뷰 또는 테스트)
- **비고**: 이미 구현되어 있으므로, 신규 개발 전 확인만 수행

---

### Step 2: 회원에게 어드민 역할 부여 API 구현

#### 2-1. User 엔티티에 역할 변경 메서드 추가

- **작업 내용**:
  - `User` 엔티티에 `changeRole(UserRole role)` 메서드 추가
  - 역할 변경 시 `updatedAt` 갱신
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/auth/model/User.java` (메서드 추가)

#### 2-2. Admin 도메인/앱 서비스 생성

- **작업 내용**:
  - Admin 역할 변경용 Command/DTO 정의
  - `AdminService` 인터페이스 및 구현체 생성
  - `grantAdminRole(Long userId)`: 특정 회원에게 ADMIN 역할 부여
  - 권한 검증: 호출자는 ADMIN이어야 함 (컨트롤러 `@PreAuthorize`로 처리)
  - 비즈니스 검증: 대상 사용자 존재 여부, 이미 ADMIN인 경우 처리
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/admin/app/command/GrantAdminRoleCommand.java` (또는 단순 Long userId)
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/admin/app/service/AdminService.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/admin/app/service/AdminServiceImpl.java`
  - `ErrorCode`에 `USER_NOT_FOUND` 등 필요 시 추가

#### 2-3. Admin 웹 컨트롤러 및 DTO

- **작업 내용**:
  - `AdminUserController` 생성 (또는 기존 Admin 관련 컨트롤러에 추가)
  - `PATCH /api/admin/users/{userId}/role` 엔드포인트 (Request Body 없음)
  - `@PreAuthorize("hasRole('ADMIN')")` 적용
  - `SecurityUtils.getCurrentUserEmail()`로 호출자 확인 (필요 시)
  - 응답: 변경된 사용자 정보 (id, email, nickname, role 등)
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/admin/controller/AdminUserController.java`
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/admin/dto/AdminUserResponse.java` (또는 기존 UserDto 활용)

#### 2-4. UserRepository 확장 (필요 시)

- **작업 내용**:
  - `UserRepository.findByIdAndDeletedAtIsNull(Long id)` 메서드 추가 (역할 변경 대상 조회용)
  - 또는 `findById` + deletedAt null 체크
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/auth/repository/UserRepository.java`

---

## API 명세 (회원 어드민 역할 부여)

### PATCH /api/admin/users/{userId}/role

- **설명**: 특정 회원에게 ADMIN 역할을 부여합니다.
- **권한**: `ROLE_ADMIN` (관리자만 호출 가능)
- **Request**:
  - Path: `userId` (Long) - 대상 회원 ID
  - Body: 없음
- **Response**: 변경된 회원 정보 (id, email, nickname, role, ...)
- **에러**:
  - 404: 대상 회원 없음
  - 403: 호출자가 ADMIN이 아님

---

## 참고

- **패키지 구조**: `domain/admin/` 신규 생성 또는 `domain/auth/` 내 Admin 관련 서비스 확장
- **향후 확장**: 어드민 역할 회수(REVOKE) API, 유저 목록 조회, 유저 상태 변경 등은 별도 할일로 진행
- **이미지 부족분**: 상품 삭제, 유저 관리, 탈퇴 관리, 회원 통계, 커뮤니티 관리, 신고 관리 등은 본 할일 완료 후 순차 구현
