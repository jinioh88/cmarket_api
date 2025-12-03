package org.cmarket.cmarket.web.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.community.app.dto.CommentCreateCommand;

/**
 * 댓글 작성 요청 DTO
 * 
 * 댓글 작성 시 필요한 모든 정보를 받습니다.
 */
@Getter
@NoArgsConstructor
public class CommentCreateRequest {
    
    @NotBlank(message = "내용은 필수입니다.")
    @Size(min = 2, max = 500, message = "내용은 2자 이상 500자 이하여야 합니다.")
    private String content;
    
    private Long parentId;  // 부모 댓글 ID (대댓글/대대댓글 작성 시, 선택적)
    
    /**
     * 웹 DTO를 앱 DTO로 변환
     * 
     * @return CommentCreateCommand
     */
    public CommentCreateCommand toCommand() {
        return CommentCreateCommand.builder()
                .content(this.content)
                .parentId(this.parentId)
                .build();
    }
}

