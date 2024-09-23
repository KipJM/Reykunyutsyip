package com.kip.reykunyu.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kip.reykunyu.ReykunyutsyipApplication
import com.kip.reykunyu.data.app.AppPreferenceRepository
import com.kip.reykunyu.data.dict.Dialect
import com.kip.reykunyu.data.dict.Language
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


data class AppPreferenceState(
    val searchLanguage: Language = Language.English,
    val dialect: Dialect = Dialect.Combined
)

class PreferenceViewModel(
    private val appPreferenceRepository: AppPreferenceRepository
): ViewModel() {


    val preferenceState: StateFlow<AppPreferenceState> =
        combine(
            appPreferenceRepository.searchLanguage,
            appPreferenceRepository.dialect
        ) { language, dialect ->
            AppPreferenceState(language, dialect)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppPreferenceState()
        )


    fun updateSearchLanguage(
        data: Language
    ){
        viewModelScope.launch {
            //ONLY SEARCH LANGUAGE
            appPreferenceRepository.updateSearchLanguagePreference(data)
        }
    }

    fun updateDialect(
        dialect: Dialect
    ){
        viewModelScope.launch {
            //ONLY SEARCH LANGUAGE
            appPreferenceRepository.updateDialectPreference(dialect)
        }
    }



    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as ReykunyutsyipApplication)
                PreferenceViewModel(application.appPreferenceRepository)
            }
        }
    }


}