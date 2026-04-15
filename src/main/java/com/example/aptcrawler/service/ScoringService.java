package com.example.aptcrawler.service;

import com.example.aptcrawler.config.UserProfile;
import com.example.aptcrawler.dto.AptAnnouncementDto;
import com.example.aptcrawler.dto.ScoredAnnouncement;
import com.example.aptcrawler.dto.ScoredAnnouncement.ScoreBreakdown;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScoringService {

    private final UserProfile userProfile;

    public ScoringService(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    public int score(AptAnnouncementDto dto) {
        return scoreWithBreakdown(dto).score();
    }

    public ScoredAnnouncement scoredAnnouncement(AptAnnouncementDto dto) {
        Result r = scoreWithBreakdown(dto);
        return new ScoredAnnouncement(dto, r.score(), r.breakdown());
    }

    public List<ScoredAnnouncement> scoreAndSort(List<AptAnnouncementDto> announcements) {
        return announcements.stream()
                .map(this::scoredAnnouncement)
                .sorted((a, b) -> Integer.compare(b.getScore(), a.getScore()))
                .toList();
    }

    private Result scoreWithBreakdown(AptAnnouncementDto dto) {
        boolean hasSpecialSupply = isPositive(dto.getSpsplyHshldco())
                || (dto.getSpsplyHshldco() == null && dto.getSpsplyRceptBgnde() != null);
        boolean hasLifeFirst = isPositive(dto.getLfeFrstHshldco())
                || (dto.getLfeFrstHshldco() == null && dto.getSpsplyRceptBgnde() != null);

        boolean newlywed = userProfile.isNewlywed() && hasSpecialSupply;
        boolean lifeFirst = userProfile.isFirstTimeBuyer() && hasLifeFirst;

        String region = dto.getSubscrptAreaCodeNm();
        String residentRegion = userProfile.getResidentRegion();
        boolean regionMatch = region != null && residentRegion != null
                && (region.contains(residentRegion) || residentRegion.contains(region));

        boolean smallUnit = hasSmallUnit(dto.getExcluseLttotDtls());

        int score = (newlywed ? 40 : 0) + (lifeFirst ? 30 : 0) + (regionMatch ? 20 : 0) + (smallUnit ? 10 : 0);
        return new Result(score, new ScoreBreakdown(newlywed, lifeFirst, regionMatch, smallUnit));
    }

    private record Result(int score, ScoreBreakdown breakdown) {}

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
