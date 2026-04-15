package com.example.aptcrawler.service;

import com.example.aptcrawler.config.UserProfile;
import com.example.aptcrawler.dto.AptAnnouncementDto;
import com.example.aptcrawler.dto.ScoredAnnouncement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScoringServiceTest {

    private ScoringService service;

    @BeforeEach
    void setUp() {
        UserProfile profile = new UserProfile();
        profile.setMonthlyIncome(4133867L);
        profile.setResidentRegion("서울특별시 강남구");
        profile.setNewlywed(true);
        profile.setFirstTimeBuyer(true);
        service = new ScoringService(profile);
    }

    private AptAnnouncementDto dto(String region, String spsply, String lfeFrst, String excluse) {
        AptAnnouncementDto d = new AptAnnouncementDto();
        d.setSubscrptAreaCodeNm(region);
        d.setSpsplyHshldco(spsply);
        d.setLfeFrstHshldco(lfeFrst);
        d.setExcluseLttotDtls(excluse);
        return d;
    }

    @Test
    void 모든조건_충족시_100점() {
        AptAnnouncementDto d = dto("서울특별시 강남구", "100", "50", "59A/84B");
        assertThat(service.score(d)).isEqualTo(100);
    }

    @Test
    void 아무조건_미충족시_0점() {
        AptAnnouncementDto d = dto("경기도 수원시", "0", "0", "120A");
        assertThat(service.score(d)).isEqualTo(0);
    }

    @Test
    void 신혼부부만_해당시_40점() {
        AptAnnouncementDto d = dto("경기도 수원시", "50", "0", "120A");
        assertThat(service.score(d)).isEqualTo(40);
    }

    @Test
    void 생애최초만_해당시_30점() {
        AptAnnouncementDto d = dto("경기도 수원시", "0", "30", "120A");
        assertThat(service.score(d)).isEqualTo(30);
    }

    @Test
    void 신혼부부_아닌경우_신혼특공점수_미적용() {
        UserProfile nonNewlywed = new UserProfile();
        nonNewlywed.setResidentRegion("경기도 수원시");
        nonNewlywed.setNewlywed(false);
        nonNewlywed.setFirstTimeBuyer(false);
        ScoringService svc = new ScoringService(nonNewlywed);

        AptAnnouncementDto d = dto("경기도 수원시", "100", "50", "120A");
        assertThat(svc.score(d)).isEqualTo(20); // 지역일치만 +20 (120㎡은 85 초과)
    }

    @Test
    void 점수기준_내림차순_정렬() {
        List<AptAnnouncementDto> list = List.of(
                dto("경기도 수원시", "50", "0", "120A"),   // 40점
                dto("서울특별시 강남구", "100", "50", "59A"), // 100점
                dto("경기도 수원시", "0", "30", "120A")    // 30점
        );
        List<ScoredAnnouncement> sorted = service.scoreAndSort(list);
        assertThat(sorted.get(0).getScore()).isEqualTo(100);
        assertThat(sorted.get(1).getScore()).isEqualTo(40);
        assertThat(sorted.get(2).getScore()).isEqualTo(30);
    }
}
