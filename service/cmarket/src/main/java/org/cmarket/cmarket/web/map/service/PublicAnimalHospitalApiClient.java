package org.cmarket.cmarket.web.map.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cmarket.cmarket.domain.map.app.dto.HospitalImportCommand;
import org.cmarket.cmarket.domain.map.model.AnimalType;
import org.cmarket.cmarket.web.map.dto.HospitalImportRequest;
import org.cmarket.cmarket.web.map.dto.PublicAnimalHospitalFetchResult;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class PublicAnimalHospitalApiClient {

    private static final int MAX_IMPORT_PAGES = 50;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String serviceKey;
    private final CoordinateTransform coordinateTransform;

    public PublicAnimalHospitalApiClient(
            ObjectMapper objectMapper,
            @Value("${public-data.animal-hospital.base-url:https://api.localdata.go.kr/api/animal_hospitals/v1}") String baseUrl,
            @Value("${public-data.animal-hospital.service-key:}") String serviceKey
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

    public PublicAnimalHospitalFetchResult fetchHospitals(HospitalImportRequest request) {
        if (!StringUtils.hasText(serviceKey)) {
            throw new IllegalStateException("공공데이터 동물병원 API 서비스 키가 설정되지 않았습니다.");
        }

        List<HospitalImportCommand> hospitals = new ArrayList<>();
        int pageNo = request.getPageNo() != null ? request.getPageNo() : 1;
        int numOfRows = request.getNumOfRows() != null ? request.getNumOfRows() : 100;
        int totalCount = 0;
        int processedPages = 0;

        while (true) {
            PublicAnimalHospitalFetchResult pageResult = fetchSinglePage(request, pageNo, numOfRows);
            totalCount = pageResult.totalCount();
            hospitals.addAll(pageResult.hospitals());
            processedPages++;

            if (!Boolean.TRUE.equals(request.getImportAllPages())) {
                break;
            }

            if (pageNo * numOfRows >= totalCount) {
                break;
            }

            if (processedPages >= MAX_IMPORT_PAGES) {
                break;
            }

            pageNo++;
        }

        return new PublicAnimalHospitalFetchResult(totalCount, hospitals);
    }

    private PublicAnimalHospitalFetchResult fetchSinglePage(HospitalImportRequest request, int pageNo, int numOfRows) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/info")
                .queryParam("serviceKey", serviceKey)
                .queryParam("pageNo", pageNo)
                .queryParam("numOfRows", numOfRows)
                .queryParam("returnType", "json")
                .queryParamIfPresent("cond[OPN_ATMY_GRP_CD::EQ]", optional(request.getOpnAtmyGrpCd()))
                .queryParamIfPresent("cond[SALS_STTS_CD::EQ]", optional(request.getSalesStatusCode()))
                .queryParamIfPresent("cond[ROAD_NM_ADDR::LIKE]", optional(request.getRoadNmAddrKeyword()))
                .queryParamIfPresent("cond[BPLC_NM::LIKE]", optional(request.getBusinessNameKeyword()))
                .queryParamIfPresent("cond[DAT_UPDT_PNT::GTE]", optional(request.getUpdatedFrom()))
                .queryParamIfPresent("cond[DAT_UPDT_PNT::LT]", optional(request.getUpdatedTo()))
                .build(true)
                .toUri();

        try {
            String responseBody = restTemplate.getForObject(uri, String.class);
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode response = root.path("response");
            JsonNode header = response.path("header");

            String resultCode = header.path("resultCode").asText();
            if (!"0".equals(resultCode)) {
                String resultMsg = header.path("resultMsg").asText();
                throw new IllegalStateException("동물병원 Open API 호출 실패: " + resultCode + " - " + resultMsg);
            }

            JsonNode body = response.path("body");
            int totalCount = body.path("totalCount").asInt(0);
            JsonNode itemNode = body.path("items").path("item");

            if (itemNode.isMissingNode() || itemNode.isNull()) {
                return new PublicAnimalHospitalFetchResult(totalCount, Collections.emptyList());
            }

            List<HospitalImportCommand> hospitals = new ArrayList<>();
            if (itemNode.isArray()) {
                for (JsonNode item : itemNode) {
                    HospitalImportCommand command = toCommand(item);
                    if (command != null) {
                        hospitals.add(command);
                    }
                }
            } else if (itemNode.isObject()) {
                HospitalImportCommand command = toCommand(itemNode);
                if (command != null) {
                    hospitals.add(command);
                }
            }

            return new PublicAnimalHospitalFetchResult(totalCount, hospitals);
        } catch (RestClientException e) {
            throw new IllegalStateException("동물병원 Open API 호출 중 통신 오류가 발생했습니다.", e);
        } catch (Exception e) {
            throw new IllegalStateException("동물병원 Open API 응답 처리 중 오류가 발생했습니다.", e);
        }
    }

    private HospitalImportCommand toCommand(JsonNode item) {
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

        return HospitalImportCommand.builder()
                .externalPlaceId(buildExternalPlaceId(item))
                .name(nullIfBlank(item.path("BPLC_NM").asText()))
                .address(resolveAddress(item))
                .phone(nullIfBlank(item.path("TELNO").asText()))
                .latitude(latitude)
                .longitude(longitude)
                .licenseDate(nullIfBlank(item.path("LCPMT_YMD").asText()))
                .salesStatusCode(nullIfBlank(item.path("SALS_STTS_CD").asText()))
                .salesStatusName(nullIfBlank(item.path("SALS_STTS_NM").asText()))
                .animalTypes(defaultAnimalTypes())
                .build();
    }

    private String buildExternalPlaceId(JsonNode item) {
        String localGovernmentCode = item.path("OPN_ATMY_GRP_CD").asText("");
        String managementNumber = item.path("MNG_NO").asText("");
        return "public-animal-hospital:" + localGovernmentCode + ":" + managementNumber;
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

    private List<AnimalType> defaultAnimalTypes() {
        return List.of(AnimalType.REPTILE, AnimalType.BIRD);
    }

    private java.util.Optional<String> optional(String value) {
        return StringUtils.hasText(value) ? java.util.Optional.of(value) : java.util.Optional.empty();
    }

    private String nullIfBlank(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
