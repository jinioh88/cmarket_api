# Profile 기능 구현 할일

> 반려동물 용품 중고거래 서비스 - **Profile** 영역 구현 작업 목록

---

## 개요

본 문서는 `02_Profile.md` 요구사항 정의서를 기반으로 구현할 작업을 순차적으로 정리한 것입니다.
아키텍처 가이드(`architecture-guide.md`) 원칙을 준수하여 구현합니다.

---

## 구현 순서

### Step 1: 도메인 모델 확장 및 생성

#### 1-1. User 엔티티 필드 추가
- **작업 내용**:
  - `cmarket-domain` 모듈의 User 엔티티에 프로필 관련 필드 추가
  - 필드 추가:
    - `profileImageUrl` (String, length 500): 프로필 이미지 URL (S3 저장 경로)
    - `introduction` (String, length 1000): 소개글 (최대 1000자)
  - User 엔티티에 프로필 업데이트 메서드 추가:
    - `updateProfile(String nickname, String addressSido, String addressGugun, String profileImageUrl, String introduction)`
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/auth/model/User.java` (수정)

#### 1-2. BlockedUser 엔티티 생성
- **작업 내용**:
  - 사용자 차단 관계를 저장하는 엔티티 생성
  - 필드: id, blockerId (차단한 사용자 ID), blockedId (차단당한 사용자 ID), createdAt
  - blockerId와 blockedId의 복합 unique 제약조건 (동일 사용자 중복 차단 방지)
  - 인덱스: blockerId에 인덱스 추가 (차단 목록 조회 성능 향상)
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/profile/model/BlockedUser.java`

---

### Step 2: 도메인 레포지토리 인터페이스 생성

#### 2-1. BlockedUserRepository 인터페이스 생성
- **작업 내용**:
  - `JpaRepository<BlockedUser, Long>` 상속
  - 차단 관계 확인: `boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId)`
  - 차단 목록 조회 (페이지네이션): `Page<BlockedUser> findByBlockerIdOrderByCreatedAtDesc(Long blockerId, Pageable pageable)`
  - 차단 해제: `void deleteByBlockerIdAndBlockedId(Long blockerId, Long blockedId)`
  - 특정 사용자 차단 여부 확인: `Optional<BlockedUser> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId)`
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/profile/repository/BlockedUserRepository.java`

---

### Step 3: 마이페이지 조회 구현 (FR-005)

#### 3-1. 마이페이지 조회 웹 DTO 및 앱 DTO 생성
- **작업 내용**:
  - 웹 DTO: `MyPageResponse` (프로필 이미지, 닉네임, 이름, 소개글, 찜한 상품 목록, 등록한 상품 목록, 판매 요청 목록, 차단한 유저 목록, 생년월일, 이메일, 거주지, 가입일)
  - 앱 DTO: `MyPageDto` (동일한 필드)
  - 찜한 상품, 등록한 상품, 판매 요청, 차단한 유저는 별도 DTO로 분리하여 포함
  - **참고**: 찜한 상품, 등록한 상품, 판매 요청은 향후 Product 도메인에서 구현 예정이므로, 현재는 빈 리스트 또는 주석 처리
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/profile/dto/MyPageResponse.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/profile/app/dto/MyPageDto.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/profile/app/dto/BlockedUserDto.java` (차단한 유저 정보)

#### 3-2. 마이페이지 조회 앱 서비스 구현
- **작업 내용**:
  - `ProfileService.getMyPage()` 메서드 구현
  - 현재 로그인한 사용자 ID로 User 조회
  - 차단한 유저 목록 조회 (BlockedUserRepository 사용)
  - 찜한 상품, 등록한 상품, 판매 요청 목록은 향후 Product 도메인 연동 예정 (현재는 빈 리스트 반환)
  - MyPageDto 반환
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/profile/app/service/ProfileService.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/profile/app/service/ProfileServiceImpl.java`

#### 3-3. 마이페이지 조회 컨트롤러 구현
- **작업 내용**:
  - `GET /api/profile/me` 엔드포인트
  - 인증 필수 (`@PreAuthorize("isAuthenticated()")`)
  - 현재 로그인한 사용자 정보 추출 (AuthenticationContext 또는 PrincipalDetails 사용)
  - 앱 서비스 호출
  - 앱 DTO → 웹 DTO 변환
  - `SuccessResponse<MyPageResponse>` 반환
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/profile/controller/ProfileController.java`

---

### Step 4: 프로필 정보 수정 구현 (FR-006)

