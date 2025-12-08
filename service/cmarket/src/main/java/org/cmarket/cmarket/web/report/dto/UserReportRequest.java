package org.cmarket.cmarket.web.report.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.cmarket.cmarket.domain.report.app.dto.ReportCreateCommand;
import org.cmarket.cmarket.domain.report.model.ReportTargetType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 사용자 신고 요청 DTO
 * 
 * 웹 계층에서 사용하는 사용자 신고 요청 DTO입니다.
 */
@Getter
public class UserReportRequest {
    
    @NotEmpty(message = "신고 사유를 선택해야 합니다.")
    private String reasonCode;  // UserReportReason enum name
    
    @Size(max = 300, message = "상세 사유는 최대 300자까지 입력 가능합니다.")
    private String detailReason;
    
    private List<MultipartFile> imageFiles;  // 최대 3장
    
    /**
     * 앱 DTO로 변환
     * 
     * @param targetUserId 신고 대상 사용자 ID
     * @param imageUrls 업로드된 이미지 URL 리스트
     * @return ReportCreateCommand
     */
    public ReportCreateCommand toCommand(Long targetUserId, List<String> imageUrls) {
        return ReportCreateCommand.builder()
                .targetType(ReportTargetType.USER)
                .targetId(targetUserId)
                .reasonCodes(List.of(this.reasonCode))  // 단일 값을 List로 변환
                .detailReason(this.detailReason)
                .imageUrls(imageUrls)
                .build();
    }
}

