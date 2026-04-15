package com.example.aptcrawler.client;

import com.example.aptcrawler.config.AptProperties;
import com.example.aptcrawler.dto.AptAnnouncementDto;
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
    private static final String PATH = "/getRLTotmHousingLttotPblancDetail";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
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
                            .queryParam("pageNo", 1)
                            .queryParam("numOfRows", 100)
                            .queryParam("startSubscriptDate", today)
                            .queryParam("_type", "json")
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

    private List<AptAnnouncementDto> parseItems(JsonNode root) {
        if (root == null) return Collections.emptyList();

        JsonNode item = root.path("response").path("body").path("items").path("item");
        if (item.isMissingNode() || item.isNull()) return Collections.emptyList();

        try {
            if (item.isArray()) {
                return MAPPER.readerForListOf(AptAnnouncementDto.class).readValue(item);
            } else {
                return List.of(MAPPER.treeToValue(item, AptAnnouncementDto.class));
            }
        } catch (Exception e) {
            log.warn("DTO 변환 오류", e);
            return Collections.emptyList();
        }
    }
}
