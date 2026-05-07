package com.seazon.feedus.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TranslationSettingsScreen(
    navBack: () -> Unit,
) {
    val viewModel = koinViewModel<TranslationSettingsViewModel>()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) navBack()
    }

    TranslationSettingsScreenComposable(
        stateFlow = viewModel.state,
        navBack = navBack,
        onSelectModel = { viewModel.selectModel(it) },
        onSave = { viewModel.save() },
    )
}
