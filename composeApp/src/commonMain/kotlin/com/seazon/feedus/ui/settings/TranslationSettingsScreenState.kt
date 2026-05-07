package com.seazon.feedus.ui.settings

data class TranslationSettingsScreenState(
    val availableModels: List<String> = emptyList(),
    val selectedModel: String = "",
    val isSaved: Boolean = false,
)
