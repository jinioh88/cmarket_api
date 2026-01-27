# 이미지 API 문서

> 반려동물 용품 중고거래 서비스 - **이미지 업로드/조회** API 문서

본 문서는 이미지 업로드 및 조회 관련 API에 대한 상세 설명을 제공합니다.
상품 이미지, 프로필 이미지 등 다양한 도메인에서 공통으로 사용됩니다.

---

## 목차

- [공통 사항](#공통-사항)
- [이미지 업로드](#1-이미지-업로드)
- [프론트엔드: 이미지 URL 규칙 (150/400/800 WebP)](#프론트엔드-이미지-url-규칙-150400800-webp) ← 리사이즈 WebP 사용 시 필수 참고
- [이미지 조회](#2-이미지-조회)

---

## 공통 사항

### Base URL
```
http://localhost:8080
```

### 공통 응답 형식

#### 성공 응답
```json
{
  "code": "SUCCESS",
  "message": "성공",
  "data": { ... }
}
```

#### 에러 응답
```json
{
  "code": "BAD_REQUEST",
  "message": "에러 메시지",
  "traceId": "e1e4456f40d648c7a24fc7d5cd85e4af",
  "timestamp": "2025-11-14T15:45:00"
}
```

### HTTP 상태 코드

| 코드 | 설명 |
|------|------|
| 200 | 성공 |
| 201 | 생성됨 |
| 400 | 잘못된 요청 (입력값 검증 실패) |
| 401 | 인증 필요 |
| 404 | 리소스를 찾을 수 없음 |
| 500 | 서버 오류 |

### 공통 헤더

| 헤더명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| Content-Type | String | 예 | `multipart/form-data` (업로드 시), `application/json` (일반) |
| Authorization | String | 예 | `Bearer <Access Token>` 형식 (업로드 시 인증 필요) |
| X-Trace-Id | String | 아니오 | 요청 추적 ID (자동 생성) |

### ResponseCode Enum

모든 API 응답에서 사용되는 `ResponseCode` enum의 값들입니다.

| 코드 | 메시지 | 설명 |
|------|--------|------|
| 200 | 성공 | 요청이 성공적으로 처리됨 |
| 201 | 생성됨 | 리소스가 성공적으로 생성됨 |
| 400 | 잘못된 요청 | 요청 파라미터가 잘못되었거나 검증 실패 |
| 401 | 인증 필요 | 인증이 필요하거나 인증 정보가 유효하지 않음 |
| 404 | 찾을 수 없음 | 요청한 리소스를 찾을 수 없음 |
| 500 | 서버 오류 | 서버 내부 오류 발생 |

---

## 1. 이미지 업로드

이미지 파일을 서버에 업로드하고 URL을 반환합니다.

### 엔드포인트

```
POST /api/images
```

### 설명

- 현재 로그인한 사용자가 이미지 파일을 업로드합니다.
- 인증이 필수입니다 (JWT Access Token 필요).
- 최대 5장까지 업로드 가능합니다.
- 한 장당 최대 5MB, 전체 최대 25MB까지 업로드 가능합니다.
- 이미지 형식만 업로드 가능합니다 (jpg, jpeg, png, gif, webp).
- 파일명 중복 방지를 위해 UUID 기반 고유 파일명을 사용합니다.
- **저장 방식**: 업로드 시 **원본만** S3에 저장합니다. 150px/400px/800px 리사이즈 및 WebP 변환은 **AWS Lambda**가 S3 이벤트 기반으로 비동기 생성합니다.
- 저장 경로(원본): `user/{userId}/{yyyy}/{MM}/{dd}/{uuid}.{원본확장자}` (예: .jpg, .png, .webp)
- 첫 번째 이미지는 대표 이미지로, 나머지는 서브 이미지로 구분됩니다.

### Request

#### Headers

| 헤더명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| Authorization | String | 예 | `Bearer <Access Token>` 형식 |
| Content-Type | String | 예 | `multipart/form-data` |

#### Body (multipart/form-data)

| 필드명 | 타입 | 필수 | 설명 | 제약조건 |
|--------|------|------|------|----------|
| files | File[] | 예 | 업로드할 이미지 파일 리스트 | 최대 5개, 각 파일 최대 5MB, 전체 최대 25MB, 이미지 형식만 (jpg, jpeg, png, gif, webp) |

### Request 예시

#### cURL
```bash
curl -X POST "http://localhost:8080/api/images" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -F "files=@/path/to/image1.jpg" \
  -F "files=@/path/to/image2.png" \
  -F "files=@/path/to/image3.gif"
```

#### JavaScript (Fetch API)
```javascript
const formData = new FormData();
formData.append('files', file1); // File 객체
formData.append('files', file2);
formData.append('files', file3);

const response = await fetch('http://localhost:8080/api/images', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${accessToken}`
    // Content-Type은 자동으로 설정되므로 명시하지 않음
  },
  body: formData
});

const result = await response.json();
```

#### JavaScript (Axios)
```javascript
const formData = new FormData();
formData.append('files', file1);
formData.append('files', file2);
formData.append('files', file3);

const response = await axios.post('http://localhost:8080/api/images', formData, {
  headers: {
    'Authorization': `Bearer ${accessToken}`
    // Content-Type은 자동으로 설정되므로 명시하지 않음
  }
});
```

#### React 예시
```jsx
const handleImageUpload = async (files) => {
  const formData = new FormData();
  
  // FileList를 배열로 변환하여 추가
  Array.from(files).forEach((file) => {
    formData.append('files', file);
  });
  
  try {
    const response = await fetch('http://localhost:8080/api/images', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
      },
      body: formData
    });
    
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message);
    }
    
    const result = await response.json();
    console.log('업로드된 이미지 URL:', result.data.imageUrls);
    console.log('대표 이미지:', result.data.mainImageUrl);
    console.log('서브 이미지:', result.data.subImageUrls);
  } catch (error) {
    console.error('이미지 업로드 실패:', error);
  }
};

