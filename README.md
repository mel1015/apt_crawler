# 청약 알리미

공공데이터포털 청약 API를 활용해 내 조건에 맞는 APT 청약 공고를 매일 자동으로 조회하고, 당첨 확률 점수 순으로 정렬해 Slack으로 알림을 보내는 Spring Boot 애플리케이션입니다.

## 주요 기능

- **자동 조회**: 매일 오전 9시 공공데이터포털 청약 API 자동 호출
- **지역 필터링**: 관심 지역(시/구) 기준으로 공고 필터링
- **당첨 확률 점수**: 사용자 조건 기반으로 공고를 점수 순 정렬
  - 신혼부부 특공 해당 +40점
  - 생애최초 특공 해당 +30점
  - 거주 지역 일치 +20점
  - 전용 85㎡ 이하 포함 +10점
- **Slack 알림**: Block Kit 포맷으로 상위 N건 전송
- **Docker 실행**: docker compose로 간단하게 배포

## 시작하기

### 1. 사전 준비

- [공공데이터포털](https://www.data.go.kr) 에서 **APT 분양정보 조회 서비스** API 키 발급
- Slack 워크스페이스에서 **Incoming Webhook URL** 생성

### 2. 환경 변수 설정

```bash
cp .env.example .env
```

`.env` 파일을 열어 값을 입력합니다:

```env
APT_API_KEY=발급받은_공공데이터포털_API_키
APT_CRON=0 0 9 * * *        # 매일 오전 9시 (기본값)
APT_REGION_1=강남구           # 관심 지역
APT_TOP_N=10                 # Slack에 전송할 최대 공고 수

USER_MONTHLY_INCOME=          # 월 소득 (원, 예: 4000000)
USER_RESIDENT_REGION=         # 거주 지역 (예: 서울특별시 강남구)
USER_IS_NEWLYWED=true
USER_IS_FIRST_TIME_BUYER=true

SLACK_WEBHOOK_URL=https://hooks.slack.com/services/...
```

### 3. 실행

```bash
docker compose up -d
```

로그 확인:
```bash
docker compose logs -f
```

## 개발

```bash
# 컴파일
./gradlew compileJava

# 테스트
./gradlew test

# 특정 테스트
./gradlew test --tests '*ScoringServiceTest'

# jar 빌드
./gradlew bootJar
```

## 기술 스택

- Java 25 / Spring Boot 3.3.5
- Spring WebFlux (WebClient)
- Gradle 9.4.0
- Docker

## 주의사항

- `.env` 파일은 `.gitignore`에 포함되어 있어 Git에 커밋되지 않습니다.
- 관심 지역을 여러 개 설정하려면 `application.yml`의 `apt.regions` 항목에 추가하면 됩니다.
