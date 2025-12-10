package org.cmarket.cmarket.domain.community.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.community.model.BoardType;

import java.util.List;

/**
 * 게시글 등록 명령 DTO
 * 
 * 앱 서비스에서 사용하는 DTO입니다.
 */
@Getter
@Builder
public class PostCreateCommand {
    // authorId는 앱 서비스에서 email로 조회하여 설정
    private String title;
    private String content;
    private List<String> imageUrls;
    private BoardType boardType;
}

