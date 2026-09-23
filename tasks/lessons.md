# 교훈 (Lessons Learned)

## 실차 테스트 관련

### L1. 실차에서 됐던 기능을 이론만으로 제거하지 말 것
- **상황**: VirtualDisplay + dadb ADB loopback 방식이 실차에서 작동하고 있었음
- **실수**: "127.0.0.1:5555 listen 안 됨"이라는 이론적 판단으로 ActivityOptions.setLaunchDisplayId() 방식으로 전환 → 임베딩 안 됨
- **교훈**: 실차에서 확인된 동작 방식은 직접 로그로 검증 후에만 변경할 것. 이론적 판단만으로 교체 금지.
- **올바른 절차**: logcat 확인 → 실제 실패 메시지 확인 → 대안 검토

### L2. 실차 로그로 먼저 원인 파악 후 수정
- **상황**: 여러 이슈(날씨, 에코드라이빙, 에어컨 온도)에서 원인을 이론으로만 추정하다 잘못 수정
- **교훈**: 항상 `adb logcat` 로 실제 오류 메시지/스택 트레이스 확인 후 수정

### L3. NaviFallbackCard 클릭이 안 되던 원인
- **실제 원인**: startActivity는 성공했지만 앱이 VirtualDisplay에서 실행 중이어서 메인 디스플레이에 안 나타남
- **해결**: Display.DEFAULT_DISPLAY 명시 + FLAG_ACTIVITY_CLEAR_TOP → 그러나 근본적으로는 dadb 복원이 정답

### L4. BYD SDK 온도 단위
- `getTemprature(zone)` 반환값 = 섭씨 × 2 (0.5°C 단위)
- raw=48 → 48/2=24°C
- raw > 32 이면 /2 처리 필요

### L5. ADB 연결 비밀번호에 ! 포함 시
- `gh secret set`에서 비밀번호를 인자로 전달할 때 작은따옴표 필수
- `--body 'silvester1!'` (O) / `--body "silvester1!"` (X, bash가 ! 해석)

## 코드 품질

### L6. LazyColumn key 중복 크래시
- `queryIntentActivities`는 동일 패키지의 복수 Activity를 반환할 수 있음
- `.distinctBy { it.activityInfo.packageName }` 필수

### L7. todo.md / lessons.md 업데이트 습관
- 세션이 끝나면 항상 lessons.md에 새로운 교훈 기록
- 다음 세션에서 같은 실수 반복 방지가 목적
- CLAUDE.md에 명시된 규칙: "수정 후 lessons.md 업데이트"

## 네비게이션 임베딩

### L8. dadb ADB loopback 방식이 실차에서 작동함
- `dadb` 라이브러리로 127.0.0.1:5555 연결 후 `am start --display <displayId>` 명령 실행
- Wi-Fi ADB가 켜진 상태에서 차량 내부 앱이 loopback으로 adbd에 접근 가능
- ActivityOptions.setLaunchDisplayId()는 일반 앱에서 무시됨 (시스템 권한 필요)
- **복원 방법**: dadb 의존성 재추가 + AdbNaviLauncher 이전 구현으로 복원
