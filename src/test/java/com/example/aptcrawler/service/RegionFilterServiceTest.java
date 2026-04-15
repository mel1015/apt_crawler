package com.example.aptcrawler.service;

import com.example.aptcrawler.config.AptProperties;
import com.example.aptcrawler.dto.AptAnnouncementDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RegionFilterServiceTest {

    private RegionFilterService service;

    @BeforeEach
    void setUp() {
        AptProperties props = new AptProperties();
        props.setRegions(List.of("강남구"));
        service = new RegionFilterService(props);
    }

    private AptAnnouncementDto dto(String region) {
        AptAnnouncementDto d = new AptAnnouncementDto();
        d.setSubscrptAreaCodeNm(region);
        return d;
    }

    @Test
    void 강남구_공고는_통과() {
        List<AptAnnouncementDto> result = service.filter(List.of(dto("서울특별시 강남구")));
        assertThat(result).hasSize(1);
    }

    @Test
    void 성남시_공고는_제외() {
        List<AptAnnouncementDto> result = service.filter(List.of(dto("경기도 성남시")));
        assertThat(result).isEmpty();
    }

    @Test
    void 혼합_목록_필터링() {
        List<AptAnnouncementDto> input = List.of(
                dto("서울특별시 강남구"),
                dto("경기도 성남시"),
                dto("서울특별시 강남구 삼성동")
        );
        List<AptAnnouncementDto> result = service.filter(input);
        assertThat(result).hasSize(2);
    }

    @Test
    void 지역설정_비어있으면_전체반환() {
        AptProperties props = new AptProperties();
        props.setRegions(List.of());
        RegionFilterService emptyService = new RegionFilterService(props);

        List<AptAnnouncementDto> result = emptyService.filter(List.of(dto("경기도 수원시")));
        assertThat(result).hasSize(1);
    }
}
