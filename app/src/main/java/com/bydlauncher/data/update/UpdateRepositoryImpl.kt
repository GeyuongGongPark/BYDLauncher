package com.bydlauncher.data.update

import com.bydlauncher.domain.update.UpdateInfo
import com.bydlauncher.domain.update.UpdateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject

class UpdateRepositoryImpl @Inject constructor(
    private val client: OkHttpClient,
) : UpdateRepository {

    override suspend fun checkForUpdate(currentVersion: String): Result<UpdateInfo?> =
        withContext(Dispatchers.IO) {
            runCatching {
                val request = Request.Builder()
                    .url("https://api.github.com/repos/GeyuongGongPark/BYDLauncher/releases/latest")
                    .header("Accept", "application/vnd.github.v3+json")
                    .build()

                val body = client.newCall(request).execute().use { resp ->
                    if (resp.code == 404) return@runCatching null  // 릴리즈 없음
                    check(resp.isSuccessful) { "HTTP ${resp.code}" }
                    resp.body?.string() ?: error("빈 응답")
                } ?: return@runCatching null

                val json = JSONObject(body)
                val tagName = json.getString("tag_name")   // e.g. "v1.2.0"
                val latestVersion = tagName.trimStart('v')
                val releaseNotes = json.optString("body", "").take(200)

                if (!isNewer(latestVersion, currentVersion)) return@runCatching null

                // assets에서 APK URL 찾기
                val assets = json.getJSONArray("assets")
                var downloadUrl: String? = null
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    if (asset.getString("name").endsWith(".apk")) {
                        downloadUrl = asset.getString("browser_download_url")
                        break
                    }
                }
                downloadUrl ?: return@runCatching null

                UpdateInfo(
                    latestVersion = latestVersion,
                    downloadUrl = downloadUrl,
                    releaseNotes = releaseNotes,
                )
            }
        }

    /** semver 비교: latest > current 이면 true */
    private fun isNewer(latest: String, current: String): Boolean {
        val l = latest.split(".").mapNotNull { it.toIntOrNull() }
        val c = current.split(".").mapNotNull { it.toIntOrNull() }
        for (i in 0 until maxOf(l.size, c.size)) {
            val lv = l.getOrElse(i) { 0 }
            val cv = c.getOrElse(i) { 0 }
            if (lv != cv) return lv > cv
        }
        return false
    }
}
