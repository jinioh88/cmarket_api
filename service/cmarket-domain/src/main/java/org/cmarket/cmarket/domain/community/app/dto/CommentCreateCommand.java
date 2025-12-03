package org.cmarket.cmarket.domain.community.app.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 댓글 작성 명령 DTO
 * 
 * 앱 서비스에서 사용하는 DTO입니다.
 */
@Getter
@Builder
public class CommentCreateCommand {
    // postId는 경로 변수로 전달
    // authorId는 앱 서비스에서 email로 조회하여 설정
    private String content;
    private Long parentId;  // 부모 댓글 ID (대댓글/대대댓글 작성 시)
}

