# BYD Launcher

BYD 차량 인포테인먼트(DiLink 3.0 / 5.0, Android 9+)용 홈 런처

[![Release](https://img.shields.io/github/v/release/GeyuongGongPark/BYDLauncher?style=flat-square)](https://github.com/GeyuongGongPark/BYDLauncher/releases)
[![License](https://img.shields.io/badge/license-MIT-blue?style=flat-square)](LICENSE)

**[🌐 랜딩 페이지](https://geyuonggongpark.github.io/BYDLauncher/) · [📄 개인정보 처리방침](https://geyuonggongpark.github.io/BYDLauncher/privacy.html)**

---

## 지원 기기

| 모델 | 디스플레이 | DiLink |
|------|-----------|--------|
| Atto 3 | 12.8" 회전 | 3.0 |
| Seal | 15.6" 회전 | 5.0 |
| Seal U DM-i | 10.25" | 5.0 |
| Dolphin | 12.8" | 3.0 |

## 주요 기능

- **홈 런처** — `CATEGORY_HOME` 등록, 홈 버튼으로 즉시 진입
- **회전 디스플레이 대응** — Landscape / Portrait 자동 전환
- **위치 기반 날씨** — `LocationManager`(AOSP 표준, Play Services 불필요) + OpenWeatherMap
- **캘린더 위젯** — 오늘 일정 최대 5개 표시
- **즐겨찾기 앱 그리드** — 최대 8개, 길게 누르기 편집 모드
- **앱 서랍 검색** — 실시간 필터링, 앱 변경 자동 반영
- **전체화면** — 시스템바 자동 숨김

## 빌드

### 요구사항

- JDK 17
- Android SDK (compileSdk 34)

### API Key 설정

`local.properties`에 OpenWeatherMap API key 추가:

```
WEATHER_API_KEY=여기에_키_입력
```

[OpenWeatherMap 무료 가입](https://openweathermap.org/api) → Current Weather Data API

### 빌드 명령

```bash
./gradlew :app:assembleDebug      # Debug APK
./gradlew :app:assembleRelease    # Release APK
```

## 설치

1. [Releases](https://github.com/GeyuongGongPark/BYDLauncher/releases)에서 최신 APK 다운로드
2. 인포테인먼트에서 **설정 > 보안 > 알 수 없는 출처** 허용
3. APK 설치 후 홈 버튼 → **BYD Launcher** 선택 → **항상**

## 릴리즈 (GitHub Actions)

`v` 접두사 태그 push 시 자동으로 APK를 빌드해 릴리즈에 첨부한다.

```bash
git tag v1.1.0
git push origin v1.1.0
```

### GitHub Secrets

| Secret | 설명 |
|--------|------|
| `WEATHER_API_KEY` | OpenWeatherMap API key |
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
- **저장**: DataStore Preferences
- **네트워크**: OkHttp + Coil

## 라이선스

MIT License
