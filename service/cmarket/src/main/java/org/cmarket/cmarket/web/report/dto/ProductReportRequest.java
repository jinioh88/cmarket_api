package org.cmarket.cmarket.web.report.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.cmarket.cmarket.domain.report.app.dto.ReportCreateCommand;
import org.cmarket.cmarket.domain.report.model.ReportTargetType;

import java.util.List;

/**
 * 상품 신고 요청 DTO
 * 
 * 웹 계층에서 사용하는 상품 신고 요청 DTO입니다.
 * 이미지는 별도 이미지 업로드 API를 통해 업로드한 후 URL 리스트를 전달합니다.
 */
@Data
public class ProductReportRequest {
    
    @NotEmpty(message = "신고 사유를 최소 1개 이상 선택해야 합니다.")
    private List<String> reasonCodes;  // ProductReportReason enum name 리스트
    
    @Size(max = 300, message = "상세 사유는 최대 300자까지 입력 가능합니다.")
    private String detailReason;
    
    private List<String> imageUrls;  // 이미지 URL 리스트 (최대 3개, 별도 이미지 업로드 API로 업로드)
    
    /**
     * 앱 DTO로 변환
     * 
     * @param productId 신고 대상 상품 ID
     * @return ReportCreateCommand
     */
    public ReportCreateCommand toCommand(Long productId) {
        return ReportCreateCommand.builder()
                .targetType(ReportTargetType.PRODUCT)
                .targetId(productId)
                .reasonCodes(this.reasonCodes)
                .detailReason(this.detailReason)
                .imageUrls(this.imageUrls != null ? this.imageUrls : List.of())
                .build();
    }
}

