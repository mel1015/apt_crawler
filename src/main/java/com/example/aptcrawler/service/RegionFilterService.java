package com.example.aptcrawler.service;

import com.example.aptcrawler.config.AptProperties;
import com.example.aptcrawler.dto.AptAnnouncementDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegionFilterService {

    private final AptProperties aptProperties;

    public RegionFilterService(AptProperties aptProperties) {
        this.aptProperties = aptProperties;
    }

    public List<AptAnnouncementDto> filter(List<AptAnnouncementDto> announcements) {
        List<String> regions = aptProperties.getRegionList();
        if (regions.isEmpty()) {
            return announcements;
        }
        return announcements.stream()
                .filter(a -> a.getSubscrptAreaCodeNm() != null
                        && regions.stream().anyMatch(r -> a.getSubscrptAreaCodeNm().contains(r)))
                .toList();
    }
}
