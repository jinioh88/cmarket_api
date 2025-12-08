# Report 기능 구현 할일

> 반려동물 용품 중고거래 서비스 - **Report/Block** 영역 구현 작업 목록

---

## 개요

본 문서는 `08_Report.md` 요구사항 정의서를 기반으로 구현할 작업을 순차적으로 정리한 것입니다.  
아키텍처 가이드(`architecture-guide.md`) 원칙(웹 → 앱 → 도메인, DTO 경계, 예외 전용 로깅)을 준수합니다.

---

## 구현 순서

### Step 1: 도메인 모델 및 Enum 정의

- **작업 내용**:
  - 신고 대상 구분 Enum: `ReportTargetType` (`USER`, `PRODUCT`, `COMMUNITY_POST`)
  - 신고 상태 Enum: `ReportStatus` (`PENDING`, `REVIEWED`, `REJECTED`, `ACTION_TAKEN`)
  - 신고 사유 Enum:
    - `UserReportReason`: 욕설/비방/괴롭힘, 사기/허위 거래 시도, 음란/불건전, 스팸/광고, 만 14세 미만, 닉네임 신고, 프로필 이미지 신고, 기타
    - `ProductReportReason`: 허위/사기성, 불법/금지 품목, 부적절 이미지, 중복 게시물, 스팸/광고, 대리 결제/구매/판매, 전문 판매 업자, 기타
    - `CommunityReportReason`: 욕설/비방/혐오, 도배, 음란/불건전, 스팸/광고, 자해/자살 의도, 기타
  - 신고 엔티티 `Report`:
    - 필드: id, reporterId, targetType, targetId, reasonCodes(리스트), detailReason(최대 300자), imageUrls(최대 3장), status, createdAt, reviewedAt, rejectedReason(선택)
    - 제약: 동일 reporterId + targetType + targetId 중복 생성 금지(“이미 신고된 ...” 메시지에 대응)
  - 사용자 차단 엔티티 `UserBlock`:
    - 필드: id, blockerId, blockedUserId, createdAt
    - 제약: blockerId = blockedUserId 금지, 동일 조합 중복 금지, 소프트 삭제는 불필요(하드 유지)
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/report/model/ReportTargetType.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/report/model/ReportStatus.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/report/model/UserReportReason.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/report/model/ProductReportReason.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/report/model/CommunityReportReason.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/report/model/Report.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/report/model/UserBlock.java`

### Step 2: 도메인 레포지토리 인터페이스

- **작업 내용**:
  - `ReportRepository`
    - `boolean existsByReporterIdAndTargetTypeAndTargetId(Long reporterId, ReportTargetType targetType, Long targetId)`
    - `Report save(Report report)`
    - 상태/대상별 조회: `Page<Report> findByTargetTypeAndStatus(...)`
  - `UserBlockRepository`
    - `boolean existsByBlockerIdAndBlockedUserId(Long blockerId, Long blockedUserId)`
    - `UserBlock save(UserBlock userBlock)`
    - 차단 여부 확인: `boolean existsByBlockerIdAndBlockedUserId(Long blockerId, Long blockedUserId)`
    - 차단 취소(필요 시): `void deleteByBlockerIdAndBlockedUserId(Long blockerId, Long blockedUserId)`
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/report/repository/ReportRepository.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/report/repository/UserBlockRepository.java`

### Step 3: 차단(블락) 유스케이스 구현

