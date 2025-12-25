# AWS EC2 배포 가이드

> Cmarket API를 AWS EC2에 Docker Compose로 배포하기 위한 단계별 가이드

---

## 전제 조건

- ✅ Duck DNS 도메인 확보 완료
- ✅ Nginx Proxy Manager를 통해 Duck DNS와 연결 완료
- ✅ EC2 인스턴스 생성 준비 완료

---

## 배포 순서

### Step 1: EC2 인스턴스 생성 및 기본 설정

#### 1-1. EC2 인스턴스 생성
- **인스턴스 타입**: t2.micro 이상 권장 (최소 2GB RAM)
- **운영체제**: Ubuntu 22.04 LTS 권장
- **스토리지**: 최소 20GB (Docker 이미지 및 데이터 저장용)

#### 1-2. 보안 그룹 설정
다음 포트를 열어야 합니다:
- **22 (SSH)**: 인스턴스 접속용
- **80 (HTTP)**: Nginx Proxy Manager가 이미 별도로 설정되어 있다면 불필요할 수 있음
- **443 (HTTPS)**: Nginx Proxy Manager가 이미 별도로 설정되어 있다면 불필요할 수 있음
- **8080 (Spring Boot)**: 내부 네트워크에서만 접근 가능하도록 설정 (선택사항)

> ⚠️ **중요**: Nginx Proxy Manager가 **별도 서버**에서 운영 중이라면, EC2에서는 8080 포트만 열어도 됩니다. Nginx Proxy Manager가 같은 EC2에서 실행된다면 80, 443 포트도 열어야 합니다.

#### 1-3. Elastic IP 할당 (권장)
- EC2 인스턴스에 Elastic IP를 할당하여 IP 주소가 변경되지 않도록 합니다.
- Duck DNS에서 A 레코드를 이 IP로 설정합니다.

---

### Step 2: EC2 인스턴스 접속 및 기본 패키지 설치

#### 2-1. SSH 접속
```bash
ssh -i your-key.pem ubuntu@your-ec2-ip
```

#### 2-2. 시스템 업데이트
```bash
sudo apt update && sudo apt upgrade -y
```

#### 2-3. Docker 설치
```bash
# Docker 설치
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# 현재 사용자를 docker 그룹에 추가 (sudo 없이 docker 명령 사용)
sudo usermod -aG docker $USER

# 재로그인 또는 다음 명령 실행
newgrp docker

# Docker 설치 확인
docker --version
```

#### 2-4. Docker Compose 설치
```bash
# Docker Compose 설치
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose

# 실행 권한 부여
sudo chmod +x /usr/local/bin/docker-compose

# Docker Compose 설치 확인
docker-compose --version
```

#### 2-5. Git 설치 (프로젝트 클론용)
```bash
sudo apt install git -y
```

---

### Step 3: 프로젝트 다운로드 및 빌드

#### 3-1. 프로젝트 클론
```bash
# 홈 디렉토리로 이동
cd ~

# 프로젝트 클론 (GitHub 저장소 URL로 변경)
git clone https://github.com/your-username/cmarket_api.git

# 또는 직접 파일 업로드 (scp 사용)
# 로컬에서 실행: scp -r -i your-key.pem /path/to/cmarket_api ubuntu@your-ec2-ip:~/
```

#### 3-2. 프로젝트 디렉토리로 이동
```bash
cd ~/cmarket_api
```

#### 3-3. Gradle Wrapper 실행 권한 부여
```bash
chmod +x gradlew
```

#### 3-4. JAR 파일 빌드
```bash
# Spring Boot JAR 파일 빌드
./gradlew :service:cmarket:clean :service:cmarket:bootJar

# 빌드 확인
ls -lh service/cmarket/build/libs/
```

> ⚠️ **주의**: EC2 인스턴스에 Java가 설치되어 있지 않다면, Docker를 사용하여 빌드할 수도 있습니다. 하지만 일반적으로는 EC2에 Java를 설치하거나, 로컬에서 빌드한 JAR 파일을 업로드하는 것이 더 빠릅니다.

#### 3-5. (선택사항) Java 설치 (EC2에서 직접 빌드하는 경우)
```bash
# Java 21 설치
sudo apt install openjdk-21-jdk -y

# Java 버전 확인
java -version
```

---

### Step 4: Docker Compose 설정 확인 및 수정

#### 4-1. docker-compose.yml 확인
현재 `docker-compose.yml`에는 Nginx Proxy Manager가 포함되어 있습니다.

**시나리오 A: Nginx Proxy Manager가 별도 서버에서 운영 중인 경우**
- `proxy` 서비스를 제거하거나 주석 처리합니다.
- `app` 서비스의 포트를 직접 노출할 필요가 없습니다 (내부 네트워크만 사용).

**시나리오 B: Nginx Proxy Manager를 같은 EC2에서 실행하는 경우**
- 현재 `docker-compose.yml` 그대로 사용합니다.
- 보안 그룹에서 80, 443, 81 포트를 열어야 합니다.

