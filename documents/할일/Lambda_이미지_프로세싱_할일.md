# Lambda 서버리스 이미지 프로세싱 할일

> 이미지 최적 구성을 150px, 400px, 800px (3가지)로 전환하고, AWS Lambda를 통한 서버리스 이미지 프로세싱 도입 시 수행할 작업 목록

---

## 개요

**요구사항 변경**
- **기존**: 서버에서 1200x800(원본), 300x300(썸네일)로 리사이즈 후 S3 업로드
- **변경**: **최적 구성 150px, 400px, 800px (3가지)** — Lighthouse 점수 최대화 목표

**도입 방향**
- **AWS Lambda**를 이용한 **서버리스 이미지 프로세싱**
- 업로드 시 원본만 S3에 저장하고, Lambda가 S3 이벤트에 반응해 150/400/800 리사이즈 + WebP 생성 후 저장
- API 서버에서는 리사이즈/썸네일 생성 로직 제거 → 원본 업로드만 담당

**현재 구조(제거 대상)**
- `ImageUploadService`: `ImageResizeService`를 사용해 1200/300 변환 후 원본·썸네일 모두 S3 업로드
- `ImageResizeService`: 1200x800, 300x300 리사이즈 및 WebP 변환
- `application*.properties`: `image.resize.*` 설정
- `build.gradle`: Scrimage(이미지 처리) 의존성

**변경 후 구조**
- `ImageUploadService`: **원본 파일만** S3에 업로드, 리사이즈/썸네일 호출 없음
- Lambda: S3 Put(원본) 이벤트 수신 → 150/400/800 리사이즈 + WebP → 같은 버킷(또는 prefix)에 저장
- 클라이언트/API: 반환 URL은 “원본 기준 경로”만 두고, 실제 파일은 150/400/800 중 선택해 사용(srcset 등)

---

## 목표 사양 요약

| 구분 | 내용 |
|------|------|
| 리사이즈 사이즈 | **150px, 400px, 800px** (3가지, 최대 변 길이 기준 등 비율 유지 권장) |
| 목적 | Lighthouse 점수 최대화 |
| 처리 주체 | AWS Lambda (서버리스) |
| 트리거 | S3 특정 prefix에 객체 업로드 시 (예: `user/`) |
| 출력 포맷 | WebP 권장 (용량·품질 균형) |

---

## 사용자가 직접 해야 할 작업

### Step 1: Lambda 이미지 프로세싱 함수 준비

#### 1-1. Lambda 함수 생성 및 런타임 선택
- **작업 내용**
  - AWS 콘솔 → Lambda → 함수 생성
  - 함수 이름: 예) `cmarket-image-resize`
  - 런타임: **Node.js 20.x** 권장 (Sharp 사용 시)
  - 아키텍처: x86_64 또는 arm64 (arm64 시 비용·성능 유리)
  - 실행 역할: S3 읽기/쓰기, CloudWatch Logs 등 필요한 권한을 가진 새 역할 또는 기존 역할
- **참고**
  - 이미지 처리 시 **Sharp** 사용 권장 (Node용, WebP 지원, 속도 좋음)
  - Sharp는 네이티브 모듈이므로 **Lambda Layer**로 분리하거나, 사전 빌드된 레이어 사용 검토

#### 1-2. Lambda 레이어 구성 (Sharp 등)
- **작업 내용**
  - Sharp 등 네이티브 의존성을 Lambda Layer로 패키징하거나, 커뮤니티 제공 레이어 활용
  - 예시: `serverless-sharp-image` 등 Sharp 포함 레이어 ARN 검색 후 연결
  - 또는 Docker 기반으로 Sharp 포함 이미지를 빌드 후 Lambda 이미지로 배포
- **출력**
  - Lambda에서 `require('sharp')` 사용 가능한 상태

#### 1-3. Lambda 핸들러 로직 (예시 사양)
- **입력**: S3 Event Notification 페이로드 (bucket, key 등)
- **동작**
  1. 이벤트에서 버킷명·객체 키 획득
  2. 해당 객체가 이미지인지 확장자/Content-Type으로 필터 (`.jpg`, `.jpeg`, `.png`, `.webp` 등)
  3. `_150`, `_400`, `_800` 등 이미 생성된 대상이면 스킵 (중복 처리 방지)
  4. S3에서 원본 다운로드
  5. Sharp로 **150, 400, 800** (최대 변 기준 리사이즈, 비율 유지) 생성 후 WebP로 인코딩
  6. 같은 버킷에 저장할 키 규칙 예:
     - 원본: `user/{userId}/{yyyy}/{MM}/{dd}/{uuid}.webp` (또는 업로드된 원본 확장자 유지)
     - 리사이즈: `user/{userId}/{yyyy}/{MM}/{dd}/{uuid}_150.webp`, `_400.webp`, `_800.webp`
     - 또는 prefix 분리: `user/.../150/`, `user/.../400/`, `user/.../800/` 등 팀 규칙에 맞게 결정
