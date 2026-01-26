# S3 URL → CloudFront URL 마이그레이션 가이드

## 개요

기존 DB에 저장된 S3 URL을 CloudFront URL로 일괄 변경하는 스크립트입니다.

## 변경 대상 테이블

1. **products** - `main_image_url` (상품 대표 이미지)
2. **product_sub_images** - `image_url` (상품 서브 이미지)
3. **users** - `profile_image_url` (사용자 프로필 이미지)
4. **posts** - `author_profile_image_url` (게시글 작성자 프로필 이미지)
5. **post_images** - `image_url` (게시글 이미지)
6. **chat_messages** - `image_url` (채팅 이미지)
7. **report_images** - `image_url` (신고 이미지)

## 실행 전 준비사항

### 1. 데이터베이스 백업
```bash
# MariaDB 백업 (예시)
mysqldump -u [username] -p [database_name] > backup_before_migration_$(date +%Y%m%d_%H%M%S).sql
```

### 2. 변경될 레코드 수 확인
```sql
-- 각 테이블별로 변경될 레코드 수 확인
SELECT COUNT(*) FROM products WHERE main_image_url LIKE 'https://cmarket-images.s3.ap-northeast-2.amazonaws.com/%';
SELECT COUNT(*) FROM product_sub_images WHERE image_url LIKE 'https://cmarket-images.s3.ap-northeast-2.amazonaws.com/%';
SELECT COUNT(*) FROM users WHERE profile_image_url LIKE 'https://cmarket-images.s3.ap-northeast-2.amazonaws.com/%';
SELECT COUNT(*) FROM posts WHERE author_profile_image_url LIKE 'https://cmarket-images.s3.ap-northeast-2.amazonaws.com/%';
SELECT COUNT(*) FROM post_images WHERE image_url LIKE 'https://cmarket-images.s3.ap-northeast-2.amazonaws.com/%';
SELECT COUNT(*) FROM chat_messages WHERE image_url LIKE 'https://cmarket-images.s3.ap-northeast-2.amazonaws.com/%';
SELECT COUNT(*) FROM report_images WHERE image_url LIKE 'https://cmarket-images.s3.ap-northeast-2.amazonaws.com/%';
```

## 실행 방법

### 방법 1: MySQL/MariaDB 클라이언트 사용
```bash
mysql -u [username] -p [database_name] < scripts/migrate-s3-to-cloudfront-urls.sql
```

### 방법 2: MySQL Workbench 또는 DBeaver 사용
1. SQL 스크립트 파일 열기
2. 각 UPDATE 문을 하나씩 실행하거나 전체 실행
3. 트랜잭션으로 묶어서 실행하는 것을 권장

### 방법 3: Spring Boot 애플리케이션에서 실행
```java
// @SpringBootTest 또는 CommandLineRunner로 실행
@Autowired
private JdbcTemplate jdbcTemplate;

public void migrateUrls() {
    // 스크립트 내용을 Java 코드로 실행
}
```

## 실행 후 검증

### 1. 변경 완료 확인
```sql
-- CloudFront URL로 변경된 레코드 수 확인
SELECT COUNT(*) FROM products WHERE main_image_url LIKE 'https://df1xl13ui5mlo.cloudfront.net/%';
SELECT COUNT(*) FROM product_sub_images WHERE image_url LIKE 'https://df1xl13ui5mlo.cloudfront.net/%';
SELECT COUNT(*) FROM users WHERE profile_image_url LIKE 'https://df1xl13ui5mlo.cloudfront.net/%';
SELECT COUNT(*) FROM posts WHERE author_profile_image_url LIKE 'https://df1xl13ui5mlo.cloudfront.net/%';
SELECT COUNT(*) FROM post_images WHERE image_url LIKE 'https://df1xl13ui5mlo.cloudfront.net/%';
SELECT COUNT(*) FROM chat_messages WHERE image_url LIKE 'https://df1xl13ui5mlo.cloudfront.net/%';
SELECT COUNT(*) FROM report_images WHERE image_url LIKE 'https://df1xl13ui5mlo.cloudfront.net/%';
```

### 2. 샘플 데이터 확인
```sql
-- 각 테이블에서 샘플 데이터 확인
SELECT id, main_image_url FROM products LIMIT 5;
SELECT product_id, image_url FROM product_sub_images LIMIT 5;
SELECT id, profile_image_url FROM users WHERE profile_image_url IS NOT NULL LIMIT 5;
```

### 3. 애플리케이션 테스트
- 이미지가 정상적으로 로드되는지 확인
- API 응답에서 CloudFront URL이 반환되는지 확인

## 주의사항

1. **반드시 백업 후 실행**: 데이터베이스 백업을 먼저 수행하세요.
2. **트랜잭션 사용 권장**: 모든 UPDATE를 하나의 트랜잭션으로 묶어서 실행하세요.
3. **단계별 실행**: 한 번에 모든 테이블을 업데이트하지 말고, 테이블별로 나누어 실행하는 것을 권장합니다.
4. **롤백 계획**: 문제 발생 시 백업으로 복구할 수 있도록 준비하세요.

## 문제 해결

### S3 URL이 남아있는 경우
```sql
-- 특정 테이블에서 S3 URL이 남아있는지 확인
SELECT * FROM products WHERE main_image_url LIKE 'https://cmarket-images.s3.ap-northeast-2.amazonaws.com/%';
```

### CloudFront URL이 제대로 적용되지 않은 경우
- CloudFront 배포가 활성화되어 있는지 확인
- S3 버킷 정책이 CloudFront OAC를 허용하는지 확인
- URL 패턴이 정확한지 확인

## 참고

- CloudFront 도메인: `df1xl13ui5mlo.cloudfront.net`
- S3 버킷: `cmarket-images`
- S3 리전: `ap-northeast-2`
