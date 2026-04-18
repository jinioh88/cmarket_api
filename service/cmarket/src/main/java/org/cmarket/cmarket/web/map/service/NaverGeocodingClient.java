package org.cmarket.cmarket.web.map.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Service
public class NaverGeocodingClient {

    private static final String GEOCODE_URL = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String clientId;
    private final String clientSecret;

    public NaverGeocodingClient(
            ObjectMapper objectMapper,
            @Value("${naver.geocoding.client-id:}") String clientId,
            @Value("${naver.geocoding.client-secret:}") String clientSecret
    ) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    /**
     * 주소를 좌표(위도, 경도)로 변환합니다.
     *
     * @param address 도로명 또는 지번 주소
     * @return [longitude, latitude] 배열, 실패 시 null
     */
    public double[] geocode(String address) {
        if (!StringUtils.hasText(address) || !StringUtils.hasText(clientId)) {
            return null;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-NCP-APIGW-API-KEY-ID", clientId);
            headers.set("X-NCP-APIGW-API-KEY", clientSecret);

            String url = GEOCODE_URL + "?query=" + address;
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode addresses = root.path("addresses");

            if (addresses.isArray() && !addresses.isEmpty()) {
                JsonNode first = addresses.get(0);
                double longitude = Double.parseDouble(first.path("x").asText());
                double latitude = Double.parseDouble(first.path("y").asText());
                return new double[]{longitude, latitude};
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
