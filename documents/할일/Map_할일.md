# Map 기능 구현 할일

> 커들마켓 지도 기반 생활 서비스 - **Map** 영역 구현 작업 목록

---

## 개요

본 문서는 아래 문서를 바탕으로 백엔드에서 구현할 작업을 체크리스트 형태로 정리한 것입니다.

- `documents/cuddle-market-map-feature-prd-v1.md`
- `documents/cuddle-market-map-backend-sprint-1-v1.md`
- `documents/cuddle-market-map-api-data-requirements-v1.md`

아키텍처 가이드(`architecture-guide.md`) 원칙을 준수하여 구현합니다.

---

## 구현 범위 정리

### MVP 1차 범위

- [x] 동물병원 중심 장소 데이터 모델 설계
- [x] 위치 기반 장소 목록 조회 API 구현
- [x] 장소 상세 조회 API 구현
- [x] 병원 전용 필터 적용
- [x] 추천 장소 구분 필드 제공
- [x] 리뷰 요약(`reviewCount`, `averageRating`) 제공
- [x] 위치 권한 거부 시 사용할 기본 좌표 기준 정책 반영

### 2차 이후 범위

- [x] 카페/식당/숙소 카테고리 확장
- [x] 관리자 추천 장소 관리 기능
- [ ] 외부 데이터 수집 자동화 및 동기화

### 이번 문서의 원칙

- [x] 스프린트 1은 `동물병원 조회 API`를 우선 구현한다.
- [x] 2차 확장을 막지 않도록 `Place` 중심의 확장 가능한 구조로 설계한다.
- [x] 리뷰 API는 1차 필수 구현이 아니더라도 테이블/요약 구조까지는 함께 고려한다.

---

## 구현 순서

### Step 1: 지도 도메인 모델 설계

#### 1-1. PlaceCategory, AnimalType Enum 생성
- [x] 장소 카테고리 enum 생성
- [x] 1차는 `HOSPITAL`만 사용하되 2차 확장을 위해 `CAFE`, `RESTAURANT`, `ACCOMMODATION`까지 정의
- [x] 병원 진료 가능 동물 타입 enum 생성
- [x] 예: `REPTILE`, `BIRD`
- [x] 출력물:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/map/model/PlaceCategory.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/map/model/AnimalType.java`

#### 1-2. Place 엔티티 생성
- [x] 지도 장소 공통 정보를 저장하는 엔티티 생성
- [x] 필드 정의:
  - `id`
  - `category`
  - `name`
  - `address`
  - `phone`
  - `operatingHours`
  - `imageUrl`
  - `latitude`
  - `longitude`
  - `isRecommended`
  - `sourceType` (NAVER / ADMIN 등 데이터 출처 구분)
  - `externalPlaceId` (외부 API 원본 ID, nullable)
  - `createdAt`, `updatedAt`
- [x] 인덱스 검토:
  - `category`
  - `isRecommended`
  - `latitude`, `longitude` 또는 공간 인덱스
  - `externalPlaceId` unique 검토
- [x] 2차 확장을 고려해 병원/카페/식당/숙소를 모두 수용 가능한 공통 엔티티로 설계
- [x] 출력물:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/map/model/Place.java`

#### 1-3. HospitalDetail 엔티티 생성
- [x] 병원 전용 상세 정보를 저장하는 엔티티 생성
- [x] 필드 정의:
  - `id`
  - `placeId`
  - `is24Hours`
  - `isEmergencyAvailable`
  - `animalTypes`
- [x] `Place`와 1:1 연관 또는 `placeId` 기반 단방향 참조 설계
- [x] `animalTypes`는 enum collection 또는 별도 테이블로 저장
- [x] 출력물:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/map/model/HospitalDetail.java`

#### 1-4. PlaceReview 엔티티 생성
- [x] 장소 리뷰 저장용 엔티티 생성
- [x] 필드 정의:
  - `id`
  - `placeId`
  - `userId`
  - `nickname`
  - `rating`
  - `content`
  - `imageUrls`
  - `createdAt`
  - `updatedAt`
  - `deletedAt`
- [x] MVP 1차에서 리뷰 API를 바로 열지 않더라도 이후 확장을 위해 구조를 선반영할지 검토
- [x] 리뷰 집계용 통계 쿼리를 고려해 `placeId`, `createdAt` 인덱스 추가
- [x] 출력물:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/map/model/PlaceReview.java`

#### 1-5. 리뷰 요약 DTO 또는 집계 정책 정의
- [x] 목록/상세 응답 공통 사용을 위한 `reviewSummary` 구조 정의
- [x] 필드 정의:
  - `reviewCount`
  - `averageRating`
