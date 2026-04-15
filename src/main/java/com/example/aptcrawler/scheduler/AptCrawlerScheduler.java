package com.example.aptcrawler.scheduler;

import com.example.aptcrawler.client.AptApiClient;
import com.example.aptcrawler.client.AptApiException;
import com.example.aptcrawler.config.AptProperties;
import com.example.aptcrawler.dto.AptAnnouncementDto;
import com.example.aptcrawler.dto.ScoredAnnouncement;
import com.example.aptcrawler.service.RegionFilterService;
import com.example.aptcrawler.service.ScoringService;
import com.example.aptcrawler.service.SlackNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class AptCrawlerScheduler {

    private static final Logger log = LoggerFactory.getLogger(AptCrawlerScheduler.class);

    private final AptApiClient aptApiClient;
    private final RegionFilterService regionFilterService;
    private final ScoringService scoringService;
    private final SlackNotificationService slackNotificationService;
    private final AptProperties aptProperties;
    private final Set<String> processedIds = new HashSet<>();

    public AptCrawlerScheduler(AptApiClient aptApiClient,
                                RegionFilterService regionFilterService,
                                ScoringService scoringService,
                                SlackNotificationService slackNotificationService,
                                AptProperties aptProperties) {
        this.aptApiClient = aptApiClient;
        this.regionFilterService = regionFilterService;
        this.scoringService = scoringService;
        this.slackNotificationService = slackNotificationService;
        this.aptProperties = aptProperties;
    }

    public void run() {
        log.info("청약 공고 조회 시작");
        try {
            List<AptAnnouncementDto> all = aptApiClient.fetchAnnouncements();
            List<AptAnnouncementDto> filtered = regionFilterService.filter(all);

            List<AptAnnouncementDto> newOnes = filtered.stream()
                    .filter(a -> a.getPblancNo() != null && !processedIds.contains(a.getPblancNo()))
                    .toList();

            if (newOnes.isEmpty()) {
                log.info("조건에 맞는 신규 공고 없음");
                return;
            }

            List<ScoredAnnouncement> scored = scoringService.scoreAndSort(newOnes);

            scored.forEach(sa -> {
                String key = sa.getAnnouncement().getHouseManageNo();
                if (key != null) sa.setUnitTypes(aptApiClient.fetchUnitTypes(key));
            });

            long maxPrice = aptProperties.getMaxPrice();
            List<ScoredAnnouncement> priceFiltered = maxPrice > 0
                    ? scored.stream().filter(sa -> hasAffordableUnit(sa, maxPrice)).toList()
                    : scored;

            List<ScoredAnnouncement> topN = priceFiltered.stream()
                    .limit(aptProperties.getTopN())
                    .toList();

            slackNotificationService.send(topN);
            newOnes.forEach(a -> processedIds.add(a.getPblancNo()));
            log.info("청약 공고 {}건 알림 전송 완료", topN.size());

        } catch (AptApiException e) {
            log.error("API 오류 발생", e);
            slackNotificationService.sendError(e.getMessage());
        }
    }

    private boolean hasAffordableUnit(ScoredAnnouncement sa, long maxPrice) {
        if (sa.getUnitTypes().isEmpty()) return true;
        long maxManwon = maxPrice / 10_000; // maxPrice는 원, API는 만원 단위
        return sa.getUnitTypes().stream().anyMatch(u -> {
            String top = u.getLttotTopAmount();
            if (top == null || top.isBlank()) return true;
            try { return Long.parseLong(top.replaceAll("[^0-9]", "")) <= maxManwon; }
            catch (NumberFormatException e) { return true; }
        });
    }
}
