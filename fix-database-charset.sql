-- MariaDB 문자셋을 utf8mb4로 변경하는 SQL 스크립트
-- AWS RDS MariaDB에서 실행하세요

-- 1. 데이터베이스 문자셋 변경
ALTER DATABASE cmarket CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 2. 모든 테이블의 문자셋 변경
-- 기존 테이블들의 문자셋을 utf8mb4로 변경
ALTER TABLE users CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE products CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE favorites CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE community_posts CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE community_comments CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE chat_rooms CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE chat_messages CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE notifications CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE reports CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 3. 특정 컬럼의 문자셋 변경 (필요한 경우)
-- 예: nickname 컬럼이 있는 경우
-- ALTER TABLE users MODIFY COLUMN nickname VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 4. 문자셋 확인 쿼리
-- 데이터베이스 문자셋 확인
SELECT DEFAULT_CHARACTER_SET_NAME, DEFAULT_COLLATION_NAME 
FROM information_schema.SCHEMATA 
WHERE SCHEMA_NAME = 'cmarket';

-- 테이블 문자셋 확인
SELECT TABLE_NAME, TABLE_COLLATION 
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'cmarket';

-- 컬럼 문자셋 확인
SELECT TABLE_NAME, COLUMN_NAME, CHARACTER_SET_NAME, COLLATION_NAME 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = 'cmarket' 
AND CHARACTER_SET_NAME IS NOT NULL;