- [x] 실시간 집계 방식 또는 별도 통계 테이블/캐시 방식 중 선택
- [x] 1차는 단순 쿼리 집계로 시작하고, 데이터 증가 시 캐시/집계 테이블로 전환 가능하게 설계
- [x] 출력물:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/map/app/dto/ReviewSummaryDto.java`

---

### Step 2: 레포지토리 및 위치 조회 기반 마련

#### 2-1. PlaceRepository 인터페이스 생성
- [x] `JpaRepository<Place, Long>` 기반 인터페이스 생성
- [x] 상세 조회용 `findById` 정의
- [x] 외부 데이터 upsert를 위한 `findByExternalPlaceId` 정의
- [x] 추천 장소 조회를 위한 메서드 검토
- [x] 출력물:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/map/repository/PlaceRepository.java`

#### 2-2. PlaceRepositoryCustom 및 구현체 생성
- [x] 위치 기반 반경 검색 전용 커스텀 레포지토리 추가
- [x] 입력 조건 정의:
  - `category`
  - `latitude`
  - `longitude`
  - `radius`
  - `isRecommended`
  - `is24Hours`
  - `isEmergencyAvailable`
  - `animalTypes`
  - `page`, `size`
- [x] 반경 기반 조회 구현
- [x] 병원 카테고리일 때만 병원 전용 필터 적용
- [x] 정렬 기준은 우선 `거리순`으로 확정
- [x] 목록 응답에 필요한 최소 필드만 조회하여 성능 최적화
- [x] 출력물:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/map/repository/PlaceRepositoryCustom.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/map/repository/PlaceRepositoryCustomImpl.java`

#### 2-3. HospitalDetailRepository, PlaceReviewRepository 생성
- [x] 병원 상세 정보 조회용 레포지토리 생성
- [x] 리뷰 조회/집계용 레포지토리 생성
- [x] 리뷰 요약 집계를 위한 count/avg 메서드 또는 QueryDSL 구현 추가
- [x] 출력물:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/map/repository/HospitalDetailRepository.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/map/repository/PlaceReviewRepository.java`

---

### Step 3: 목록 조회 API 구현

#### 3-1. 장소 목록 조회 웹 DTO 및 앱 DTO 생성
- [x] 웹 DTO `PlaceSearchRequest` 생성
- [x] 요청 필드 정의:
  - `category`
  - `latitude`
  - `longitude`
  - `radius`
  - `isRecommended`
  - `is24Hours`
  - `isEmergencyAvailable`
  - `animalTypes`
  - `page`
  - `size`
- [x] 앱 DTO `PlaceSearchCommand` 생성
- [x] 앱 DTO `PlaceListItemDto` 생성
- [x] 앱 DTO `PlaceSearchResultDto` 생성
- [x] 웹 DTO `PlaceListResponse` 생성
- [x] 페이지네이션은 기존 공통 타입인 `PageResult` 사용
- [x] 출력물:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/map/dto/PlaceSearchRequest.java`
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/map/dto/PlaceListResponse.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/map/app/dto/PlaceSearchCommand.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/map/app/dto/PlaceListItemDto.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/map/app/dto/PlaceSearchResultDto.java`

#### 3-2. 장소 목록 조회 앱 서비스 구현
- [x] `MapService.searchPlaces()` 메서드 구현
- [x] 필수값 검증:
  - `category`
  - `latitude`
  - `longitude`
  - `radius`
- [x] 정책 검증:
  - 허용 반경 최대값 제한
  - 병원 전용 필터는 `category=HOSPITAL`일 때만 허용
  - `animalTypes` 복수 선택 처리
- [x] `PlaceRepositoryCustom` 호출
- [x] 각 장소별 리뷰 요약 주입
- [x] 결과를 `PageResult`로 감싸 반환
- [x] 출력물:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/map/app/service/MapService.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/map/app/service/MapServiceImpl.java`

#### 3-3. 장소 목록 조회 컨트롤러 구현
- [x] `GET /api/places` 엔드포인트 구현
- [x] 인증 없이 조회 가능하도록 처리
- [x] 쿼리 파라미터를 웹 DTO로 매핑
- [x] 웹 DTO → 앱 DTO 변환
- [x] `SuccessResponse<PlaceListResponse>` 반환
- [x] 위치 권한 거부 상황 시 서버도 서울 시청 기본 좌표를 사용하도록 반영
- [x] 출력물:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/map/controller/MapController.java`

---

### Step 4: 상세 조회 API 구현

