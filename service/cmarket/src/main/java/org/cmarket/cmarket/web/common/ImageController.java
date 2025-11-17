package org.cmarket.cmarket.web.common;

import jakarta.servlet.http.HttpServletRequest;
import org.cmarket.cmarket.web.common.response.ResponseCode;
import org.cmarket.cmarket.web.common.response.SuccessResponse;
import org.cmarket.cmarket.web.common.security.SecurityUtils;
import org.cmarket.cmarket.web.product.dto.ImageUploadResponse;
import org.cmarket.cmarket.web.product.service.ImageUploadService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * 이미지 업로드/조회 컨트롤러
 * 
 * 이미지 업로드 및 조회 API를 제공합니다.
 * 상품, 프로필 등 다양한 도메인에서 공통으로 사용됩니다.
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
     * 이미지 파일을 업로드합니다.
     * - 인증 필수
     * - 최대 5장까지 업로드 가능
     * - 한 장당 최대 5MB, 전체 최대 25MB
     * - 이미지 형식만 업로드 가능 (jpg, jpeg, png, gif, webp)
     * - 파일명 중복 방지를 위해 UUID 기반 고유 파일명 사용
     * - 저장 경로: user/{userId}/{yyyy}/{MM}/{dd}/{uuid}.{ext}
     * 
     * @param files 업로드할 이미지 파일 리스트
     * @return 업로드된 이미지 URL 리스트 (첫 번째는 대표 이미지, 나머지는 서브 이미지)
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<ImageUploadResponse>> uploadImages(
            @RequestParam("files") List<MultipartFile> files
    ) throws IOException {
        // 현재 로그인한 사용자의 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 이미지 업로드 (로컬 파일 시스템에 저장)
        // ImageUploadService 내부에서 email로 userId를 조회합니다.
        List<String> imageUrls = imageUploadService.uploadImages(files, email);
        
        // 응답 DTO 생성
        ImageUploadResponse response = new ImageUploadResponse(imageUrls);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse<>(ResponseCode.CREATED, response));
    }
    
    /**
     * 이미지 조회
     * 
     * GET /api/images/user/{userId}/{yyyy}/{MM}/{dd}/{filename}
     * 
     * 업로드된 이미지를 조회합니다.
     * - imagePath 형식: user/{userId}/{yyyy}/{MM}/{dd}/{filename}
     * - 예시: /api/images/user/1/2024/01/15/uuid-filename.jpg
     * 
     * @param request HTTP 요청 객체 (경로 추출용)
     * @return 이미지 파일
     */
    @GetMapping("/**")
    public ResponseEntity<Resource> getImage(HttpServletRequest request) {
        try {
            // 요청 URI에서 /api/images 제거하여 실제 이미지 경로 추출
            String requestURI = request.getRequestURI();
            String basePath = "/api/images";
            
            if (!requestURI.startsWith(basePath)) {
                return ResponseEntity.notFound().build();
            }
            
            // /api/images/user/1/2024/01/15/filename.jpg -> user/1/2024/01/15/filename.jpg
            String imagePath = requestURI.substring(basePath.length() + 1);
            
            // 이미지 파일 읽기
            Path filePath = imageUploadService.getImagePath(imagePath);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }
            
            // 파일명 추출 (Content-Disposition 헤더용)
            String filename = imagePath.substring(imagePath.lastIndexOf("/") + 1);
            
            // Content-Type 설정
            String contentType = determineContentType(filename);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * 파일명으로 Content-Type 결정
     */
    private String determineContentType(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "application/octet-stream";
        }
        
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }
}

