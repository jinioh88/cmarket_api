-- 이미지 URL 확장자를 .webp로 변환하는 스크립트
-- 실행 전 반드시 백업을 수행하세요!

-- 확장자 패턴: .png, .jpg, .jpeg, .gif → .webp

-- ============================================
-- 1. products 테이블 - main_image_url
-- ============================================
UPDATE products
SET main_image_url = CASE
    WHEN main_image_url LIKE '%.png' THEN REPLACE(main_image_url, '.png', '.webp')
    WHEN main_image_url LIKE '%.jpg' THEN REPLACE(main_image_url, '.jpg', '.webp')
    WHEN main_image_url LIKE '%.jpeg' THEN REPLACE(main_image_url, '.jpeg', '.webp')
    WHEN main_image_url LIKE '%.gif' THEN REPLACE(main_image_url, '.gif', '.webp')
    ELSE main_image_url
END
WHERE main_image_url IS NOT NULL
  AND (main_image_url LIKE '%.png' 
       OR main_image_url LIKE '%.jpg' 
       OR main_image_url LIKE '%.jpeg' 
       OR main_image_url LIKE '%.gif');

-- ============================================
-- 2. product_sub_images 테이블 - image_url
-- ============================================
UPDATE product_sub_images
SET image_url = CASE
    WHEN image_url LIKE '%.png' THEN REPLACE(image_url, '.png', '.webp')
    WHEN image_url LIKE '%.jpg' THEN REPLACE(image_url, '.jpg', '.webp')
    WHEN image_url LIKE '%.jpeg' THEN REPLACE(image_url, '.jpeg', '.webp')
    WHEN image_url LIKE '%.gif' THEN REPLACE(image_url, '.gif', '.webp')
    ELSE image_url
END
WHERE image_url IS NOT NULL
  AND (image_url LIKE '%.png' 
       OR image_url LIKE '%.jpg' 
       OR image_url LIKE '%.jpeg' 
       OR image_url LIKE '%.gif');

-- ============================================
-- 3. users 테이블 - profile_image_url
-- ============================================
UPDATE users
SET profile_image_url = CASE
    WHEN profile_image_url LIKE '%.png' THEN REPLACE(profile_image_url, '.png', '.webp')
    WHEN profile_image_url LIKE '%.jpg' THEN REPLACE(profile_image_url, '.jpg', '.webp')
    WHEN profile_image_url LIKE '%.jpeg' THEN REPLACE(profile_image_url, '.jpeg', '.webp')
    WHEN profile_image_url LIKE '%.gif' THEN REPLACE(profile_image_url, '.gif', '.webp')
    ELSE profile_image_url
END
WHERE profile_image_url IS NOT NULL
  AND (profile_image_url LIKE '%.png' 
       OR profile_image_url LIKE '%.jpg' 
       OR profile_image_url LIKE '%.jpeg' 
       OR profile_image_url LIKE '%.gif');

-- ============================================
-- 4. posts 테이블 - author_profile_image_url
-- ============================================
UPDATE posts
SET author_profile_image_url = CASE
    WHEN author_profile_image_url LIKE '%.png' THEN REPLACE(author_profile_image_url, '.png', '.webp')
    WHEN author_profile_image_url LIKE '%.jpg' THEN REPLACE(author_profile_image_url, '.jpg', '.webp')
    WHEN author_profile_image_url LIKE '%.jpeg' THEN REPLACE(author_profile_image_url, '.jpeg', '.webp')
    WHEN author_profile_image_url LIKE '%.gif' THEN REPLACE(author_profile_image_url, '.gif', '.webp')
    ELSE author_profile_image_url
END
WHERE author_profile_image_url IS NOT NULL
  AND (author_profile_image_url LIKE '%.png' 
       OR author_profile_image_url LIKE '%.jpg' 
       OR author_profile_image_url LIKE '%.jpeg' 
       OR author_profile_image_url LIKE '%.gif');

-- ============================================
-- 5. post_images 테이블 - image_url
-- ============================================
UPDATE post_images
SET image_url = CASE
    WHEN image_url LIKE '%.png' THEN REPLACE(image_url, '.png', '.webp')
    WHEN image_url LIKE '%.jpg' THEN REPLACE(image_url, '.jpg', '.webp')
    WHEN image_url LIKE '%.jpeg' THEN REPLACE(image_url, '.jpeg', '.webp')
    WHEN image_url LIKE '%.gif' THEN REPLACE(image_url, '.gif', '.webp')
    ELSE image_url
