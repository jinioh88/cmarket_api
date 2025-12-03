package org.cmarket.cmarket.domain.community.app.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 게시글 수정 명령 DTO
 * 
 * 앱 서비스에서 사용하는 DTO입니다.
 */
@Getter
@Builder
public class PostUpdateCommand {
    private String title;
    private String content;
    private List<String> imageUrls;
}

