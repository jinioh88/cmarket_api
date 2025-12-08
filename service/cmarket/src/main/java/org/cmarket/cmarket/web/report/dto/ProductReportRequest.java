package org.cmarket.cmarket.web.report.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.cmarket.cmarket.domain.report.app.dto.ReportCreateCommand;
import org.cmarket.cmarket.domain.report.model.ReportTargetType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 상품 신고 요청 DTO
 * 
 * 웹 계층에서 사용하는 상품 신고 요청 DTO입니다.
 */
@Getter
public class ProductReportRequest {
    
    @NotEmpty(message = "신고 사유를 최소 1개 이상 선택해야 합니다.")
    private List<String> reasonCodes;  // ProductReportReason enum name 리스트
    
    @Size(max = 300, message = "상세 사유는 최대 300자까지 입력 가능합니다.")
    private String detailReason;
    
    private List<MultipartFile> imageFiles;  // 최대 3장
    
    /**
     * 앱 DTO로 변환
     * 
     * @param productId 신고 대상 상품 ID
     * @param imageUrls 업로드된 이미지 URL 리스트
     * @return ReportCreateCommand
     */
    public ReportCreateCommand toCommand(Long productId, List<String> imageUrls) {
        return ReportCreateCommand.builder()
                .targetType(ReportTargetType.PRODUCT)
                .targetId(productId)
                .reasonCodes(this.reasonCodes)
                .detailReason(this.detailReason)
                .imageUrls(imageUrls)
                .build();
    }
}

