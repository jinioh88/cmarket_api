-- Post 및 Comment 초기 데이터 삽입
-- 정렬 테스트를 위한 다양한 날짜, 댓글 수, 조회수를 포함한 데이터

-- ============================================
-- FREE 게시판 (12개)
-- ============================================

-- Post 1 (FREE) - 최신, 댓글 5개, 조회수 120
INSERT INTO posts (author_id, author_nickname, title, content, board_type, view_count, comment_count, created_at, updated_at) 
VALUES (1, '사용자1', '자유게시판 첫 번째 글', '이것은 자유게시판의 첫 번째 게시글입니다. 다양한 내용을 담고 있어요.', 'FREE', 120, 5, 
        TIMESTAMPADD(DAY, -2, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -2, CURRENT_TIMESTAMP));

-- Post 2 (FREE) - 3일 전, 댓글 12개, 조회수 250
INSERT INTO posts (author_id, author_nickname, title, content, board_type, view_count, comment_count, created_at, updated_at) 
VALUES (2, '사용자2', '자유게시판 두 번째 글', '자유롭게 이야기를 나눠봐요. 다양한 의견을 환영합니다!', 'FREE', 250, 12, 
        TIMESTAMPADD(DAY, -3, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -3, CURRENT_TIMESTAMP));

-- Post 3 (FREE) - 5일 전, 댓글 0개, 조회수 45
INSERT INTO posts (author_id, author_nickname, title, content, board_type, view_count, comment_count, created_at, updated_at) 
VALUES (3, '사용자3', '자유게시판 세 번째 글', '댓글이 없는 게시글입니다. 조회수는 적지만 내용은 유용해요.', 'FREE', 45, 0, 
        TIMESTAMPADD(DAY, -5, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -5, CURRENT_TIMESTAMP));

-- Post 4 (FREE) - 7일 전, 댓글 25개, 조회수 380
INSERT INTO posts (author_id, author_nickname, title, content, board_type, view_count, comment_count, created_at, updated_at) 
VALUES (1, '사용자1', '자유게시판 네 번째 글', '많은 댓글이 달린 인기 게시글입니다. 활발한 토론이 이루어지고 있어요.', 'FREE', 380, 25, 
        TIMESTAMPADD(DAY, -7, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -7, CURRENT_TIMESTAMP));

-- Post 5 (FREE) - 10일 전, 댓글 8개, 조회수 150
INSERT INTO posts (author_id, author_nickname, title, content, board_type, view_count, comment_count, created_at, updated_at) 
VALUES (2, '사용자2', '자유게시판 다섯 번째 글', '10일 전에 작성된 게시글입니다. 여전히 관심을 받고 있어요.', 'FREE', 150, 8, 
        TIMESTAMPADD(DAY, -10, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -10, CURRENT_TIMESTAMP));

-- Post 6 (FREE) - 12일 전, 댓글 35개, 조회수 420
INSERT INTO posts (author_id, author_nickname, title, content, board_type, view_count, comment_count, created_at, updated_at) 
VALUES (3, '사용자3', '자유게시판 여섯 번째 글', '많은 댓글과 조회수를 기록한 인기 게시글입니다.', 'FREE', 420, 35, 
        TIMESTAMPADD(DAY, -12, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -12, CURRENT_TIMESTAMP));

-- Post 7 (FREE) - 15일 전, 댓글 3개, 조회수 80
INSERT INTO posts (author_id, author_nickname, title, content, board_type, view_count, comment_count, created_at, updated_at) 
VALUES (1, '사용자1', '자유게시판 일곱 번째 글', '15일 전에 작성된 게시글입니다. 조용한 게시글이에요.', 'FREE', 80, 3, 
        TIMESTAMPADD(DAY, -15, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -15, CURRENT_TIMESTAMP));

-- Post 8 (FREE) - 18일 전, 댓글 18개, 조회수 290
INSERT INTO posts (author_id, author_nickname, title, content, board_type, view_count, comment_count, created_at, updated_at) 
VALUES (2, '사용자2', '자유게시판 여덟 번째 글', '18일 전 게시글이지만 여전히 댓글이 달리고 있어요.', 'FREE', 290, 18, 
        TIMESTAMPADD(DAY, -18, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -18, CURRENT_TIMESTAMP));

