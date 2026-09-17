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

## Phase 4 — UI 폴리싱 (예정)

- [ ] 레이아웃 테마 선택 (Kinex Jett/Delta 방식)
- [ ] 애니메이션 / 트랜지션
- [ ] 큰 터치 타겟 최적화

## Phase 5 — 차량 제어 (예정, API 접근 가능 시)

- [ ] 에어컨, 창문, 트렁크

## Phase 6 — 에코 드라이브 코치 (예정)

> **기획 배경**: OpenAPI의 Speed·Energy·Statistic·Gearbox 모듈을 조합해 운전 습관을 실시간 분석.
> Phase 3 텔레메트리 완료 후 착수.

### 6-1. 데이터 수집 레이어

- [ ] `DriveSessionRepository.kt` — 주행 세션 단위 데이터 수집 (시작: D기어, 종료: P기어 감지)
- [ ] `DriveEventDetector.kt` — 이벤트 감지 로직
  - 급가속: `AccelerateDeepness` 가 80% 이상으로 빠르게 상승
  - 급제동: `BrakeDeepness` 가 70% 이상
  - 에너지 회수: `PowerGenerationState` 활성 구간 추적
- [ ] `EcoScoreCalculator.kt` — 세션 점수 계산 (100점 기준, 이벤트 발생 시 감점)
- [ ] 세션 데이터 Room DB 저장 (`DriveSession`, `DriveEvent` 엔티티)

### 6-2. 홈 화면 위젯

- [ ] 홈 화면 상단 에코 점수 인디케이터 컴포넌트
  - 현재 세션 점수 (0~100, 색상으로 구분: 초록/주황/빨강)
  - 순간 전비 `getInstantElecConValue()` 실시간 표시
  - 주행 모드 표시 (EV / HEV / SPORT)
- [ ] 급가속/급제동 감지 시 화면 테두리 플래시 피드백

### 6-3. 드라이브 탭 (세션 리포트)

- [ ] `DriveCoachScreen.kt` — 별도 탭으로 접근
- [ ] 현재 세션 요약: 경과 시간, 급가속 횟수, 급제동 횟수, 평균 전비
- [ ] P기어 진입 시 자동 세션 종료 + 결과 팝업 표시
  - 에코 점수, 전 세션 대비 비교, 절약 추정 kWh
- [ ] 세션 히스토리 리스트 (날짜별)

### 6-4. 자동 모드 제안 (옵션)

- [ ] 스포츠 모드 일정 시간 지속 시 → "에코 전환 시 X km 추가 주행 가능" 토스트
- [ ] 에너지 회수 강도 자동 추천 (`SettingDevice.setEnergyFeedback()`)

---

## 검토 (완료 시 작성)

### Phase 1~3 완료 (2026-09)
- Phase 1 런처 기반 전체 구현 완료
- Phase 2 날씨(IP fallback 포함) + 캘린더 + 즐겨찾기(DataStore, max 8) 완료
- Phase 3 차량 텔레메트리 전체 완료 (OpenAPI Reflection, 캠핑 모드, 네비 임베딩, DPI 조정)
