# 📄 커들마켓 지도 서비스 백엔드 스프린트 1 (v1.0)

---

## 1. 스프린트 목표

지도에서 사용할 **동물병원 조회 API를 구축하는 것**

---

## 2. 이번 스프린트 범위

- 장소 데이터 모델 설계
- 동물병원 데이터 저장 구조
- 장소 목록 조회 API 구현
- 위치 기반 조회 구현

---

## 3. 제외 범위

- 리뷰 기능
- 카페/식당/숙소
- 추천 로직 고도화

---

## 4. 데이터 모델

### Place

- id
- category (hospital)
- name
- address
- phone
- operatingHours
- imageUrl (nullable)
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
- animalTypes

---

## 5. API 구현

---

### 5-1. 장소 목록 조회

GET /api/places

---

### Query Parameters

- category=hospital (필수)
- latitude (필수)
- longitude (필수)
- radius (필수, 단위: km)
- is24Hours
- isEmergencyAvailable
- animalTypes (복수 선택 가능)
- page
- size

---

### Response

    {
      "items": [
        {
          "id": 101,
          "category": "hospital",
          "name": "커들 동물병원",
          "latitude": 37.123,
          "longitude": 127.123,
          "isRecommended": false,
          "reviewSummary": {
            "reviewCount": 0,
            "averageRating": 0
          },
          "detail": {
            "is24Hours": true,
            "isEmergencyAvailable": true,
            "animalTypes": ["파충류", "조류"]
          }
        }
      ],
      "page": 1,
      "size": 20
    }

---

### 5-2. 장소 상세 조회

GET /api/places/{placeId}

### Response

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
      "isRecommended": false,
      "reviewSummary": {
        "reviewCount": 0,
        "averageRating": 0
      },
      "detail": {
        "is24Hours": true,
        "isEmergencyAvailable": true,
        "animalTypes": ["파충류", "조류"]
      }
    }

---

## 6. 핵심 구현 사항

- 카테고리 필터 (hospital)
- 위치 기반 조회 (lat/lng + radius)
- 병원 전용 필터 (is24Hours, isEmergencyAvailable, animalTypes)
- 추천 필드 포함
- 장소 상세 조회 API
- 페이지네이션 (page, size)

---

## 7. DB 고려사항

- latitude/longitude 인덱스 적용
- 위치 검색 최적화

---

## 8. 완료 기준 (Definition of Done)

- GET /api/places 목록 조회 정상 동작 (category, 위치, 필터, 페이지네이션)
- GET /api/places/{placeId} 상세 조회 정상 동작
- hospital 전용 필터 (is24Hours, isEmergencyAvailable, animalTypes) 정상 적용
- 위치 기반 반경 조회 정상 동작
- 응답 스키마가 API 요구사항 정의서와 일치

---

## 9. 주의사항

- 좌표 정확도
- 필터 조합 대응
- null 데이터 처리

---
