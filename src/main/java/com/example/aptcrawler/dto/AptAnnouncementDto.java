package com.example.aptcrawler.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AptAnnouncementDto {

    @JsonProperty("PBLANC_NO")
    private String pblancNo;

    @JsonProperty("PBLANC_NM")
    private String pblancNm;

    @JsonProperty("SUBSCRPT_AREA_CODE_NM")
    private String subscrptAreaCodeNm;

    @JsonProperty("RCEPT_BGNDE")
    private String rceptBgnde;

    @JsonProperty("RCEPT_ENDDE")
    private String rceptEndde;

    @JsonProperty("TOT_SUPLY_HSHLDCO")
    private String totSuplyHshldco;

    @JsonProperty("SPSPLY_HSHLDCO")
    private String spsplyHshldco;

    @JsonProperty("LFE_FRST_HSHLDCO")
    private String lfeFrstHshldco;

    @JsonProperty("EXCLUSE_LTTOT_DTLS")
    private String excluseLttotDtls;

    @JsonProperty("LTTOT_TOP_AMOUNT")
    private String lttotTopAmount;

    public String getPblancNo() { return pblancNo; }
    public void setPblancNo(String pblancNo) { this.pblancNo = pblancNo; }

    public String getPblancNm() { return pblancNm; }
    public void setPblancNm(String pblancNm) { this.pblancNm = pblancNm; }

    public String getSubscrptAreaCodeNm() { return subscrptAreaCodeNm; }
    public void setSubscrptAreaCodeNm(String subscrptAreaCodeNm) { this.subscrptAreaCodeNm = subscrptAreaCodeNm; }

    public String getRceptBgnde() { return rceptBgnde; }
    public void setRceptBgnde(String rceptBgnde) { this.rceptBgnde = rceptBgnde; }

    public String getRceptEndde() { return rceptEndde; }
    public void setRceptEndde(String rceptEndde) { this.rceptEndde = rceptEndde; }

    public String getTotSuplyHshldco() { return totSuplyHshldco; }
    public void setTotSuplyHshldco(String totSuplyHshldco) { this.totSuplyHshldco = totSuplyHshldco; }

    public String getSpsplyHshldco() { return spsplyHshldco; }
    public void setSpsplyHshldco(String spsplyHshldco) { this.spsplyHshldco = spsplyHshldco; }

    public String getLfeFrstHshldco() { return lfeFrstHshldco; }
    public void setLfeFrstHshldco(String lfeFrstHshldco) { this.lfeFrstHshldco = lfeFrstHshldco; }

    public String getExcluseLttotDtls() { return excluseLttotDtls; }
    public void setExcluseLttotDtls(String excluseLttotDtls) { this.excluseLttotDtls = excluseLttotDtls; }

    public String getLttotTopAmount() { return lttotTopAmount; }
    public void setLttotTopAmount(String lttotTopAmount) { this.lttotTopAmount = lttotTopAmount; }
}