// 사용 예시
<input 
  type="file" 
  multiple 
  accept="image/jpeg,image/jpg,image/png,image/gif,image/webp"
  onChange={(e) => handleImageUpload(e.target.files)}
/>
```

### Response

#### 성공 응답 (201 Created)

| 필드명 | 타입 | 설명 |
|--------|------|------|
| code | String | 응답 코드 (예: "CREATED", "SUCCESS") |
| message | String | 응답 메시지 ("성공") |
| data | Object | 이미지 업로드 결과 |
| data.imageUrls | String[] | 업로드된 이미지 URL 리스트 (전체, **원본 기준 URL**) |
| data.mainImageUrl | String | 첫 번째 이미지 URL (대표 이미지, **원본 기준 URL**) |
| data.subImageUrls | String[] | 나머지 이미지 URL 리스트 (서브 이미지, 최대 4장, **원본 기준 URL**) |

> **150/400/800 리사이즈 URL**: 응답에 담기는 것은 원본 객체 기준 URL 하나뿐입니다. 클라이언트는 이 URL에서 파일명의 `.{확장자}`를 `_150.webp`, `_400.webp`, `_800.webp`로 바꿔 리사이즈 버전을 요청할 수 있습니다.  
> 예) 원본 `.../uuid.jpg` → 150px `.../uuid_150.webp`, 400px `.../uuid_400.webp`, 800px `.../uuid_800.webp`  
> 리사이즈 파일은 Lambda가 비동기 생성하므로, 업로드 직후에는 아직 생성 중일 수 있습니다. `srcset` 등으로 활용 시 Lighthouse 점수에 유리합니다.

#### Response Body 예시

```json
{
  "code": "CREATED",
  "message": "성공",
  "data": {
    "imageUrls": [
      "/api/images/user/1/2024/01/15/a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg",
      "/api/images/user/1/2024/01/15/b2c3d4e5-f6a7-8901-bcde-f12345678901.png",
      "/api/images/user/1/2024/01/15/c3d4e5f6-a7b8-9012-cdef-123456789012.gif"
    ],
    "mainImageUrl": "/api/images/user/1/2024/01/15/a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg",
    "subImageUrls": [
      "/api/images/user/1/2024/01/15/b2c3d4e5-f6a7-8901-bcde-f12345678901.png",
      "/api/images/user/1/2024/01/15/c3d4e5f6-a7b8-9012-cdef-123456789012.gif"
    ]
  }
}
```

### 에러 응답

#### 400 Bad Request - 파일이 없음
```json
{
  "code": "BAD_REQUEST",
  "message": "업로드할 이미지 파일이 없습니다.",
  "traceId": "e1e4456f40d648c7a24fc7d5cd85e4af",
  "timestamp": "2025-01-15T10:30:00"
}
```

#### 400 Bad Request - 파일 개수 초과
```json
{
  "code": "BAD_REQUEST",
  "message": "이미지는 최대 5장까지 업로드 가능합니다.",
  "traceId": "e1e4456f40d648c7a24fc7d5cd85e4af",
  "timestamp": "2025-01-15T10:30:00"
}
```

#### 400 Bad Request - 파일 형식 오류
```json
{
  "code": "BAD_REQUEST",
  "message": "이미지 파일만 업로드 가능합니다. (jpg, jpeg, png, gif, webp)",
  "traceId": "e1e4456f40d648c7a24fc7d5cd85e4af",
  "timestamp": "2025-01-15T10:30:00"
}
```

#### 400 Bad Request - 파일 크기 초과 (개별)
```json
{
  "code": "BAD_REQUEST",
  "message": "파일 크기는 한 장당 최대 5MB까지 가능합니다.",
  "traceId": "e1e4456f40d648c7a24fc7d5cd85e4af",
  "timestamp": "2025-01-15T10:30:00"
}
```

#### 400 Bad Request - 파일 크기 초과 (전체)
```json
{
  "code": "BAD_REQUEST",
  "message": "전체 파일 크기는 최대 25MB까지 가능합니다.",
  "traceId": "e1e4456f40d648c7a24fc7d5cd85e4af",
  "timestamp": "2025-01-15T10:30:00"
}
```

#### 400 Bad Request - 빈 파일
```json
{
  "code": "BAD_REQUEST",
  "message": "빈 파일은 업로드할 수 없습니다.",
  "traceId": "e1e4456f40d648c7a24fc7d5cd85e4af",
  "timestamp": "2025-01-15T10:30:00"
}
```

#### 400 Bad Request - 사용자 없음
```json
{
  "code": "BAD_REQUEST",
  "message": "사용자를 찾을 수 없습니다.",
  "traceId": "e1e4456f40d648c7a24fc7d5cd85e4af",
  "timestamp": "2025-01-15T10:30:00"
}
```

#### 401 Unauthorized - 인증 필요
```json
{
  "code": "UNAUTHORIZED",
  "message": "인증이 필요합니다.",
  "traceId": "e1e4456f40d648c7a24fc7d5cd85e4af",
  "timestamp": "2025-01-15T10:30:00"
}
```

### 주의사항

1. **파일명 중복 방지**: 서버에서 자동으로 UUID 기반 고유 파일명을 생성하므로, 클라이언트에서 파일명을 변경할 필요가 없습니다.

2. **저장 경로 구조**: 업로드된 이미지(원본)는 `user/{userId}/{yyyy}/{MM}/{dd}/{uuid}.{원본확장자}` 형식으로 S3에 저장됩니다. 150/400/800 WebP는 Lambda가 같은 경로에 `{uuid}_150.webp`, `{uuid}_400.webp`, `{uuid}_800.webp`로 비동기 생성합니다.
   - 원본 예: `user/1/2024/01/15/a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg`
   - 리사이즈 예: `.../a1b2c3d4-e5f6-7890-abcd-ef1234567890_150.webp` 등

3. **이미지 URL 사용**: 응답의 `imageUrls`/`mainImageUrl`/`subImageUrls`는 원본 기준 URL입니다. 상품 등록·프로필 수정 등에는 그대로 사용하고, 리사이즈가 필요하면 위 규칙(`_150.webp` 등)으로 URL을 조합해 사용하면 됩니다.
   - 대표 이미지: `data.mainImageUrl` 사용
   - 서브 이미지: `data.subImageUrls` 배열 사용

4. **파일 검증**: 클라이언트에서도 파일 형식과 크기를 미리 검증하는 것을 권장합니다.

5. **에러 처리**: 파일 업로드 실패 시 `traceId`를 포함하여 에러를 로깅하면 디버깅에 도움이 됩니다.

### 이미지 URL 규칙 (150/400/800)

업로드 응답의 `imageUrls`/`mainImageUrl`/`subImageUrls`는 **원본 객체 기준 URL**입니다. 150/400/800 리사이즈 WebP는 Lambda가 비동기 생성하며, 아래 규칙으로 URL을 조합해 사용할 수 있습니다.

| 용도 | URL 규칙 |
|------|-----------|
| 원본(API 반환) | `.../user/{userId}/{yyyy/MM/dd}/{uuid}.{원본확장자}` (예: .jpg, .png) |
| 150px | `.../user/{userId}/{yyyy/MM/dd}/{uuid}_150.webp` |
| 400px | `.../user/{userId}/{yyyy/MM/dd}/{uuid}_400.webp` |
| 800px | `.../user/{userId}/{yyyy/MM/dd}/{uuid}_800.webp` |

- **조합 방법**: 원본 URL에서 `.{확장자}`를 `_150.webp` / `_400.webp` / `_800.webp`로 바꾸면 됩니다.
- 업로드 직후에는 리사이즈 파일이 아직 생성 중일 수 있으므로, 필요 시 약간의 지연 후 요청하거나 폴백(원본)을 사용하면 됩니다.
- `srcset`으로 150/400/800을 지정하면 Lighthouse 등 성능 점수에 유리합니다.

---

## 프론트엔드: 이미지 URL 규칙 (150/400/800 WebP)

**프론트엔드 개발자는 화면에 이미지를 노출할 때 아래 규칙을 따라 150/400/800 WebP URL을 사용하세요.**  
API가 반환하는 URL은 **원본**만 포함하며, 리사이즈 WebP는 **URL 문자열 변환**으로 조합합니다.

### 1. 규칙 요약

| 용도 | 사용할 URL | 비고 |
|------|------------|------|
| 원본(업로드 응답 그대로) | `https://{cloudfront}/user/.../{uuid}.jpg` (또는 .png 등) | 상품/프로필 등에 저장·조회용으로 그대로 사용 |
| 150px WebP | `https://{cloudfront}/user/.../{uuid}_150.webp` | 썸네일, 목록 카드, 소형 뷰 |
| 400px WebP | `https://{cloudfront}/user/.../{uuid}_400.webp` | 중형 뷰, 그리드 |
| 800px WebP | `https://{cloudfront}/user/.../{uuid}_800.webp` | 상세/라이트박스, 대형 뷰 |

