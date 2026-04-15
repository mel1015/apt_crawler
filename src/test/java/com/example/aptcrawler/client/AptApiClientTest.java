package com.example.aptcrawler.client;

import com.example.aptcrawler.config.AptProperties;
import com.example.aptcrawler.dto.AptAnnouncementDto;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AptApiClientTest {

    private MockWebServer mockWebServer;
    private AptApiClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        AptProperties props = new AptProperties();
        props.setApiKey("test-key");
        props.setApiUrl(mockWebServer.url("").toString().replaceAll("/$", ""));

        client = new AptApiClient(WebClient.builder().build(), props);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void fetchAnnouncements_배열응답_파싱성공() {
        String json = """
                {
                  "data": [
                    {
                      "PBLANC_NO": "2024-001",
                      "HOUSE_NM": "강남 래미안 분양",
                      "SUBSCRPT_AREA_CODE_NM": "서울",
                      "RCEPT_BGNDE": "2024-11-01",
                      "RCEPT_ENDDE": "2024-11-05",
                      "TOT_SUPLY_HSHLDCO": 300,
                      "SPSPLY_HSHLDCO": "100",
                      "LFE_FRST_HSHLDCO": "50"
                    }
                  ],
                  "totalCount": 1,
                  "page": 1,
                  "perPage": 10
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(json)
                .addHeader("Content-Type", "application/json"));

        List<AptAnnouncementDto> result = client.fetchAnnouncements();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPblancNo()).isEqualTo("2024-001");
        assertThat(result.get(0).getPblancNm()).isEqualTo("강남 래미안 분양");
        assertThat(result.get(0).getSubscrptAreaCodeNm()).isEqualTo("서울");
    }

    @Test
    void fetchAnnouncements_단건응답_파싱성공() {
        String json = """
                {
                  "data": [
                    {
                      "PBLANC_NO": "2024-002",
                      "HOUSE_NM": "서초 아이파크",
                      "SUBSCRPT_AREA_CODE_NM": "서울",
                      "RCEPT_BGNDE": "2024-11-10",
                      "RCEPT_ENDDE": "2024-11-15",
                      "TOT_SUPLY_HSHLDCO": 150
                    }
                  ],
                  "totalCount": 1
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(json)
                .addHeader("Content-Type", "application/json"));

        List<AptAnnouncementDto> result = client.fetchAnnouncements();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPblancNo()).isEqualTo("2024-002");
    }

    @Test
    void fetchAnnouncements_빈응답_빈리스트반환() {
        String json = """
                {
                  "data": [],
                  "totalCount": 0
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(json)
                .addHeader("Content-Type", "application/json"));

        List<AptAnnouncementDto> result = client.fetchAnnouncements();

        assertThat(result).isEmpty();
    }

    @Test
    void fetchAnnouncements_서버오류_예외발생() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        assertThatThrownBy(() -> client.fetchAnnouncements())
                .isInstanceOf(AptApiException.class);
    }
}
