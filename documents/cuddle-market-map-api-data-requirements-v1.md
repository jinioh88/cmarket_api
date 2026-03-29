# 📄 커들마켓 지도 기반 생활 서비스 API/데이터 요구사항 정의서 (v1.0)

---

## 1. 문서 목적

본 문서는 커들마켓 지도 기반 생활 서비스 기능 개발을 위해  
백엔드와 프론트엔드가 공통으로 참고할 API 및 데이터 요구사항을 정의하는 것을 목적으로 한다.

---

## 2. 기능 범위

- 동물병원 조회
- 카페/식당/숙소 조회
- 지도 기반 위치 검색
- 필터 적용
- 추천 장소 구분
- 리뷰 조회/작성

### 제외

- 예약
- 결제
- 길찾기

---

## 3. 데이터 설계 방향

- 마커용 데이터와 상세 데이터 분리
- 추천 장소 구분 필드 필요
- 카테고리 확장 가능 구조
- 리뷰는 장소 단위 연결

---

## 4. 데이터 모델

### Place

- id
- category (hospital / cafe / restaurant / accommodation)
- name
- address
- phone
- operatingHours
- imageUrl (nullable, 대표 이미지)
- latitude
- longitude
- isRecommended
- createdAt
- updatedAt

---

### HospitalDetail

- placeId
- is24Hours
- isEmergencyAvailable
- animalTypes (예: ["파충류", "조류"])

---

### Review

- id
- placeId
- userId
- nickname
- rating (1~5)
- content
- imageUrls (선택사항, 리뷰 사진)
- createdAt

---

## 5. 필터

### 공통

- category
- isRecommended

### 병원

- is24Hours
- isEmergencyAvailable
- animalTypes

### 카페 / 식당 / 숙소

- 별도 필터 없음 (반려동물 동반 가능 장소만 노출되므로 필터 불필요)

---

## 6. API 설계 원칙

- 목록 / 상세 API 분리
- 위치 기반 조회: 반경 기반 (중심 좌표 + radius)
- 필터는 query parameter
- 리뷰는 별도 API
- 기본 위치: 서울 시청 (위치 권한 거부 시 fallback, 위도: 37.5666, 경도: 126.9784)

---

## 7. API

---

### 7-1. 장소 목록 조회

GET /api/places

Query:

- category (필수)
- latitude (필수)
- longitude (필수)
- radius (필수, 단위: km)
- isRecommended
- is24Hours (병원 전용)
- isEmergencyAvailable (병원 전용)
- animalTypes (병원 전용, 복수 선택 가능)
- page
- size

Response 예시:

    {
      "items": [
        {
          "id": 101,
          "category": "hospital",
          "name": "커들 24시 동물병원",
          "latitude": 37.123,
          "longitude": 127.123,
          "isRecommended": true,
          "reviewSummary": {
            "reviewCount": 18,
            "averageRating": 4.7
          }
        }
      ],
      "page": 1,
      "size": 20
    }

---

### 7-2. 장소 상세 조회

GET /api/places/{placeId}

Response 예시:

    {
      "id": 101,
      "category": "hospital",
      "name": "커들 동물병원",
      "address": "서울특별시 강남구 테헤란로 123",
      "phone": "02-1234-5678",
      "operatingHours": "09:00 ~ 18:00",
      "imageUrl": "https://example.com/image.jpg",
      "latitude": 37.4979,
      "longitude": 127.0276,
      "isRecommended": true,
      "reviewSummary": {
        "reviewCount": 18,
        "averageRating": 4.7
      },
      "detail": {
        "is24Hours": true,
        "isEmergencyAvailable": true,
        "animalTypes": ["파충류", "조류"]
      }
    }

---

### 7-3. 리뷰 조회

GET /api/places/{placeId}/reviews

Query:

- sort (latest / rating, 기본값: latest)
- page
- size

Response 예시:

    {
      "items": [
        {
          "id": 1,
          "nickname": "댕댕이맘",
          "rating": 5,
          "content": "친절하고 좋아요",
          "imageUrls": ["https://example.com/review1.jpg"],
          "createdAt": "2026-03-28T10:00:00"
        }
      ],
      "page": 1,
      "size": 10,
      "totalCount": 18
    }

---

### 7-4. 리뷰 작성

POST /api/places/{placeId}/reviews

Request:

    {
      "rating": 5,
      "content": "좋아요",
      "imageUrls": []
    }

---

## 8. 프론트 요구사항

- **지도 SDK**: Naver Maps JavaScript API v3 연동
- **마커 클러스터링**: 지도 축소 시 인접 마커를 그룹화하여 숫자로 표시
- **조회 방식**: 반경 기반 (현재 지도 중심 좌표 + radius)
- **기본 위치**: 서울 시청 (위도: 37.5666, 경도: 126.9784) — 위치 권한 거부 시 사용
- **기본 카테고리**: 동물병원
- **상세 데이터**: 마커 클릭 시 상세 조회 API 호출
- **리뷰 요약**: 목록 조회 응답의 reviewSummary 사용
- **네이버 지도앱 연결**: 상세 정보의 좌표(latitude/longitude)를 활용한 딥링크 생성
- **디바이스별 UI 분기**: 데스크탑(사이드바) / 모바일(하단 슬라이드 카드)

---

## 9. 백엔드 요구사항

- **위치 기반 조회**: 반경 검색 (중심 좌표 + radius km)
- **공간 인덱싱**: PostGIS 또는 동등한 공간 쿼리 지원
- **필터 지원**: 카테고리별 조건부 필터 처리 (병원 전용 필터는 category=hospital일 때만 적용)
- **추천 필드**: isRecommended로 일반/추천 장소 구분
- **리뷰 CRUD**: 조회(정렬/페이지네이션), 작성(사진 첨부 선택사항)
- **데이터 소스**: 네이버 지도 API 연동 (장소 검색/상세 정보)

---

## 10. 정책 고려사항

- 추천 장소 = 광고 가능
- 리뷰 권한 정책 필요
- 데이터 누락 대응 필요
- 결과 없음 처리 필요

---

## 11. 확장 가능성

- 즐겨찾기
- 리뷰 수정
- 제보 기능
- 광고 시스템
- 예약/결제
