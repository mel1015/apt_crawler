# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# 컴파일
./gradlew compileJava

# 전체 테스트
./gradlew test

# 특정 테스트 클래스
./gradlew test --tests '*ScoringServiceTest'
./gradlew test --tests '*AptApiClientTest'
./gradlew test --tests '*RegionFilterServiceTest'

# 빌드 (jar 생성)
./gradlew bootJar

# Docker 실행 (먼저 .env.example → .env 복사 후 값 입력)
docker compose up -d
docker compose logs -f
```

## 아키텍처

매일 09:00 KST에 공공데이터포털 APT 청약 API를 조회하고, 조건에 맞는 공고를 Slack으로 알림하는 Spring Boot 배치 애플리케이션.

### 실행 흐름

```
AptCrawlerScheduler (@Scheduled)
  → AptApiClient          공공데이터포털 API 호출 (WebClient, JSON)
  → RegionFilterService   apt.regions 설정 기준 지역 필터링
  → ScoringService        사용자 프로필 기반 당첨 확률 점수 계산 후 내림차순 정렬
  → SlackNotificationService  상위 top-n건 Slack Block Kit 메시지 전송
```

### 점수 계산 기준 (`ScoringService`)

| 조건 | 점수 |
|------|------|
| 신혼부부 특공 세대 있음 + `user.newlywed=true` | +40 |
| 생애최초 특공 세대 있음 + `user.first-time-buyer=true` | +30 |
| 공고 지역이 `user.resident-region` 포함 | +20 |
| 전용 85㎡ 이하 타입 포함 | +10 |

### 설정 구조

모든 민감 값은 `.env`에 저장하고 `application.yml`에서 `${ENV_VAR}` 형식으로 참조.

- `AptProperties` — `apt.*` (api-key, api-url, cron, regions, top-n)
- `UserProfile` — `user.*` (monthly-income, resident-region, newlywed, first-time-buyer)
- `SlackProperties` — `slack.webhook-url`

`.env.example`에 키 이름 목록이 있음. `.env`는 `.gitignore`로 제외.

### 주요 제약사항

- **Java 25 환경**: Lombok이 Java 25와 호환되지 않아(`TypeTag::UNKNOWN` 미존재) 사용 불가. 모든 getter/setter/constructor는 수동 작성.
- **Gradle 9.4.0**: `testRuntimeOnly 'org.junit.platform:junit-platform-launcher'` 명시 필요. `jar { enabled = false }` 로 plain jar 생성 비활성화.
- 중복 알림 방지용 `processedIds` Set은 메모리 캐시(앱 재시작 시 초기화). 일 1회 실행이므로 허용.

### API 응답 파싱

공공데이터포털은 단건과 복수건 응답 구조가 다름:
- 복수건: `response.body.items.item` → JSON 배열
- 단건: `response.body.items.item` → JSON 객체

`AptApiClient.parseItems()`에서 `isArray()` 분기로 처리.