#### 4-2. 환경 변수 확인
`docker-compose.yml`의 환경 변수가 올바른지 확인:
```yaml
environment:
  - SPRING_PROFILES_ACTIVE=prod
  - OAUTH2_REDIRECT_URI=https://cuddle-market.duckdns.org/oauth-redirect
  - APP_IMAGE_SERVER_URL=https://cmarket-api.duckdns.org
```

> ⚠️ **중요**: 
> - `OAUTH2_REDIRECT_URI`는 **프론트엔드 도메인**으로 설정해야 합니다 (예: `https://cuddle-market.duckdns.org/oauth-redirect`)
> - `APP_IMAGE_SERVER_URL`은 **백엔드 도메인**으로 설정해야 합니다 (예: `https://cmarket-api.duckdns.org`)
> - Duck DNS 도메인이 다르다면, 이 값들을 실제 도메인으로 수정해야 합니다.

#### 4-3. 볼륨 폴더 생성 및 권한 설정
```bash
# 볼륨 폴더 생성
mkdir -p h2data uploads/images proxy/data proxy/letsencrypt

# 권한 설정
chmod -R 777 h2data
chmod -R 755 uploads
chmod -R 755 proxy
```

---

### Step 5: Docker 이미지 빌드 및 컨테이너 실행

#### 5-1. Docker 이미지 빌드
```bash
# 프로젝트 루트에서 실행
docker-compose build

# 또는 특정 서비스만 빌드
docker-compose build app
```

#### 5-2. 컨테이너 실행
```bash
# 백그라운드에서 실행
docker-compose up -d

# 실행 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs -f app
```

#### 5-3. 컨테이너 상태 확인
```bash
# 모든 컨테이너 상태 확인
docker-compose ps

# 특정 컨테이너 로그 확인
docker-compose logs app
docker-compose logs redis
docker-compose logs proxy  # proxy 서비스를 사용하는 경우
```

#### 5-4. JAR 직접 실행 (Docker Compose 미사용 시)

Docker Compose를 사용하지 않고 JAR 파일을 직접 실행하는 경우:

**방법 1: 환경 변수로 export 후 실행 (권장)**
```bash
# 환경 변수 설정
export JWT_SECRET="your-actual-jwt-secret-key-here"
export OAUTH2_REDIRECT_URI="https://cuddle-market.duckdns.org/oauth-redirect"
export APP_IMAGE_SERVER_URL="https://cmarket-api.duckdns.org"
export FRONTEND_URL="https://cuddle-market.duckdns.org"
export REDIS_HOST="localhost"
export REDIS_PORT="6379"

# JAR 실행
nohup java -jar \
  -Dspring.profiles.active=prod \
  service/cmarket/build/libs/cmarket-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
```

**방법 2: 시스템 프로퍼티로 직접 전달**
```bash
nohup java -jar \
  -Dspring.profiles.active=prod \
  -DJWT_SECRET="your-actual-jwt-secret-key-here" \
  -DREDIS_HOST=localhost \
  -DREDIS_PORT=6379 \
  -DAPP_IMAGE_SERVER_URL=https://cmarket-api.duckdns.org \
  -DOAUTH2_REDIRECT_URI=https://cuddle-market.duckdns.org/oauth-redirect \
  -DFRONTEND_URL=https://cuddle-market.duckdns.org \
  service/cmarket/build/libs/cmarket-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
```

**방법 3: 환경 변수 파일 사용 (가장 안전)**
```bash
# 환경 변수 파일 생성 (보안을 위해 권한 제한)
cat > ~/.cmarket_env << EOF
export JWT_SECRET="your-actual-jwt-secret-key-here"
export OAUTH2_REDIRECT_URI="https://cuddle-market.duckdns.org/oauth-redirect"
export APP_IMAGE_SERVER_URL="https://cmarket-api.duckdns.org"
export FRONTEND_URL="https://cuddle-market.duckdns.org"
export REDIS_HOST="localhost"
export REDIS_PORT="6379"
EOF

# 권한 설정 (소유자만 읽기 가능)
chmod 600 ~/.cmarket_env

# 환경 변수 로드 후 실행
source ~/.cmarket_env && nohup java -jar \
  -Dspring.profiles.active=prod \
  service/cmarket/build/libs/cmarket-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
```

**실행 확인 및 관리**
```bash
# 프로세스 확인
ps aux | grep java

# 로그 확인
tail -f app.log

# 프로세스 종료
# PID 확인 후
kill <PID>
```

> ⚠️ **중요**: 
> - `JWT_SECRET`은 반드시 실제 값으로 설정해야 합니다 (최소 32바이트 이상의 랜덤 문자열)
> - `OAUTH2_REDIRECT_URI`는 **프론트엔드 도메인**으로 설정해야 합니다
> - `APP_IMAGE_SERVER_URL`은 **백엔드 도메인**으로 설정해야 합니다
> - 환경 변수 파일(`~/.cmarket_env`)은 Git에 커밋하지 않도록 주의하세요

---

### Step 6: Nginx Proxy Manager 설정 (별도 서버인 경우)

만약 Nginx Proxy Manager가 **별도 서버**에서 운영 중이라면:

