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
        val savedModelId = appSettings.getAppPreferences().translationModelId
        val models = translationHelper.getAvailableModels()
        _state.update {
            it.copy(
                availableModels = models,
                selectedModelId = savedModelId.ifEmpty { models.firstOrNull()?.modelId.orEmpty() },
            )
        }
    }

    fun selectModel(modelId: String) {
        _state.update { it.copy(selectedModelId = modelId) }
    }

    fun save() {
        val prefs = appSettings.getAppPreferences()
        appSettings.saveAppPreferences(prefs.copy(translationModelId = _state.value.selectedModelId))
        _state.update { it.copy(isSaved = true) }
    }
}
