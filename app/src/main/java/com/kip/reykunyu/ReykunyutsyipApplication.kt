package com.kip.reykunyu

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.kip.reykunyu.data.app.AppPreferenceRepository


private const val APP_PREFERENCES_NAME = "app_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = APP_PREFERENCES_NAME
)

class ReykunyutsyipApplication: Application() {
    lateinit var appPreferenceRepository: AppPreferenceRepository

    override fun onCreate() {
        super.onCreate()
        appPreferenceRepository = AppPreferenceRepository(dataStore)
    }
}