- **에러 처리**: 실패 시 재시도·DLQ 등 Lambda/SQS 설정 검토

#### 1-4. Lambda 권한 및 환경
- **작업 내용**
  - Lambda 실행 역할에 다음 최소 권한 부여:
    - `s3:GetObject` (원본 버킷, 트리거 prefix)
    - `s3:PutObject` (리사이즈 결과 저장 위치)
    - `logs:*` (CloudWatch Logs)
  - 필요 시 메모리 512MB~1024MB, 타임아웃 30~60초 정도로 튜닝

---

### Step 2: S3 이벤트로 Lambda 트리거 연결

#### 2-1. S3 버킷 이벤트 알림 설정
- **작업 내용**
  - 이미지가 업로드되는 S3 버킷(예: `cmarket-images`) 선택
  - 버킷 → 속성 → 이벤트 알림 → 알림 만들기
  - **이벤트 유형**: `s3:ObjectCreated:*` (Put, Post, Copy 등)
  - **접두사**(선택): `user/` — 사용자 업로드 경로만 Lambda로 보냄
  - **접미사**(선택): `.jpg`, `.jpeg`, `.png`, `.webp` 등으로 한정 가능
  - **대상**: Lambda 함수 → `cmarket-image-resize` 선택
  - Lambda 리소스 정책은 “S3가 해당 Lambda를 호출할 수 있도록” 자동 추가되는지 확인
- **주의**
  - Lambda가 리사이즈 결과를 같은 버킷 같은 prefix에 다시 넣으면, 그 Put 이벤트로 인해 **무한 루프** 가능
  - 반드시 Lambda 출력 키에 `_150`, `_400`, `_800` 접미사나 별도 prefix를 두고, **이벤트 필터/접미사에서 “원본만” 트리거되도록** 제한

#### 2-2. 무한 루프 방지 규칙 정리
- **작업 내용**
  - 트리거 접미사에 `_150`, `_400`, `_800` 을 **포함하지 않도록** 하거나,
  - Lambda 내부에서 키 이름에 `_150`, `_400`, `_800` 이 있으면 바로 return 하도록 구현
- **문서화**
  - “원본 업로드 키 규칙”과 “Lambda 출력 키 규칙”을 팀 문서나 이 할일 문서 하단에 적어 두기

---

### Step 3: CloudFront 및 캐시 (선택, 기존 유지)

- **작업 내용**
  - 기존에 CloudFront를 쓰는 경우, 150/400/800 객체도 같은 오리진( S3 )으로 서빙되므로 별도 오리진 추가 없이 사용 가능
  - Cache-Control은 Lambda에서 Put 시 `public, max-age=31536000` 등으로 설정해 두면 Lighthouse/캐시에 유리

---

## AI에게 위임할 수 있는 작업

### Step 4: 기존 서버 리사이즈 로직 제거

#### 4-1. ImageUploadService에서 리사이즈·썸네일 제거
- **작업 내용**
  - `ImageUploadService.uploadImages()` 수정:
    - `ImageResizeService` 주입 및 호출 제거
    - `imageResizeService.resizeAndConvertToWebp(file)` 호출 제거
    - `imageResizeService.createThumbnail(file)` 호출 제거
    - `imageResizeService.getFileExtension()`, `imageResizeService.getContentType()` 사용 부분 제거
  - **원본만** S3에 업로드하도록 변경:
    - 업로드 대상: 클라이언트가 보낸 `MultipartFile` 내용을 그대로 사용 (또는 확장자/Content-Type은 기존 검증 로직 유지)
    - 저장 키: 예) `user/{userId}/{yyyy}/{MM}/{dd}/{uuid}.{원본확장자}` (기존과 동일해도 됨)
  - 반환 URL은 **원본 객체 기준 URL** 하나만 반환 (기존 API 스펙 유지).  
    클라이언트는 나중에 150/400/800 URL 규칙을 따라 `srcset` 등으로 요청하면 됨.
- **출력물**
  - `service/cmarket/src/main/java/org/cmarket/cmarket/web/product/service/ImageUploadService.java` 수정

