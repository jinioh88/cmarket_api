package org.cmarket.cmarket.web.report.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cmarket.cmarket.domain.report.app.dto.ReportCreateCommand;
import org.cmarket.cmarket.domain.report.app.dto.ReportDto;
import org.cmarket.cmarket.domain.report.app.service.ReportService;
import org.cmarket.cmarket.web.common.response.ResponseCode;
import org.cmarket.cmarket.web.common.response.SuccessResponse;
import org.cmarket.cmarket.web.common.security.SecurityUtils;
import org.cmarket.cmarket.web.product.service.ImageUploadService;
import org.cmarket.cmarket.web.report.dto.CommunityReportRequest;
import org.cmarket.cmarket.web.report.dto.ProductReportRequest;
import org.cmarket.cmarket.web.report.dto.ReportResponse;
import org.cmarket.cmarket.web.report.dto.UserReportRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 신고 컨트롤러
 * 
 * 신고 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    
    private final ReportService reportService;
    private final ImageUploadService imageUploadService;
    
    /**
     * 사용자 신고
     * 
     * POST /api/reports/users/{targetUserId}
     * 
     * @param targetUserId 신고 대상 사용자 ID
     * @param request 신고 요청 DTO
     * @return 신고 결과
     * @throws IOException 이미지 업로드 실패 시
     */
    @PostMapping("/users/{targetUserId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<ReportResponse>> reportUser(
            @PathVariable Long targetUserId,
            @Valid @ModelAttribute UserReportRequest request
    ) throws IOException {
        // 현재 로그인한 사용자 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 이미지 업로드 처리 (웹 계층에서 처리)
        List<String> imageUrls = new ArrayList<>();
        if (request.getImageFiles() != null && !request.getImageFiles().isEmpty()) {
            // 빈 파일 제거
            List<MultipartFile> validFiles = request.getImageFiles().stream()
                    .filter(file -> file != null && !file.isEmpty())
                    .toList();
            
            if (!validFiles.isEmpty()) {
                imageUrls = imageUploadService.uploadImages(validFiles, email);
            }
        }
        
        // 웹 DTO → 앱 DTO 변환
        ReportCreateCommand command = request.toCommand(targetUserId, imageUrls);
        
        // 앱 서비스 호출
        ReportDto reportDto = reportService.createReport(email, command);
        
        // 앱 DTO → 웹 DTO 변환
        ReportResponse response = ReportResponse.fromDto(reportDto);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse<>(ResponseCode.CREATED, response));
    }
    
    /**
     * 상품 신고
     * 
     * POST /api/reports/products/{productId}
     * 
     * @param productId 신고 대상 상품 ID
     * @param request 신고 요청 DTO
     * @return 신고 결과
     * @throws IOException 이미지 업로드 실패 시
     */
    @PostMapping("/products/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<ReportResponse>> reportProduct(
            @PathVariable Long productId,
            @Valid @ModelAttribute ProductReportRequest request
    ) throws IOException {
        // 현재 로그인한 사용자 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 이미지 업로드 처리 (웹 계층에서 처리)
        List<String> imageUrls = new ArrayList<>();
        if (request.getImageFiles() != null && !request.getImageFiles().isEmpty()) {
            // 빈 파일 제거
            List<MultipartFile> validFiles = request.getImageFiles().stream()
                    .filter(file -> file != null && !file.isEmpty())
                    .toList();
            
            if (!validFiles.isEmpty()) {
                imageUrls = imageUploadService.uploadImages(validFiles, email);
            }
        }
        
        // 웹 DTO → 앱 DTO 변환
        ReportCreateCommand command = request.toCommand(productId, imageUrls);
        
        // 앱 서비스 호출
        ReportDto reportDto = reportService.createReport(email, command);
        
        // 앱 DTO → 웹 DTO 변환
        ReportResponse response = ReportResponse.fromDto(reportDto);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse<>(ResponseCode.CREATED, response));
    }
    
    /**
     * 커뮤니티 게시글 신고
     * 
     * POST /api/reports/community-posts/{postId}
     * 
     * @param postId 신고 대상 게시글 ID
     * @param request 신고 요청 DTO
     * @return 신고 결과
     * @throws IOException 이미지 업로드 실패 시
     */
    @PostMapping("/community-posts/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<ReportResponse>> reportCommunityPost(
            @PathVariable Long postId,
            @Valid @ModelAttribute CommunityReportRequest request
    ) throws IOException {
        // 현재 로그인한 사용자 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 이미지 업로드 처리 (웹 계층에서 처리)
        List<String> imageUrls = new ArrayList<>();
        if (request.getImageFiles() != null && !request.getImageFiles().isEmpty()) {
            // 빈 파일 제거
            List<MultipartFile> validFiles = request.getImageFiles().stream()
                    .filter(file -> file != null && !file.isEmpty())
                    .toList();
            
            if (!validFiles.isEmpty()) {
                imageUrls = imageUploadService.uploadImages(validFiles, email);
            }
        }
        
        // 웹 DTO → 앱 DTO 변환
        ReportCreateCommand command = request.toCommand(postId, imageUrls);
        
        // 앱 서비스 호출
        ReportDto reportDto = reportService.createReport(email, command);
        
        // 앱 DTO → 웹 DTO 변환
        ReportResponse response = ReportResponse.fromDto(reportDto);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse<>(ResponseCode.CREATED, response));
    }
}

