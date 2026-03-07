# Admin 기능 구현 할일 (2차: 어드민 전용 API)

> 반려동물 용품 중고거래 서비스 - **Admin** 영역 API 구현
> 
> 이미지 "어드민에 필요한 API vs 기존 API 부족분" 기반으로 작성

---

## 개요

본 문서는 어드민 페이지에서 필요한 API를 6개 영역별로 정리합니다.
- `09_Admin.md` 요구사항 정의서 및 이미지 부족분 분석 반영
- 아키텍처 가이드(`architecture-guide.md`) 원칙 준수
- **선행 완료**: ADMIN ROLE, 역할 부여 API (`Admin_할일.md` 1차)

---

## 현황 요약

| 영역 | 기존 API | 부족분 | 우선순위 |
|------|----------|--------|----------|
| 1. 상품 관리 | 목록/상세/삭제 있음 | 목록에 nickname, category, updatedAt 부족 / 삭제 시 관리자 권한 없음 | 중 |
| 2. 유저 관리 | 상세(profile/{id}) 있음 | 전체 목록(검색/필터/페이징), 유저 상태 변경 없음 | 상 |
| 3. 탈퇴 관리 | 탈퇴 실행만 있음 | 탈퇴 목록, 탈퇴 상태 관리 없음 | 상 |
| 4. 회원 통계 | 없음 | 월별 가입/탈퇴, 탈퇴 사유별, 총 상품/유저/거래수 없음 | 중 |
| 5. 커뮤니티 관리 | 목록/상세/삭제 있음 | 삭제 시 관리자 권한 없음 | 중 |
| 6. 신고 관리 | 목록 조회, 검토 있음 | 신고 상세 조회 확인 필요 | 하 |

---

## 구현 순서

---

## 1. 상품 관리 (Product)

### 1-1. 어드민 상품 목록 조회 API (신규 또는 기존 확장)

- **현황**: `GET /api/products/search` 존재, 목록에 `nickname`(판매자), `category`, `updatedAt` 미포함
- **작업 내용**:
  - 어드민 전용 `GET /api/admin/products` 또는 기존 search API에 어드민용 응답 필드 확장
  - 목록 항목: id, title, price, productType, category, **sellerNickname**, **updatedAt**, createdAt, productStatus, tradeStatus 등
  - 검색/필터: keyword, productType(SELL/REQUEST), category, 페이징
- **출력물**:
  - `AdminProductController` 또는 `SearchController` 확장
  - `AdminProductListItemResponse` (nickname, category, updatedAt 포함)

### 1-2. 상품 삭제 시 관리자 권한 추가

- **현황**: `DELETE /api/products/{id}` 본인만 삭제 가능
- **작업 내용**:
  - `ProductServiceImpl.deleteProduct()`: sellerId 일치 또는 호출자 role=ADMIN이면 삭제 허용
  - `SecurityUtils.getCurrentUserEmail()` → User 조회 → role 확인
- **출력물**:
  - `ProductServiceImpl.deleteProduct()` 수정

---

## 2. 유저 관리 (User)

### 2-1. User 엔티티에 상태 필드 추가 (선택)

- **작업 내용**:
  - `UserStatus` enum 추가: `ACTIVE`, `INACTIVE`, `SUSPENDED`
  - User 엔티티에 `userStatus` 필드 추가 (기본값 ACTIVE)
  - 또는 deletedAt 기반으로 상태 유도 (활성/탈퇴) + 별도 정지 플래그
- **출력물**:
  - `UserStatus` enum, `User` 엔티티 수정
  - DB 마이그레이션 (필요 시)

### 2-2. 전체 유저 목록 조회 API (신규)

- **작업 내용**:
  - `GET /api/admin/users` - 검색(닉네임, 이메일, 이름, ID), 필터(상태, 권한), 페이징(10개 기본)
  - 목록 항목: id, nickname, name, email, birthDate, status, role, createdAt, addressSido, addressGugun
  - 정렬: PK 오름차순 기본
- **출력물**:
  - `AdminUserController` 확장
  - `AdminUserQueryService`, `AdminUserListResponse`
  - `UserRepository` 커스텀 쿼리 (QueryDSL 또는 @Query)

### 2-3. 유저 상태 변경 API (신규)

- **작업 내용**:
  - `PATCH /api/admin/users/{userId}/status` - 활성/비활성/정지 변경
  - Request: `{ "status": "ACTIVE" | "INACTIVE" | "SUSPENDED" }`
- **출력물**:
  - `AdminUserController`, `AdminService` 확장

### 2-4. 유저 상세 조회

- **현황**: `GET /api/profile/{id}` 기존 API로 가능 → 별도 작업 불필요 (또는 어드민 전용 상세 응답 확장)

---

## 3. 탈퇴 관리 (Withdrawal)

### 3-1. 탈퇴 회원 목록 조회 API (신규)

- **작업 내용**:
  - `GET /api/admin/withdrawals` - deletedAt IS NOT NULL 사용자 목록
  - 목록 항목: id, email, nickname, name, role, addressSido, addressGugun, birthDate, **withdrawalReason**, **withdrawalDetailReason**, **deletedAt**
  - 검색/필터, 페이징, 정렬(ID 순)
