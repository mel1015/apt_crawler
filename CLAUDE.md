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

매일 09:00 KST에 청약홈 ApplyhomeInfoDetailSvc API를 조회하고, 조건에 맞는 공고를 Slack으로 알림하는 Spring Boot 배치 애플리케이션.

### 실행 흐름

GitHub Actions가 매일 KST 09:00에 트리거 → 앱 시작 → `ApplicationRunner.run()` → 완료 후 종료.

```
AptCrawlerApplication (ApplicationRunner)
  → AptCrawlerScheduler.run()
      → AptApiClient          청약홈 API 호출 (WebClient, JSON)
          fetchAnnouncements()   getAPTLttotPblancDetail — 공고 목록
          fetchUnitTypes()       getAPTLttotPblancMdl    — 주택형별 상세
      → RegionFilterService   apt.regions 설정 기준 지역 필터링
      → ScoringService        사용자 프로필 기반 당첨 확률 점수 계산 후 내림차순 정렬
      → AptCrawlerScheduler   APT_MAX_PRICE 초과 공고 제거
      → SlackNotificationService  상위 top-n건 Slack Block Kit 메시지 전송
```

### 점수 계산 기준 (`ScoringService`)

| 조건 | 점수 |
|------|------|
| 신혼부부 특공 세대 있음 + `user.newlywed=true` | +40 |
| 생애최초 특공 세대 있음 + `user.first-time-buyer=true` | +30 |
| 공고 지역이 `user.resident-region` 포함 | +20 |
| 전용 85㎡ 이하 타입 포함 | +10 |

지역 매칭은 양방향 contains: `경기`.contains(`경기도 김포시`) 또는 `경기도 김포시`.contains(`경기`) 모두 통과.

### 설정 구조

모든 민감 값은 `.env`에 저장하고 `application.yml`에서 `${ENV_VAR}` 형식으로 참조.

- `AptProperties` — `apt.*` (api-key, api-url, cron, regions, top-n, max-price)
  - `regions`: 쉼표 구분 문자열 (예: `경기,서울`). `getRegionList()`로 List 변환.
  - `max-price`: 원 단위. 0이면 필터 비활성. API 가격(만원)과 비교 시 `/10_000` 변환.
- `UserProfile` — `user.*` (monthly-income, resident-region, newlywed, first-time-buyer)
- `SlackProperties` — `slack.webhook-url`

`.env.example`에 키 이름 목록이 있음. `.env`는 `.gitignore`로 제외.

### 주요 제약사항

- **Java 25 환경**: Lombok이 Java 25와 호환되지 않아(`TypeTag::UNKNOWN` 미존재) 사용 불가. 모든 getter/setter/constructor는 수동 작성.
- **Gradle 9.4.0**: `testRuntimeOnly 'org.junit.platform:junit-platform-launcher'` 명시 필요. `jar { enabled = false }` 로 plain jar 생성 비활성화.
- 중복 알림 방지용 `processedIds` Set은 메모리 캐시(앱 재시작 시 초기화). 일 1회 실행이므로 허용.

### API 응답 구조

청약홈 API 응답 최상위: `{ data: [...], totalCount: N, page: N, perPage: N }`

- `AptApiClient.parseItems()`: `root.path("data")` 배열 파싱
- `SUBSCRPT_AREA_CODE_NM`: 시/도 약칭 반환 (예: `경기`, `서울`, `울산`)
- `LTTOT_TOP_AMOUNT`: **만원 단위** (예: `"70000"` = 7억원). `SlackNotificationService.formatManwon()` 참고.
- `SPSPLY_HSHLDCO` / `LFE_FRST_HSHLDCO`: 공고 목록 endpoint에서 null일 수 있음 → `SPSPLY_RCEPT_BGNDE` 존재 여부로 대체 판단.
