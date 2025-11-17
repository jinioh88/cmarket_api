package org.cmarket.cmarket.web.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 이미지 업로드 응답 DTO
 * 
 * 이미지 업로드 결과를 담는 웹 DTO입니다.
 */
@Getter
@NoArgsConstructor
public class ImageUploadResponse {
    private List<String> imageUrls;  // 업로드된 이미지 URL 리스트
    private String mainImageUrl;     // 첫 번째 이미지 (대표 이미지)
    private List<String> subImageUrls;  // 나머지 이미지들 (서브 이미지, 최대 4장)
    
    public ImageUploadResponse(List<String> imageUrls) {
        this.imageUrls = imageUrls;
        if (imageUrls != null && !imageUrls.isEmpty()) {
            this.mainImageUrl = imageUrls.get(0);
            if (imageUrls.size() > 1) {
                this.subImageUrls = imageUrls.subList(1, imageUrls.size());
            }
        }
    }
}