- **출력물**:
  - `AdminWithdrawalController`
  - `AdminWithdrawalQueryService`, `WithdrawalListItemDto`

### 3-2. 탈퇴 회원 상세 조회 API (신규)

- **작업 내용**:
  - `GET /api/admin/withdrawals/{userId}` - 탈퇴 회원 상세 (이름, 닉네임, 이메일, 지역, 가입일, 상태, 권한, 프로필 이미지, 탈퇴 정보)
- **출력물**:
  - `AdminWithdrawalController` 확장

### 3-3. 탈퇴 회원 복구 API (신규)

- **작업 내용**:
  - `POST /api/admin/withdrawals/{userId}/restore` - User.restore() 호출
  - deletedAt, withdrawalReason, withdrawalDetailReason 초기화
- **출력물**:
  - `AdminWithdrawalController`, `AdminService` 확장

### 3-4. 탈퇴 처리 상태 관리

- **작업 내용**: 현재 소프트 삭제만 사용. "탈퇴 처리 상태"가 별도 워크플로우(예: 30일 후 완전 삭제)를 의미하면 추가 도메인 설계 필요. 1차는 복구만 구현.

---

## 4. 회원 통계 (Dashboard)

### 4-1. 월별 가입/탈퇴 추세 API (신규)

- **작업 내용**:
  - `GET /api/admin/statistics/trends` - 월별 가입 수, 탈퇴 수
  - Query: startMonth, endMonth (선택)
- **출력물**:
  - `AdminStatisticsController`, `AdminStatisticsService`

### 4-2. 탈퇴 사유별 통계 API (신규)

- **작업 내용**:
  - `GET /api/admin/statistics/withdrawal-reasons` - WithdrawalReasonType별 건수
- **출력물**:
  - `AdminStatisticsController` 확장

### 4-3. 대시보드 요약 API (신규)

- **작업 내용**:
  - `GET /api/admin/statistics/summary` - 총 유저수, 총 상품수, 거래수(또는 활성 상품 수) 등
- **출력물**:
  - `AdminStatisticsController` 확장

---

## 5. 커뮤니티 관리 (Community)

### 5-1. 게시글 목록/상세

- **현황**: `GET /api/community/posts`, `GET /api/community/posts/{id}` 존재 → 별도 작업 불필요

### 5-2. 게시글 삭제 시 관리자 권한 추가

- **현황**: `DELETE /api/community/posts/{id}` 본인만 삭제 가능
- **작업 내용**:
  - `CommunityServiceImpl.deletePost()`: authorId 일치 또는 호출자 role=ADMIN이면 삭제 허용
- **출력물**:
  - `CommunityServiceImpl.deletePost()` 수정

### 5-3. 커뮤니티 댓글 삭제 시 관리자 권한 (선택)

- **작업 내용**: 댓글 삭제에도 관리자 권한 추가 (요구사항 확인)

---

## 6. 신고 관리 (Report)

### 6-1. 신고 목록 조회

- **현황**: `GET /api/admin/reports` 존재 (targetType, status 필터, 페이징) → **완료**

### 6-2. 신고 상세 조회 API (확인/신규)

- **작업 내용**:
  - `GET /api/admin/reports/{reportId}` - 단건 상세 조회
  - 기존 목록 응답에 상세 정보가 충분하면 별도 엔드포인트 불필요. 부족 시 신규 추가.
- **출력물**:
  - `AdminReportController` 확장 (필요 시)

### 6-3. 신고 처리 (승인/반려)

- **현황**: `PATCH /api/admin/reports/{reportId}/review` 존재 → **완료**

### 6-4. 신고 대상 타입 (판매요청)

- **현황**: `ReportTargetType`: USER, PRODUCT, COMMUNITY_POST
- **비고**: 판매요청(REQUEST) 상품 신고는 PRODUCT 타입으로 targetId=productId로 저장. productType으로 SELL/REQUEST 구분 가능. 별도 enum 추가는 선택.

---

## 권장 구현 순서

| 순서 | 작업 | 비고 |
|------|------|------|
| 1 | 1-2, 5-2 | 상품/커뮤니티 삭제 시 관리자 권한 (기존 로직 수정만) |
| 2 | 2-2 | 전체 유저 목록 조회 (핵심) |
| 3 | 2-1, 2-3 | 유저 상태 필드 및 변경 API |
| 4 | 3-1, 3-2, 3-3 | 탈퇴 목록, 상세, 복구 |
| 5 | 1-1 | 어드민 상품 목록 (필드 확장) |
| 6 | 4-1, 4-2, 4-3 | 회원 통계 |
| 7 | 6-2 | 신고 상세 (필요 시) |

---

## 참고

- **공통**: 모든 어드민 API에 `@PreAuthorize("hasRole('ADMIN')")` 적용
- **페이징**: `PageResult<T>` 또는 기존 `Page` 래핑 사용
- **정렬**: 화이트리스트 검증 (아키텍처 가이드)