END
WHERE image_url IS NOT NULL
  AND (image_url LIKE '%.png' 
       OR image_url LIKE '%.jpg' 
       OR image_url LIKE '%.jpeg' 
       OR image_url LIKE '%.gif');

-- ============================================
-- 6. chat_messages 테이블 - image_url
-- ============================================
UPDATE chat_messages
SET image_url = CASE
    WHEN image_url LIKE '%.png' THEN REPLACE(image_url, '.png', '.webp')
    WHEN image_url LIKE '%.jpg' THEN REPLACE(image_url, '.jpg', '.webp')
    WHEN image_url LIKE '%.jpeg' THEN REPLACE(image_url, '.jpeg', '.webp')
    WHEN image_url LIKE '%.gif' THEN REPLACE(image_url, '.gif', '.webp')
    ELSE image_url
END
WHERE image_url IS NOT NULL
  AND (image_url LIKE '%.png' 
       OR image_url LIKE '%.jpg' 
       OR image_url LIKE '%.jpeg' 
       OR image_url LIKE '%.gif');

-- ============================================
-- 7. report_images 테이블 - image_url
-- ============================================
UPDATE report_images
SET image_url = CASE
    WHEN image_url LIKE '%.png' THEN REPLACE(image_url, '.png', '.webp')
    WHEN image_url LIKE '%.jpg' THEN REPLACE(image_url, '.jpg', '.webp')
    WHEN image_url LIKE '%.jpeg' THEN REPLACE(image_url, '.jpeg', '.webp')
    WHEN image_url LIKE '%.gif' THEN REPLACE(image_url, '.gif', '.webp')
    ELSE image_url
END
WHERE image_url IS NOT NULL
  AND (image_url LIKE '%.png' 
       OR image_url LIKE '%.jpg' 
       OR image_url LIKE '%.jpeg' 
       OR image_url LIKE '%.gif');

-- ============================================
-- 검증 쿼리 (변경 전 확인)
-- ============================================
-- 실행 전에 아래 쿼리로 변경될 레코드 수를 확인하세요:

-- SELECT COUNT(*) FROM products WHERE main_image_url LIKE '%.png' OR main_image_url LIKE '%.jpg' OR main_image_url LIKE '%.jpeg' OR main_image_url LIKE '%.gif';
-- SELECT COUNT(*) FROM product_sub_images WHERE image_url LIKE '%.png' OR image_url LIKE '%.jpg' OR image_url LIKE '%.jpeg' OR image_url LIKE '%.gif';
-- SELECT COUNT(*) FROM users WHERE profile_image_url LIKE '%.png' OR profile_image_url LIKE '%.jpg' OR profile_image_url LIKE '%.jpeg' OR profile_image_url LIKE '%.gif';
-- SELECT COUNT(*) FROM posts WHERE author_profile_image_url LIKE '%.png' OR author_profile_image_url LIKE '%.jpg' OR author_profile_image_url LIKE '%.jpeg' OR author_profile_image_url LIKE '%.gif';
-- SELECT COUNT(*) FROM post_images WHERE image_url LIKE '%.png' OR image_url LIKE '%.jpg' OR image_url LIKE '%.jpeg' OR image_url LIKE '%.gif';
-- SELECT COUNT(*) FROM chat_messages WHERE image_url LIKE '%.png' OR image_url LIKE '%.jpg' OR image_url LIKE '%.jpeg' OR image_url LIKE '%.gif';
-- SELECT COUNT(*) FROM report_images WHERE image_url LIKE '%.png' OR image_url LIKE '%.jpg' OR image_url LIKE '%.jpeg' OR image_url LIKE '%.gif';

-- ============================================
-- 검증 쿼리 (변경 후 확인)
-- ============================================
-- 실행 후에 아래 쿼리로 변경이 완료되었는지 확인하세요:

-- SELECT COUNT(*) FROM products WHERE main_image_url LIKE '%.webp';
-- SELECT COUNT(*) FROM product_sub_images WHERE image_url LIKE '%.webp';
-- SELECT COUNT(*) FROM users WHERE profile_image_url LIKE '%.webp';
-- SELECT COUNT(*) FROM posts WHERE author_profile_image_url LIKE '%.webp';
-- SELECT COUNT(*) FROM post_images WHERE image_url LIKE '%.webp';
-- SELECT COUNT(*) FROM chat_messages WHERE image_url LIKE '%.webp';
-- SELECT COUNT(*) FROM report_images WHERE image_url LIKE '%.webp';
