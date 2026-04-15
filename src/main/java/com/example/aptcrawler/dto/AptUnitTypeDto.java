package com.example.aptcrawler.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AptUnitTypeDto {

    @JsonProperty("HOUSE_TY")
    private String houseTy;

    @JsonProperty("SUPLY_AR")
    private String supAr;

    @JsonProperty("SUPLY_HSHLDCO")
    private String supHshldco;

    @JsonProperty("SPSPLY_HSHLDCO")
    private String spsplyHshldco;

    @JsonProperty("NWWDS_HSHLDCO")
    private String nwwdsHshldco;

    @JsonProperty("LFE_FRST_HSHLDCO")
    private String lfeFrstHshldco;

    @JsonProperty("LTTOT_TOP_AMOUNT")
    private String lttotTopAmount;

    public String getHouseTy() { return houseTy; }
    public void setHouseTy(String houseTy) { this.houseTy = houseTy; }

    public String getSupAr() { return supAr; }
    public void setSupAr(String supAr) { this.supAr = supAr; }

    public String getSupHshldco() { return supHshldco; }
    public void setSupHshldco(String supHshldco) { this.supHshldco = supHshldco; }

    public String getSpsplyHshldco() { return spsplyHshldco; }
    public void setSpsplyHshldco(String spsplyHshldco) { this.spsplyHshldco = spsplyHshldco; }

    public String getNwwdsHshldco() { return nwwdsHshldco; }
    public void setNwwdsHshldco(String nwwdsHshldco) { this.nwwdsHshldco = nwwdsHshldco; }

    public String getLfeFrstHshldco() { return lfeFrstHshldco; }
    public void setLfeFrstHshldco(String lfeFrstHshldco) { this.lfeFrstHshldco = lfeFrstHshldco; }

    public String getLttotTopAmount() { return lttotTopAmount; }
    public void setLttotTopAmount(String lttotTopAmount) { this.lttotTopAmount = lttotTopAmount; }
}