- **원본 URL**은 **확장자만 바꾸고**, 파일명 끝에 **`_150` / `_400` / `_800`** 를 붙이면 됩니다.
- 확장자는 항상 **`.webp`** 입니다.

### 2. 조합 방법 (구현 시 참고)

- **입력**: API 응답의 `data.imageUrls[i]` 또는 `data.mainImageUrl` (예: `https://df1xl13ui5mlo.cloudfront.net/user/1/2026/01/27/aff67e9a-e968-4c26-bd1c-5b8af72a3989.jpg`)
- **출력**: 같은 경로·파일명에서 확장자 부분을 `_{size}.webp` 로 변경한 URL

**예시**

- 원본: `https://df1xl13ui5mlo.cloudfront.net/user/1/2026/01/27/aff67e9a-e968-4c26-bd1c-5b8af72a3989.jpg`
- 150px: `https://df1xl13ui5mlo.cloudfront.net/user/1/2026/01/27/aff67e9a-e968-4c26-bd1c-5b8af72a3989_150.webp`
- 400px: `https://df1xl13ui5mlo.cloudfront.net/user/1/2026/01/27/aff67e9a-e968-4c26-bd1c-5b8af72a3989_400.webp`
- 800px: `https://df1xl13ui5mlo.cloudfront.net/user/1/2026/01/27/aff67e9a-e968-4c26-bd1c-5b8af72a3989_800.webp`

