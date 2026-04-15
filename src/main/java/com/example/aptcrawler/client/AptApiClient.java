package com.example.aptcrawler.client;

import com.example.aptcrawler.config.AptProperties;
import com.example.aptcrawler.dto.AptAnnouncementDto;
import com.example.aptcrawler.dto.AptUnitTypeDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Component
public class AptApiClient {

    private static final Logger log = LoggerFactory.getLogger(AptApiClient.class);
    private static final String PATH = "/getAPTLttotPblancDetail";
    private static final String MODEL_PATH = "/getAPTLttotPblancMdl";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final WebClient webClient;
    private final AptProperties aptProperties;

    public AptApiClient(WebClient webClient, AptProperties aptProperties) {
        this.webClient = webClient;
        this.aptProperties = aptProperties;
    }

    public List<AptAnnouncementDto> fetchAnnouncements() {
        String today = LocalDate.now().format(DATE_FMT);
        try {
            JsonNode root = webClient.get()
                    .uri(aptProperties.getApiUrl() + PATH, uriBuilder -> uriBuilder
                            .queryParam("serviceKey", aptProperties.getApiKey())
                            .queryParam("page", 1)
                            .queryParam("perPage", 100)
                            .queryParam("cond[RCEPT_ENDDE::GTE]", today)
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            return parseItems(root);
        } catch (Exception e) {
            log.error("청약 API 호출 실패", e);
            throw new AptApiException("청약 API 호출 실패: " + e.getMessage(), e);
        }
    }

    public List<AptUnitTypeDto> fetchUnitTypes(String houseManageNo) {
        try {
            JsonNode root = webClient.get()
                    .uri(aptProperties.getApiUrl() + MODEL_PATH, uriBuilder -> uriBuilder
                            .queryParam("serviceKey", aptProperties.getApiKey())
                            .queryParam("page", 1)
                            .queryParam("perPage", 50)
                            .queryParam("cond[HOUSE_MANAGE_NO::EQ]", houseManageNo)
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            if (root == null) return Collections.emptyList();
            JsonNode data = root.path("data");
            if (data.isMissingNode() || !data.isArray() || data.isEmpty()) return Collections.emptyList();
            return MAPPER.readerForListOf(AptUnitTypeDto.class).readValue(data);
        } catch (Exception e) {
            log.warn("주택형 API 호출 실패 ({}): {}", houseManageNo, e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<AptAnnouncementDto> parseItems(JsonNode root) {
        if (root == null) return Collections.emptyList();

        JsonNode data = root.path("data");
        if (data.isMissingNode() || data.isNull() || !data.isArray()) return Collections.emptyList();

        try {
            return MAPPER.readerForListOf(AptAnnouncementDto.class).readValue(data);
        } catch (Exception e) {
            log.warn("DTO 변환 오류", e);
            return Collections.emptyList();
        }
    }
}
