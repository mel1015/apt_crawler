package com.example.aptcrawler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties("apt")
public class AptProperties {
    private String apiKey;
    private String apiUrl;
    private String cron;
    private List<String> regions;
    private int topN;

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getApiUrl() { return apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }

    public String getCron() { return cron; }
    public void setCron(String cron) { this.cron = cron; }

    public List<String> getRegions() { return regions; }
    public void setRegions(List<String> regions) { this.regions = regions; }

    public int getTopN() { return topN; }
    public void setTopN(int topN) { this.topN = topN; }
}
