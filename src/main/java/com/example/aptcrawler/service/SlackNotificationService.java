package com.example.aptcrawler.service;

import com.example.aptcrawler.config.SlackProperties;
import com.example.aptcrawler.dto.AptAnnouncementDto;
import com.example.aptcrawler.dto.AptUnitTypeDto;
import com.example.aptcrawler.dto.ScoredAnnouncement;
import com.example.aptcrawler.dto.ScoredAnnouncement.ScoreBreakdown;
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
            ScoreBreakdown bd = sa.getBreakdown();

            // 기본 정보
            sb.append(String.format(
                "{\"type\":\"section\",\"fields\":["
                + "{\"type\":\"mrkdwn\",\"text\":\"*공고명*\\n%s\"},"
                + "{\"type\":\"mrkdwn\",\"text\":\"*지역*\\n%s\"},"
                + "{\"type\":\"mrkdwn\",\"text\":\"*청약기간*\\n%s ~ %s\"},"
                + "{\"type\":\"mrkdwn\",\"text\":\"*총 세대수*\\n%s세대\"}"
                + "]},",
                escape(a.getPblancNm()),
                escape(a.getSubscrptAreaCodeNm()),
                nvl(a.getRceptBgnde()), nvl(a.getRceptEndde()),
                nvl(a.getTotSuplyHshldco())
            ));

            // 적합도 점수 breakdown
            String scoreText = String.format(
                "*적합도 %d점* | 신혼특공%s 생애최초%s 지역일치%s 85㎡이하%s",
                sa.getScore(),
                bd.newlywed() ? "+40" : "✗",
                bd.lifeFirst() ? "+30" : "✗",
                bd.region() ? "+20" : "✗",
                bd.smallUnit() ? "+10" : "✗"
            );
            sb.append(String.format(
                "{\"type\":\"section\",\"text\":{\"type\":\"mrkdwn\",\"text\":\"%s\"}},",
                escape(scoreText)
            ));

            // 주택형별 공급금액 (최대 5개)
            if (!sa.getUnitTypes().isEmpty()) {
                StringBuilder unitSb = new StringBuilder("*주택형별 공급가격*\n");
                sa.getUnitTypes().stream().limit(5).forEach(u -> {
                    String price = formatManwon(u.getLttotTopAmount());
                    String special = buildSpecialInfo(u);
                    unitSb.append(String.format("• %s (공급%s㎡) | %s | %s세대%s\n",
                        nvl(u.getHouseTy()), nvl(u.getSupAr()), price,
                        nvl(u.getSupHshldco()), special));
                });
                sb.append(String.format(
                    "{\"type\":\"section\",\"text\":{\"type\":\"mrkdwn\",\"text\":\"%s\"}},",
                    escape(unitSb.toString().trim())
                ));
            }

            // 당첨 팁
            String tips = buildTips(sa);
            if (!tips.isEmpty()) {
                sb.append(String.format(
                    "{\"type\":\"section\",\"text\":{\"type\":\"mrkdwn\",\"text\":\"*당첨 팁*\\n%s\"}},",
                    escape(tips)
                ));
            }

            sb.append("{\"type\":\"divider\"},");
        }

        if (sb.charAt(sb.length() - 1) == ',') sb.deleteCharAt(sb.length() - 1);
        sb.append("]}");
        return sb.toString();
    }

    private String buildSpecialInfo(AptUnitTypeDto u) {
        StringBuilder sb = new StringBuilder();
        if (isPositive(u.getNwwdsHshldco())) sb.append(" (신혼").append(u.getNwwdsHshldco()).append("세대");
        else if (isPositive(u.getSpsplyHshldco())) sb.append(" (특공").append(u.getSpsplyHshldco()).append("세대");
        if (isPositive(u.getLfeFrstHshldco())) {
            sb.append(sb.length() > 0 ? "/생애최초" : " (생애최초").append(u.getLfeFrstHshldco()).append("세대");
        }
        if (sb.length() > 0) sb.append(")");
        return sb.toString();
    }

    private String buildTips(ScoredAnnouncement sa) {
        ScoreBreakdown bd = sa.getBreakdown();
        StringBuilder sb = new StringBuilder();
        if (bd.newlywed()) sb.append("• 신혼부부 특공 1순위 신청 가능\n");
        if (bd.lifeFirst()) sb.append("• 생애최초 특공 신청 가능\n");
        if (!bd.newlywed() && !bd.lifeFirst()) sb.append("• 일반공급으로 지원 (청약통장 납입횟수 최대화 권장)\n");
        if (!bd.region()) sb.append("• 거주지역 불일치 — 기타지역 배정물량으로 신청\n");
        if (!bd.smallUnit()) sb.append("• 85㎡ 초과 평형만 있음 — 중대형 분양가 확인 필요\n");
        return sb.toString().trim();
    }

    private String formatManwon(String manwon) {
        if (manwon == null || manwon.isBlank()) return "-";
        try {
            long v = Long.parseLong(manwon.replaceAll("[^0-9]", "")); // 만원 단위
            if (v >= 10_000) {
                long uk = v / 10_000;
                long rem = (v % 10_000) / 1_000;
                return rem > 0 ? uk + "억" + rem + "천만원" : uk + "억원";
            }
            return v + "만원";
        } catch (NumberFormatException e) {
            return manwon;
        }
    }

    private boolean isPositive(String v) {
        if (v == null) return false;
        try { return Integer.parseInt(v.trim()) > 0; } catch (NumberFormatException e) { return false; }
    }

    private String escape(String s) {
        return s == null ? "-" : s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private String nvl(String s) {
        return s == null ? "-" : s;
    }
}