-- Post 9 (FREE) - 20일 전, 댓글 1개, 조회수 60
INSERT INTO posts (author_id, author_nickname, title, content, board_type, view_count, comment_count, created_at, updated_at) 
VALUES (3, '사용자3', '자유게시판 아홉 번째 글', '20일 전 게시글입니다. 조용한 게시글이에요.', 'FREE', 60, 1, 
        TIMESTAMPADD(DAY, -20, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -20, CURRENT_TIMESTAMP));

-- Post 10 (FREE) - 22일 전, 댓글 42개, 조회수 480
INSERT INTO posts (author_id, author_nickname, title, content, board_type, view_count, comment_count, created_at, updated_at) 
VALUES (1, '사용자1', '자유게시판 열 번째 글', '많은 댓글이 달린 매우 인기 있는 게시글입니다!', 'FREE', 480, 42, 
        TIMESTAMPADD(DAY, -22, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -22, CURRENT_TIMESTAMP));

-- Post 11 (FREE) - 25일 전, 댓글 6개, 조회수 110
INSERT INTO posts (author_id, author_nickname, title, content, board_type, view_count, comment_count, created_at, updated_at) 
VALUES (2, '사용자2', '자유게시판 열한 번째 글', '25일 전에 작성된 게시글입니다.', 'FREE', 110, 6, 
        TIMESTAMPADD(DAY, -25, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -25, CURRENT_TIMESTAMP));

-- Post 12 (FREE) - 28일 전, 댓글 50개, 조회수 500
INSERT INTO posts (author_id, author_nickname, title, content, board_type, view_count, comment_count, created_at, updated_at) 
VALUES (3, '사용자3', '자유게시판 열두 번째 글', '최대 댓글 수와 조회수를 기록한 최고 인기 게시글입니다!', 'FREE', 500, 50, 
        TIMESTAMPADD(DAY, -28, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -28, CURRENT_TIMESTAMP));

-- ============================================
-- QUESTION 게시판 (12개)
-- ============================================

-- Post 13 (QUESTION) - 1일 전, 댓글 7개, 조회수 180
INSERT INTO posts (author_id, author_nickname, title, content, board_type, view_count, comment_count, created_at, updated_at) 
VALUES (1, '사용자1', '질문있어요 첫 번째 글', '이것에 대해 궁금한 점이 있어서 질문드립니다. 도움 부탁드려요!', 'QUESTION', 180, 7, 
        TIMESTAMPADD(DAY, -1, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -1, CURRENT_TIMESTAMP));

-- Post 14 (QUESTION) - 4일 전, 댓글 15개, 조회수 320
INSERT INTO posts (author_id, author_nickname, title, content, board_type, view_count, comment_count, created_at, updated_at) 
VALUES (2, '사용자2', '질문있어요 두 번째 글', '많은 답변이 달린 질문입니다. 감사합니다!', 'QUESTION', 320, 15, 
        TIMESTAMPADD(DAY, -4, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -4, CURRENT_TIMESTAMP));

-- Post 15 (QUESTION) - 6일 전, 댓글 0개, 조회수 55
INSERT INTO posts (author_id, author_nickname, title, content, board_type, view_count, comment_count, created_at, updated_at) 
VALUES (3, '사용자3', '질문있어요 세 번째 글', '아직 답변이 없는 질문입니다. 도움을 기다리고 있어요.', 'QUESTION', 55, 0, 
        TIMESTAMPADD(DAY, -6, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -6, CURRENT_TIMESTAMP));

-- Post 16 (QUESTION) - 8일 전, 댓글 22개, 조회수 360
INSERT INTO posts (author_id, author_nickname, title, content, board_type, view_count, comment_count, created_at, updated_at) 
VALUES (1, '사용자1', '질문있어요 네 번째 글', '많은 답변이 달려서 도움이 많이 되었어요!', 'QUESTION', 360, 22, 
        TIMESTAMPADD(DAY, -8, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -8, CURRENT_TIMESTAMP));

