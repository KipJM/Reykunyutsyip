package com.kip.reykunyu.viewmodels

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")


class SettingsViewModel: ViewModel() {
    enum class LanguageSettings
    {
        AppLanguage,
        SearchLanguage
    }


}