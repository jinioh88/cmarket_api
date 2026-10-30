-- posts.board_type 에 EVENT(행사 소식)를 넣을 수 있게 칸을 문자열로 바꾼다
--
-- 하이버네이트 6 은 MySQL 에서 enum 칸을 ENUM('QUESTION','INFO') 로 만든다.
-- ddl-auto=update 는 이미 있는 칸을 고치지 않아서, EVENT 를 저장하면 "Data truncated" 가 난다.
-- (pet_type 때도 같은 일이 있었다 — alter-pet-type-columns.sql)
--
-- 1) 먼저 칸 모양을 본다. Type 이 varchar(20) 이면 아래 ALTER 는 필요 없다.
SHOW COLUMNS FROM posts LIKE 'board_type';

-- 2) Type 이 enum(...) 이면 실행한다. 실행 전 반드시 백업을 수행하세요!
ALTER TABLE posts
    MODIFY COLUMN board_type VARCHAR(20) NOT NULL;
