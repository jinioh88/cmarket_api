package org.cmarket.cmarket.domain.community.app.dto;

import java.util.List;

/**
 * 댓글 목록 DTO
 * 
 * 댓글 목록 조회 결과를 담는 앱 DTO입니다.
 */
public record CommentListDto(
    List<CommentListItemDto> comments
) {
}

