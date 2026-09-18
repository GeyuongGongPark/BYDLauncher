# BYD Launcher

BYD 차량 인포테인먼트(DiLink 3.0 / 5.0, Android 9+)용 홈 런처

[![Release](https://img.shields.io/github/v/release/GeyuongGongPark/BYDLauncher?style=flat-square)](https://github.com/GeyuongGongPark/BYDLauncher/releases)
[![License](https://img.shields.io/badge/license-MIT-blue?style=flat-square)](LICENSE)

**[🌐 랜딩 페이지](https://geyuonggongpark.github.io/BYDLauncher/) · [📄 개인정보 처리방침](https://geyuonggongpark.github.io/BYDLauncher/privacy.html)**

---

## 지원 기기

| 모델 | 디스플레이 | DiLink | 비고 |
|------|-----------|--------|------|
| Atto 3 | 12.8" 회전 | 3.0 | |
| Seal | 15.6" 회전 | 5.0 | |
| Seal U DM-i | 10.25" | 5.0 | |
| Dolphin | 12.8" | 3.0 | |
| Sea Lion 7 Plus (씨라이언 7+) | — | 5.0 | 메모리 시트 전용 |

## 주요 기능

- **홈 런처** — `CATEGORY_HOME` 등록, 홈 버튼으로 즉시 진입
- **회전 디스플레이 대응** — Landscape / Portrait 자동 전환
- **위치 기반 날씨** — `LocationManager`(AOSP 표준, Play Services 불필요) + Open-Meteo API (API Key 불필요)
- **캘린더 위젯** — 오늘 일정 최대 5개 표시
- **즐겨찾기 앱 그리드** — 최대 8개, 길게 누르기 편집 모드
- **앱 서랍 검색** — 실시간 필터링, 앱 변경 자동 반영
- **네비게이션 앱 연동** — 카카오맵·T맵·네이버지도·구글맵 선택 실행
- **네비 오버레이 모드** — 네비게이션 앱 위에 반투명 독 오버레이 표시 (Kinex 방식)
- **기본 홈 앱 설정** — 앱 내에서 바로 기본 런처 변경 가능
- **캠핑 모드** — 주차 중 공조/배터리 모니터링
- **AC 퀵 컨트롤** — SidePanel에서 에어컨 ON/OFF, 온도 ±1°C 직접 조작
- **창문 개방률 표시** — 4개 창문 실시간 개방률 (BYDAutoBodyworkDevice)
- **에코 드라이브 코치** — D기어 진입 시 세션 자동 시작, 급가속/급제동/회생제동 감지, 에코 점수(0~100)
- **주행 세션 기록** — Room DB에 세션별 기록 저장, P기어 진입 시 결과 팝업
- **메모리 시트** — 드라이빙 포지션 저장, P기어 시 시트 최대 뒤로(하차 편의), D기어 시 자동 복원 *(씨라이언 7 플러스 전용)*
- **테마 전환** — Dark / Darker(완전 블랙) 두 가지 팔레트, DataStore 저장
- **전체화면** — 시스템바 자동 숨김

## 빌드

### 요구사항

- JDK 17
- Android SDK (compileSdk 34)

### 빌드 명령

```bash
./gradlew :app:assembleDebug      # Debug APK
./gradlew :app:assembleRelease    # Release APK
```

> 날씨는 [Open-Meteo](https://open-meteo.com/) API를 사용하며 API Key가 필요 없습니다.

## 설치

1. [Releases](https://github.com/GeyuongGongPark/BYDLauncher/releases)에서 최신 APK 다운로드
2. 인포테인먼트에서 **설정 > 보안 > 알 수 없는 출처** 허용
3. APK 설치 후 홈 버튼 → **BYD Launcher** 선택 → **항상**
4. (선택) 앱 내 **기본 홈 앱으로 설정** 버튼으로 즉시 변경 가능

## 릴리즈 (GitHub Actions)

`v` 접두사 태그 push 시 자동으로 APK를 빌드해 릴리즈에 첨부한다.

```bash
git tag v1.1.0
git push origin v1.1.0
```

### GitHub Secrets

| Secret | 설명 |
|--------|------|
| `SIGNING_KEY_BASE64` | Keystore를 base64로 인코딩한 값 |
| `SIGNING_KEY_ALIAS` | Key alias |
| `SIGNING_STORE_PASSWORD` | Keystore 비밀번호 |
| `SIGNING_KEY_PASSWORD` | Key 비밀번호 |

서명 Secrets 없으면 unsigned APK로 릴리즈된다.

## 기술 스택

- **언어**: Kotlin 2.0
- **UI**: Jetpack Compose + Material3
- **아키텍처**: MVVM + Clean Architecture
- **DI**: Hilt
- **비동기**: Coroutines + Flow
- **저장**: DataStore Preferences + Room 2.6.1 (주행 세션)
- **네트워크**: OkHttp + Coil
- **날씨**: Open-Meteo (무료, API Key 불필요)
- **차량 SDK**: BYD DiLink Reflection (BYDAutoAcDevice, BYDAutoBodyworkDevice, BYDAutoSpeedDevice, BYDAutoEnergyDevice, BYDAutoGearboxDevice, BYDAutoSeatDevice¹)
- ¹ 씨라이언 7 플러스(DiLink 5.0) 전용. 실차에서 logcat `SeatController` 태그로 API 탐색 결과 확인 가능.

## 라이선스

MIT License
