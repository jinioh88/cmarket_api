package org.cmarket.cmarket.web.map.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cmarket.cmarket.domain.map.app.dto.PetFriendlyPlaceImportCommand;
import org.cmarket.cmarket.domain.map.model.PlaceCategory;
import org.cmarket.cmarket.web.map.dto.CafeImportRequest;
import org.cmarket.cmarket.web.map.dto.PublicAnimalExhibitionFetchResult;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;
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
public class PublicAnimalExhibitionApiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String serviceKey;
    private final CoordinateTransform coordinateTransform;

    public PublicAnimalExhibitionApiClient(
            ObjectMapper objectMapper,
            @Value("${public-data.animal-exhibition.base-url:https://apis.data.go.kr/1741000/animal_exhibition}") String baseUrl,
            @Value("${public-data.animal-exhibition.service-key:}") String serviceKey
    ) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.serviceKey = serviceKey;

        CRSFactory crsFactory = new CRSFactory();
        CoordinateReferenceSystem source = crsFactory.createFromParameters(
                "EPSG:5174",
                "+proj=tmerc +lat_0=38 +lon_0=127.0028902777778 +k=1 +x_0=200000 +y_0=500000 +ellps=bessel +units=m +no_defs"
        );
        CoordinateReferenceSystem target = crsFactory.createFromParameters(
                "EPSG:4326",
                "+proj=longlat +datum=WGS84 +no_defs"
        );
        this.coordinateTransform = new CoordinateTransformFactory().createTransform(source, target);
    }

    public PublicAnimalExhibitionFetchResult fetchCafesByRange(CafeImportRequest request) {
        if (!StringUtils.hasText(serviceKey)) {
            throw new IllegalStateException("공공데이터 동물전시업 API 서비스 키가 설정되지 않았습니다.");
        }

        int startPage = request.getStartPage() != null ? request.getStartPage() : 1;
        int endPage = request.getEndPage() != null ? request.getEndPage() : startPage;
        int numOfRows = request.getNumOfRows() != null ? request.getNumOfRows() : 100;

        if (startPage <= 0 || endPage < startPage) {
            throw new IllegalArgumentException("동물전시업 API 페이지 범위가 올바르지 않습니다.");
        }

        List<PetFriendlyPlaceImportCommand> places = new ArrayList<>();
        int totalCount = 0;

        for (int pageNo = startPage; pageNo <= endPage; pageNo++) {
            PublicAnimalExhibitionFetchResult pageResult = fetchSinglePage(request, pageNo, numOfRows);
            totalCount = pageResult.totalCount();
            places.addAll(pageResult.places());
        }

        return new PublicAnimalExhibitionFetchResult(totalCount, places);
    }

    private PublicAnimalExhibitionFetchResult fetchSinglePage(CafeImportRequest request, int pageNo, int numOfRows) {
        URI uri = URI.create(buildRequestUrl(request, pageNo, numOfRows));

        try {
            String responseBody = restTemplate.getForObject(uri, String.class);
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode response = root.path("response");
            JsonNode header = response.path("header");

            String resultCode = header.path("resultCode").asText();
            if (!"0".equals(resultCode)) {
                String resultMsg = header.path("resultMsg").asText();
                throw new IllegalStateException("동물전시업 Open API 호출 실패: " + resultCode + " - " + resultMsg);
            }

            JsonNode body = response.path("body");
            int totalCount = body.path("totalCount").asInt(0);
            JsonNode itemNode = body.path("items").path("item");
            if (itemNode.isMissingNode() || itemNode.isNull()) {
                return new PublicAnimalExhibitionFetchResult(totalCount, Collections.emptyList());
            }

            List<PetFriendlyPlaceImportCommand> places = new ArrayList<>();
            if (itemNode.isArray()) {
                for (JsonNode item : itemNode) {
                    PetFriendlyPlaceImportCommand command = toCommand(item);
                    if (command != null) {
                        places.add(command);
                    }
                }
            } else if (itemNode.isObject()) {
                PetFriendlyPlaceImportCommand command = toCommand(itemNode);
                if (command != null) {
                    places.add(command);
                }
            }

            return new PublicAnimalExhibitionFetchResult(totalCount, places);
        } catch (RestClientException e) {
            throw new IllegalStateException("동물전시업 Open API 호출 중 통신 오류가 발생했습니다.", e);
        } catch (Exception e) {
            throw new IllegalStateException("동물전시업 Open API 응답 처리 중 오류가 발생했습니다.", e);
        }
    }

    private String buildRequestUrl(CafeImportRequest request, int pageNo, int numOfRows) {
        List<String> queryParams = new ArrayList<>();
        queryParams.add(queryParam("serviceKey", serviceKey));
        queryParams.add(queryParam("pageNo", String.valueOf(pageNo)));
        queryParams.add(queryParam("numOfRows", String.valueOf(numOfRows)));
        queryParams.add(queryParam("returnType", "json"));
        addOptionalQueryParam(queryParams, "cond[SALS_STTS_CD::EQ]", request.getSalesStatusCode());
        addOptionalQueryParam(queryParams, "cond[ROAD_NM_ADDR::LIKE]", request.getRoadNmAddrKeyword());
        addOptionalQueryParam(queryParams, "cond[BPLC_NM::LIKE]", request.getBusinessNameKeyword());
        return baseUrl + "/info?" + String.join("&", queryParams);
    }

    private void addOptionalQueryParam(List<String> queryParams, String name, String value) {
        if (StringUtils.hasText(value)) {
            queryParams.add(queryParam(name, value));
        }
    }

    private String queryParam(String name, String value) {
        return UriUtils.encodeQueryParam(name, StandardCharsets.UTF_8)
                + "="
                + UriUtils.encodeQueryParam(value, StandardCharsets.UTF_8);
    }

    private PetFriendlyPlaceImportCommand toCommand(JsonNode item) {
        Double longitude = null;
        Double latitude = null;

        String x = item.path("CRD_INFO_X").asText();
        String y = item.path("CRD_INFO_Y").asText();
        if (StringUtils.hasText(x) && StringUtils.hasText(y)) {
            double[] coordinates = transformToWgs84(x, y);
            if (coordinates != null) {
                longitude = coordinates[0];
                latitude = coordinates[1];
            }
        }

        return PetFriendlyPlaceImportCommand.builder()
                .category(PlaceCategory.CAFE)
                .externalPlaceId(buildExternalPlaceId(item))
                .name(nullIfBlank(item.path("BPLC_NM").asText()))
                .address(resolveAddress(item))
                .phone(nullIfBlank(item.path("TELNO").asText()))
                .imageUrl(null)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }

    private String buildExternalPlaceId(JsonNode item) {
        String localGovernmentCode = item.path("OPN_ATMY_GRP_CD").asText("");
        String managementNumber = item.path("MNG_NO").asText("");
        return "public-animal-exhibition:" + localGovernmentCode + ":" + managementNumber;
    }

    private String resolveAddress(JsonNode item) {
        String roadAddress = nullIfBlank(item.path("ROAD_NM_ADDR").asText());
        String lotNumberAddress = nullIfBlank(item.path("LOTNO_ADDR").asText());
        return roadAddress != null ? roadAddress : lotNumberAddress;
    }

    private double[] transformToWgs84(String x, String y) {
        try {
            ProjCoordinate sourceCoordinate = new ProjCoordinate(Double.parseDouble(x), Double.parseDouble(y));
            ProjCoordinate targetCoordinate = new ProjCoordinate();
            coordinateTransform.transform(sourceCoordinate, targetCoordinate);
            return new double[]{targetCoordinate.x, targetCoordinate.y};
        } catch (Exception e) {
            return null;
        }
    }

    private String nullIfBlank(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
