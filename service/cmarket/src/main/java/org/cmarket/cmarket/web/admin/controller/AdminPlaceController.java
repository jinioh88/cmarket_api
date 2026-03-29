package org.cmarket.cmarket.web.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cmarket.cmarket.domain.map.app.service.MapAdminQueryService;
import org.cmarket.cmarket.domain.map.app.service.MapAdminService;
import org.cmarket.cmarket.domain.map.app.service.MapImportService;
import org.cmarket.cmarket.domain.map.model.PlaceCategory;
import org.cmarket.cmarket.web.admin.dto.AdminPlaceListResponse;
import org.cmarket.cmarket.web.common.response.ResponseCode;
import org.cmarket.cmarket.web.common.response.SuccessResponse;
import org.cmarket.cmarket.web.map.dto.AdminPlaceRequest;
import org.cmarket.cmarket.web.map.dto.AdminPlaceResponse;
import org.cmarket.cmarket.web.map.dto.HospitalImportRequest;
import org.cmarket.cmarket.web.map.dto.HospitalImportResponse;
import org.cmarket.cmarket.web.map.dto.PlaceRecommendationUpdateRequest;
import org.cmarket.cmarket.web.map.dto.PublicAnimalHospitalFetchResult;
import org.cmarket.cmarket.web.map.service.PublicAnimalHospitalApiClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/places")
public class AdminPlaceController {

    private static final int HOSPITAL_FULL_IMPORT_START_PAGE = 1;
    private static final int HOSPITAL_FULL_IMPORT_END_PAGE = 105;
    private static final int HOSPITAL_FULL_IMPORT_PAGE_SIZE = 100;

    private final MapAdminService mapAdminService;
    private final MapAdminQueryService mapAdminQueryService;
    private final MapImportService mapImportService;
    private final PublicAnimalHospitalApiClient publicAnimalHospitalApiClient;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<SuccessResponse<AdminPlaceListResponse>> getPlaces(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) PlaceCategory category,
            @RequestParam(required = false) Boolean isRecommended,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        AdminPlaceListResponse response = AdminPlaceListResponse.fromPageResult(
                mapAdminQueryService.getPlacesForAdmin(keyword, category, isRecommended, page, size).places()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<SuccessResponse<AdminPlaceResponse>> createPlace(
            @Valid @RequestBody AdminPlaceRequest request
    ) {
        AdminPlaceResponse response = AdminPlaceResponse.fromDto(
                mapAdminService.createPlace(request.toCommand())
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse<>(ResponseCode.CREATED, response));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{placeId}")
    public ResponseEntity<SuccessResponse<AdminPlaceResponse>> updatePlace(
            @PathVariable Long placeId,
            @Valid @RequestBody AdminPlaceRequest request
    ) {
        AdminPlaceResponse response = AdminPlaceResponse.fromDto(
                mapAdminService.updatePlace(placeId, request.toCommand())
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{placeId}/recommendation")
    public ResponseEntity<SuccessResponse<AdminPlaceResponse>> updateRecommendation(
            @PathVariable Long placeId,
            @Valid @RequestBody PlaceRecommendationUpdateRequest request
    ) {
        AdminPlaceResponse response = AdminPlaceResponse.fromDto(
                mapAdminService.updateRecommendation(placeId, request.toCommand())
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/import/hospitals")
    public ResponseEntity<SuccessResponse<HospitalImportResponse>> importHospitals(
            @RequestBody(required = false) HospitalImportRequest request
    ) {
        HospitalImportRequest importRequest = request != null ? request : new HospitalImportRequest();
        PublicAnimalHospitalFetchResult fetchResult = publicAnimalHospitalApiClient.fetchHospitals(importRequest);
        org.cmarket.cmarket.domain.map.app.dto.HospitalImportResultDto importResult =
                mapImportService.importHospitals(fetchResult.hospitals());

        HospitalImportResponse response = HospitalImportResponse.of(
                fetchResult.hospitals().size(),
                importRequest.getPageNo() != null ? importRequest.getPageNo() : 1,
                importRequest.getNumOfRows() != null ? importRequest.getNumOfRows() : 100,
                Boolean.TRUE.equals(importRequest.getImportAllPages()),
                fetchResult.totalCount(),
                importResult
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/import/hospitals/full")
    public ResponseEntity<SuccessResponse<HospitalImportResponse>> importHospitalsFull() {
        HospitalImportRequest importRequest = new HospitalImportRequest();
        importRequest.setNumOfRows(HOSPITAL_FULL_IMPORT_PAGE_SIZE);
        importRequest.setImportAllPages(true);

        PublicAnimalHospitalFetchResult fetchResult = publicAnimalHospitalApiClient.fetchHospitalsByPageRange(
                importRequest,
                HOSPITAL_FULL_IMPORT_START_PAGE,
                HOSPITAL_FULL_IMPORT_END_PAGE
        );
        org.cmarket.cmarket.domain.map.app.dto.HospitalImportResultDto importResult =
                mapImportService.importHospitals(fetchResult.hospitals());

        HospitalImportResponse response = HospitalImportResponse.ofPageRange(
                fetchResult.hospitals().size(),
                HOSPITAL_FULL_IMPORT_START_PAGE,
                HOSPITAL_FULL_IMPORT_END_PAGE,
                HOSPITAL_FULL_IMPORT_PAGE_SIZE,
                fetchResult.totalCount(),
                importResult
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }
}
