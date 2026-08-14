# BYDLauncher 개발 TODO

## Phase 1 — 런처 기반 구축

### 1-1. 프로젝트 셋업
- [ ] settings.gradle.kts
- [ ] build.gradle.kts (root)
- [ ] gradle/libs.versions.toml
- [ ] gradle/wrapper/gradle-wrapper.properties
- [ ] app/build.gradle.kts
- [ ] app/proguard-rules.pro

### 1-2. 앱 기반 파일
- [ ] app/src/main/AndroidManifest.xml (CATEGORY_HOME 런처 등록)
- [ ] LauncherApplication.kt (Hilt)
- [ ] MainActivity.kt (전체화면, 방향 대응)

### 1-3. 테마 / 디자인 시스템
- [ ] ui/theme/Color.kt (BYD 다크 팔레트)
- [ ] ui/theme/Theme.kt (Material3 다크 스킴)
- [ ] ui/theme/Type.kt

### 1-4. 도메인 / 데이터 레이어 (앱 목록)
- [ ] domain/apps/AppInfo.kt
- [ ] domain/apps/AppRepository.kt
- [ ] data/apps/AppRepositoryImpl.kt
- [ ] di/AppModule.kt

### 1-5. UI 컴포넌트
- [ ] ui/utils/DrawableExt.kt
- [ ] ui/components/ClockWidget.kt
- [ ] ui/components/StatusBar.kt
- [ ] ui/components/AppDock.kt
- [ ] ui/components/SidePanel.kt

### 1-6. 앱 서랍
- [ ] ui/apps/AppDrawerViewModel.kt
- [ ] ui/apps/AppDrawer.kt

### 1-7. 홈 화면
- [ ] ui/home/HomeViewModel.kt
- [ ] ui/home/HomeScreen.kt (Landscape / Portrait 반응형)

### 1-8. 리소스
- [ ] res/values/strings.xml
- [ ] res/values/themes.xml

---

## Phase 2 — 날씨 + 캘린더 (예정)

- [ ] Open-Meteo API 연동 (위치 기반 날씨)
- [ ] CalendarContract Provider 연동 (오늘 일정)
- [ ] 권한 요청 흐름

## Phase 3 — 차량 텔레메트리 (예정)

- [ ] DiLink 브로드캐스트 API 리서치 (adb logcat)
- [ ] 속도, 배터리, 타이어 기압 표시
- [ ] DiLink 3.0 / 5.0 분기 처리

## Phase 4 — UI 폴리싱 (예정)

- [ ] 레이아웃 테마 선택 (Kinex Jett/Delta 방식)
- [ ] 애니메이션 / 트랜지션
- [ ] 큰 터치 타겟 최적화

## Phase 5 — 차량 제어 (예정, API 접근 가능 시)

- [ ] 에어컨, 창문, 트렁크

---

## 검토 (완료 시 작성)
