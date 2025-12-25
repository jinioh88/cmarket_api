#!/bin/bash

# WebSocket 연결 테스트 스크립트
# 사용법: 
#   ./test-websocket.sh [JWT_TOKEN] [HOST]
#   예: ./test-websocket.sh "eyJhbGciOiJIUzM4NCJ9..." "localhost:8080"

JWT_TOKEN=${1:-""}
HOST=${2:-"localhost:8080"}

echo "=== WebSocket 연결 테스트 ==="
echo "Host: $HOST"
echo "Endpoint: /ws-stomp"
echo ""

# 1. SockJS info 엔드포인트 테스트
echo "1. SockJS Info 엔드포인트 테스트..."
echo ""

if [ -z "$JWT_TOKEN" ]; then
    echo "⚠️  JWT 토큰이 없습니다. 인증 없이 테스트합니다."
    echo ""
    echo "요청: curl http://$HOST/ws-stomp/info"
    echo ""
    RESPONSE=$(curl -s "http://$HOST/ws-stomp/info")
    echo "응답: $RESPONSE"
    echo ""
    
    # JSON 포맷팅 (python3가 있는 경우)
    if command -v python3 &> /dev/null; then
        echo "포맷팅된 응답:"
        echo "$RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"
    fi
else
    echo "✅ JWT 토큰 사용: ${JWT_TOKEN:0:30}..."
    echo ""
    echo "요청: curl -H 'Authorization: Bearer $JWT_TOKEN' http://$HOST/ws-stomp/info"
    echo ""
    RESPONSE=$(curl -s -H "Authorization: Bearer $JWT_TOKEN" "http://$HOST/ws-stomp/info")
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer $JWT_TOKEN" "http://$HOST/ws-stomp/info")
    
    echo "HTTP 상태 코드: $HTTP_CODE"
    echo "응답: $RESPONSE"
    echo ""
    
    # JSON 포맷팅 (python3가 있는 경우)
    if command -v python3 &> /dev/null; then
        echo "포맷팅된 응답:"
        echo "$RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"
    fi
    
    # HTTP 상태 코드에 따른 메시지
    if [ "$HTTP_CODE" = "200" ]; then
        echo ""
        echo "✅ 연결 성공! WebSocket 서버가 정상 작동 중입니다."
    elif [ "$HTTP_CODE" = "401" ] || [ "$HTTP_CODE" = "403" ]; then
        echo ""
        echo "❌ 인증 실패 (HTTP $HTTP_CODE)"
        echo "   - 토큰이 만료되었을 수 있습니다"
        echo "   - 토큰 형식이 올바른지 확인하세요"
        echo "   - 애플리케이션 로그를 확인하세요"
    else
        echo ""
        echo "⚠️  예상치 못한 응답 (HTTP $HTTP_CODE)"
        echo "   - 애플리케이션 로그를 확인하세요"
    fi
fi

echo ""
echo "=== 테스트 완료 ==="
echo ""
echo "다음 단계:"
echo "1. 애플리케이션 로그 확인: tail -f app.log"
echo "2. 로그에서 'WebSocket 연결 성공' 또는 'WebSocket 연결 실패' 메시지 확인"
echo "3. 'AccessDeniedException' 오류가 없는지 확인"

