package org.cmarket.cmarket.domain.community.app.dto;

import org.cmarket.cmarket.domain.profile.app.dto.PageResult;

/**
 * 게시글 목록 DTO
 * 
 * 게시글 목록 조회 결과를 담는 앱 DTO입니다.
 */
public record PostListDto(
    PageResult<PostListItemDto> posts
) {
}

