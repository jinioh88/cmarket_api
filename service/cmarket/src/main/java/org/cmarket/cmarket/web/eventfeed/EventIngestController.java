package org.cmarket.cmarket.web.eventfeed;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cmarket.cmarket.domain.eventfeed.app.EventIngestService;
import org.cmarket.cmarket.domain.eventfeed.app.dto.EventIngestResult;
import org.cmarket.cmarket.web.common.response.ErrorResponse;
import org.cmarket.cmarket.web.common.response.ResponseCode;
import org.cmarket.cmarket.web.common.response.SuccessResponse;
import org.cmarket.cmarket.web.eventfeed.dto.EventIngestRequest;
import org.cmarket.cmarket.web.eventfeed.dto.UnseenRequest;
import org.cmarket.cmarket.web.eventfeed.dto.UnseenResponse;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 행사 수집 컨트롤러
 * 
 * 크롤러(GitHub Actions)만 부르는 주소다. 로그인 대신 X-Ingest-Key 전용 키로 연다.
 * - 키가 없거나 틀리면 401
 * - 서버에 키가 설정되지 않았으면 503 (설정을 빠뜨린 배포를 바로 알 수 있게)
 */
@RestController
@RequestMapping("/api/ingest/events")
@RequiredArgsConstructor
public class EventIngestController {

    private static final String KEY_HEADER = "X-Ingest-Key";

    private final IngestKeyVerifier keyVerifier;
    private final EventIngestService eventIngestService;

    /**
     * 처음 보는 출처 글 번호만 돌려준다
     * 
     * POST /api/ingest/events/unseen
     */
    @PostMapping("/unseen")
    public ResponseEntity<?> findUnseen(
            @RequestHeader(value = KEY_HEADER, required = false) String key,
            @Valid @RequestBody UnseenRequest request
    ) {
        ResponseEntity<ErrorResponse> denied = checkKey(key);
        if (denied != null) {
            return denied;
        }
        UnseenResponse response = new UnseenResponse(
                eventIngestService.findUnseen(request.getSourceCode(), request.getExternalIds()));
        return ResponseEntity.ok(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }

    /**
     * 행사 글을 만들거나 건너뜀으로 기록한다
     * 
     * POST /api/ingest/events
     */
    @PostMapping
    public ResponseEntity<?> ingest(
            @RequestHeader(value = KEY_HEADER, required = false) String key,
            @Valid @RequestBody EventIngestRequest request
    ) {
        ResponseEntity<ErrorResponse> denied = checkKey(key);
        if (denied != null) {
            return denied;
        }
        EventIngestResult result = eventIngestService.ingest(request.toCommand());
        return ResponseEntity.ok(new SuccessResponse<>(ResponseCode.SUCCESS, result));
    }

    private ResponseEntity<ErrorResponse> checkKey(String key) {
        if (!keyVerifier.isConfigured()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ErrorResponse(ResponseCode.SERVICE_UNAVAILABLE, "수집 키가 설정되지 않았습니다.", traceId()));
        }
        if (!keyVerifier.verify(key)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(ResponseCode.UNAUTHORIZED, "수집 키가 올바르지 않습니다.", traceId()));
        }
        return null;
    }

    private String traceId() {
        String traceId = MDC.get("traceId");
        return traceId != null ? traceId : "unknown";
    }
}
