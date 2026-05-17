package com.seazon.feedus.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import feedus.composeapp.generated.resources.Res
import feedus.composeapp.generated.resources.common_save
import feedus.composeapp.generated.resources.translation_model
import feedus.composeapp.generated.resources.translation_settings_title
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource

@Composable
fun TranslationSettingsScreenComposable(
    stateFlow: StateFlow<TranslationSettingsScreenState>,
    navBack: () -> Unit,
    onSelectModel: (String) -> Unit,
    onSave: () -> Unit,
) {
    val state by stateFlow.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(Res.string.translation_settings_title),
                    style = MaterialTheme.typography.titleMedium,
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = navBack,
                    modifier = Modifier.padding(start = 2.dp, top = 8.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            },
            backgroundColor = MaterialTheme.colorScheme.surface,
            elevation = 0.dp,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(Res.string.translation_model),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (state.availableModels.isEmpty()) {
                Text(
                    text = "No models available. Please open AiModelHub and enable a model.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                state.availableModels.forEach { model ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = state.selectedModelId == model.modelId,
                                onClick = { onSelectModel(model.modelId) },
                                role = Role.RadioButton,
                            )
                            .padding(vertical = 4.dp),
                    ) {
                        RadioButton(
                            selected = state.selectedModelId == model.modelId,
                            onClick = null,
                        )
                        Column(
                            modifier = Modifier.padding(start = 8.dp),
                        ) {
                            Text(
                                text = model.displayName.ifEmpty { model.name },
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            if (model.description.isNotEmpty()) {
                                Text(
                                    text = model.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.selectedModelId.isNotEmpty(),
            ) {
                Text(stringResource(Res.string.common_save))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
