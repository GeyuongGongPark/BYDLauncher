# BYDLauncher 개발 TODO

## Phase 1 — 런처 기반 구축

### 1-1. 프로젝트 셋업
- [x] settings.gradle.kts
- [x] build.gradle.kts (root)
- [x] gradle/libs.versions.toml
- [x] gradle/wrapper/gradle-wrapper.properties
- [x] app/build.gradle.kts
- [x] app/proguard-rules.pro

### 1-2. 앱 기반 파일
- [x] app/src/main/AndroidManifest.xml (CATEGORY_HOME 런처 등록)
- [x] LauncherApplication.kt (Hilt)
- [x] MainActivity.kt (전체화면, 방향 대응)

### 1-3. 테마 / 디자인 시스템
- [x] ui/theme/Color.kt (BYD 다크 팔레트)
- [x] ui/theme/Theme.kt (Material3 다크 스킴)
- [x] ui/theme/Type.kt

### 1-4. 도메인 / 데이터 레이어 (앱 목록)
- [x] domain/apps/AppInfo.kt
- [x] domain/apps/AppRepository.kt
- [x] data/apps/AppRepositoryImpl.kt
- [x] di/AppModule.kt

### 1-5. UI 컴포넌트
- [x] ui/utils/DrawableExt.kt
- [x] ui/components/ClockWidget.kt
- [x] ui/components/StatusBar.kt
- [x] ui/components/AppDock.kt
- [x] ui/components/SidePanel.kt

### 1-6. 앱 서랍
- [x] ui/apps/AppDrawerViewModel.kt
- [x] ui/apps/AppDrawer.kt

### 1-7. 홈 화면
- [x] ui/home/HomeViewModel.kt
- [x] ui/home/HomeScreen.kt (Landscape / Portrait 반응형)

### 1-8. 리소스
- [x] res/values/strings.xml
- [x] res/values/themes.xml

---

## Phase 2 — 날씨 + 캘린더

- [x] Open-Meteo API 연동 (위치 기반 날씨) + IP Geolocation fallback (ipapi.co)
- [x] CalendarContract Provider 연동 (오늘 일정)
- [x] 권한 요청 흐름

## Phase 3 — 차량 텔레메트리

- [x] DiLink BYD Auto OpenAPI 리서치 (HAL SDK, Reflection + DexClassLoader)
- [x] 속도, 배터리, 타이어 기압, 오작동, PM2.5 표시 (VehicleViewModel)
- [x] DiLink 3.0 / 5.0 분기 처리 (DexClassLoader chain)
- [x] 네비게이션 앱 임베딩 (VirtualDisplay + ADB loopback, 카카오맵/T맵/네이버/구글)
- [x] 네비 VirtualDisplay DPI 조정 (+20/-20, DataStore 저장)
- [x] 캠핑 모드 (P기어 안전 확인, AC 내부순환, 충전 중 배터리 소진 방지)
- [x] 차량 상태 위젯 (배터리, 연료, 충전, 주행거리)
- [x] PM2.5 공기질 카드 (실내/실외, 레벨별 색상)
- [x] 경고 배너 (오작동, 타이어, 주행 중 문 열림)
- [x] 주행 중 앱 서랍 잠금 (속도 > 1km/h)

## Phase 4 — UI 폴리싱

- [x] Dark / Darker 테마 전환 (ThemeMode enum, DataStore 저장, SidePanel 토글)
- [x] 슬라이드 애니메이션 / 트랜지션 (220ms, ordinal 기반 방향)
- [x] 큰 터치 타겟 최적화 (48dp defaultMinSize)

## Phase 5 — 차량 제어

- [x] AC 퀵 컨트롤 (ON/OFF, 온도 ±1°C, 풍량 표시) — SidePanel VehicleControlCard
- [x] 창문 개방률 표시 (4개 창문, BodyworkReader)
- [x] AcReader.kt + VehicleControlState + VehicleControlViewModel

## Phase 6 — 에코 드라이브 코치

### 6-1. 데이터 수집 레이어

