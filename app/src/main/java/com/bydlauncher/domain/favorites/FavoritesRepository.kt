package com.bydlauncher.domain.favorites

import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun getFavoritePackages(): Flow<List<String>>
    suspend fun add(packageName: String)
    suspend fun remove(packageName: String)
    suspend fun reorder(packageNames: List<String>)
}
