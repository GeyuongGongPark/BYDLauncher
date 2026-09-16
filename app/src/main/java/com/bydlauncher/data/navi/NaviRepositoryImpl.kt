package com.bydlauncher.data.navi

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bydlauncher.domain.navi.NaviRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.naviDataStore: DataStore<Preferences>
    by preferencesDataStore(name = "navi")

class NaviRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : NaviRepository {

    private val KEY_PKG = stringPreferencesKey("selected_package")
    private val KEY_DPI = intPreferencesKey("density_dpi")

    override fun getSelectedPackage(): Flow<String?> =
        context.naviDataStore.data.map { it[KEY_PKG] }

    override suspend fun setSelectedPackage(packageName: String?) {
        context.naviDataStore.edit { prefs ->
            if (packageName == null) prefs.remove(KEY_PKG)
            else prefs[KEY_PKG] = packageName
        }
    }

    override fun getDensityDpi(): Flow<Int> =
        context.naviDataStore.data.map { it[KEY_DPI] ?: 0 }

    override suspend fun setDensityDpi(dpi: Int) {
        context.naviDataStore.edit { it[KEY_DPI] = dpi }
    }
}
