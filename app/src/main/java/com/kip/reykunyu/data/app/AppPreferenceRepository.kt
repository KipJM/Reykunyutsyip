package com.kip.reykunyu.data.app

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kip.reykunyu.data.dict.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class AppPreferenceRepository(
    private val dataStore: DataStore<Preferences>
) {


    val searchLanguage: Flow<Language> = dataStore.data
        .catch {
            if(it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            //Defaults to english
            Language.fromCode(preferences[SEARCH_LANGUAGE] ?: "en") ?: Language.English
        }



    suspend fun updateSearchLanguagePreference(
        language: Language
    ) {
        val targetKey = SEARCH_LANGUAGE

        val targetValue = language.toString()

        dataStore.edit { preferences ->
            preferences[targetKey] = targetValue
        }
    }


    private companion object {
        //val APP_LANGUAGE = stringPreferencesKey("app_lang")
        val SEARCH_LANGUAGE = stringPreferencesKey("search_lang")

        const val TAG = "REYKUNYUSettingsRepo"

    }
}