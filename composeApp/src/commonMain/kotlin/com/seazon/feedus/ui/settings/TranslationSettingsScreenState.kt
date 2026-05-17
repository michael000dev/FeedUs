package com.seazon.feedus.ui.settings

import com.seazon.feedus.translation.ModelInfo

data class TranslationSettingsScreenState(
    val availableModels: List<ModelInfo> = emptyList(),
    val selectedModelId: String = "",
    val isSaved: Boolean = false,
)
