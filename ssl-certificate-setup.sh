#!/bin/bash
# cmarket-api.duckdns.org SSL 인증서 발급 및 설정 스크립트

set -e

echo "=== SSL 인증서 확인 및 발급 ==="

# 1. 기존 인증서 확인
echo "1. 기존 인증서 확인 중..."
if [ -d "/etc/letsencrypt/live/cmarket-api.duckdns.org" ]; then
    echo "✓ cmarket-api.duckdns.org 인증서가 이미 존재합니다."
    ls -la /etc/letsencrypt/live/cmarket-api.duckdns.org/
    exit 0
fi

echo "✗ cmarket-api.duckdns.org 인증서가 없습니다. 발급을 진행합니다."

# 2. nginx 상태 확인 및 중지
echo "2. nginx 상태 확인 중..."
if systemctl is-active --quiet nginx; then
    echo "   nginx가 실행 중입니다. 인증서 발급을 위해 일시 중지합니다..."
    sudo systemctl stop nginx
    NGINX_WAS_RUNNING=true
else
    echo "   nginx가 실행 중이 아닙니다."
    NGINX_WAS_RUNNING=false
fi

# 3. 80 포트 사용 확인
echo "3. 80 포트 사용 확인 중..."
if sudo lsof -i :80 > /dev/null 2>&1; then
    echo "   ⚠️  경고: 80 포트가 사용 중입니다."
    sudo lsof -i :80
    echo "   위 프로세스를 종료한 후 다시 시도하세요."
    if [ "$NGINX_WAS_RUNNING" = true ]; then
        sudo systemctl start nginx
    fi
    exit 1
fi

# 4. Let's Encrypt 인증서 발급
echo "4. Let's Encrypt 인증서 발급 중..."
echo "   (DNS 조회가 필요하므로 시간이 걸릴 수 있습니다)"

# 최대 3번 재시도
MAX_RETRIES=3
RETRY_COUNT=0

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if sudo certbot certonly --standalone \
        -d cmarket-api.duckdns.org \
        --email jinioh88@gmail.com \
        --agree-tos \
        --non-interactive \
        --preferred-challenges http; then
        echo "✓ 인증서 발급 완료"
        break
    else
        RETRY_COUNT=$((RETRY_COUNT + 1))
        if [ $RETRY_COUNT -lt $MAX_RETRIES ]; then
            echo "   ⚠️  인증서 발급 실패. ${RETRY_COUNT}/${MAX_RETRIES} 재시도 중..."
            sleep 10
        else
            echo "   ✗ 인증서 발급 실패 (최대 재시도 횟수 초과)"
            if [ "$NGINX_WAS_RUNNING" = true ]; then
                sudo systemctl start nginx
            fi
            exit 1
        fi
    fi
done

# 5. nginx 재시작 (중지했던 경우)
if [ "$NGINX_WAS_RUNNING" = true ]; then
    echo "5. nginx 재시작 중..."
    sudo systemctl start nginx
    sudo systemctl status nginx --no-pager
fi

# 6. 인증서 자동 갱신 설정 확인
echo "6. 인증서 자동 갱신 설정 확인 중..."
if ! sudo systemctl is-enabled certbot.timer > /dev/null 2>&1; then
    echo "   certbot.timer 활성화 중..."
    sudo systemctl enable certbot.timer
    sudo systemctl start certbot.timer
fi

echo "=== SSL 인증서 설정 완료 ==="

