package org.cmarket.cmarket.web.common;

import org.cmarket.cmarket.web.common.response.ResponseCode;
import org.cmarket.cmarket.web.common.response.SuccessResponse;
import org.cmarket.cmarket.web.common.security.SecurityUtils;
import org.cmarket.cmarket.web.product.dto.ImageUploadResponse;
import org.cmarket.cmarket.web.product.service.ImageUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 이미지 업로드 컨트롤러
 * 
 * 이미지 업로드 API를 제공합니다.
 * 상품, 프로필 등 다양한 도메인에서 공통으로 사용됩니다.
 * 
 * 이미지는 AWS S3에 저장되며, 업로드 시 S3 URL을 반환합니다.
 * 프론트엔드는 반환된 S3 URL을 직접 사용하여 이미지를 표시합니다.
 */
@RestController
@RequestMapping("/api/images")
public class ImageController {
    
    private final ImageUploadService imageUploadService;
    
    public ImageController(ImageUploadService imageUploadService) {
        this.imageUploadService = imageUploadService;
    }
    
    /**
     * 이미지 업로드
     * 
     * POST /api/images
     * 
     * 이미지 파일을 AWS S3에 업로드합니다.
     * - 인증 필수
     * - 최대 5장까지 업로드 가능
     * - 한 장당 최대 5MB, 전체 최대 25MB
     * - 이미지 형식만 업로드 가능 (jpg, jpeg, png, gif, webp)
     * - 파일명 중복 방지를 위해 UUID 기반 고유 파일명 사용
     * - S3 저장 경로: user/{userId}/{yyyy}/{MM}/{dd}/{uuid}.{ext}
     * - 반환 URL 형식: https://{bucket-name}.s3.{region}.amazonaws.com/{key}
     * 
     * @param files 업로드할 이미지 파일 리스트
     * @return 업로드된 이미지 S3 URL 리스트 (첫 번째는 대표 이미지, 나머지는 서브 이미지)
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<ImageUploadResponse>> uploadImages(
            @RequestParam("files") List<MultipartFile> files
    ) throws IOException {
        // 현재 로그인한 사용자의 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 이미지 업로드 (AWS S3에 저장)
        // ImageUploadService 내부에서 email로 userId를 조회합니다.
        List<String> imageUrls = imageUploadService.uploadImages(files, email);
        
        // 응답 DTO 생성
        ImageUploadResponse response = new ImageUploadResponse(imageUrls);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse<>(ResponseCode.CREATED, response));
    }
}

