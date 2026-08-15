package com.bydlauncher.domain.navi

import kotlinx.coroutines.flow.Flow

interface NaviRepository {
    fun getSelectedPackage(): Flow<String?>
    suspend fun setSelectedPackage(packageName: String?)
}
