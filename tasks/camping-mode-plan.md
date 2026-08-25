# Phase 4 — 캠핑 모드 통합 계획 (SDK 직접 방식)

## 배경 & 핵심 결정

BYDCamp(REST API 방식)에서 **DiLink SDK 직접 제어 방식**으로 전환.
`BydAutoStart_2.1.6-beta.apk` 리버싱 + `wheregoes/byd-dolphin-hacking` 문서로 검증 완료.

| 항목 | REST API (폐기) | SDK 직접 (채택) |
|------|----------------|----------------|
| 계정/비밀번호 | 필요 | **불필요** |
| PIN | 필요 | **불필요** |
| 인터넷 | 필요 | **불필요** |
| 28분 재호출 | 필요 | **불필요** — 꺼짐 이벤트로 즉시 재시작 |
| 설정 항목 | 6개 | **3개** (온도/배터리%/최대시간) |

---

## SDK 레퍼런스 (실차 검증 완료)

### 에어컨 제어 — BYDAutoAcDevice

```kotlin
// AC on/off — 실차 WORKS
acDevice.start(0)   // 0 = UI_KEY source
acDevice.stop(0)

// 온도 설정 — direct Celsius, source=1 필수, param4=1 필수
acDevice.setAcTemperature(1, 22, 1, 1)  // zone=1(driver), temp=22°C

// 상태 조회 — 권한 체크 없음, 아무 앱에서 가능
acDevice.getAcStartState()   // 1=on, 0=off
acDevice.getTemprature(4)    // 외부 온도 (typo: Temprature)

// 이벤트 리스너
override fun onAcStoped() { /* 에어컨 꺼짐 → 즉시 재시작 */ }
```

### 배터리 SOC — BYDAutoStatisticDevice

```kotlin
statisticDevice.getESTIMATE_SOC_V1()  // 배터리 잔량 %
```

### 권한 우회 — VehicleContextWrapper (BydAutoStart 검증)

```kotlin
class VehicleContextWrapper(ctx: Context) : ContextWrapper(ctx) {
    override fun checkSelfPermission(p: String) = PackageManager.PERMISSION_GRANTED
    override fun enforceCallingOrSelfPermission(p: String, msg: String?) {}
    // ... (모든 permission 체크 override)
}
```

### SDK 로딩 전략 (BydAutoStart 방식)

```kotlin
// DiLink 5.0 (씨라이언7): 시스템 ClassLoader에 이미 존재
// DiLink 3.0 (아토3/돌핀): 외부 APK에서 DexClassLoader로 로딩
private val SDK_PACKAGES = listOf(
    "com.byd.hvac", "com.byd.carsettings", "com.byd.mycar", "com.byd.scenemode"
)
```

---

## 구현 계획

### Step 1 — SDK 레이어 (신규)

**`camping/sdk/VehicleContextWrapper.kt`**
- BydAutoStart의 VehicleContextWrapper 이식 (Kotlin 변환)
- 모든 BYDAUTO_* 권한 체크 GRANTED로 우회

**`camping/sdk/AcController.kt`**
- DexClassLoader로 BYD SDK APK 로딩 (DiLink 3/5 분기)
- `BYDAutoAcDevice` reflection 래핑
- 메서드: `connect()`, `start()`, `stop()`, `setTemperature(temp: Int)`
- 리스너: `onAcStopped: () -> Unit` 콜백

**`camping/sdk/BatteryReader.kt`**
- `BYDAutoStatisticDevice` reflection 래핑
- `getSoc(): Int` — `getESTIMATE_SOC_V1()` 호출

### Step 2 — 설정 저장소 (신규)

**`camping/storage/CampingPrefs.kt`**
- SharedPreferences 기반
- 저장 항목 3개만:
  ```kotlin
  var targetTemp: Int    // 기본 24, 범위 16~30
  var stopBatteryPct: Int // 기본 30, 범위 10~50
  var maxHours: Int      // 기본 8, 범위 1~12
  ```

### Step 3 — CampingService (신규)

**`camping/service/CampingService.kt`**
- LifecycleService (Foreground Service)

동작 흐름:
```
시작
 ├─ AcController.connect()   — SDK 로딩
 ├─ ac.start(0)              — 에어컨 켜기
 ├─ ac.setTemperature(temp)  — 목표 온도
 └─ 리스너 등록

루프
 ├─ onAcStopped() 이벤트 → start() + setTemperature() 즉시 재시작
 ├─ [5분마다] BatteryReader.getSoc()
 │   └─ soc < stopBatteryPct → stopCamping()
 └─ [경과시간] maxHours 초과 → stopCamping()

종료
 └─ ac.stop(0) → stopForeground() → stopSelf()
```

