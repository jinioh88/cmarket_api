package org.cmarket.cmarket.web.product.service;

import lombok.extern.slf4j.Slf4j;
import org.cmarket.cmarket.domain.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 이미지 업로드 서비스
 * 
 * AWS S3에 이미지를 저장하고 S3 URL을 반환합니다.
 * 외부 인프라(S3)를 다루는 웹 계층 서비스입니다.
 */
@Slf4j
@Service
public class ImageUploadService {
    
    private final UserRepository userRepository;
    private S3Client s3Client;
    
    public ImageUploadService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final long MAX_TOTAL_SIZE = 25 * 1024 * 1024; // 25MB
    private static final int MAX_FILE_COUNT = 5;
    
    @Value("${aws.s3.bucket-name}")
    private String bucketName;
    
    @Value("${aws.s3.region:ap-northeast-2}")
    private String region;
    
    /**
     * S3 클라이언트 초기화
     * 환경 변수 또는 자격 증명 파일을 통해 자동으로 인증됩니다.
     */
    @PostConstruct
    public void initS3Client() {
        try {
            s3Client = S3Client.builder()
                    .region(Region.of(region))
                    .build();
            log.info("S3 Client initialized successfully. Region: {}, Bucket: {}", region, bucketName);
        } catch (Exception e) {
            log.error("Failed to initialize S3 Client", e);
            throw new RuntimeException("S3 Client 초기화 실패", e);
        }
    }
    
    /**
     * S3 클라이언트 종료
     */
    @PreDestroy
    public void closeS3Client() {
        if (s3Client != null) {
            s3Client.close();
            log.info("S3 Client closed");
        }
    }
    
    /**
     * 이미지 파일들을 S3에 업로드하고 URL 리스트를 반환합니다.
     * 
     * @param files 업로드할 이미지 파일 리스트
     * @param email 현재 로그인한 사용자 이메일 (디렉토리 구조에 사용)
     * @return 업로드된 이미지 S3 URL 리스트
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
        
        // 파일 업로드 및 S3 URL 생성
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            // 고유 파일명 생성: UUID + 원본 파일 확장자
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            
            // S3 키(파일 경로) 생성: user/{userId}/{yyyy}/{MM}/{dd}/{uuid}.{ext}
            String s3Key = userDir + "/" + uniqueFilename;
            
            try {
                // S3에 파일 업로드
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Key)
                        .contentType(file.getContentType())
                        .build();
                
                RequestBody requestBody = RequestBody.fromInputStream(file.getInputStream(), file.getSize());
                s3Client.putObject(putObjectRequest, requestBody);
                
                // S3 URL 생성: https://{bucket-name}.s3.{region}.amazonaws.com/{key}
                String s3Url = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Key);
                imageUrls.add(s3Url);
                
                log.debug("Image uploaded to S3: {}", s3Url);
            } catch (S3Exception e) {
                log.error("Failed to upload image to S3: {}", s3Key, e);
                throw new IOException("이미지 업로드에 실패했습니다: " + e.getMessage(), e);
            }
        }
        
        return imageUrls;
    }
}