**조합 로직 (의사코드)**

```
toResizedWebpUrl(originalUrl, size) {
  // originalUrl: "https://.../uuid.jpg" 형태
  // size: 150 | 400 | 800
  // 반환: "https://.../uuid_150.webp" 형태
  return originalUrl.replace(/\.[^.]+$/, `_${size}.webp`);
}
```

- 정규식 `/\.[^.]+$/` 는 **마지막 `.{확장자}`** 만 매칭합니다.  
  예: `.../uuid.jpg` → `_150.webp` 로 치환 → `.../uuid_150.webp`

### 3. HTML/React 사용 예

- **단일 크기** (예: 썸네일 150px만 사용)
  - `src={toResizedWebpUrl(mainImageUrl, 150)}`
- **반응형 / srcset** (Lighthouse·성능 목적)
  - `srcSet={`${toResizedWebpUrl(url, 150)} 150w, ${toResizedWebpUrl(url, 400)} 400w, ${toResizedWebpUrl(url, 800)} 800w`}`  
  - `sizes`는 뷰포트에 맞게 지정 (예: `(max-width: 600px) 100vw, 400px` 등)

### 4. 주의사항

- **업로드 직후**: 리사이즈 WebP는 Lambda가 비동기 생성하므로, 직후 일시적으로 404가 나올 수 있습니다.  
  - 필요 시 원본 URL로 폴백하거나, 재시도/지연 후 `_150.webp` 등을 요청하세요.
- **저장/API 연동**: 상품 등록·프로필 수정 등 **서버에 넘기는 값**은 **원본 URL** (`imageUrls` / `mainImageUrl` / `subImageUrls`) 그대로 사용합니다.  
  - 150/400/800 WebP URL은 **화면 표시용**으로만 위 규칙으로 조합해 사용하면 됩니다.

---

## 2. 이미지 조회

업로드된 이미지를 조회합니다.

