package org.cmarket.cmarket.domain.report.app.service;

import org.cmarket.cmarket.domain.report.app.dto.ReportCreateCommand;
import org.cmarket.cmarket.domain.report.app.dto.ReportDto;
import org.cmarket.cmarket.domain.report.app.dto.UserBlockCreateCommand;
import org.cmarket.cmarket.domain.report.app.dto.UserBlockDto;

/**
 * 신고/차단 서비스 인터페이스.
 */
public interface ReportService {

    /**
     * 사용자 차단.
     *
     * @param email 현재 로그인한 사용자 이메일
     * @param command 차단 대상 정보
     * @return 차단 결과 DTO
     */
    UserBlockDto blockUser(String email, UserBlockCreateCommand command);

    /**
     * 신고 생성.
     *
     * @param email 현재 로그인한 사용자 이메일
     * @param command 신고 생성 명령
     * @return 신고 결과 DTO
     */
    ReportDto createReport(String email, ReportCreateCommand command);

    /**
     * 신고 검토/상태 변경.
     *
     * @param email 관리자 이메일
     * @param command 신고 검토 명령
     * @return 변경된 신고 DTO
     */
    ReportDto reviewReport(String email, org.cmarket.cmarket.domain.report.app.dto.ReportReviewCommand command);
}

