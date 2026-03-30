package org.cmarket.cmarket.web.map.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;
import org.cmarket.cmarket.domain.map.app.dto.PetFriendlyPlaceImportCommand;
import org.cmarket.cmarket.domain.map.model.PlaceCategory;
import org.cmarket.cmarket.web.map.dto.PetFriendlyPlaceImportRequest;
import org.cmarket.cmarket.web.map.dto.PublicPetTravelFetchResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class PublicPetTravelApiClient {

    private static final String MOBILE_OS = "ETC";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String serviceKey;
    private final String mobileApp;

    public PublicPetTravelApiClient(
            ObjectMapper objectMapper,
            @Value("${public-data.pet-travel.base-url:https://apis.data.go.kr/B551011/KorPetTourService2}") String baseUrl,
            @Value("${public-data.pet-travel.service-key:}") String serviceKey,
            @Value("${public-data.pet-travel.mobile-app:CMarket}") String mobileApp
    ) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.serviceKey = serviceKey;
        this.mobileApp = mobileApp;
    }

    public PublicPetTravelFetchResult fetchPlacesByRange(PetFriendlyPlaceImportRequest request) {
        if (!StringUtils.hasText(serviceKey)) {
            throw new IllegalStateException("공공데이터 반려동물 동반여행 API 서비스 키가 설정되지 않았습니다.");
        }

        int startPage = request.getStartPage() != null ? request.getStartPage() : 1;
        int endPage = request.getEndPage() != null ? request.getEndPage() : startPage;
        int numOfRows = request.getNumOfRows() != null ? request.getNumOfRows() : 100;

        if (startPage <= 0 || endPage < startPage) {
            throw new IllegalArgumentException("반려동물 동반여행 API 페이지 범위가 올바르지 않습니다.");
        }

        List<PetFriendlyPlaceImportCommand> places = new ArrayList<>();
        int totalCount = 0;

        for (int pageNo = startPage; pageNo <= endPage; pageNo++) {
            PublicPetTravelFetchResult pageResult = fetchSinglePage(request.getCategory(), pageNo, numOfRows);
            totalCount = pageResult.totalCount();
            places.addAll(pageResult.places());
        }

        return new PublicPetTravelFetchResult(totalCount, places);
    }

    private PublicPetTravelFetchResult fetchSinglePage(PlaceCategory category, int pageNo, int numOfRows) {
        URI uri = URI.create(buildRequestUrl(category, pageNo, numOfRows));

        try {
            String responseBody = restTemplate.getForObject(uri, String.class);
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode response = root.path("response");
            JsonNode header = response.path("header");

            String resultCode = header.path("resultCode").asText();
            if (!"0000".equals(resultCode)) {
                String resultMsg = header.path("resultMsg").asText();
                throw new IllegalStateException("반려동물 동반여행 Open API 호출 실패: " + resultCode + " - " + resultMsg);
            }

            JsonNode body = response.path("body");
            int totalCount = body.path("totalCount").asInt(0);
            JsonNode itemNode = body.path("items").path("item");
            if (itemNode.isMissingNode() || itemNode.isNull()) {
                return new PublicPetTravelFetchResult(totalCount, Collections.emptyList());
            }

            List<PetFriendlyPlaceImportCommand> places = new ArrayList<>();
            if (itemNode.isArray()) {
                for (JsonNode item : itemNode) {
                    PetFriendlyPlaceImportCommand command = toCommand(category, item);
                    if (command != null) {
                        places.add(command);
                    }
                }
            } else if (itemNode.isObject()) {
                PetFriendlyPlaceImportCommand command = toCommand(category, itemNode);
                if (command != null) {
                    places.add(command);
                }
            }

            return new PublicPetTravelFetchResult(totalCount, places);
        } catch (RestClientException e) {
            throw new IllegalStateException("반려동물 동반여행 Open API 호출 중 통신 오류가 발생했습니다.", e);
        } catch (Exception e) {
            throw new IllegalStateException("반려동물 동반여행 Open API 응답 처리 중 오류가 발생했습니다.", e);
        }
    }

    private String buildRequestUrl(PlaceCategory category, int pageNo, int numOfRows) {
        List<String> queryParams = new ArrayList<>();
        queryParams.add(queryParam("serviceKey", serviceKey));
        queryParams.add(queryParam("numOfRows", String.valueOf(numOfRows)));
        queryParams.add(queryParam("pageNo", String.valueOf(pageNo)));
        queryParams.add(queryParam("MobileOS", MOBILE_OS));
        queryParams.add(queryParam("MobileApp", mobileApp));
        queryParams.add(queryParam("_type", "json"));
        queryParams.add(queryParam("showflag", "1"));
        queryParams.add(queryParam("arrange", "C"));
        queryParams.add(queryParam("contentTypeId", String.valueOf(toContentTypeId(category))));
        return baseUrl + "/petTourSyncList2?" + String.join("&", queryParams);
    }

    private int toContentTypeId(PlaceCategory category) {
        if (category == PlaceCategory.ACCOMMODATION) {
            return 32;
        }
        if (category == PlaceCategory.RESTAURANT) {
            return 39;
        }
        throw new BusinessException(ErrorCode.UNSUPPORTED_PLACE_CATEGORY, "반려동물 동반여행 import는 숙소와 식당만 지원합니다.");
    }

    private PetFriendlyPlaceImportCommand toCommand(PlaceCategory category, JsonNode item) {
        String mapX = nullIfBlank(item.path("mapx").asText());
        String mapY = nullIfBlank(item.path("mapy").asText());

        Double longitude = parseDouble(mapX);
        Double latitude = parseDouble(mapY);

        return PetFriendlyPlaceImportCommand.builder()
                .category(category)
                .externalPlaceId("kto-pet:" + item.path("contentid").asText(""))
                .name(nullIfBlank(item.path("title").asText()))
                .address(buildAddress(item))
                .phone(nullIfBlank(item.path("tel").asText()))
                .imageUrl(nullIfBlank(item.path("firstimage").asText()))
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }

    private String buildAddress(JsonNode item) {
        String addr1 = nullIfBlank(item.path("addr1").asText());
        String addr2 = nullIfBlank(item.path("addr2").asText());
        if (addr1 == null) {
            return null;
        }
        return addr2 != null ? addr1 + " " + addr2 : addr1;
    }

    private Double parseDouble(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String nullIfBlank(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String queryParam(String name, String value) {
        return UriUtils.encodeQueryParam(name, StandardCharsets.UTF_8)
                + "="
                + UriUtils.encodeQueryParam(value, StandardCharsets.UTF_8);
    }
}
