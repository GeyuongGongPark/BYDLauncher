package com.bydlauncher.camping.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bydlauncher.LauncherApplication
import com.bydlauncher.MainActivity
import com.bydlauncher.camping.CampingState
import com.bydlauncher.camping.sdk.AcController
import com.bydlauncher.camping.sdk.BatteryReader
import com.bydlauncher.camping.sdk.ChargingReader
import com.bydlauncher.camping.sdk.GearReader
import com.bydlauncher.camping.storage.CampingPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CampingService : Service() {

    companion object {
        const val ACTION_STOP = "com.bydlauncher.STOP_CAMPING"

        private const val TAG = "CampingService"
        private const val CHANNEL_ID = "camping_channel"
        private const val NOTIFICATION_ID = 2001
        private const val AC_POLL_INTERVAL_MS = 60_000L   // 1분마다 AC 상태 확인
        private const val BAT_POLL_INTERVAL_MS = 300_000L // 5분마다 배터리 확인
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var prefs: CampingPrefs
    private lateinit var ac: AcController
    private lateinit var battery: BatteryReader
    private lateinit var charging: ChargingReader
    private lateinit var gear: GearReader

    private var startTimeMs = 0L
    private var lastBatteryPct = -1
    private var lastCharging = false
    private var lastChargingPowerKw = 0.0
    private var lastRangeKm = -1
    private var acPollJob: Job? = null
    private var batPollJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        prefs = CampingPrefs(this)
        ac = AcController(this)
        battery = BatteryReader(this)
        charging = ChargingReader(this)
        gear = GearReader(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopCamping()
            return START_NOT_STICKY
        }

        startTimeMs = System.currentTimeMillis()
        startForeground(NOTIFICATION_ID, buildNotification("캠핑 모드 시작 중..."))
        publishState(CampingState.Starting)

        scope.launch {
            runCatching {
                ac.connect()
                battery.connect()
                charging.connect()
                gear.connect()

                // P단 체크: SDK 로딩 성공 시에만 강제
                if (!gear.isParked()) {
                    error("P단(주차) 상태에서만 캠핑 모드를 시작할 수 있습니다")
                }

                ac.start()
                ac.setTemperature(prefs.targetTemp)
                ac.setCycleMode(inLoop = true)  // 내기순환으로 전환
                publishState(running())
                updateNotification("에어컨 켜짐 — ${prefs.targetTemp}°C (내기순환)")
                Log.i(TAG, "캠핑 모드 시작 성공")
            }.onFailure { e ->
                Log.e(TAG, "시작 실패", e)
                publishState(CampingState.Error(e.message ?: "SDK 로딩 실패"))
                updateNotification("시작 실패: ${e.message}")
                stopSelfClean()
                return@launch
            }

            startPolling()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        acPollJob?.cancel()
        batPollJob?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // --- polling ---

    private fun startPolling() {
        // AC 상태 폴링 — 꺼져 있으면 즉시 재시작
        acPollJob = scope.launch {
            while (isActive) {
                delay(AC_POLL_INTERVAL_MS)
                if (ac.getStartState() == 0) {
                    Log.i(TAG, "AC 꺼짐 감지 → 재시작")
                    runCatching {
                        ac.start()
                        ac.setTemperature(prefs.targetTemp)
                        ac.setCycleMode(inLoop = true)
                    }
                }
                // 최대 시간 체크
                val elapsed = System.currentTimeMillis() - startTimeMs
                if (elapsed >= prefs.maxHours * 3_600_000L) {
                    Log.i(TAG, "최대 시간 초과 → 종료")
                    stopCamping()
                    return@launch
                }
                publishState(running())
            }
        }

        // 배터리 + 충전 폴링
        batPollJob = scope.launch {
            while (isActive) {
                delay(BAT_POLL_INTERVAL_MS)

                lastCharging = charging.isConnected()
                lastChargingPowerKw = charging.getChargingPowerKw()
                lastRangeKm = battery.getElecDrivingRangeKm()

                val soc = battery.getSoc()
                if (soc != -1) {
                    lastBatteryPct = soc
                    // 충전 중이면 배터리 임계값 체크 스킵
                    if (!lastCharging && soc < prefs.stopBatteryPct) {
                        Log.i(TAG, "배터리 부족 ($soc% < ${prefs.stopBatteryPct}%) → 종료")
                        stopCamping()
                        return@launch
                    }
                }
                publishState(running())
                updateNotification(statusText())
            }
        }
    }

    private fun stopCamping() {
        scope.launch {
            runCatching {
                ac.stop()
                ac.setCycleMode(inLoop = false)  // 외기순환 복원
            }
            ac.disconnect()
            publishState(CampingState.Idle)
            stopSelfClean()
        }
    }

    private fun stopSelfClean() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun running() = CampingState.Running(
        batteryPct = lastBatteryPct,
        elapsedMs = System.currentTimeMillis() - startTimeMs,
        outsideTemp = ac.getOutsideTemp(),
        targetTemp = prefs.targetTemp,
        isCharging = lastCharging,
        chargingPowerKw = lastChargingPowerKw,
        estimatedRangeKm = lastRangeKm,
    )

    private fun publishState(state: CampingState) {
        (application as LauncherApplication).campingState.value = state
    }

    // --- notification ---

    private fun statusText(): String {
        val elapsed = System.currentTimeMillis() - startTimeMs
        val min = elapsed / 60_000
        val h = min / 60; val m = min % 60
        val timeStr = if (h > 0) "${h}시간 ${m}분" else "${m}분"
        val batStr = if (lastBatteryPct >= 0) " · 배터리 ${lastBatteryPct}%" else ""
        val chargeStr = if (lastCharging) " · 충전중 ${String.format("%.1f", lastChargingPowerKw)}kW" else ""
        return "캠핑 모드 실행 중 ($timeStr$batStr$chargeStr)"
    }

    private fun createNotificationChannel() {
        val ch = NotificationChannel(CHANNEL_ID, "캠핑 모드", NotificationManager.IMPORTANCE_LOW)
            .apply { description = "BYD 캠핑 모드 실행 중" }
        getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
    }

    private fun buildNotification(text: String): Notification {
        val stopPi = PendingIntent.getService(
            this, 0,
            Intent(this, CampingService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val mainPi = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("BYD 캠핑 모드")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(mainPi)
            .addAction(android.R.drawable.ic_media_pause, "종료", stopPi)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, buildNotification(text))
    }
}
