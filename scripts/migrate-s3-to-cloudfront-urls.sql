-- S3 URL을 CloudFront URL로 마이그레이션 스크립트
-- 실행 전 반드시 백업을 수행하세요!

-- S3 URL 패턴: https://cmarket-images.s3.ap-northeast-2.amazonaws.com/{path}
-- CloudFront URL 패턴: https://df1xl13ui5mlo.cloudfront.net/{path}

-- ============================================
-- 1. products 테이블 - main_image_url
-- ============================================
UPDATE products
SET main_image_url = REPLACE(
    main_image_url,
    'https://cmarket-images.s3.ap-northeast-2.amazonaws.com/',
    'https://df1xl13ui5mlo.cloudfront.net/'
)
WHERE main_image_url LIKE 'https://cmarket-images.s3.ap-northeast-2.amazonaws.com/%';

-- ============================================
-- 2. product_sub_images 테이블 - image_url
-- ============================================
UPDATE product_sub_images
SET image_url = REPLACE(
    image_url,
    'https://cmarket-images.s3.ap-northeast-2.amazonaws.com/',
    'https://df1xl13ui5mlo.cloudfront.net/'
)
WHERE image_url LIKE 'https://cmarket-images.s3.ap-northeast-2.amazonaws.com/%';

-- ============================================
-- 3. users 테이블 - profile_image_url
-- ============================================
UPDATE users
SET profile_image_url = REPLACE(
    profile_image_url,
    'https://cmarket-images.s3.ap-northeast-2.amazonaws.com/',
    'https://df1xl13ui5mlo.cloudfront.net/'
)
WHERE profile_image_url LIKE 'https://cmarket-images.s3.ap-northeast-2.amazonaws.com/%';

-- ============================================
-- 4. posts 테이블 - author_profile_image_url
-- ============================================
UPDATE posts
SET author_profile_image_url = REPLACE(
    author_profile_image_url,
    'https://cmarket-images.s3.ap-northeast-2.amazonaws.com/',
    'https://df1xl13ui5mlo.cloudfront.net/'
)
WHERE author_profile_image_url LIKE 'https://cmarket-images.s3.ap-northeast-2.amazonaws.com/%';

-- ============================================
-- 5. post_images 테이블 - image_url
-- ============================================
UPDATE post_images
SET image_url = REPLACE(
    image_url,
    'https://cmarket-images.s3.ap-northeast-2.amazonaws.com/',
    'https://df1xl13ui5mlo.cloudfront.net/'
)
WHERE image_url LIKE 'https://cmarket-images.s3.ap-northeast-2.amazonaws.com/%';

-- ============================================
-- 6. chat_messages 테이블 - image_url
-- ============================================
UPDATE chat_messages
SET image_url = REPLACE(
    image_url,
    'https://cmarket-images.s3.ap-northeast-2.amazonaws.com/',
    'https://df1xl13ui5mlo.cloudfront.net/'
)
WHERE image_url LIKE 'https://cmarket-images.s3.ap-northeast-2.amazonaws.com/%';

-- ============================================
-- 7. report_images 테이블 - image_url
-- ============================================
UPDATE report_images
SET image_url = REPLACE(
    image_url,
    'https://cmarket-images.s3.ap-northeast-2.amazonaws.com/',
    'https://df1xl13ui5mlo.cloudfront.net/'
)
WHERE image_url LIKE 'https://cmarket-images.s3.ap-northeast-2.amazonaws.com/%';

-- ============================================
-- 검증 쿼리 (변경 전 확인)
-- ============================================
-- 실행 전에 아래 쿼리로 변경될 레코드 수를 확인하세요:

-- SELECT COUNT(*) FROM products WHERE main_image_url LIKE 'https://cmarket-images.s3.ap-northeast-2.amazonaws.com/%';
-- SELECT COUNT(*) FROM product_sub_images WHERE image_url LIKE 'https://cmarket-images.s3.ap-northeast-2.amazonaws.com/%';
-- SELECT COUNT(*) FROM users WHERE profile_image_url LIKE 'https://cmarket-images.s3.ap-northeast-2.amazonaws.com/%';
-- SELECT COUNT(*) FROM posts WHERE author_profile_image_url LIKE 'https://cmarket-images.s3.ap-northeast-2.amazonaws.com/%';
-- SELECT COUNT(*) FROM post_images WHERE image_url LIKE 'https://cmarket-images.s3.ap-northeast-2.amazonaws.com/%';
-- SELECT COUNT(*) FROM chat_messages WHERE image_url LIKE 'https://cmarket-images.s3.ap-northeast-2.amazonaws.com/%';
-- SELECT COUNT(*) FROM report_images WHERE image_url LIKE 'https://cmarket-images.s3.ap-northeast-2.amazonaws.com/%';

-- ============================================
-- 검증 쿼리 (변경 후 확인)
-- ============================================
-- 실행 후에 아래 쿼리로 변경이 완료되었는지 확인하세요:

-- SELECT COUNT(*) FROM products WHERE main_image_url LIKE 'https://df1xl13ui5mlo.cloudfront.net/%';
-- SELECT COUNT(*) FROM product_sub_images WHERE image_url LIKE 'https://df1xl13ui5mlo.cloudfront.net/%';
-- SELECT COUNT(*) FROM users WHERE profile_image_url LIKE 'https://df1xl13ui5mlo.cloudfront.net/%';
-- SELECT COUNT(*) FROM posts WHERE author_profile_image_url LIKE 'https://df1xl13ui5mlo.cloudfront.net/%';
-- SELECT COUNT(*) FROM post_images WHERE image_url LIKE 'https://df1xl13ui5mlo.cloudfront.net/%';
-- SELECT COUNT(*) FROM chat_messages WHERE image_url LIKE 'https://df1xl13ui5mlo.cloudfront.net/%';
-- SELECT COUNT(*) FROM report_images WHERE image_url LIKE 'https://df1xl13ui5mlo.cloudfront.net/%';
