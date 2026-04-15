package com.example.aptcrawler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("user")
public class UserProfile {
    private long monthlyIncome;
    private String residentRegion;
    private boolean newlywed;
    private boolean firstTimeBuyer;

    public long getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(long monthlyIncome) { this.monthlyIncome = monthlyIncome; }

    public String getResidentRegion() { return residentRegion; }
    public void setResidentRegion(String residentRegion) { this.residentRegion = residentRegion; }

    public boolean isNewlywed() { return newlywed; }
    public void setNewlywed(boolean newlywed) { this.newlywed = newlywed; }

    public boolean isFirstTimeBuyer() { return firstTimeBuyer; }
    public void setFirstTimeBuyer(boolean firstTimeBuyer) { this.firstTimeBuyer = firstTimeBuyer; }
}