#### 4-2. ImageResizeService 제거
- **작업 내용**
  - `ImageResizeService`를 사용하는 코드가 `ImageUploadService`뿐인지 확인 후, 모두 제거
  - `ImageResizeService.java` 파일 삭제
- **출력물**
  - `ImageResizeService.java` 삭제
  - `ImageUploadService` 및 설정에서 해당 의존성 제거

#### 4-3. 이미지 리사이즈 관련 설정 제거
- **작업 내용**
  - `application.properties`, `application-prod.properties` 등에서 아래 프로퍼티 삭제:
    - `image.resize.enabled`
    - `image.resize.original.max-width`, `image.resize.original.max-height`
    - `image.resize.thumbnail.width`, `image.resize.thumbnail.height`
    - `image.resize.webp.enabled`, `image.resize.webp.quality`
- **출력물**
  - `service/cmarket/src/main/resources/application.properties` 수정
  - `service/cmarket/src/main/resources/application-prod.properties` 수정

#### 4-4. Scrimage 의존성 제거
- **작업 내용**
  - `service/cmarket/build.gradle`에서 다음 라인 삭제:
    - `implementation 'com.sksamuel.scrimage:scrimage-core:4.1.0'`
    - `implementation 'com.sksamuel.scrimage:scrimage-formats-extra:4.1.0'`
    - `implementation 'com.sksamuel.scrimage:scrimage-webp:4.3.5'`
  - `./gradlew :service:cmarket:compileJava` 등으로 빌드 가능 여부 확인
- **출력물**
  - `service/cmarket/build.gradle` 수정

---

### Step 5: URL 규칙 및 API 문서 정리

#### 5-1. 150/400/800 URL 규칙 문서화
- **작업 내용**
  - Lambda 저장 규칙이 확정된 뒤, 예시 URL 규칙을 문서로 정리
  - 예시:
    - 원본 URL(API 반환): `https://{cloudfront}/{user}/{userId}/{date}/{uuid}.webp`
    - 150px: `https://{cloudfront}/{user}/{userId}/{date}/{uuid}_150.webp`
    - 400px: `https://{cloudfront}/{user}/{userId}/{date}/{uuid}_400.webp`
    - 800px: `https://{cloudfront}/{user}/{userId}/{date}/{uuid}_800.webp`
  - 실제 규칙은 Lambda 구현(접미사/prefix)에 맞게 작성
- **출력물**
  - `documents/API문서/` 또는 이 할일 문서 하단에 “이미지 URL 규칙 (150/400/800)” 섹션 추가

#### 5-2. 이미지 업로드 API 스펙 정리
- **작업 내용**
  - “업로드 시 원본만 저장하고, 150/400/800은 Lambda가 비동기 생성”이라는 점을 API 문서에 명시
  - 응답 필드가 “원본 기준 URL” 하나만 있으면, 클라이언트가 150/400/800은 위 URL 규칙으로 조합한다고 기술
- **출력물**
  - `documents/API문서/` 내 이미지/상품 관련 API 문서 수정

---

### Step 6: 테스트 및 검증

#### 6-1. 업로드 플로우 검증
- **작업 내용**
  - API로 이미지 업로드 → 원본만 S3에 올라가는지 확인
  - 해당 키로 Lambda 트리거가 호출되는지, CloudWatch Logs로 확인
  - 150/400/800 객체가 기대한 키로 생성되는지 S3에서 확인
- **검증 항목**
  - API는 리사이즈 없이 원본만 업로드
  - Lambda 로그에 에러 없음
  - 150/400/800 파일 존재 및 용량·해상도 적절

#### 6-2. 기존 코드 영향 확인
- **작업 내용**
  - `ImageResizeService` 검색으로 다른 참조가 없는지 최종 확인
  - 상품/채팅/커뮤니티 등에서 “썸네일 URL” 또는 “_thumb” 등을 사용하는 부분이 있다면, 150/400/800 규칙으로 대체할지 여부 정리 후 반영
- **출력물**
  - 필요 시 해당 호출부를 150/400/800 URL 규칙 사용으로 수정

---

### 로컬 환경 검증 시나리오

아래는 **로컬(local)** 에서 API·S3·Lambda 동작을 단계별로 확인할 때 쓰는 검증 시나리오입니다.  
로컬 API는 `localhost`에서 띄우고, S3는 실제 AWS 버킷 또는 LocalStack을 사용한다고 가정합니다.

---

#### 사전 준비

