# Java 21 JDK 이미지 사용 (Ubuntu 22.04 Jammy 기반)
# 로컬 개발: native 아키텍처 사용 (Mac M1/M2는 arm64)
# 배포 시: docker buildx --platform linux/amd64로 빌드
# 구체적인 버전 태그 사용: 21.0.9_10-jdk-jammy (최신 Java 21)
FROM eclipse-temurin:21.0.9_10-jdk-jammy

WORKDIR /app

# Java 버전 확인 (디버깅용) - 반드시 Java 21이어야 함
RUN java -version && \
    java -version 2>&1 | grep -q "21" || (echo "ERROR: Java version is not 21!" && exit 1)

# 보안을 위한 non-root 사용자 생성
RUN addgroup --system spring && adduser --system spring --ingroup spring

# Healthcheck를 위한 wget 설치
RUN apt-get update && apt-get install -y wget && rm -rf /var/lib/apt/lists/*

# 빌드된 jar 파일을 컨테이너 내부로 복사
COPY service/cmarket/build/libs/*.jar app.jar

# H2 데이터 및 이미지 업로드용 폴더 생성 및 권한 설정
RUN mkdir -p /data/uploads/images && \
    chown -R spring:spring /data && \
    chown -R spring:spring /app

# non-root 사용자로 전환
USER spring:spring

# 포트 노출 (Spring Boot 기본 포트)
EXPOSE 8080

# Java 경로 명시적으로 지정 및 버전 확인
# JAVA_HOME은 eclipse-temurin 이미지에서 자동 설정됨
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# 실행 시 Java 버전 확인 (디버깅용)
ENTRYPOINT ["sh", "-c", "java -version && java -jar app.jar"]
