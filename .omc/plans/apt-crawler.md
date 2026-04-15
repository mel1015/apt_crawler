# 청약 알리미 작업 플랜

## 요구사항 요약

- Spring Boot 3.x / Java 17, Gradle
- 청약홈 공공 API(공공데이터포털) 매일 1회 자동 조회
- 지역(시/구) 필터링
- 신혼부부·생애최초·소득·거주지역 기반 당첨 확률 점수 정렬
- Slack Incoming Webhook 알림
- Docker + docker-compose 실행
- 개인정보는 `.env` + `.gitignore`로 GitHub 보호

## 수용 기준

1. 매일 지정 시각(기본 09:00 KST)에 청약 API 자동 호출
2. `apt.regions` 설정과 공고 지역명이 일치하는 공고만 처리
3. UserProfile 기반 특공 적합도 점수 계산 후 내림차순 정렬
4. 신규 공고 있으면 상위 N건 Slack Block Kit 포맷 전송
5. 신규 공고 없으면 알림 미전송
6. API 키·Webhook URL·개인 프로필 수치는 `.env`에만 존재, `.gitignore` 등록
7. `docker compose up -d`로 실행 가능

## 개인정보 보호 전략

- `.env` — 실제 민감 값 (API키, Webhook URL, 소득, 지역 등)
- `.env.example` — 키 이름만 있고 값 비워진 예시 (Git 추적)
- `application.yml` — `${ENV_VAR}` 참조만, 기본값 없음
- `.gitignore` — `.env` 명시

## 점수 계산 모델

| 조건 | 점수 |
|------|------|
| 신혼부부 특공 유형 포함 | +40 |
| 생애최초 특공 유형 포함 | +30 |
| 공고 지역 = 거주 지역 일치 | +20 |
| 전용 85㎡ 이하 | +10 |

## 프로젝트 구조

```
apt-crawler/
├── src/main/java/com/example/aptcrawler/
│   ├── AptCrawlerApplication.java
│   ├── client/AptApiClient.java
│   ├── config/AppConfig.java              # WebClient Bean
│   ├── config/UserProfile.java            # @ConfigurationProperties("user")
│   ├── config/AptProperties.java          # @ConfigurationProperties("apt")
│   ├── dto/AptAnnouncementDto.java
│   ├── dto/SlackMessage.java
│   ├── scheduler/AptCrawlerScheduler.java
│   └── service/
│       ├── RegionFilterService.java
│       ├── ScoringService.java
│       └── SlackNotificationService.java
├── src/main/resources/
│   └── application.yml
├── src/test/java/com/example/aptcrawler/
│   ├── client/AptApiClientTest.java
│   ├── service/ScoringServiceTest.java
│   └── service/RegionFilterServiceTest.java
├── .env.example
├── .gitignore
├── Dockerfile
├── docker-compose.yml
└── build.gradle
```

## 구현 단계

### 1. build.gradle
- spring-boot-starter-webflux (WebClient)
- spring-boot-starter-json
- lombok
- spring-boot-configuration-processor
- spring-boot-starter-test + okhttp3:mockwebserver

### 2. application.yml
```yaml
apt:
  api-key: ${APT_API_KEY}
  api-url: https://apis.data.go.kr/B552555/APTRentSvc
  cron: ${APT_CRON:0 0 9 * * *}
  regions:
    - ${APT_REGION_1:강남구}
  top-n: ${APT_TOP_N:10}

user:
  monthly-income: ${USER_MONTHLY_INCOME}
  resident-region: ${USER_RESIDENT_REGION}
  is-newlywed: ${USER_IS_NEWLYWED:true}
  is-first-time-buyer: ${USER_IS_FIRST_TIME_BUYER:true}

slack:
  webhook-url: ${SLACK_WEBHOOK_URL}
```

### 3. .env.example
```
APT_API_KEY=
APT_CRON=0 0 9 * * *
APT_REGION_1=강남구
APT_TOP_N=10
USER_MONTHLY_INCOME=4133867
USER_RESIDENT_REGION=서울특별시 강남구
USER_IS_NEWLYWED=true
USER_IS_FIRST_TIME_BUYER=true
SLACK_WEBHOOK_URL=
```

### 4. AptApiClient
- 공공데이터포털 APT 분양정보 조회 서비스 (`getRLTotmHousingLttotPblancDetail`)
- WebClient GET, 파라미터: serviceKey, pageNo, numOfRows, startSubscriptDate(오늘)
- 응답 XML/JSON → List<AptAnnouncementDto>

### 5. AptAnnouncementDto 주요 필드
- pblancNo (공고번호)
- pblancNm (공고명)
- subscrptAreaCodeNm (지역명)
- rceptBgnde / rceptEndde (청약 기간)
- totSuplyHshldco (총 공급 세대수)
- spsplyHshldco (특별공급 세대수)
- spcltRdnEarthAt (신혼부부 특공 여부)
- lttotTopAmount (분양가)

### 6. ScoringService
```java
int score = 0;
if (신혼부부 특공 포함) score += 40;
if (생애최초 특공 포함) score += 30;
if (공고지역.contains(거주지역)) score += 20;
if (전용면적 <= 85) score += 10;
```

### 7. SlackNotificationService
- Block Kit: 헤더 + 섹션(공고명, 지역, 기간, 세대수, 점수)
- 에러 시 `[오류]` 메시지 별도 전송

### 8. Dockerfile
```dockerfile
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY . .
RUN ./gradlew bootJar -x test

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 9. docker-compose.yml
```yaml
services:
  apt-crawler:
    build: .
    env_file: .env
    restart: unless-stopped
```

## 리스크 및 대응

| 리스크 | 대응 |
|--------|------|
| API 일일 호출 한도 초과 | 에러 시 Slack [오류] 알림 |
| 공공 API 응답 구조 변경 | @JsonIgnoreProperties(ignoreUnknown=true) |
| 중복 알림 | 당일 공고 ID Set 메모리 캐시 |
| .env 실수 커밋 | .gitignore 최상단 명시 |

## 검증 단계

1. AptApiClientTest — MockWebServer 응답 파싱
2. ScoringServiceTest — 신혼+생애최초 조합 점수 검증
3. RegionFilterServiceTest — 지역 매칭
4. cron `0 * * * * *`으로 Slack 실제 수신 E2E 확인
5. .env 없이 실행 시 명확한 에러 확인
