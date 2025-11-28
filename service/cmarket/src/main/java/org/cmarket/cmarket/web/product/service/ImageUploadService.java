package org.cmarket.cmarket.web.product.service;

import lombok.extern.slf4j.Slf4j;
import org.cmarket.cmarket.domain.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 이미지 업로드 서비스
 * 
 * 로컬 파일 시스템에 이미지를 저장하고 URL을 반환합니다.
 * 외부 인프라(파일 시스템)를 다루는 웹 계층 서비스입니다.
 */
@Slf4j
@Service
public class ImageUploadService {
    
    private final UserRepository userRepository;
    
    public ImageUploadService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final long MAX_TOTAL_SIZE = 25 * 1024 * 1024; // 25MB
    private static final int MAX_FILE_COUNT = 5;
    
    @Value("${app.image.upload-dir:uploads/images}")
    private String uploadDir;
    
    @Value("${app.image.base-url:/api/images}")
    private String baseUrl;
    
    @Value("${app.image.server-url:}")
    private String serverUrl;
    
    /**
     * 이미지 파일들을 업로드하고 URL 리스트를 반환합니다.
     * 
     * @param files 업로드할 이미지 파일 리스트
     * @param email 현재 로그인한 사용자 이메일 (디렉토리 구조에 사용)
     * @return 업로드된 이미지 URL 리스트
     * @throws IllegalArgumentException 파일 유효성 검증 실패 시, 사용자를 찾을 수 없을 때
     * @throws IOException 파일 저장 실패 시
     */
    public List<String> uploadImages(List<MultipartFile> files, String email) throws IOException {
        // 사용자 ID 조회
        Long userId = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."))
                .getId();
        // 파일 개수 검증
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("업로드할 이미지 파일이 없습니다.");
        }
        
        if (files.size() > MAX_FILE_COUNT) {
            throw new IllegalArgumentException("이미지는 최대 " + MAX_FILE_COUNT + "장까지 업로드 가능합니다.");
        }
        
        // 전체 파일 크기 검증
        long totalSize = 0;
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("빈 파일은 업로드할 수 없습니다.");
            }
            
            // 파일 형식 검증
            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
                throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다. (jpg, jpeg, png, gif, webp)");
            }
            
            // 파일 크기 검증
            long fileSize = file.getSize();
            if (fileSize > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("파일 크기는 한 장당 최대 5MB까지 가능합니다.");
            }
            
            totalSize += fileSize;
        }
        
        if (totalSize > MAX_TOTAL_SIZE) {
            throw new IllegalArgumentException("전체 파일 크기는 최대 25MB까지 가능합니다.");
        }
        
        // 날짜 기반 디렉토리 구조 생성: user/{userId}/{yyyy}/{MM}/{dd}/
        LocalDate now = LocalDate.now();
        String datePath = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String userDir = "user/" + userId + "/" + datePath;
        Path uploadPath = Paths.get(uploadDir, userDir);
        
        // 디렉토리 생성
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // 파일 업로드 및 URL 생성
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            // 고유 파일명 생성: UUID + 원본 파일 확장자
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            
            // 파일 저장
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // URL 생성: http://서버주소/api/images/user/{userId}/{yyyy}/{MM}/{dd}/{uuid}.{ext}
            String relativePath = baseUrl + "/" + userDir + "/" + uniqueFilename;
            String imageUrl;
            if (serverUrl != null && !serverUrl.isEmpty()) {
                // 완전한 URL 생성 (프론트엔드가 다른 컴퓨터에서 접근 가능)
                imageUrl = serverUrl + relativePath;
            } else {
                // 기존 방식 (상대 경로, 로컬 개발용)
                imageUrl = relativePath;
            }
            imageUrls.add(imageUrl);
            
            log.debug("Image uploaded: {}", imageUrl);
        }
        
        return imageUrls;
    }
    
    /**
     * 이미지 파일을 읽어서 Path를 반환합니다.
     * 
     * @param imagePath 이미지 경로 (user/{userId}/{yyyy}/{MM}/{dd}/{filename})
     * @return 이미지 파일 Path
     * @throws IOException 파일을 찾을 수 없을 때
     */
    public Path getImagePath(String imagePath) throws IOException {
        Path filePath = Paths.get(uploadDir, imagePath);
        
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new IOException("이미지 파일을 찾을 수 없습니다: " + imagePath);
        }
        
        return filePath;
    }
}

