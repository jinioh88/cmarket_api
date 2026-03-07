# Admin API 구현 완료

## 목적
- 어드민 API 구현 (1차: 역할 부여, 2차: 전용 API)
- 참고: `documents/할일/Admin_할일.md`, `Admin_할일_2차.md`

---

## 1차 구현 내역 (역할 부여)

### 1. User 엔티티
- **경로**: `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/auth/model/User.java`
- **추가**: `changeRole(UserRole role)` 메서드
- **역할**: 역할 변경 시 `updatedAt` 갱신

### 2. UserRepository
- **경로**: `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/auth/repository/UserRepository.java`
- **추가**: `findByIdAndDeletedAtIsNull(Long id)` 메서드
- **역할**: 소프트 삭제 제외 사용자 조회 (역할 변경 대상 검증용)

### 3. Admin 도메인
- **AdminUserDto**: `domain/admin/app/dto/AdminUserDto.java`
  - id, email, name, nickname, birthDate, addressSido, addressGugun, role, createdAt
- **AdminService**: `domain/admin/app/service/AdminService.java`
  - `grantAdminRole(Long userId)`: 특정 회원에게 ADMIN 역할 부여
- **AdminServiceImpl**: `domain/admin/app/service/AdminServiceImpl.java`
  - 대상 회원 존재 여부 검증, `User.changeRole(UserRole.ADMIN)` 호출

### 4. Admin 웹 계층
- **AdminUserController**: `web/admin/controller/AdminUserController.java`
  - `PATCH /api/admin/users/{userId}/role`
  - `@PreAuthorize("hasRole('ADMIN')")` 적용
- **AdminUserResponse**: `web/admin/dto/AdminUserResponse.java`
  - AdminUserDto → 웹 응답 변환

---

## API 명세

### PATCH /api/admin/users/{userId}/role

| 항목 | 내용 |
|------|------|
| 권한 | `ROLE_ADMIN` (관리자만 호출 가능) |
| Path | `userId` (Long) - 대상 회원 ID |
| Body | 없음 |
| Response | 변경된 회원 정보 (id, email, name, nickname, birthDate, addressSido, addressGugun, role, createdAt) |
| 에러 | 401: 인증 필요 / 403: 권한 없음 / 404: 대상 회원 없음 |

---

## 테스트
- `./gradlew :service:cmarket-domain:compileJava :service:cmarket:compileJava` 성공

---

---

## 2차 구현 내역 (어드민 전용 API)

### 1-2, 5-2. 상품/커뮤니티 삭제 시 관리자 권한
- `ProductServiceImpl.deleteProduct()`: 판매자 본인 또는 ADMIN 삭제 허용
- `CommunityServiceImpl.deletePost()`: 작성자 본인 또는 ADMIN 삭제 허용

### 2-2. 전체 유저 목록 조회
- `GET /api/admin/users` - keyword, status(ACTIVE/WITHDRAWN), role, page, size
- `UserRepositoryCustom`, `AdminUserQueryService`, `AdminUserController`

### 3-1, 3-2, 3-3. 탈퇴 관리
- `GET /api/admin/withdrawals` - 탈퇴 회원 목록
- `GET /api/admin/withdrawals/{userId}` - 탈퇴 회원 상세
- `POST /api/admin/withdrawals/{userId}/restore` - 탈퇴 회원 복구
- `AdminWithdrawalController`, `AdminWithdrawalQueryService`, `AdminService.restoreWithdrawnUser()`

### 1-1. 어드민 상품 목록
- `GET /api/admin/products` - keyword, productType, category, page, size
- `AdminProductController`, `AdminProductQueryService` - sellerNickname, category, updatedAt 포함

### 4-1, 4-2, 4-3. 회원 통계
- `GET /api/admin/statistics/trends` - 월별 가입/탈퇴 추세 (startMonth, endMonth)
- `GET /api/admin/statistics/withdrawal-reasons` - 탈퇴 사유별 통계
- `GET /api/admin/statistics/summary` - 총 유저/상품 수 등 대시보드 요약
- `AdminStatisticsController`, `AdminStatisticsService`

---

## 기타
- **OAuth2UserPersistenceService**: `git package` 오타 수정 (package로 복구)
