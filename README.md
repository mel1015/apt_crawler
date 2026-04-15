# 청약 알리미

내 조건에 맞는 APT 청약 공고를 매일 자동으로 조회하고, 당첨 확률 점수 순으로 정렬해 Slack으로 알림을 보내는 Spring Boot 애플리케이션입니다. GitHub Actions로 매일 KST 09:00에 자동 실행됩니다.

## 주요 기능

- **자동 조회**: 매일 오전 9시 GitHub Actions로 청약홈 ApplyhomeInfoDetailSvc API 호출
- **지역 필터링**: 관심 지역(쉼표 구분) 기준으로 공고 필터링
- **당첨 확률 점수**: 사용자 조건 기반으로 공고를 점수 순 정렬
  - 신혼부부 특공 해당 +40점
  - 생애최초 특공 해당 +30점
  - 거주 지역 일치 +20점
  - 전용 85㎡ 이하 포함 +10점
- **분양가 필터링**: 설정한 최대 금액 초과 공고 제외 (`APT_MAX_PRICE`)
- **Slack 알림**: Block Kit 포맷으로 상위 N건 전송 (적합도 점수 breakdown, 주택형별 공급가격, 당첨 팁 포함)

## 시작하기

### 1. 사전 준비

- [공공데이터포털](https://www.data.go.kr) 에서 **청약홈 ApplyhomeInfoDetailSvc** API 키 발급
- Slack 워크스페이스에서 **Incoming Webhook URL** 생성

### 2. GitHub Secrets / Variables 등록

GitHub 저장소 → **Settings → Secrets and variables → Actions**

**Secrets** (민감 정보)

| 키 | 설명 | 예시 |
|---|---|---|
| `APT_API_KEY` | 공공데이터포털 API 키 | |
| `APT_REGIONS` | 관심 지역 (쉼표 구분, 시/도 약칭) | `경기,서울` |
| `USER_RESIDENT_REGION` | 거주 지역 | `경기` |
| `SLACK_WEBHOOK_URL` | Slack Incoming Webhook URL | |

**Variables** (일반 설정)

| 키 | 설명 | 예시 |
|---|---|---|
| `APT_TOP_N` | Slack에 전송할 최대 공고 수 | `10` |
| `APT_MAX_PRICE` | 분양가 상한 (원 단위, 0이면 비활성) | `600000000` |
| `USER_IS_NEWLYWED` | 신혼부부 여부 | `true` |
| `USER_IS_FIRST_TIME_BUYER` | 생애최초 여부 | `true` |
| `USER_MONTHLY_INCOME` | 월 소득 (원) | `4000000` |

### 3. 실행 확인

등록 후 **Actions → 청약 알리미 일일 실행 → Run workflow** 버튼으로 수동 테스트.
이후 매일 KST 09:00 자동 실행됩니다.

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
- GitHub Actions

## 주의사항

- 관심 지역은 `APT_REGIONS=경기,서울,부산` 형식으로 쉼표 구분. API가 시/도 약칭(`경기`, `서울` 등)을 반환하므로 약칭 기준으로 작성.
- `APT_MAX_PRICE`는 원 단위. 6억 이하 필터링 → `600000000`.
