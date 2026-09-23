package com.bydlauncher.ui.update

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bydlauncher.BuildConfig
import com.bydlauncher.domain.update.UpdateInfo
import com.bydlauncher.domain.update.UpdateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import javax.inject.Inject

sealed class UpdateState {
    data object Idle : UpdateState()
    data object Checking : UpdateState()
    data class Available(val info: UpdateInfo) : UpdateState()
    data object Downloading : UpdateState()
    data class ReadyToInstall(val apkUri: Uri) : UpdateState()
    data class Error(val message: String) : UpdateState()
}

@HiltViewModel
class UpdateViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val updateRepository: UpdateRepository,
    private val httpClient: OkHttpClient,
) : ViewModel() {

    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)

    companion object {
        private const val TAG = "UpdateViewModel"
    }
    val state: StateFlow<UpdateState> = _state.asStateFlow()

    init {
        checkForUpdate()
    }

    fun checkForUpdate() {
        viewModelScope.launch {
            _state.value = UpdateState.Checking
            val result = updateRepository.checkForUpdate(BuildConfig.VERSION_NAME)
            _state.value = result.fold(
                onSuccess = { info ->
                    if (info != null) UpdateState.Available(info) else UpdateState.Idle
                },
                onFailure = { UpdateState.Idle },  // 업데이트 체크 실패는 조용히 무시
            )
        }
    }

    fun downloadAndInstall(info: UpdateInfo) {
        viewModelScope.launch {
            _state.value = UpdateState.Downloading
            runCatching {
                val apkFile = downloadApk(info.downloadUrl)

                if (!verifyApkSignature(apkFile)) {
                    apkFile.delete()
                    _state.value = UpdateState.Error("서명 검증 실패 — 신뢰할 수 없는 APK")
                    return@launch
                }

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    apkFile,
                )
                _state.value = UpdateState.ReadyToInstall(uri)
                launchInstaller(uri)
            }.onFailure {
                _state.value = UpdateState.Error(it.message ?: "다운로드 실패")
            }
        }
    }

    /**
     * 다운로드된 APK의 서명 인증서가 현재 앱의 서명과 일치하는지 검증.
     * 동일한 개인키로 서명된 APK만 통과.
     */
    private fun verifyApkSignature(apkFile: File): Boolean = runCatching {
        val pm = context.packageManager
        val flags = PackageManager.GET_SIGNING_CERTIFICATES

        val currentFingerprints = pm.getPackageInfo(context.packageName, flags)
            .signingInfo?.signingCertificateHistory
            ?.map { it.sha256() }
            ?.toSet()
            ?: return false

        val apkFingerprints = pm.getPackageArchiveInfo(apkFile.absolutePath, flags)
            ?.signingInfo?.signingCertificateHistory
            ?.map { it.sha256() }
            ?.toSet()
            ?: return false

        val matched = currentFingerprints.intersect(apkFingerprints).isNotEmpty()
        Log.i(TAG, "서명 검증: ${if (matched) "통과" else "실패"}")
        matched
    }.getOrElse { e ->
        Log.e(TAG, "서명 검증 오류: ${e.message}")
        false
    }

    private fun android.content.pm.Signature.sha256(): String {
        val cert = CertificateFactory.getInstance("X.509")
            .generateCertificate(toByteArray().inputStream())
        return MessageDigest.getInstance("SHA-256")
            .digest(cert.encoded)
            .joinToString("") { "%02x".format(it) }
    }

    private suspend fun downloadApk(url: String): File = withContext(Dispatchers.IO) {
        val apkDir = File(context.cacheDir, "apk").also { it.mkdirs() }
        val apkFile = File(apkDir, "update.apk")

        val request = Request.Builder().url(url).build()
        httpClient.newCall(request).execute().use { resp ->
            check(resp.isSuccessful) { "HTTP ${resp.code}" }
            resp.body?.byteStream()?.use { input ->
                apkFile.outputStream().use { output -> input.copyTo(output) }
            } ?: error("빈 응답")
        }
        apkFile
    }

    private fun launchInstaller(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