#### 4-1. 프로필 수정 웹 DTO 및 앱 DTO 생성
- **작업 내용**:
  - 웹 DTO: `ProfileUpdateRequest` (nickname, addressSido, addressGugun, profileImageUrl, introduction)
  - 검증 어노테이션 추가:
    - nickname: @NotBlank, @Size(min=1, max=10)
    - introduction: @Size(max=1000)
  - 앱 DTO: `ProfileUpdateCommand`
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/profile/dto/ProfileUpdateRequest.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/profile/app/dto/ProfileUpdateCommand.java`

#### 4-2. 프로필 수정 앱 서비스 구현
- **작업 내용**:
  - `ProfileService.updateProfile()` 메서드 구현
  - 사용자 조회
  - 닉네임 중복 검증 (본인 닉네임 제외)
  - User 엔티티의 `updateProfile()` 메서드 호출
  - 업데이트된 User 정보를 DTO로 변환하여 반환
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/profile/app/service/ProfileService.java` (메서드 추가)

#### 4-3. 프로필 수정 컨트롤러 구현
- **작업 내용**:
  - `PATCH /api/profile/me` 엔드포인트
  - 인증 필수 (`@PreAuthorize("isAuthenticated()")`)
  - 현재 로그인한 사용자 정보 추출
  - 웹 DTO → 앱 DTO 변환
  - 앱 서비스 호출
  - 프로필 이미지 업로드는 별도 엔드포인트로 분리 (S3 업로드 후 URL 반환)
  - `SuccessResponse<UserWebDto>` 반환
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/profile/controller/ProfileController.java` (메서드 추가)

#### 4-4. 프로필 이미지 업로드 컨트롤러 구현 (선택)
- **작업 내용**:
  - `POST /api/profile/me/image` 엔드포인트
  - 인증 필수
  - MultipartFile로 이미지 파일 받기
  - S3 업로드 (컨트롤러에서 처리, 아키텍처 가이드 준수)
  - 업로드된 이미지 URL 반환
  - 이미지 유효성 검증 (파일 형식, 크기 제한)
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/profile/controller/ProfileController.java` (메서드 추가)
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/profile/service/ImageUploadService.java` (S3 업로드 서비스)

---

### Step 5: 유저 프로필 조회 구현 (FR-023)

#### 5-1. 유저 프로필 조회 웹 DTO 및 앱 DTO 생성
- **작업 내용**:
  - 웹 DTO: `UserProfileResponse` (프로필 이미지, 거주지, 닉네임, 가입일, 소개글, 등록한 상품 목록)
  - 앱 DTO: `UserProfileDto` (동일한 필드)
  - 등록한 상품 목록은 별도 DTO로 분리하여 포함
  - **참고**: 등록한 상품 목록은 향후 Product 도메인에서 구현 예정이므로, 현재는 빈 리스트 또는 주석 처리
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/profile/dto/UserProfileResponse.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/profile/app/dto/UserProfileDto.java`

#### 5-2. 유저 프로필 조회 앱 서비스 구현
- **작업 내용**:
  - `ProfileService.getUserProfile()` 메서드 구현
  - userId로 User 조회 (소프트 삭제된 사용자 제외)
  - 등록한 상품 목록 조회는 향후 Product 도메인 연동 예정 (현재는 빈 리스트 반환)
  - UserProfileDto 반환
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/profile/app/service/ProfileService.java` (메서드 추가)

#### 5-3. 유저 프로필 조회 컨트롤러 구현
- **작업 내용**:
  - `GET /api/profile/{userId}` 엔드포인트
  - 인증 필수 (`@PreAuthorize("isAuthenticated()")`)
  - userId로 사용자 조회
  - 앱 서비스 호출
  - 앱 DTO → 웹 DTO 변환
  - `SuccessResponse<UserProfileResponse>` 반환
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/profile/controller/ProfileController.java` (메서드 추가)

---

### Step 6: 차단한 유저 목록 조회 구현

#### 6-1. 차단한 유저 목록 조회 웹 DTO 및 앱 DTO 생성
- **작업 내용**:
  - 웹 DTO: `BlockedUserListResponse` (차단한 유저 목록, 페이지네이션 정보)
  - 앱 DTO: `BlockedUserListDto` (동일한 필드)
  - 차단한 유저 정보: 닉네임, 프로필 이미지
  - 페이지네이션: PageResult 사용 (아키텍처 가이드 준수)
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/profile/dto/BlockedUserListResponse.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/profile/app/dto/BlockedUserListDto.java`

#### 6-2. 차단한 유저 목록 조회 앱 서비스 구현
- **작업 내용**:
  - `ProfileService.getBlockedUsers()` 메서드 구현
  - 현재 로그인한 사용자 ID로 차단 목록 조회 (페이지네이션, 최신순 정렬)
  - 차단당한 사용자 정보 조회 (User 엔티티)
  - BlockedUserListDto 반환
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/profile/app/service/ProfileService.java` (메서드 추가)