- [x] `DriveSessionRepository.kt` — 주행 세션 단위 데이터 수집 (시작: D기어, 종료: P기어 감지)
- [x] `DriveEventDetector.kt` — 급가속(≥80%), 급제동(≥70%), 회생제동 감지 / 쿨다운 3초
- [x] `EcoScoreCalculator.kt` — 100점 기준, 급가속 -5, 급제동 -3, 회생 +1
- [x] Room DB 저장 (`DriveSessionEntity`, `DriveEventEntity`, Room 2.6.1)

### 6-2. 홈 화면 위젯

- [x] `EcoScoreWidget.kt` — 현재 세션 점수(색상 구분), 순간 전비, 주행 모드
- [x] `DriveFlashOverlay.kt` — 급가속(주황)/급제동(빨강) 화면 테두리 플래시 600ms

### 6-3. 드라이브 탭 (세션 리포트)

- [x] `DriveCoachScreen.kt` — 별도 탭 (AppDock 🍃 버튼으로 진입)
- [x] 현재 세션 요약: 경과 시간, 급가속/급제동/회생 횟수, 에코 점수
- [x] `SessionResultDialog.kt` — P기어 진입 시 결과 팝업 (점수 색상 코딩, 이벤트 요약)
- [x] 세션 히스토리 리스트 (날짜별, Room DB)

### 6-4. 자동 모드 제안

- [ ] 스포츠 모드 일정 시간 지속 시 → "에코 전환 시 X km 추가 주행 가능" 토스트 (미구현)

## 버그 수정 — 실차 이슈 (2026-09-26)

- [x] 네비 실행 불가 — 원인 분석 (dadb 성공 후 렌더링 없으면 onEmbeddingFailed 미호출)
- [x] 네비 실행 불가 — 수정 (8초 timeout → fallback, AtomicBoolean hasRendered)
- [x] 전화 버튼 → 인터셉터 화면 노출 — 원인 분석 (ACTION_DIAL → BYD 인터셉터)
- [x] 전화 버튼 → 인터셉터 화면 노출 — 수정 (BYD 전화 앱 패키지 직접 실행)
- [ ] 빌드 + 설치 + 실차 검증
- [x] lessons.md 업데이트

## 보안 강화

- [x] 자동 업데이트 APK 서명 검증 — 현재 앱 서명과 다운로드 APK 서명 비교 후 불일치 시 설치 거부
  - `UpdateViewModel.verifyApkSignature()` 추가
  - `GET_SIGNING_CERTIFICATES`로 현재 앱 서명 핑거프린트(SHA-256) 추출
  - 다운로드한 APK에서 동일하게 추출 후 교집합 검사
  - 불일치 시 APK 삭제 + `UpdateState.Error` 반환
  - 컴파일 검증 완료

---

## Phase 7 — KBO 야구 팀 트래커 + 런처 설정 탭

> 목표:
> - SidePanel WeatherCard 아래 KboCard (경기 정보 표시)
> - AppDock "설정" → 런처 설정 탭 (HomeTab.SETTINGS 신규)
> - 런처 설정 탭 안에 KBO 응원팀 선택 포함
> - Mock 데이터 UI 먼저, API 연동 추후
>
> 포스트시즌 지원: Scheduled/Live/Final에 isPostseason + roundName + seriesRecord 필드 포함
> (API 연동 시 round 필드로 정규/포스트 구분)

### 7-1. 도메인 레이어
- [ ] `domain/kbo/KboTeam.kt` — 10개 팀 enum (이름, 약칭, 홈구장, 팀 색상)
- [ ] `domain/kbo/KboGame.kt` — KboGameState sealed class
  - `NoTeam` — 팀 미선택
  - `NoGame(rank)` — 오늘 경기 없음 (휴식일)
  - `Scheduled(opponent, time, venue, rank, isPostseason, roundName?, seriesRecord?)` — 예정
  - `Live(myScore, oppScore, inning, isTop, outs, rank, isPostseason, roundName?, seriesRecord?)` — 진행 중
  - `Final(myScore, oppScore, result, rank, isPostseason, roundName?, seriesRecord?)` — 종료
  - `OffSeason` — 오프시즌