| 항목 | 내용 |
|------|------|
| API 실행 | `./gradlew :service:cmarket:bootRun` 또는 IDE에서 `CmarketApiApplication` 실행, `http://localhost:8080` 에서 기동 확인 |
| AWS 연동 | 로컬에서 S3 접근 시 `~/.aws/credentials` 또는 `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` 설정. `application.properties`의 `aws.s3.bucket-name`, `aws.s3.region`이 테스트용 버킷을 가리키는지 확인 |
| 인증 토큰 | 로그인 API로 Access Token 발급 후, `Authorization: Bearer <token>` 사용 |
| 테스트 이미지 | jpg/png 등 원본 확장자 그대로 쓸 수 있는 이미지 파일 1~2개 (예: `test.jpg`, `test.png`) |

---

#### 시나리오 1: API 업로드 → 응답(원본 URL) 검증

**목표**: 서버가 리사이즈/썸네일 없이 **원본만** 올리고, 응답에 **원본 기준 URL**만 내려오는지 확인.

1. **요청**
   - `POST http://localhost:8080/api/images`
   - Header: `Authorization: Bearer <Access Token>`
   - Body: `multipart/form-data`, 필드명 `files`, 파일 예: `test.jpg` 1개

2. **응답 확인**
   - HTTP `201 Created`
   - `data.imageUrls`: 길이 1, 원소 하나가 원본 객체 URL
   - `data.mainImageUrl`: 위와 동일한 URL
   - `data.subImageUrls`: 빈 배열 또는 없음

3. **URL 형식 검증**
   - CloudFront 사용 시: `https://{cloudfront-domain}/user/{userId}/{yyyy/MM/dd}/{uuid}.jpg` 형태
   - CloudFront 미사용 시: `https://{bucket}.s3.{region}.amazonaws.com/user/.../{uuid}.jpg` 형태
   - **원본 확장자 유지**: 업로드한 파일이 `.jpg`면 URL도 `.../{uuid}.jpg`로 끝나야 함 (`.webp`로 바뀌면 서버 리사이즈가 남아 있는 것).

4. **다른 확장자**
   - `test.png` 업로드 → 응답 URL이 `.../{uuid}.png`로 끝나는지 확인.

---

#### 시나리오 2: S3에 원본만 존재하는지 확인

**목표**: 서버가 **원본 1개**만 넣고, `_thumb` / `_150` / `_400` / `_800` 은 서버가 생성하지 않음을 확인.

1. 시나리오 1로 업로드 후, 응답에 나온 URL에서 S3 키를 추출한다.  
   예: `https://.../user/1/2025/01/27/abc-uuid.jpg` → 키 `user/1/2025/01/27/abc-uuid.jpg`

2. **AWS 콘솔 → S3 → 해당 버킷**에서 해당 prefix(`user/1/2025/01/27/`) 아래 객체 목록 확인.

3. **기대 결과**
   - `abc-uuid.jpg` (또는 업로드한 확장자) **한 개만** 존재.
   - `abc-uuid_thumb.webp`, `abc-uuid_150.webp` 등은 **없는 것**이 정상 (이건 Lambda 쪽 검증 대상).

4. **(선택)** 객체 상세에서 **용량/메타** 확인: 원본 파일 크기와 비슷해야 함. 리사이즈된 작은 파일 크기만 있으면 이전 로직이 남아 있는 것.

---

#### 시나리오 3: 다중 파일 업로드 · 개수/용량 제한

**목표**: 2~5장 업로드, 개수/용량 제한, 원본 확장자 유지가 API 스펙대로 동작하는지 확인.

1. **정상**
   - 파일 2개 업로드 → `data.imageUrls` 길이 2, `mainImageUrl` = 첫 번째, `subImageUrls` = 나머지 1개.
   - 각 URL이 `.../{uuid}.{원본확장자}` 형태인지 확인.

2. **제한**
   - 파일 6개 업로드 → `400 Bad Request`, 메시지에 "최대 5장" 포함되는지 확인.
   - 5MB 초과 파일 1개 업로드 → `400`, "한 장당 최대 5MB" 등 메시지 확인.

---

#### 시나리오 4: Lambda 연동 시 (실제 AWS 환경)

**목표**: 원본 Put 이후 Lambda가 호출되어 150/400/800 WebP가 생성되는지 확인.  
로컬 API에서 S3로만 올리고, Lambda는 AWS에 배포된 상태라는 가정.

1. 시나리오 1·2로 원본이 `user/.../` prefix에 하나만 올라가는지 먼저 확인.

2. **Lambda**
   - AWS 콘솔 → Lambda → 해당 함수(예: `cmarket-image-resize`) → Monitor → CloudWatch Logs.
   - 업로드 직후 수 초 안에 로그 스트림이 생기고, 해당 객체 키로 처리 로그가 찍히는지 확인.
   - 에러 로그 없이 완료되는지 확인.

