package com.example.aptcrawler.dto;

import java.util.List;

public class ScoredAnnouncement {

    public record ScoreBreakdown(boolean newlywed, boolean lifeFirst, boolean region, boolean smallUnit) {}

    private final AptAnnouncementDto announcement;
    private final int score;
    private final ScoreBreakdown breakdown;
    private List<AptUnitTypeDto> unitTypes = List.of();

    public ScoredAnnouncement(AptAnnouncementDto announcement, int score, ScoreBreakdown breakdown) {
        this.announcement = announcement;
        this.score = score;
        this.breakdown = breakdown;
    }

    public AptAnnouncementDto getAnnouncement() { return announcement; }
    public int getScore() { return score; }
    public ScoreBreakdown getBreakdown() { return breakdown; }
    public List<AptUnitTypeDto> getUnitTypes() { return unitTypes; }
    public void setUnitTypes(List<AptUnitTypeDto> unitTypes) { this.unitTypes = unitTypes; }
}
