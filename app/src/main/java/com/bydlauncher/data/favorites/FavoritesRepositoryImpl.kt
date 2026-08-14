package com.bydlauncher.data.favorites

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bydlauncher.domain.favorites.FavoritesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.favoritesDataStore: DataStore<Preferences>
    by preferencesDataStore(name = "favorites")

class FavoritesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : FavoritesRepository {

    private val KEY = stringPreferencesKey("packages")
    private val MAX = 8

    override fun getFavoritePackages(): Flow<List<String>> =
        context.favoritesDataStore.data.map { prefs ->
            prefs[KEY]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
        }

    override suspend fun add(packageName: String) {
        context.favoritesDataStore.edit { prefs ->
            val current = prefs[KEY]?.split(",")?.filter { it.isNotBlank() }?.toMutableList()
                ?: mutableListOf()
            if (packageName !in current && current.size < MAX) {
                current.add(packageName)
                prefs[KEY] = current.joinToString(",")
            }
        }
    }

    override suspend fun remove(packageName: String) {
        context.favoritesDataStore.edit { prefs ->
            val current = prefs[KEY]?.split(",")?.filter { it.isNotBlank() }?.toMutableList()
                ?: mutableListOf()
            current.remove(packageName)
            prefs[KEY] = current.joinToString(",")
        }
    }

    override suspend fun reorder(packageNames: List<String>) {
        context.favoritesDataStore.edit { prefs ->
            prefs[KEY] = packageNames.joinToString(",")
        }
    }
}