#### 4-1. 장소 상세 조회 웹 DTO 및 앱 DTO 생성
- [x] 웹 DTO `PlaceDetailResponse` 생성
- [x] 앱 DTO `PlaceDetailDto` 생성
- [x] 포함 필드 정의:
  - 공통 정보
  - `reviewSummary`
  - 병원일 경우 `detail.is24Hours`, `detail.isEmergencyAvailable`, `detail.animalTypes`
- [x] 카테고리별 상세 필드 확장을 고려해 `detail` 구조를 nullable 또는 서브 DTO로 설계
- [x] 출력물:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/map/dto/PlaceDetailResponse.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/map/app/dto/PlaceDetailDto.java`

#### 4-2. 장소 상세 조회 앱 서비스 구현
- [x] `MapService.getPlaceDetail()` 메서드 구현
- [x] 장소 기본 정보 조회
- [x] 카테고리가 병원이면 `HospitalDetail` 조회
- [x] 리뷰 요약 조회
- [x] 존재하지 않는 장소 예외 처리
- [x] 출력물:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/map/app/service/MapService.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/map/app/service/MapServiceImpl.java`

#### 4-3. 장소 상세 조회 컨트롤러 구현
- [x] `GET /api/places/{placeId}` 엔드포인트 구현
- [x] 인증 없이 조회 가능하도록 처리
- [x] `SuccessResponse<PlaceDetailResponse>` 반환
- [x] 출력물:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/map/controller/MapController.java`

---

### Step 5: 리뷰 API 구현 준비 또는 2차 구현

#### 5-1. 리뷰 목록 조회 DTO 및 앱 서비스 구현
- [x] `GET /api/places/{placeId}/reviews` 설계
- [x] 정렬 기준 정의:
  - `latest`
  - `rating`
- [x] 페이지네이션 적용
- [x] 응답 필드 정의:
  - `id`
  - `nickname`
  - `rating`
  - `content`
  - `imageUrls`
  - `createdAt`
- [x] 출력물:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/map/dto/PlaceReviewListResponse.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/map/app/dto/PlaceReviewDto.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/map/app/dto/PlaceReviewListDto.java`

#### 5-2. 리뷰 작성 DTO 및 앱 서비스 구현
- [x] `POST /api/places/{placeId}/reviews` 설계
- [x] 인증 필수 정책 적용
- [x] 검증 조건 정의:
  - `rating` 1~5
  - `content` 길이 제한
  - `imageUrls` 최대 개수 제한
- [x] 닉네임은 현재 사용자 정보에서 주입
- [x] 리뷰 작성 후 요약 집계가 즉시 반영되도록 처리
- [x] 출력물:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/map/dto/PlaceReviewCreateRequest.java`
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/map/dto/PlaceReviewResponse.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/map/app/dto/PlaceReviewCreateCommand.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/map/app/service/MapReviewService.java`
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/map/app/service/MapReviewServiceImpl.java`

#### 5-3. 리뷰 컨트롤러 구현
- [x] 리뷰 목록 조회/작성 엔드포인트를 `MapController` 또는 별도 `MapReviewController`로 분리
- [x] 인증 처리 시 `SecurityUtils` 활용
- [x] `SuccessResponse` 표준 응답 적용
- [x] 출력물:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/map/controller/MapReviewController.java`

---

### Step 6: 외부 데이터 연동 및 적재 정책 정의

#### 6-1. 장소 데이터 수집 전략 수립
- [ ] 네이버 지도 API에서 확보 가능한 필드와 내부 저장 필드 매핑 정의
- [ ] 병원 외 카테고리의 "반려동물 동반 가능" 여부는 외부 API 단독으로 부족하므로 데이터 출처 전략 분리
- [ ] 데이터 출처 정책 정의:
  - 병원: 외부 API 기반 수집 + 내부 보정
  - 카페/식당/숙소: 관리자 등록 우선 또는 별도 오픈데이터 검토
- [ ] 출력물:
  - `documents/cuddle-market-map-api-data-requirements-v1.md`와 연결되는 내부 운영 메모 또는 별도 적재 정책 문서

#### 6-2. 관리자/배치 적재 기능 설계
- [ ] 초기에는 수동 적재 SQL/시드 데이터로 시작 가능
- [ ] 이후 배치/관리자 기능으로 확장
- [ ] 중복 방지 기준 정의:
  - `externalPlaceId`
  - 이름 + 주소 유사도
- [ ] 폐업/정보 변경 대응 정책 수립
- [ ] 출력물:
  - 배치 또는 관리자 기능 구현 시 별도 문서/작업으로 분리

---

### Step 7: 예외 처리 및 검증

