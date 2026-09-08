package com.bydlauncher.domain.update

interface UpdateRepository {
    /** 최신 릴리즈 정보 조회. 현재 버전보다 높으면 UpdateInfo 반환, 같으면 null */
    suspend fun checkForUpdate(currentVersion: String): Result<UpdateInfo?>
}
