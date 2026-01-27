package org.cmarket.cmarket.web.product.service;

import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.nio.JpegWriter;
import com.sksamuel.scrimage.webp.WebpWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 이미지 리사이징 및 최적화 서비스
 * 
 * 이미지 리사이징, WebP 변환, 썸네일 생성을 담당합니다.
 */
@Slf4j
@Service
public class ImageResizeService {
    
    @Value("${image.resize.enabled:true}")
    private boolean resizeEnabled;
    
    @Value("${image.resize.original.max-width:1200}")
    private int originalMaxWidth;
    
    @Value("${image.resize.original.max-height:800}")
    private int originalMaxHeight;
    
    @Value("${image.resize.thumbnail.width:300}")
    private int thumbnailWidth;
    
    @Value("${image.resize.thumbnail.height:300}")
    private int thumbnailHeight;
    
    @Value("${image.resize.webp.enabled:true}")
    private boolean webpEnabled;
    
    @Value("${image.resize.webp.quality:85}")
    private int webpQuality;
    
    /**
     * 원본 이미지를 리사이징하고 WebP로 변환합니다.
     * 
     * @param file 원본 이미지 파일
     * @return WebP로 변환된 이미지 바이트 배열
     * @throws IOException 이미지 처리 실패 시
     */
    public byte[] resizeAndConvertToWebp(MultipartFile file) throws IOException {
        if (!resizeEnabled) {
            // 리사이징이 비활성화된 경우 원본 파일을 그대로 반환
            return file.getBytes();
        }
        
        try {
            ImmutableImage image = ImmutableImage.loader().fromStream(file.getInputStream());
            
            // 원본 이미지 리사이징 (비율 유지)
            ImmutableImage resizedImage = image.max(originalMaxWidth, originalMaxHeight);
            
            // 이미지 포맷 변환
            if (webpEnabled) {
                // WebP 변환 (품질 설정)
                WebpWriter webpWriter = new WebpWriter()
                        .withQ(webpQuality);
                return resizedImage.bytes(webpWriter);
            } else {
                // JPEG로 변환 (기본 포맷, 품질 85%)
                return resizedImage.bytes(new JpegWriter().withCompression(85));
            }
        } catch (Exception e) {
            log.error("Failed to resize and convert image to WebP", e);
            throw new IOException("이미지 리사이징 및 WebP 변환에 실패했습니다: " + e.getMessage(), e);
        }
    }
    
    /**
     * 썸네일 이미지를 생성하고 WebP로 변환합니다.
     * 
     * @param file 원본 이미지 파일
     * @return WebP로 변환된 썸네일 이미지 바이트 배열
     * @throws IOException 이미지 처리 실패 시
     */
    public byte[] createThumbnail(MultipartFile file) throws IOException {
        if (!resizeEnabled) {
            // 리사이징이 비활성화된 경우 원본 파일을 그대로 반환
            return file.getBytes();
        }
        
        try {
            ImmutableImage image = ImmutableImage.loader().fromStream(file.getInputStream());
            
            // 썸네일 리사이징 (비율 유지, 크롭 없음)
            ImmutableImage thumbnail = image.max(thumbnailWidth, thumbnailHeight);
            
            // 이미지 포맷 변환
            if (webpEnabled) {
                // WebP 변환 (품질 설정)
                WebpWriter webpWriter = new WebpWriter()
                        .withQ(webpQuality);
                return thumbnail.bytes(webpWriter);
            } else {
                // JPEG로 변환 (기본 포맷, 품질 85%)
                return thumbnail.bytes(new JpegWriter().withCompression(85));
            }
        } catch (Exception e) {
            log.error("Failed to create thumbnail", e);
            throw new IOException("썸네일 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }
    
    /**
     * 이미지의 Content-Type을 반환합니다.
     * WebP 변환이 활성화된 경우 "image/webp"를 반환합니다.
     * 
     * @return Content-Type 문자열
     */
    public String getContentType() {
        if (webpEnabled) {
            return "image/webp";
        }
        return "image/jpeg"; // 기본값
    }
    
    /**
     * 파일 확장자를 반환합니다.
     * WebP 변환이 활성화된 경우 ".webp"를 반환합니다.
     * 
     * @return 파일 확장자 (예: ".webp", ".jpg")
     */
    public String getFileExtension() {
        if (webpEnabled) {
            return ".webp";
        }
        return ".jpg"; // 기본값
    }
}
