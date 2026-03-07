package org.cmarket.cmarket.web.admin.controller;

import lombok.RequiredArgsConstructor;
import org.cmarket.cmarket.domain.admin.app.dto.AdminProductListItemDto;
import org.cmarket.cmarket.domain.admin.app.service.AdminProductQueryService;
import org.cmarket.cmarket.domain.product.model.Category;
import org.cmarket.cmarket.domain.product.model.ProductType;
import org.cmarket.cmarket.domain.profile.app.dto.PageResult;
import org.cmarket.cmarket.web.admin.dto.AdminProductListResponse;
import org.cmarket.cmarket.web.common.response.ResponseCode;
import org.cmarket.cmarket.web.common.response.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 어드민 상품 관리 컨트롤러
 */
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductQueryService adminProductQueryService;

    /**
     * 어드민 상품 목록 조회 (nickname, category, updatedAt 포함)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<SuccessResponse<AdminProductListResponse>> getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ProductType productType,
            @RequestParam(required = false) Category category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResult<AdminProductListItemDto> pageResult = adminProductQueryService.getProductsForAdmin(
                keyword, productType, category, page, size
        );
        AdminProductListResponse response = AdminProductListResponse.fromPageResult(pageResult);
        return ResponseEntity.ok(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }
}
