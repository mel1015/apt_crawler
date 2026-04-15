package com.example.aptcrawler.service;

import com.example.aptcrawler.config.SlackProperties;
import com.example.aptcrawler.dto.AptAnnouncementDto;
import com.example.aptcrawler.dto.ScoredAnnouncement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class SlackNotificationService {

    private static final Logger log = LoggerFactory.getLogger(SlackNotificationService.class);

    private final WebClient webClient;
    private final SlackProperties slackProperties;

    public SlackNotificationService(WebClient webClient, SlackProperties slackProperties) {
        this.webClient = webClient;
        this.slackProperties = slackProperties;
    }

    public void send(List<ScoredAnnouncement> announcements) {
        if (announcements.isEmpty()) {
            log.info("신규 청약 공고 없음 - Slack 알림 생략");
            return;
        }
        postToSlack(buildPayload(announcements));
    }

    public void sendError(String message) {
        postToSlack("{\"text\":\"[오류] 청약 알리미: " + message + "\"}");
    }

    private void postToSlack(String payload) {
        try {
            webClient.post()
                    .uri(slackProperties.getWebhookUrl())
                    .header("Content-Type", "application/json")
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            log.error("Slack 전송 실패", e);
        }
    }

    private String buildPayload(List<ScoredAnnouncement> announcements) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"blocks\":[");
        sb.append("{\"type\":\"header\",\"text\":{\"type\":\"plain_text\",\"text\":\"오늘의 청약 공고 (")
          .append(announcements.size()).append("건)\"}},");

        for (ScoredAnnouncement sa : announcements) {
            AptAnnouncementDto a = sa.getAnnouncement();
            sb.append(String.format(
                "{\"type\":\"section\",\"fields\":["
                + "{\"type\":\"mrkdwn\",\"text\":\"*공고명*\\n%s\"},"
                + "{\"type\":\"mrkdwn\",\"text\":\"*지역*\\n%s\"},"
                + "{\"type\":\"mrkdwn\",\"text\":\"*청약기간*\\n%s ~ %s\"},"
                + "{\"type\":\"mrkdwn\",\"text\":\"*총 세대수*\\n%s세대\"},"
                + "{\"type\":\"mrkdwn\",\"text\":\"*적합도 점수*\\n%d점\"}"
                + "]},",
                escape(a.getPblancNm()),
                escape(a.getSubscrptAreaCodeNm()),
                nvl(a.getRceptBgnde()), nvl(a.getRceptEndde()),
                nvl(a.getTotSuplyHshldco()),
                sa.getScore()
            ));
            sb.append("{\"type\":\"divider\"},");
        }

        if (sb.charAt(sb.length() - 1) == ',') sb.deleteCharAt(sb.length() - 1);
        sb.append("]}");
        return sb.toString();
    }

    private String escape(String s) {
        return s == null ? "-" : s.replace("\"", "\\\"").replace("\n", " ");
    }

    private String nvl(String s) {
        return s == null ? "-" : s;
    }
}
