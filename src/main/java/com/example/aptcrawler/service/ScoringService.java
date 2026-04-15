package com.example.aptcrawler.service;

import com.example.aptcrawler.config.UserProfile;
import com.example.aptcrawler.dto.AptAnnouncementDto;
import com.example.aptcrawler.dto.ScoredAnnouncement;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScoringService {

    private final UserProfile userProfile;

    public ScoringService(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    public int score(AptAnnouncementDto dto) {
        int score = 0;

        if (userProfile.isNewlywed() && isPositive(dto.getSpsplyHshldco())) {
            score += 40;
        }
        if (userProfile.isFirstTimeBuyer() && isPositive(dto.getLfeFrstHshldco())) {
            score += 30;
        }
        String region = dto.getSubscrptAreaCodeNm();
        if (region != null && userProfile.getResidentRegion() != null
                && region.contains(userProfile.getResidentRegion())) {
            score += 20;
        }
        if (hasSmallUnit(dto.getExcluseLttotDtls())) {
            score += 10;
        }
        return score;
    }

    public List<ScoredAnnouncement> scoreAndSort(List<AptAnnouncementDto> announcements) {
        return announcements.stream()
                .map(a -> new ScoredAnnouncement(a, score(a)))
                .sorted((a, b) -> Integer.compare(b.getScore(), a.getScore()))
                .toList();
    }

    private boolean isPositive(String value) {
        if (value == null) return false;
        try {
            return Integer.parseInt(value.trim()) > 0;
        } catch (NumberFormatException e) {
            return "Y".equalsIgnoreCase(value.trim());
        }
    }

    private boolean hasSmallUnit(String excluseDetails) {
        if (excluseDetails == null) return false;
        for (String part : excluseDetails.split("[,/\\s]+")) {
            String numStr = part.replaceAll("[^0-9]", "");
            if (!numStr.isEmpty()) {
                try {
                    if (Integer.parseInt(numStr) <= 85) return true;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return false;
    }
}
