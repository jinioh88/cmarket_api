#!/bin/bash
# DB_URL에 문자셋 파라미터를 자동으로 추가하는 스크립트

echo "=== DB_URL 문자셋 파라미터 추가 ==="
echo ""

# 현재 DB_URL 확인
if [ -z "$DB_URL" ]; then
    echo "❌ DB_URL 환경 변수가 설정되어 있지 않습니다."
    exit 1
fi

echo "현재 DB_URL: $DB_URL"
echo ""

# 문자셋 파라미터가 이미 포함되어 있는지 확인
if [[ "$DB_URL" == *"characterEncoding"* ]] || [[ "$DB_URL" == *"utf8mb4"* ]]; then
    echo "✅ DB_URL에 이미 문자셋 파라미터가 포함되어 있습니다."
    echo "현재 DB_URL: $DB_URL"
else
    echo "⚠️ DB_URL에 문자셋 파라미터가 없습니다. 추가합니다..."
    
    # ? 또는 & 확인
    if [[ "$DB_URL" == *"?"* ]]; then
        # 이미 파라미터가 있는 경우 & 추가
        NEW_DB_URL="${DB_URL}&characterEncoding=utf8mb4&useUnicode=true&connectionCollation=utf8mb4_general_ci"
    else
        # 파라미터가 없는 경우 ? 추가
        NEW_DB_URL="${DB_URL}?characterEncoding=utf8mb4&useUnicode=true&connectionCollation=utf8mb4_general_ci"
    fi
    
    echo ""
    echo "수정된 DB_URL:"
    echo "export DB_URL=\"${NEW_DB_URL}\""
    echo ""
    echo "이 명령어를 실행하여 DB_URL을 업데이트하세요."
fi

echo ""
echo "=== 완료 ==="


