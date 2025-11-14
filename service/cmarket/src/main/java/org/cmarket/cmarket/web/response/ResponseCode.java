package org.cmarket.cmarket.web.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseCode {
    SUCCESS(200, "성공"),
    CREATED(201, "생성됨"),
    NO_CONTENT(204, "내용 없음"),
    
    BAD_REQUEST(400, "잘못된 요청"),
    UNAUTHORIZED(401, "인증 필요"),
    FORBIDDEN(403, "권한 없음"),
    NOT_FOUND(404, "찾을 수 없음"),
    CONFLICT(409, "충돌"),
    
    INTERNAL_SERVER_ERROR(500, "서버 오류");
    
    private final int code;
    private final String message;
}