**`AndroidManifest.xml` 추가:**
```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>

<service
    android:name=".camping.service.CampingService"
    android:exported="false"
    android:foregroundServiceType="specialUse"/>
```

### Step 4 — 도메인/뷰모델 (신규)

**`camping/CampingState.kt`**
```kotlin
sealed interface CampingState {
    object Idle : CampingState
    object SdkUnavailable : CampingState      // BYD SDK APK 없음
    object Starting : CampingState
    data class Running(
        val batteryPct: Int,
        val elapsedMs: Long,
        val outsideTemp: Int,
    ) : CampingState
}
```

**`ui/camping/CampingViewModel.kt`**
- LocalBroadcast 수신 → StateFlow 변환
- 서비스 시작/종료
- SDK 가용성 체크 (시작 전 AcController.canConnect())

**`di/CampingModule.kt`**
- CampingPrefs Hilt 제공

### Step 5 — UI (신규 + 수정)

**`ui/camping/CampingSettingsDialog.kt`**
- 슬라이더 3개만:
  - 목표 온도: 16~30°C
  - 종료 배터리%: 10~50%
  - 최대 시간: 1~12시간
- 계정/PIN/VIN 입력 없음

**`ui/components/CampingModeButton.kt`**

| 상태 | UI |
|------|----|
| `Idle` | "캠핑 모드" 버튼 |
| `SdkUnavailable` | "지원되지 않는 차종" (비활성) |
| `Starting` | "시작 중..." + 스피너 |
| `Running` | 배터리% + 경과시간 + "종료" 버튼 |

롱프레스 → 설정 Dialog 열기

**`ui/components/SidePanel.kt` 수정**
- CalendarCard 아래 `CampingModeButton()` 추가

---

## 파일 구조 (완성 후)

```
app/src/main/java/com/bydlauncher/
├── camping/
│   ├── sdk/
│   │   ├── VehicleContextWrapper.kt   ← BydAutoStart 이식 (Kotlin 변환)
│   │   ├── AcController.kt            ← 신규
│   │   └── BatteryReader.kt           ← 신규
│   ├── service/
│   │   └── CampingService.kt          ← 신규
│   ├── storage/
│   │   └── CampingPrefs.kt            ← 신규
│   └── CampingState.kt                ← 신규
├── di/
│   └── CampingModule.kt               ← 신규
└── ui/
    ├── camping/
    │   ├── CampingViewModel.kt        ← 신규
    │   └── CampingSettingsDialog.kt   ← 신규
    └── components/
        ├── CampingModeButton.kt       ← 신규
        └── SidePanel.kt              ← 수정 (버튼 추가)
```

---

## 체크리스트

### Step 1 — SDK 레이어
- [ ] `camping/sdk/VehicleContextWrapper.kt`
- [ ] `camping/sdk/AcController.kt`
- [ ] `camping/sdk/BatteryReader.kt`

### Step 2 — 설정 저장소
- [ ] `camping/storage/CampingPrefs.kt`

### Step 3 — Service
- [ ] `camping/service/CampingService.kt`
- [ ] `AndroidManifest.xml` 권한 + 서비스 등록

### Step 4 — 도메인/뷰모델
- [ ] `camping/CampingState.kt`
- [ ] `ui/camping/CampingViewModel.kt`
- [ ] `di/CampingModule.kt`

### Step 5 — UI
- [ ] `ui/camping/CampingSettingsDialog.kt`
- [ ] `ui/components/CampingModeButton.kt`
- [ ] `ui/components/SidePanel.kt` 수정

### 검증
- [ ] 빌드 성공
- [ ] SidePanel 버튼 표시 확인
- [ ] 설정 Dialog 동작 확인
- [ ] (차량 실기) SDK 로딩 성공 로그 확인
- [ ] (차량 실기) 에어컨 시작/정지 확인
- [ ] (차량 실기) onAcStopped 재시작 확인

---

## 알려진 리스크

| 리스크 | 대응 |
|--------|------|
| SDK APK 패키지명이 차종/펌웨어마다 다를 수 있음 | 후보 패키지 4개 순서대로 시도 |
| `setAcTemperature` source=0이면 INVALID_VALUE | source=1 고정 사용 |
| `getESTIMATE_SOC_V1()` 미지원 차종 | 65535/-10011 반환 시 배터리 감시 비활성, 시간 기반만 |
| DiLink 5.0 (씨라이언7) — 이미 자체 캠핑 모드 있음 | SdkUnavailable 상태로 처리 or 그냥 작동시켜도 무방 |
