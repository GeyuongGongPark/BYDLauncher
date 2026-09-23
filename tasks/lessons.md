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

### L7. todo.md / lessons.md 업데이트 습관 (CLAUDE.md 핵심 규칙)
- **작업 시작 전**: todo.md에 체크리스트 작성
- **작업 완료 후**: todo.md 체크 + lessons.md 업데이트 — 사용자가 지적하기 전에 먼저
- **수정 받았을 때**: 즉시 lessons.md에 해당 패턴 기록
- 이 규칙을 반복적으로 어길 경우 프로젝트 폐기됨 (사용자 경고)

## 보안

### L9. 자동 업데이트 기능 구현 시 APK 서명 검증 필수
- **문제**: GitHub releases에서 APK를 HTTPS로 다운로드해도 서명 검증 없으면 GitHub 계정 탈취 시 악성 APK 배포 가능
- **해결**: `PackageManager.GET_SIGNING_CERTIFICATES`로 현재 앱 서명 SHA-256 핑거프린트와 다운로드 APK 서명 비교
- **패턴**: 설치 전 `verifyApkSignature()` 호출 → 불일치 시 파일 삭제 + 오류 반환
- **보장**: 동일 개인키로 서명된 APK만 통과. 키 없이는 우회 불가.

### L10. 보안 검토는 기능 구현 시점에 함께 수행
- 업데이트 기능처럼 외부 데이터를 실행하는 코드는 구현 즉시 위협 모델 검토
- 나중에 별도 검토하면 이미 배포된 취약점이 될 수 있음

## 네비게이션 임베딩

### L8. dadb ADB loopback 방식이 실차에서 작동함
- `dadb` 라이브러리로 127.0.0.1:5555 연결 후 `am start --display <displayId>` 명령 실행
- Wi-Fi ADB가 켜진 상태에서 차량 내부 앱이 loopback으로 adbd에 접근 가능
- ActivityOptions.setLaunchDisplayId()는 일반 앱에서 무시됨 (시스템 권한 필요)
- **복원 방법**: dadb 의존성 재추가 + AdbNaviLauncher 이전 구현으로 복원
