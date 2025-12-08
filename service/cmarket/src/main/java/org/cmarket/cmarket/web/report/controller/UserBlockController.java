package org.cmarket.cmarket.web.report.controller;

import lombok.RequiredArgsConstructor;
import org.cmarket.cmarket.domain.report.app.dto.UserBlockCreateCommand;
import org.cmarket.cmarket.domain.report.app.dto.UserBlockDto;
import org.cmarket.cmarket.domain.report.app.service.ReportService;
import org.cmarket.cmarket.web.common.response.ResponseCode;
import org.cmarket.cmarket.web.common.response.SuccessResponse;
import org.cmarket.cmarket.web.common.security.SecurityUtils;
import org.cmarket.cmarket.web.report.dto.UserBlockResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports/blocks")
@RequiredArgsConstructor
public class UserBlockController {

    private final ReportService reportService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/users/{blockedUserId}")
    public ResponseEntity<SuccessResponse<UserBlockResponse>> blockUser(
            @PathVariable Long blockedUserId
    ) {
        String email = SecurityUtils.getCurrentUserEmail();

        UserBlockCreateCommand command = UserBlockCreateCommand.builder()
                .blockedUserId(blockedUserId)
                .build();

        UserBlockDto result = reportService.blockUser(email, command);
        UserBlockResponse response = UserBlockResponse.fromDto(result);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse<>(ResponseCode.CREATED, response));
    }
}

