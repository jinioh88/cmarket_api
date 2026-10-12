package org.cmarket.cmarket.web.community.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.community.app.dto.PostListItemDto;
import org.cmarket.cmarket.domain.community.model.BoardType;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 게시글 목록 항목 응답 DTO
 * 
 * 게시글 목록에서 사용하는 개별 게시글 정보 웹 DTO입니다.
 */
@Getter
@NoArgsConstructor
public class PostListItemResponse {
    private static final Pattern MARKDOWN_IMAGE_PATTERN = Pattern.compile("!\\[[^\\]]*\\]\\((https?://[^\\s)]+)");
    /** 사진 표시 한 덩이 전체 — ![대체 문구](주소) */
    private static final Pattern MARKDOWN_IMAGE_WHOLE_PATTERN = Pattern.compile("!\\[[^\\]]*\\]\\([^)]*\\)");

    private Long id;
    private String title;
    private String contentPreview;  // 내용 미리보기 (최대 100자)
    private String thumbnailImageUrl;
    private String authorNickname;
    private BoardType boardType;
    private Long viewCount;
    private Long commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isModified;
    
    /**
     * 앱 DTO를 웹 DTO로 변환
     * 
     * @param dto PostListItemDto
     * @return PostListItemResponse
     */
    public static PostListItemResponse fromDto(PostListItemDto dto) {
        PostListItemResponse response = new PostListItemResponse();
        response.id = dto.getId();
        response.title = dto.getTitle();
        
        // content를 100글자로 제한
        // 사진 표시 ![](주소) 를 먼저 뺀다. 그대로 자르면 주소가 중간에서 끊겨 목록에 글자로 보인다 (#1120)
        String content = dto.getContent();
        String preview = toPreviewText(content);
        if (preview != null && preview.length() > 100) {
            response.contentPreview = preview.substring(0, 100);
        } else {
            response.contentPreview = preview;
        }
        response.thumbnailImageUrl = extractThumbnailImageUrl(content);
        response.authorNickname = dto.getAuthorNickname();
        response.boardType = dto.getBoardType();
        response.viewCount = dto.getViewCount();
        response.commentCount = dto.getCommentCount();
        response.createdAt = dto.getCreatedAt();
        response.updatedAt = dto.getUpdatedAt();
        response.isModified = dto.getIsModified();
        return response;
    }

    /**
     * 미리보기에 쓸 글자만 남긴다. 사진 표시를 통째로 빼고, 빠진 자리에 남는 빈 줄·빈칸을 한 칸으로 줄인다.
     */
    private static String toPreviewText(String content) {
        if (content == null) {
            return null;
        }

        return MARKDOWN_IMAGE_WHOLE_PATTERN.matcher(content).replaceAll(" ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static String extractThumbnailImageUrl(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }

        Matcher matcher = MARKDOWN_IMAGE_PATTERN.matcher(content);
        return matcher.find() ? matcher.group(1) : null;
    }
}