3. **S3 리사이즈 결과**
   - 같은 prefix 아래에 `{uuid}_150.webp`, `{uuid}_400.webp`, `{uuid}_800.webp`가 **Lambda에 의해** 생성되는지 확인.
   - 생성 시점은 업로드 후 수 초~수십 초일 수 있음.

4. **URL 조합 확인**
   - 원본 URL `.../uuid.jpg` → `.../uuid_150.webp`, `.../uuid_400.webp`, `.../uuid_800.webp` 로 바꿔 브라우저나 `curl`로 GET 요청 시, 200으로 이미지가 내려오는지 확인.

---

#### 시나리오 5: 기존 코드 영향 확인

**목표**: 리사이즈/썸네일 제거 후, 다른 모듈에서 `_thumb`/썸네일 URL을 쓰는지 여부 확인.

1. 코드베이스에서 다음 검색:
   - `_thumb` / `thumbnail` / `썸네일` / `ImageResizeService`
2. **기대**: `ImageResizeService` 참조는 없음. `_thumb`·썸네일 URL을 조합하는 코드도 없음.
3. 상품·채팅·커뮤니티 등에서 “썸네일용 URL”을 쓰는 기존 로직이 있다면, 150/400/800 규칙(`_150.webp` 등)으로 바꿀지 정리하고, 필요 시 해당 호출부만 수정.

---

#### 로컬에서 바로 할 수 있는 것 / AWS에서만 할 수 있는 것

| 검증 항목 | 로컬 API + 로컬만 | 로컬 API + 실제 S3 | 로컬 API + S3 + Lambda |
|-----------|-------------------|--------------------|--------------------------|
| 업로드 API 201, 응답 구조·URL 형식 | ✅ (S3 mock/테스트 시) | ✅ | ✅ |
| 응답 URL이 원본 확장자(.jpg 등) | ✅ | ✅ | ✅ |
| S3에 원본 1개만 존재 | — | ✅ | ✅ |
| Lambda 호출·150/400/800 생성 | — | — | ✅ |
| _thumb 없음, ImageResizeService 미참조 | ✅ (코드 검색) | ✅ | ✅ |

로컬 테스트만 할 경우에는 **시나리오 1·3·5**를 중심으로 두고, S3를 쓰는 환경이면 **시나리오 2**를 추가하면 됩니다.

---

## 작업 순서 권장

1. **선행 (AI 위임)**  
   Step 4 전체: 기존 서버 리사이즈 로직 제거(ImageUploadService, ImageResizeService, 설정, Scrimage)
2. **AWS (사용자)**  
   Step 1~2: Lambda 함수·레이어·S3 이벤트 설정
3. **정리 (AI 위임)**  
   Step 5: URL 규칙·API 문서 반영
4. **검증**  
   Step 6: 업로드 → Lambda → 150/400/800 생성 플로우 테스트

---

## 이미지 URL 규칙 (150/400/800)

- 서버는 **원본만** S3에 저장하고, Lambda가 S3 이벤트로 **150/400/800 WebP**를 비동기 생성합니다.
- API는 **원본 객체 기준 URL** 하나만 반환합니다. 클라이언트는 아래 규칙으로 150/400/800 URL을 조합해 사용합니다.

| 용도 | URL 규칙 | 비고 |
|------|----------|------|
| 원본(API 반환) | `https://{cloudfront}/user/{userId}/{yyyy/MM/dd}/{uuid}.{원본확장자}` | 업로드 직후 반환 (예: .jpg, .png, .webp) |
| 150px | `https://{cloudfront}/user/{userId}/{yyyy/MM/dd}/{uuid}_150.webp` | Lighthouse 등 소형 뷰 |
| 400px | `https://{cloudfront}/user/{userId}/{yyyy/MM/dd}/{uuid}_400.webp` | 중형 뷰 |
| 800px | `https://{cloudfront}/user/{userId}/{yyyy/MM/dd}/{uuid}_800.webp` | 대형/상세 뷰 |

- **조합 방법**: API에서 받은 원본 URL에서 `.{확장자}`를 `_150.webp`, `_400.webp`, `_800.webp`로 바꾸면 됩니다.  
  예) 원본 `.../uuid.jpg` → 150px `.../uuid_150.webp`, 400px `.../uuid_400.webp`, 800px `.../uuid_800.webp`
- 클라이언트는 `srcset`으로 150/400/800을 지정하면 Lighthouse에 유리합니다.
