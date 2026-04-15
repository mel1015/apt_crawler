package com.example.aptcrawler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties("apt")
public class AptProperties {
    private String apiKey;
    private String apiUrl;
    private String regions;
    private int topN;
    private long maxPrice;

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getApiUrl() { return apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }

    public String getRegions() { return regions; }
    public void setRegions(String regions) { this.regions = regions; }

    public List<String> getRegionList() {
        if (regions == null || regions.isBlank()) return List.of();
        return List.of(regions.split(",")).stream()
                .map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    public int getTopN() { return topN; }
    public void setTopN(int topN) { this.topN = topN; }

    public long getMaxPrice() { return maxPrice; }
    public void setMaxPrice(long maxPrice) { this.maxPrice = maxPrice; }
}