### 엔드포인트

```
GET /api/images/{imagePath}
```

### 설명

- 업로드된 이미지 파일을 조회합니다.
- 인증이 필요하지 않습니다 (공개 접근 가능).
- 이미지 경로는 업로드 시 받은 URL에서 `/api/images/` 부분을 제거한 경로입니다.
- 예: URL이 `/api/images/user/1/2024/01/15/filename.jpg`인 경우, 경로는 `user/1/2024/01/15/filename.jpg`입니다.

### Request

#### URL Path Parameters

| 파라미터명 | 타입 | 필수 | 설명 | 예시 |
|------------|------|------|------|------|
| imagePath | String | 예 | 이미지 경로 (user/{userId}/{yyyy}/{MM}/{dd}/{filename}) | `user/1/2024/01/15/a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg` |

#### Headers

| 헤더명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| - | - | - | 인증 불필요 |

### Response

#### 성공 응답 (200 OK)

| 필드명 | 타입 | 설명 |
|--------|------|------|
| Content-Type | String | 이미지 MIME 타입 (image/jpeg, image/png, image/gif, image/webp) |
| Content-Disposition | String | `inline; filename="{filename}"` 형식 |
| Body | Binary | 이미지 파일 바이너리 데이터 |

#### Response Headers 예시

```
HTTP/1.1 200 OK
Content-Type: image/jpeg
Content-Disposition: inline; filename="a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg"
Content-Length: 123456
```

### 에러 응답

#### 404 Not Found - 이미지 파일 없음
```json
{
  "code": "NOT_FOUND",
  "message": "이미지 파일을 찾을 수 없습니다.",
  "traceId": "e1e4456f40d648c7a24fc7d5cd85e4af",
  "timestamp": "2025-01-15T10:30:00"
}
```

### 주의사항

1. **URL 형식**: 이미지 URL은 업로드 시 받은 전체 URL을 그대로 사용하면 됩니다.
   - 예: `/api/images/user/1/2024/01/15/a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg`

2. **Content-Type**: 서버에서 파일 확장자에 따라 자동으로 Content-Type을 설정합니다.
   - `.jpg`, `.jpeg` → `image/jpeg`
   - `.png` → `image/png`
   - `.gif` → `image/gif`
   - `.webp` → `image/webp`

3. **캐싱**: 브라우저에서 이미지를 캐싱할 수 있으므로, 이미지 변경 시 URL이 변경됩니다 (UUID 기반).

4. **직접 접근**: 이미지 URL은 공개적으로 접근 가능하므로, 민감한 정보가 포함된 이미지는 업로드하지 않도록 주의해야 합니다.

---

## 전체 사용 흐름 예시

### 1. 이미지 업로드 → 상품 등록

```javascript
// 1. 이미지 업로드
const uploadImages = async (files) => {
  const formData = new FormData();
  Array.from(files).forEach((file) => {
    formData.append('files', file);
  });
  
  const response = await fetch('http://localhost:8080/api/images', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${accessToken}`
    },
    body: formData
  });
  
  const result = await response.json();
  return result.data; // { imageUrls, mainImageUrl, subImageUrls }
};
```

// 2. 상품 등록 시 업로드된 이미지 URL 사용
``` js
const createProduct = async (productData, uploadedImages) => {
  const response = await fetch('http://localhost:8080/api/products', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${accessToken}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      ...productData,
      mainImageUrl: uploadedImages.mainImageUrl,
      subImageUrls: uploadedImages.subImageUrls
    })
  });
  
  return await response.json();
};
```

### 2. 이미지 업로드 → 프로필 수정

``` js
// 1. 프로필 이미지 업로드 (단일 파일)
const uploadProfileImage = async (file) => {
  const formData = new FormData();
  formData.append('files', file);
  
  const response = await fetch('http://localhost:8080/api/images', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${accessToken}`
    },
    body: formData
  });
  
  const result = await response.json();
  return result.data.mainImageUrl; // 단일 이미지이므로 mainImageUrl 사용
};
```

// 2. 프로필 수정 시 업로드된 이미지 URL 사용
``` js
const updateProfile = async (profileData, profileImageUrl) => {
  const response = await fetch('http://localhost:8080/api/profile/me', {
    method: 'PATCH',
    headers: {
      'Authorization': `Bearer ${accessToken}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      ...profileData,
      profileImageUrl: profileImageUrl
    })
  });
  
  return await response.json();
};
```

## 변경 이력

| 버전 | 날짜 | 변경 내용 |
|------|------|----------|
| 1.0.0 | 2025-01-15 | 초기 문서 작성 |

