#!/bin/bash
# EC2에서 nginx 설치 및 설정 스크립트

set -e

echo "=== Cmarket API Nginx 설정 스크립트 ==="

# 1. Nginx 설치
echo "1. Nginx 설치 중..."
sudo yum update -y
sudo yum install -y nginx

# 2. Nginx 설정 파일 복사
echo "2. Nginx 설정 파일 복사 중..."
sudo cp nginx.conf /etc/nginx/sites-available/cmarket-api
sudo mkdir -p /etc/nginx/sites-enabled

# 3. 심볼릭 링크 생성
echo "3. 심볼릭 링크 생성 중..."
sudo ln -sf /etc/nginx/sites-available/cmarket-api /etc/nginx/sites-enabled/cmarket-api

# 4. 기본 nginx 설정 비활성화 (선택사항)
if [ -f /etc/nginx/conf.d/default.conf ]; then
    echo "4. 기본 설정 파일 백업 중..."
    sudo mv /etc/nginx/conf.d/default.conf /etc/nginx/conf.d/default.conf.bak
fi

# 5. nginx.conf에 sites-enabled 포함 확인
if ! grep -q "include /etc/nginx/sites-enabled/\*;" /etc/nginx/nginx.conf; then
    echo "5. nginx.conf에 sites-enabled 포함 추가 중..."
    sudo sed -i '/http {/a\    include /etc/nginx/sites-enabled/*;' /etc/nginx/nginx.conf
fi

# 6. Let's Encrypt 인증을 위한 디렉토리 생성
echo "6. Let's Encrypt 인증 디렉토리 생성 중..."
sudo mkdir -p /var/www/certbot

# 7. Nginx 설정 테스트
echo "7. Nginx 설정 테스트 중..."
sudo nginx -t

# 8. Nginx 시작 및 자동 시작 설정
echo "8. Nginx 시작 중..."
sudo systemctl enable nginx
sudo systemctl start nginx

echo "=== Nginx 설정 완료 ==="
echo ""
echo "다음 단계:"
echo "1. nginx.conf 파일에서 도메인을 실제 도메인으로 변경"
echo "2. Let's Encrypt 인증서 발급:"
echo "   sudo certbot --nginx -d cmarket-api.duckdns.org"
echo "3. Nginx 재시작: sudo systemctl restart nginx"

