# 이미지 API 문서

> 반려동물 용품 중고거래 서비스 - **이미지 업로드/조회** API 문서

본 문서는 이미지 업로드 및 조회 관련 API에 대한 상세 설명을 제공합니다.
상품 이미지, 프로필 이미지 등 다양한 도메인에서 공통으로 사용됩니다.

---

## 목차

- [공통 사항](#공통-사항)
- [이미지 업로드](#1-이미지-업로드)
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
- 저장 경로: `user/{userId}/{yyyy}/{MM}/{dd}/{uuid}.{ext}`
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
| data.imageUrls | String[] | 업로드된 이미지 URL 리스트 (전체) |
| data.mainImageUrl | String | 첫 번째 이미지 URL (대표 이미지) |
| data.subImageUrls | String[] | 나머지 이미지 URL 리스트 (서브 이미지, 최대 4장) |

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

2. **저장 경로 구조**: 업로드된 이미지는 `user/{userId}/{yyyy}/{MM}/{dd}/{uuid}.{ext}` 형식으로 저장됩니다.
   - 예: `user/1/2024/01/15/a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg`

3. **이미지 URL 사용**: 응답으로 받은 이미지 URL은 상품 등록, 프로필 수정 등에서 바로 사용할 수 있습니다.
   - 대표 이미지: `data.mainImageUrl` 사용
   - 서브 이미지: `data.subImageUrls` 배열 사용

4. **파일 검증**: 클라이언트에서도 파일 형식과 크기를 미리 검증하는 것을 권장합니다.

5. **에러 처리**: 파일 업로드 실패 시 `traceId`를 포함하여 에러를 로깅하면 디버깅에 도움이 됩니다.

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