- **작업 내용**:
  - 앱 DTO: `UserBlockCreateCommand` (blockedUserId)
  - 앱 서비스: `ReportService.blockUser(UserBlockCreateCommand)`
    - 현재 로그인 사용자(blockerId) 기준
    - 자기 자신 차단 금지 검사
    - 이미 차단된 경우 `ErrorCode.USER_ALREADY_BLOCKED` 예외
    - 차단 기록 저장
  - 웹 DTO: `UserBlockRequest`, `UserBlockResponse`
  - 컨트롤러: `POST /api/reports/blocks/users/{blockedUserId}`
    - 인증 필수, SecurityUtils로 blockerId 추출
    - 성공 시 201 반환
  - 차단 체크 헬퍼: 다른 도메인(채팅/커뮤니티/상품 조회)에서 사용할 `UserBlockQueryService` (읽기 전용) 제공
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/report/app/dto/UserBlockCreateCommand.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/report/app/service/ReportService.java` (메서드 추가)
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/report/app/service/ReportServiceImpl.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/report/app/service/UserBlockQueryService.java`
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/report/dto/UserBlockRequest.java`
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/report/dto/UserBlockResponse.java`
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/report/controller/UserBlockController.java`

### Step 4: 신고 생성 유스케이스 (사용자/상품/커뮤니티)

- **공통 작업 내용**:
  - 앱 DTO: `ReportCreateCommand` (targetType, targetId, List<reasonCodes>, detailReason, List<imageUrls>)
    - 검증: 이유 최소 1개 선택, detailReason 최대 300자, imageUrls 최대 3개
  - 앱 서비스: `ReportService.createReport(ReportCreateCommand)`
    - targetType 별 reason Enum 검증
    - reporterId는 SecurityUtils에서 전달받은 현재 사용자
    - 중복 신고 시 `ErrorCode.ALREADY_REPORTED` 예외 → 메시지 “이미 신고된 {대상명}입니다.”
    - 이미지 URL만 저장(업로드는 웹 계층에서 처리)
  - 웹 DTO: `UserReportRequest`, `ProductReportRequest`, `CommunityReportRequest` (각 reason Enum 타입/리스트, detailReason, imageFiles 혹은 imageUrls)
  - 이미지 업로드:
    - `MultipartFile` 최대 3장, 파일 형식/크기 검증(한 장당 5MB 등 기존 이미지 업로드 규칙 재사용)
    - 컨트롤러에서 업로드 처리 후 URL 리스트를 앱 DTO에 전달
- **엔드포인트**:
  - `POST /api/reports/users/{targetUserId}`
  - `POST /api/reports/products/{productId}`
  - `POST /api/reports/community-posts/{postId}`
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/report/app/dto/ReportCreateCommand.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/report/app/service/ReportService.java` (메서드 추가)
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/report/app/service/ReportServiceImpl.java`
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/report/dto/UserReportRequest.java`
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/report/dto/ProductReportRequest.java`
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/report/dto/CommunityReportRequest.java`
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/report/dto/ReportResponse.java`
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/report/controller/ReportController.java`
  - (이미지 업로드 재사용 시) 기존 업로드 서비스 연계 또는 `ReportImageUploadService` 추가

### Step 5: 관리자 신고 검토/조치 플로우

- **작업 내용**:
  - 앱 DTO: `ReportReviewCommand` (reportId, status, rejectedReason?, actionNote?)
  - 앱 서비스: `ReportService.reviewReport(...)`
    - 상태 전환 검증: `PENDING` → `REVIEWED`/`REJECTED`/`ACTION_TAKEN`
    - 관리자 계정만 호출 가능(컨트롤러에서 권한 체크)
  - 조회용 앱 DTO/쿼리 서비스: `ReportQueryService`
    - 상태/대상별 페이지 조회, reporter/target 필터링 지원
  - 웹 DTO: `ReportReviewRequest`, `AdminReportResponse`, `ReportListResponse`
  - 엔드포인트:
    - `GET /api/admin/reports` (필터: targetType, status, reporterId, targetId)
    - `PATCH /api/admin/reports/{reportId}/review`
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/report/app/dto/ReportReviewCommand.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/report/app/service/ReportQueryService.java`
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/report/dto/ReportReviewRequest.java`
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/report/dto/AdminReportResponse.java`
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/report/dto/ReportListResponse.java`
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/report/controller/AdminReportController.java`

### Step 6: 차단 연동 가드(교차 도메인)

- **작업 내용**:
  - `UserBlockQueryService`를 활용해 채팅/커뮤니티/상품 댓글 등에서 상호작용 차단 여부 확인 API 제공
  - 필요한 곳에서 `UserBlockQueryService.isBlocked(currentUserId, targetUserId)` 호출하여 접근 제한 및 에러 코드 반환
  - 최소한 REST 가드 컨트롤러/필터/인터셉터 수준에서 통합 재사용 가능하도록 인터페이스 정의
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/report/app/service/UserBlockQueryService.java` (Step 3에서 정의한 인터페이스 활용)
  - 교차 도메인에서 호출할 헬퍼/컴포넌트 (각 도메인 구현 시 적용)

### Step 7: 예외 코드 정리

- **작업 내용**:
  - 에러 코드 추가: `USER_ALREADY_BLOCKED`, `BLOCK_SELF_NOT_ALLOWED`, `ALREADY_REPORTED`, `REPORT_NOT_FOUND`, `REPORT_STATUS_INVALID`
- **출력물**:
  - `service/cmarket-domain/src/test/java/org/cmarket/cmarket/domain/report/...`

---

## 유의 사항

- 모든 컨트롤러는 `SuccessResponse`/`ErrorResponse` 표준 응답 사용, 예외 전용 로깅 준수.
- DTO 변환은 웹 DTO ↔ 앱 DTO 경계로만 수행하며 도메인 엔티티 직접 노출 금지.
- 이미지 업로드는 웹 계층에서 처리 후 URL만 앱 계층으로 전달(도메인은 순수 문자열 URL만 보관).
- GET/DELETE는 `@RequestParam`/`@PathVariable`, POST/PATCH는 DTO에 facilityId 등 컨텍스트 키 포함 원칙 준수.

