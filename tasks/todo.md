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