-- Post 17 (QUESTION) - 11일 전, 댓글 4개, 조회수 95
INSERT INTO posts (author_id, author_nickname, title, content, board_type, view_count, comment_count, created_at, updated_at) 
VALUES (2, '사용자2', '질문있어요 다섯 번째 글', '11일 전에 올린 질문입니다.', 'QUESTION', 95, 4, 
        TIMESTAMPADD(DAY, -11, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -11, CURRENT_TIMESTAMP));

-- Post 18 (QUESTION) - 13일 전, 댓글 30개, 조회수 410
INSERT INTO posts (author_id, author_nickname, title, content, board_type, view_count, comment_count, created_at, updated_at) 
VALUES (3, '사용자3', '질문있어요 여섯 번째 글', '많은 답변이 달린 인기 질문입니다!', 'QUESTION', 410, 30, 
        TIMESTAMPADD(DAY, -13, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -13, CURRENT_TIMESTAMP));

-- Post 19 (QUESTION) - 16일 전, 댓글 2개, 조회수 70
INSERT INTO posts (author_id, author_nickname, title, content, board_type, view_count, comment_count, created_at, updated_at) 
VALUES (1, '사용자1', '질문있어요 일곱 번째 글', '16일 전 질문입니다. 조용한 질문이에요.', 'QUESTION', 70, 2, 
        TIMESTAMPADD(DAY, -16, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -16, CURRENT_TIMESTAMP));

-- Post 20 (QUESTION) - 19일 전, 댓글 20개, 조회수 310
INSERT INTO posts (author_id, author_nickname, title, content, board_type, view_count, comment_count, created_at, updated_at) 
VALUES (2, '사용자2', '질문있어요 여덟 번째 글', '19일 전 질문이지만 여전히 답변이 달리고 있어요.', 'QUESTION', 310, 20, 
        TIMESTAMPADD(DAY, -19, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -19, CURRENT_TIMESTAMP));

-- Post 21 (QUESTION) - 21일 전, 댓글 0개, 조회수 40
INSERT INTO posts (author_id, author_nickname, title, content, board_type, view_count, comment_count, created_at, updated_at) 
VALUES (3, '사용자3', '질문있어요 아홉 번째 글', '21일 전 질문입니다. 아직 답변이 없어요.', 'QUESTION', 40, 0, 
        TIMESTAMPADD(DAY, -21, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -21, CURRENT_TIMESTAMP));

-- Post 22 (QUESTION) - 23일 전, 댓글 38개, 조회수 450
INSERT INTO posts (author_id, author_nickname, title, content, board_type, view_count, comment_count, created_at, updated_at) 
VALUES (1, '사용자1', '질문있어요 열 번째 글', '많은 답변이 달린 매우 인기 있는 질문입니다!', 'QUESTION', 450, 38, 
        TIMESTAMPADD(DAY, -23, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -23, CURRENT_TIMESTAMP));

-- Post 23 (QUESTION) - 26일 전, 댓글 9개, 조회수 130
INSERT INTO posts (author_id, author_nickname, title, content, board_type, view_count, comment_count, created_at, updated_at) 
VALUES (2, '사용자2', '질문있어요 열한 번째 글', '26일 전에 올린 질문입니다.', 'QUESTION', 130, 9, 
        TIMESTAMPADD(DAY, -26, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -26, CURRENT_TIMESTAMP));

-- Post 24 (QUESTION) - 29일 전, 댓글 45개, 조회수 490
INSERT INTO posts (author_id, author_nickname, title, content, board_type, view_count, comment_count, created_at, updated_at) 
VALUES (3, '사용자3', '질문있어요 열두 번째 글', '최대 댓글 수에 가까운 답변이 달린 최고 인기 질문입니다!', 'QUESTION', 490, 45, 
        TIMESTAMPADD(DAY, -29, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -29, CURRENT_TIMESTAMP));

-- ============================================
-- INFO 게시판 (12개)
-- ============================================