#### 7-1. 지도 도메인 예외 클래스 생성
- [x] `PlaceNotFoundException` 생성
- [x] `InvalidPlaceFilterException` 생성
- [x] `InvalidLocationRangeException` 생성
- [x] `UnsupportedPlaceCategoryException` 생성
- [x] 출력물:
  - `service/cmarket-domain/src/main/java/org/cmarket/cmarket/domain/map/app/exception/` 패키지 내 예외 클래스

#### 7-2. GlobalExceptionHandler 연계
- [x] 지도 도메인 예외를 공통 예외 핸들러에 연결
- [x] 잘못된 좌표/반경/필터 조합에 대한 명확한 에러 메시지 반환
- [x] 출력물:
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/common/exception/GlobalExceptionHandler.java`

---

## 스프린트 1 우선 구현 체크리스트

- [x] `Place`, `HospitalDetail` 모델 설계 완료
- [x] `GET /api/places` 구현 완료
- [x] `GET /api/places/{placeId}` 구현 완료
- [x] 위치 기반 반경 검색 적용 완료
- [x] 병원 전용 필터 적용 완료
- [x] `reviewSummary` 응답 포함 완료
- [x] 추천 장소 구분 필드 포함 완료
- [x] API 응답이 요구사항 정의서와 일치하도록 조정 완료
- [x] 기본 좌표(서울 시청: `37.5666`, `126.9784`) 정책 확인 완료

---

## 구현 시 주의사항

1. **스프린트 범위 관리**
- [x] 1차는 동물병원 조회 API 완성에 집중한다.
- [x] 리뷰 CRUD는 구현했고, 카페/식당/숙소는 관리자 등록 기반으로 확장했다.

2. **확장 가능한 모델링**
- [x] `Place`는 공통 필드 중심으로 설계하고 카테고리별 세부 정보는 별도 상세 엔티티로 분리한다.
- [x] 병원 외 카테고리가 추가되어도 엔티티 재설계가 최소화되도록 한다.

3. **위치 검색 성능**
- [x] 단순 `latitude/longitude between` 방식은 정확도 한계가 있으므로 반경 계산 쿼리 또는 공간 확장 사용을 검토한다.
- [ ] 데이터 규모가 커지면 공간 인덱스 기반으로 전환한다.

4. **필터 정책 명확화**
- [x] `is24Hours`, `isEmergencyAvailable`, `animalTypes`는 `category=HOSPITAL`에서만 허용한다.
- [ ] 그 외 카테고리에서 해당 필터가 들어오면 무시할지, 예외 처리할지 정책을 정한다.

5. **리뷰 요약 일관성**
- [x] 목록과 상세의 `reviewSummary` 계산 기준이 같아야 한다.
- [x] 삭제 리뷰 제외 여부를 명확히 고정한다.

6. **데이터 품질**
- [ ] 외부 API 데이터에는 전화번호, 영업시간, 좌표 누락이 있을 수 있다.
- [x] null 허용 필드와 필수 필드를 분리해 응답 안정성을 유지한다.

7. **추천 정책**
- [x] `isRecommended`는 광고/운영 노출용이므로 필터 조건으로만 동작하도록 정한다.
- [x] 추천 우선 노출은 적용하지 않는다.
- [x] 관리자 영역에서 추천 상태를 조회/수정할 수 있도록 관리 API를 제공한다.

8. **권한 정책**
- [x] 목록/상세/리뷰 조회는 비회원 허용
- [x] 리뷰 작성은 인증 필수
- [x] 관리자 추천 등록/수정 기능은 별도 Admin 영역에서 분리 관리

9. **응답 스키마 고정**
- [x] 프론트가 마커 목록과 상세를 분리 호출하므로 목록 응답은 가볍게 유지한다.
- [x] 상세 응답에서만 주소, 전화번호, 영업시간, 세부 정보 전체를 제공한다.

10. **기본 좌표 처리**
- [x] 위치 권한 거부 시 서버도 기본 좌표를 사용하도록 처리한다.
- [x] 서버 문서와 테스트 데이터도 동일한 기본 좌표 기준으로 맞춰 혼선을 줄인다.

---

## 권장 구현 순서 요약

- [x] `Place`, `HospitalDetail`, `PlaceReview` 모델 설계
- [x] 위치 기반 `PlaceRepositoryCustom` 구현
- [x] `GET /api/places` 구현
- [x] `GET /api/places/{placeId}` 구현
- [x] 리뷰 요약 집계 반영
- [x] 리뷰 API 구현
- [x] 카테고리 확장 진행
- [x] 추천 장소 관리 API 확장
