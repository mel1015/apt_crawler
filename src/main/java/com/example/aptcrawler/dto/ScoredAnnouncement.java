package com.example.aptcrawler.dto;

public class ScoredAnnouncement {
    private final AptAnnouncementDto announcement;
    private final int score;

    public ScoredAnnouncement(AptAnnouncementDto announcement, int score) {
        this.announcement = announcement;
        this.score = score;
    }

    public AptAnnouncementDto getAnnouncement() { return announcement; }
    public int getScore() { return score; }
}