-- Post 25 (INFO) - 오늘, 댓글 10개, 조회수 200
INSERT INTO posts (author_id, author_nickname, title, content, board_type, view_count, comment_count, created_at, updated_at) 
VALUES (1, '사용자1', '정보공유 첫 번째 글', '유용한 정보를 공유합니다. 많은 분들께 도움이 되길 바라요!', 'INFO', 200, 10, 
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Post 26 (INFO) - 3일 전, 댓글 14개, 조회수 280
INSERT INTO posts (author_id, author_nickname, title, content, board_type, view_count, comment_count, created_at, updated_at) 
VALUES (2, '사용자2', '정보공유 두 번째 글', '좋은 정보를 공유해주셔서 감사합니다!', 'INFO', 280, 14, 
        TIMESTAMPADD(DAY, -3, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -3, CURRENT_TIMESTAMP));

-- Post 27 (INFO) - 6일 전, 댓글 0개, 조회수 50
INSERT INTO posts (author_id, author_nickname, title, content, board_type, view_count, comment_count, created_at, updated_at) 
VALUES (3, '사용자3', '정보공유 세 번째 글', '유용한 정보지만 아직 댓글이 없네요.', 'INFO', 50, 0, 
        TIMESTAMPADD(DAY, -6, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -6, CURRENT_TIMESTAMP));

-- Post 28 (INFO) - 9일 전, 댓글 28개, 조회수 390
INSERT INTO posts (author_id, author_nickname, title, content, board_type, view_count, comment_count, created_at, updated_at) 
VALUES (1, '사용자1', '정보공유 네 번째 글', '많은 댓글이 달린 인기 정보 공유 글입니다!', 'INFO', 390, 28, 
        TIMESTAMPADD(DAY, -9, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -9, CURRENT_TIMESTAMP));

-- Post 29 (INFO) - 12일 전, 댓글 5개, 조회수 140
INSERT INTO posts (author_id, author_nickname, title, content, board_type, view_count, comment_count, created_at, updated_at) 
VALUES (2, '사용자2', '정보공유 다섯 번째 글', '12일 전에 공유한 정보입니다.', 'INFO', 140, 5, 
        TIMESTAMPADD(DAY, -12, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -12, CURRENT_TIMESTAMP));

-- Post 30 (INFO) - 14일 전, 댓글 32개, 조회수 430
INSERT INTO posts (author_id, author_nickname, title, content, board_type, view_count, comment_count, created_at, updated_at) 
VALUES (3, '사용자3', '정보공유 여섯 번째 글', '많은 댓글이 달린 매우 유용한 정보 공유 글입니다!', 'INFO', 430, 32, 
        TIMESTAMPADD(DAY, -14, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -14, CURRENT_TIMESTAMP));

-- Post 31 (INFO) - 17일 전, 댓글 3개, 조회수 75
INSERT INTO posts (author_id, author_nickname, title, content, board_type, view_count, comment_count, created_at, updated_at) 
VALUES (1, '사용자1', '정보공유 일곱 번째 글', '17일 전에 공유한 정보입니다.', 'INFO', 75, 3, 
        TIMESTAMPADD(DAY, -17, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -17, CURRENT_TIMESTAMP));

-- Post 32 (INFO) - 20일 전, 댓글 16개, 조회수 270
INSERT INTO posts (author_id, author_nickname, title, content, board_type, view_count, comment_count, created_at, updated_at) 
VALUES (2, '사용자2', '정보공유 여덟 번째 글', '20일 전 정보지만 여전히 유용해요.', 'INFO', 270, 16, 
        TIMESTAMPADD(DAY, -20, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -20, CURRENT_TIMESTAMP));

-- Post 33 (INFO) - 24일 전, 댓글 1개, 조회수 65
INSERT INTO posts (author_id, author_nickname, title, content, board_type, view_count, comment_count, created_at, updated_at) 
VALUES (3, '사용자3', '정보공유 아홉 번째 글', '24일 전에 공유한 정보입니다.', 'INFO', 65, 1, 
        TIMESTAMPADD(DAY, -24, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -24, CURRENT_TIMESTAMP));

-- Post 34 (INFO) - 27일 전, 댓글 40개, 조회수 470
INSERT INTO posts (author_id, author_nickname, title, content, board_type, view_count, comment_count, created_at, updated_at) 
VALUES (1, '사용자1', '정보공유 열 번째 글', '많은 댓글이 달린 최고 인기 정보 공유 글입니다!', 'INFO', 470, 40, 
        TIMESTAMPADD(DAY, -27, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -27, CURRENT_TIMESTAMP));

-- Post 35 (INFO) - 30일 전, 댓글 11개, 조회수 160
INSERT INTO posts (author_id, author_nickname, title, content, board_type, view_count, comment_count, created_at, updated_at) 
VALUES (2, '사용자2', '정보공유 열한 번째 글', '30일 전에 공유한 정보입니다. 오래되었지만 여전히 유용해요.', 'INFO', 160, 11, 
        TIMESTAMPADD(DAY, -30, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -30, CURRENT_TIMESTAMP));

-- Post 36 (INFO) - 30일 전, 댓글 48개, 조회수 495
INSERT INTO posts (author_id, author_nickname, title, content, board_type, view_count, comment_count, created_at, updated_at) 
VALUES (3, '사용자3', '정보공유 열두 번째 글', '최대 댓글 수에 가까운 답변이 달린 최고 인기 정보 공유 글입니다!', 'INFO', 495, 48, 
        TIMESTAMPADD(DAY, -30, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -30, CURRENT_TIMESTAMP));

-- ============================================
-- Comment 데이터 삽입
-- 각 Post의 comment_count에 맞는 댓글 생성
-- ============================================

-- Post 1에 대한 댓글 10개 (다양한 날짜와 depth)
-- 댓글 1 (depth 1) - 2일 전 오전 10시
INSERT INTO comments (post_id, author_id, author_nickname, parent_id, content, depth, created_at, updated_at) 
VALUES (1, 1, '사용자1', NULL, '좋은 글 감사합니다! 많은 도움이 되었어요.', 1, 
        TIMESTAMPADD(HOUR, -50, CURRENT_TIMESTAMP), TIMESTAMPADD(HOUR, -50, CURRENT_TIMESTAMP));

-- 댓글 2 (depth 1) - 2일 전 오후 3시
INSERT INTO comments (post_id, author_id, author_nickname, parent_id, content, depth, created_at, updated_at) 
VALUES (1, 2, '사용자2', NULL, '저도 비슷한 경험이 있어서 공감이 가네요.', 1, 
        TIMESTAMPADD(HOUR, -45, CURRENT_TIMESTAMP), TIMESTAMPADD(HOUR, -45, CURRENT_TIMESTAMP));

-- 댓글 3 (depth 1) - 1일 전 오전 9시
INSERT INTO comments (post_id, author_id, author_nickname, parent_id, content, depth, created_at, updated_at) 
VALUES (1, 3, '사용자3', NULL, '추가로 궁금한 점이 있는데, 더 자세히 설명해주실 수 있나요?', 1, 
        TIMESTAMPADD(HOUR, -27, CURRENT_TIMESTAMP), TIMESTAMPADD(HOUR, -27, CURRENT_TIMESTAMP));

-- 댓글 4 (depth 2, 댓글 1의 대댓글) - 1일 전 오후 2시
-- 댓글 1은 첫 번째 댓글이므로 author_id=1, content에 '좋은 글' 포함
INSERT INTO comments (post_id, author_id, author_nickname, parent_id, content, depth, created_at, updated_at) 
VALUES (1, 2, '사용자2', 
        (SELECT id FROM comments WHERE post_id = 1 AND author_id = 1 AND content LIKE '%좋은 글%' LIMIT 1), 
        '저도 같은 생각이에요! 정말 유용한 정보였습니다.', 2, 
        TIMESTAMPADD(HOUR, -22, CURRENT_TIMESTAMP), TIMESTAMPADD(HOUR, -22, CURRENT_TIMESTAMP));

-- 댓글 5 (depth 1) - 1일 전 저녁 8시
INSERT INTO comments (post_id, author_id, author_nickname, parent_id, content, depth, created_at, updated_at) 
VALUES (1, 1, '사용자1', NULL, '다른 분들도 많은 관심을 가져주셔서 감사합니다.', 1, 
        TIMESTAMPADD(HOUR, -16, CURRENT_TIMESTAMP), TIMESTAMPADD(HOUR, -16, CURRENT_TIMESTAMP));

-- 댓글 6 (depth 2, 댓글 3의 대댓글) - 12시간 전
-- 댓글 3은 author_id=3, content에 '추가로 궁금한 점' 포함
INSERT INTO comments (post_id, author_id, author_nickname, parent_id, content, depth, created_at, updated_at) 
VALUES (1, 1, '사용자1', 
        (SELECT id FROM comments WHERE post_id = 1 AND author_id = 3 AND content LIKE '%추가로 궁금한 점%' LIMIT 1), 
        '네, 물론이죠! 더 자세한 내용은 다음에 추가로 올려드릴게요.', 2, 
        TIMESTAMPADD(HOUR, -12, CURRENT_TIMESTAMP), TIMESTAMPADD(HOUR, -12, CURRENT_TIMESTAMP));

-- 댓글 7 (depth 3, 댓글 4의 대대댓글) - 6시간 전
-- 댓글 4는 depth=2, content에 '저도 같은 생각' 포함
INSERT INTO comments (post_id, author_id, author_nickname, parent_id, content, depth, created_at, updated_at) 
VALUES (1, 3, '사용자3', 
        (SELECT id FROM comments WHERE post_id = 1 AND depth = 2 AND content LIKE '%저도 같은 생각%' LIMIT 1), 
        '저도 동의합니다! 좋은 정보 공유 감사해요.', 3, 
        TIMESTAMPADD(HOUR, -6, CURRENT_TIMESTAMP), TIMESTAMPADD(HOUR, -6, CURRENT_TIMESTAMP));

-- 댓글 8 (depth 2, 댓글 5의 대댓글) - 3시간 전
-- 댓글 5는 author_id=1, content에 '다른 분들도' 포함
INSERT INTO comments (post_id, author_id, author_nickname, parent_id, content, depth, created_at, updated_at) 
VALUES (1, 2, '사용자2', 
        (SELECT id FROM comments WHERE post_id = 1 AND author_id = 1 AND content LIKE '%다른 분들도%' LIMIT 1), 
        '저도 도움이 많이 되었습니다. 앞으로도 좋은 글 부탁드려요!', 2, 
        TIMESTAMPADD(HOUR, -3, CURRENT_TIMESTAMP), TIMESTAMPADD(HOUR, -3, CURRENT_TIMESTAMP));

-- 댓글 9 (depth 1) - 1시간 전
INSERT INTO comments (post_id, author_id, author_nickname, parent_id, content, depth, created_at, updated_at) 
VALUES (1, 3, '사용자3', NULL, '최근에 이런 내용이 궁금했는데 정말 시기적절한 글이네요!', 1, 
        TIMESTAMPADD(HOUR, -1, CURRENT_TIMESTAMP), TIMESTAMPADD(HOUR, -1, CURRENT_TIMESTAMP));

-- 댓글 10 (depth 2, 댓글 9의 대댓글) - 30분 전
-- 댓글 9는 author_id=3, content에 '최근에 이런 내용' 포함
INSERT INTO comments (post_id, author_id, author_nickname, parent_id, content, depth, created_at, updated_at) 
VALUES (1, 1, '사용자1', 
        (SELECT id FROM comments WHERE post_id = 1 AND author_id = 3 AND content LIKE '%최근에 이런 내용%' LIMIT 1), 
        '도움이 되었다니 다행이에요! 앞으로도 유용한 정보 공유하겠습니다.', 2, 
        TIMESTAMPADD(MINUTE, -30, CURRENT_TIMESTAMP), TIMESTAMPADD(MINUTE, -30, CURRENT_TIMESTAMP));

