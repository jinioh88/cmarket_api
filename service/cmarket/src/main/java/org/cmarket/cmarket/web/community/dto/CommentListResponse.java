package org.cmarket.cmarket.web.community.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.community.app.dto.CommentListDto;

import java.util.List;

/**
 * 댓글 목록 응답 DTO
 * 
 * 댓글 목록 조회 결과를 담는 웹 DTO입니다.
 */
@Getter
@NoArgsConstructor
public class CommentListResponse {
    private List<CommentListItemResponse> comments;
    
    /**
     * 앱 DTO를 웹 DTO로 변환
     * 
     * @param dto CommentListDto
     * @return CommentListResponse
     */
    public static CommentListResponse fromDto(CommentListDto dto) {
        CommentListResponse response = new CommentListResponse();
        response.comments = dto.comments().stream()
                .map(CommentListItemResponse::fromDto)
                .toList();
        return response;
    }
}