#### 6-3. 차단한 유저 목록 조회 컨트롤러 구현
- **작업 내용**:
  - `GET /api/profile/me/blocked-users` 엔드포인트
  - 인증 필수 (`@PreAuthorize("isAuthenticated()")`)
  - 페이지네이션 파라미터: page, size (기본값: page=0, size=10)
  - 현재 로그인한 사용자 정보 추출
  - 앱 서비스 호출
  - 앱 DTO → 웹 DTO 변환
  - `SuccessResponse<BlockedUserListResponse>` 반환
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/profile/controller/ProfileController.java` (메서드 추가)

---

### Step 7: 유저 차단 해제 구현

#### 7-1. 유저 차단 해제 앱 서비스 구현
- **작업 내용**:
  - `ProfileService.unblockUser()` 메서드 구현
  - 현재 로그인한 사용자 ID와 차단 해제할 사용자 ID로 차단 관계 조회
  - 차단 관계 삭제
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/profile/app/service/ProfileService.java` (메서드 추가)

#### 7-2. 유저 차단 해제 컨트롤러 구현
- **작업 내용**:
  - `DELETE /api/profile/me/blocked-users/{blockedUserId}` 엔드포인트
  - 인증 필수 (`@PreAuthorize("isAuthenticated()")`)
  - 현재 로그인한 사용자 정보 추출
  - 앱 서비스 호출
  - `SuccessResponse` 반환
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/profile/controller/ProfileController.java` (메서드 추가)

---

### Step 8: 커스텀 예외 클래스 생성

#### 8-1. Profile 관련 예외 클래스 생성
- **작업 내용**:
  - `NicknameAlreadyExistsException` (닉네임 중복 - Auth 도메인에 이미 있을 수 있음, 확인 필요)
  - `UserNotFoundException` (사용자 없음 - Auth 도메인에 이미 있을 수 있음, 확인 필요)
  - `BlockedUserNotFoundException` (차단한 유저 없음)
- **출력물**:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/profile/app/exception/` 패키지 내 예외 클래스들

#### 8-2. GlobalExceptionHandler에 커스텀 예외 처리 추가
- **작업 내용**:
  - 각 커스텀 예외에 대한 핸들러 메서드 추가
  - 적절한 HTTP 상태 코드 및 에러 메시지 반환
  - traceId 포함하여 로깅
- **출력물**:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/common/exception/GlobalExceptionHandler.java` (수정)

---

## 구현 시 주의사항

1. **아키텍처 원칙 준수**:
   - 웹 → 앱 → 도메인 의존 방향 준수
   - 도메인 모델은 웹 계층에서 직접 사용 금지
   - DTO 변환 필수

2. **프로필 이미지**:
   - S3 업로드는 컨트롤러에서 처리 (아키텍처 가이드 준수)
   - 이미지 파일 형식 검증 (jpg, png, gif 등)
   - 이미지 크기 제한 (예: 5MB)

3. **닉네임 중복 검증**:
   - 프로필 수정 시 본인 닉네임은 중복 체크에서 제외

4. **소개글**:
   - 최대 1000자 제한

5. **차단 기능**:
   - 동일 사용자 중복 차단 방지 (복합 unique 제약조건)
   - 차단 목록은 최신순 정렬 (createdAt DESC)
   - 페이지네이션: 10개씩

6. **예외 처리**:
   - 모든 예외는 GlobalExceptionHandler에서 처리
   - traceId 포함하여 로깅
   - 사용자 친화적인 에러 메시지 반환

7. **인증/인가**:
   - 모든 엔드포인트는 인증 필수 (`@PreAuthorize("isAuthenticated()")`)
   - 본인 정보 수정/조회는 본인만 가능하도록 검증

8. **Product 도메인 연동**:
   - 찜한 상품, 등록한 상품, 판매 요청 목록은 향후 Product 도메인에서 구현 예정
   - 현재는 빈 리스트 반환하거나 주석 처리

9. **페이지네이션**:
    - Spring Data `Page` 직노출 금지
    - `PageResult<T>` 전용 타입 사용 (아키텍처 가이드 준수)

---

## 완료 체크리스트

- [x] Step 1: 도메인 모델 확장 및 생성
- [x] Step 2: 도메인 레포지토리 인터페이스 생성
- [x] Step 3: 마이페이지 조회 구현 (FR-005)
- [x] Step 4: 프로필 정보 수정 구현 (FR-006)
- [x] Step 5: 유저 프로필 조회 구현 (FR-023)
- [x] Step 6: 차단한 유저 목록 조회 구현
- [x] Step 7: 유저 차단 해제 구현
- [x] Step 8: 커스텀 예외 클래스 생성

---

## 참고사항

- 각 Step을 완료한 후 사용자 리뷰를 받고 다음 Step을 진행합니다.
- 아키텍처 가이드의 원칙을 반드시 준수합니다.
- 테스트는 각 Step 완료 후 작성합니다.
- 찜한 상품, 등록한 상품, 판매 요청 목록은 Product 도메인 구현 후 연동합니다.

