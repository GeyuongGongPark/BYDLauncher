package com.bydlauncher.domain.navi

data class NaviApp(
    val packageName: String,
    val displayName: String,
)

/** DiLink에서 흔히 쓰는 네비 앱 목록 */
val SUPPORTED_NAVI_APPS = listOf(
    NaviApp("net.daum.android.map", "카카오맵"),
    NaviApp("com.skt.tmap.ku", "T맵"),
    NaviApp("com.nhn.android.nmap", "네이버지도"),
    NaviApp("com.google.android.apps.maps", "구글맵"),
)
