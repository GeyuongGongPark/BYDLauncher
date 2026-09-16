package com.bydlauncher.domain.navi

import kotlinx.coroutines.flow.Flow

interface NaviRepository {
    fun getSelectedPackage(): Flow<String?>
    suspend fun setSelectedPackage(packageName: String?)
    fun getDensityDpi(): Flow<Int>      // 0 = 디바이스 기본값
    suspend fun setDensityDpi(dpi: Int)
}
