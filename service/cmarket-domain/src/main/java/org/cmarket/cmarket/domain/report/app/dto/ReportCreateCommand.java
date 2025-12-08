package org.cmarket.cmarket.domain.report.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.report.model.ReportTargetType;

import java.util.List;

/**
 * 신고 생성 명령 DTO
 * 
 * 앱 서비스에서 사용하는 DTO입니다.
 */
@Getter
@Builder
public class ReportCreateCommand {
    private ReportTargetType targetType;
    private Long targetId;
    private List<String> reasonCodes;  // 신고 사유 코드 리스트 (최소 1개)
    private String detailReason;  // 상세 사유 (최대 300자)
    private List<String> imageUrls;  // 이미지 URL 리스트 (최대 3개)
}