1. Nginx Proxy Manager 웹 UI 접속 (`http://your-npm-server:81`)
2. **Proxy Hosts** 메뉴에서 새 프록시 호스트 추가:
   - **Domain Names**: `cmarket-api.duckdns.org` (또는 실제 도메인)
   - **Scheme**: `http`
   - **Forward Hostname / IP**: EC2 인스턴스의 **Private IP** 또는 **Elastic IP**
   - **Forward Port**: `8080`
   - **Websockets Support**: 체크 (채팅 기능 사용 시)
3. **SSL 탭**에서 Let's Encrypt 인증서 발급

> ⚠️ **중요**: 
> - Nginx Proxy Manager가 EC2와 같은 VPC 내부에 있다면 **Private IP** 사용
> - Nginx Proxy Manager가 외부에 있다면 **Elastic IP (Public IP)** 사용
> - 보안 그룹에서 8080 포트를 Nginx Proxy Manager 서버의 IP에서만 접근 가능하도록 제한하는 것을 권장합니다.

---

### Step 7: 배포 확인

#### 7-1. API 서버 Health Check
```bash
# EC2 인스턴스 내부에서 확인
curl http://localhost:8080/actuator/health

# 또는 외부에서 확인 (보안 그룹에서 8080 포트가 열려있는 경우)
curl http://your-ec2-ip:8080/actuator/health
```

#### 7-2. 도메인을 통한 접근 확인
```bash
# HTTPS로 접근 확인
curl https://cmarket-api.duckdns.org/actuator/health
```

#### 7-3. 브라우저에서 확인
- `https://cmarket-api.duckdns.org` 접속하여 API가 정상 동작하는지 확인

---

## 자주 묻는 질문 (FAQ)

### Q1: Nginx Proxy Manager를 같은 EC2에서 실행해야 하나요?
**A**: 아니요. Nginx Proxy Manager는 별도 서버에서 운영할 수 있습니다. 이 경우 `docker-compose.yml`에서 `proxy` 서비스를 제거하거나 주석 처리하면 됩니다.

### Q2: EC2에서 빌드하지 않고 로컬에서 빌드한 JAR를 업로드할 수 있나요?
**A**: 네, 가능합니다. 로컬에서 빌드한 JAR 파일을 `scp`로 업로드하면 됩니다:
```bash
# 로컬에서 실행
scp -i your-key.pem service/cmarket/build/libs/cmarket-0.0.1-SNAPSHOT.jar ubuntu@your-ec2-ip:~/cmarket_api/service/cmarket/build/libs/
```

### Q3: Docker 이미지를 미리 빌드해서 업로드할 수 있나요?
**A**: 네, 가능합니다. 로컬에서 이미지를 빌드하고 Docker Hub나 ECR에 푸시한 후, EC2에서 `docker pull`로 받을 수 있습니다.

### Q4: 보안 그룹 설정은 어떻게 해야 하나요?
**A**: 
- **시나리오 A (Nginx Proxy Manager 별도 서버)**: 22(SSH), 8080(Spring Boot, 선택사항) 포트만 열기
- **시나리오 B (Nginx Proxy Manager 같은 EC2)**: 22(SSH), 80(HTTP), 443(HTTPS), 81(NPM UI) 포트 열기

### Q5: 데이터 백업은 어떻게 하나요?
**A**: `h2data`와 `uploads` 폴더를 정기적으로 백업해야 합니다:
```bash
# 백업 스크립트 예시
tar -czf backup-$(date +%Y%m%d).tar.gz h2data uploads
# S3나 다른 스토리지에 업로드
```

---

## 트러블슈팅

### 컨테이너가 시작되지 않는 경우
1. 로그 확인: `docker-compose logs app`
2. JAR 파일 존재 확인: `ls -lh service/cmarket/build/libs/`
3. 볼륨 권한 확인: `ls -la h2data uploads`

### 포트 충돌
```bash
# 사용 중인 포트 확인
sudo netstat -tulpn | grep :8080
sudo netstat -tulpn | grep :80
```

### 메모리 부족
```bash
# 메모리 사용량 확인
free -h
# Docker 컨테이너 리소스 사용량 확인
docker stats
```

---

## 완료 체크리스트

- [ ] EC2 인스턴스 생성 및 보안 그룹 설정
- [ ] Docker 및 Docker Compose 설치
- [ ] 프로젝트 클론 또는 업로드
- [ ] JAR 파일 빌드
- [ ] docker-compose.yml 설정 확인 및 수정
- [ ] 볼륨 폴더 생성 및 권한 설정
- [ ] Docker 이미지 빌드
- [ ] 컨테이너 실행
- [ ] Nginx Proxy Manager 설정 (별도 서버인 경우)
- [ ] 배포 확인 및 테스트

---

## 참고 자료

- [AWS EC2 공식 문서](https://docs.aws.amazon.com/ec2/)
- [Docker 공식 문서](https://docs.docker.com/)
- [Docker Compose 공식 문서](https://docs.docker.com/compose/)
- [Nginx Proxy Manager 공식 문서](https://nginxproxymanager.com/)




