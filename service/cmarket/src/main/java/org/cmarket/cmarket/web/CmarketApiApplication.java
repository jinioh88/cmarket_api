package org.cmarket.cmarket.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Cmarket API 애플리케이션 메인 클래스
 * 
 * @EntityScan: 도메인 모듈의 엔티티를 스캔하도록 설정
 * @EnableJpaRepositories: 도메인 모듈의 레포지토리를 스캔하도록 설정
 * @EnableScheduling: 스케줄링 기능 활성화 (SSE Heartbeat 등)
 */
@SpringBootApplication(scanBasePackages = "org.cmarket")
@EntityScan(basePackages = "org.cmarket.cmarket.domain")
@EnableJpaRepositories(basePackages = "org.cmarket.cmarket.domain")
@EnableScheduling
public class CmarketApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CmarketApiApplication.class, args);
	}

}