- [ ] `domain/kbo/KboRepository.kt` — 인터페이스

### 7-2. 데이터 레이어 (Mock)
- [ ] `data/kbo/KboRepositoryMock.kt` — 7가지 시나리오 Mock 구현
  - NO_TEAM / NO_GAME / SCHEDULED / LIVE / FINAL (정규시즌)
  - POSTSEASON_LIVE — 포스트시즌 진행 중 (roundName, seriesRecord 포함)
  - OFF_SEASON

### 7-3. ViewModel
- [ ] `ui/kbo/KboViewModel.kt`
  - DataStore에서 즐겨찾기 팀 로드
  - `selectTeam(team)` — 팀 선택 저장
  - 폴링 루프: Live 30초, Scheduled 60초, 나머지 5분
  - 이전 상태와 비교해 아웃카운트/이닝 변화 감지 시 즉시 갱신 트리거 구조 준비

### 7-4. KboCard UI
- [ ] `ui/components/KboCard.kt` — SidePanel WeatherCard 아래 배치
  - `NoTeam`: "설정에서 응원팀을 선택하세요" 안내
  - `NoGame / OffSeason`: 팀명 + 순위 + 메시지
  - `Scheduled`: 상대팀, 시작 시간, 장소 + 포스트시즌 배지
  - `Live`: 스코어(크게), 이닝+상하, 아웃카운트(●○) + 포스트시즌 배지 + 시리즈 현황
  - `Final`: 최종 스코어, 승/패/무 배지 + 포스트시즌 배지 + 시리즈 현황
  - 공통 하단: 최근 5경기 W/L 도트 (포스트시즌 포함)

### 7-5. 런처 설정 탭
- [ ] `HomeTab.SETTINGS` 추가 (`HomeViewModel.kt`)
- [ ] `HomeViewModel.toggleSettings()` 추가
- [ ] `ui/settings/SettingsScreen.kt` — 풀스크린 설정 화면
  - [KBO 응원팀] 섹션: 10개 팀 선택 그리드 (현재 선택 팀 하이라이트)
  - [시스템] 섹션: "BYD 시스템 설정 열기" 버튼 (기존 AppDock 설정 동작 이전)
- [ ] `ui/components/AppDock.kt` — "설정" 버튼 → `onOpenSettings()` 콜백으로 변경
- [ ] `ui/home/HomeScreen.kt` — `HomeTab.SETTINGS` 케이스 + `SettingsScreen()` 연결

### 7-6. 연결
- [ ] `di/AppModule.kt` — KboRepository Mock 바인딩 추가
- [ ] `data/settings/AppSettingsRepositoryImpl.kt` — `kbo_favorite_team` 키 추가
- [ ] `ui/components/SidePanel.kt` — WeatherCard 아래 KboCard 삽입

### 7-7. 검증
- [ ] 빌드 성공 확인
- [ ] 7가지 Mock 시나리오 Compose Preview 확인

---

## 검토 (완료 시 작성)

### Phase 1~3 완료 (2026-09)
- Phase 1 런처 기반 전체 구현 완료
- Phase 2 날씨(IP fallback 포함) + 캘린더 + 즐겨찾기(DataStore, max 8) 완료
- Phase 3 차량 텔레메트리 전체 완료 (OpenAPI Reflection, 캠핑 모드, 네비 임베딩, DPI 조정)

### Phase 4~6 완료 (2026-09)
- Phase 5 AC 퀵 컨트롤 + 창문 개방률 (VehicleControlCard in SidePanel)
- Phase 6 에코 드라이브 코치 전체 (Room DB, DriveEventDetector, EcoScoreWidget, DriveFlashOverlay, DriveCoachScreen, SessionResultDialog)
- Phase 4 UI 폴리싱 (Dark/Darker 테마, 슬라이드 트랜지션 220ms, 터치 타겟 48dp)
- 문서: README + 랜딩 페이지 업데이트, 영문 랜딩/개인정보 처리방침 페이지 신규 생성
