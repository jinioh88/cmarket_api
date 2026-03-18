-- pet_type, pet_detail_type 컬럼 길이 확장
-- 새 PetType/PetDetailType enum 추가로 인한 "Data truncated" 오류 해결
-- 실행 전 반드시 백업을 수행하세요!
--
-- 최대 길이: pet_type=AMPHIBIAN_REAL(13), pet_detail_type=AQUATIC_PLANT(13)
-- 여유를 두어 VARCHAR(50)으로 확장

ALTER TABLE products
    MODIFY COLUMN pet_type VARCHAR(50) NOT NULL,
    MODIFY COLUMN pet_detail_type VARCHAR(50) NOT NULL;
