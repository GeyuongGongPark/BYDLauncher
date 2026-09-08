package com.bydlauncher.domain.update

data class UpdateInfo(
    val latestVersion: String,   // e.g. "1.2.0"
    val downloadUrl: String,
    val releaseNotes: String,
)
