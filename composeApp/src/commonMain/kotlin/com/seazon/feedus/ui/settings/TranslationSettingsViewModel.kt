package com.seazon.feedus.ui.settings

import com.seazon.feedus.data.AppSettings
import com.seazon.feedus.translation.TranslationHelper
import com.seazon.feedus.ui.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class TranslationSettingsViewModel(
    private val appSettings: AppSettings,
    private val translationHelper: TranslationHelper,
) : BaseViewModel() {

    private val _state = MutableStateFlow(TranslationSettingsScreenState())
    val state: StateFlow<TranslationSettingsScreenState> = _state

    init {
        val savedModel = appSettings.getAppPreferences().translationModelName
        val models = translationHelper.getAvailableModels()
        _state.update {
            it.copy(
                availableModels = models,
                selectedModel = if (savedModel.isNotEmpty()) savedModel else models.firstOrNull().orEmpty(),
            )
        }
    }

    fun selectModel(modelName: String) {
        _state.update { it.copy(selectedModel = modelName) }
    }

    fun save() {
        val prefs = appSettings.getAppPreferences()
        appSettings.saveAppPreferences(prefs.copy(translationModelName = _state.value.selectedModel))
        _state.update { it.copy(isSaved = true) }
    }
}
