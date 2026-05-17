package com.seazon.feedus.translation

import android.os.Build
import com.ai_model_hub.sdk.AiHubClient

actual class TranslationHelper actual constructor() {

    private val client = AiHubClient()

    init {
        client.connect()
    }

    actual fun getAvailableModels(): List<ModelInfo> =
        client.getAvailableModels().map {
            ModelInfo(
                modelId = it.modelId,
                name = it.name,
                displayName = it.displayName,
                description = it.description,
            )
        }

    actual suspend fun translate(text: String, targetLanguage: String, modelId: String): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            throw TranslationNotSupportedException(
                "Translation requires Android 12 (API 31) or higher."
            )
        }
        return try {
            com.ai_model_hub.sdk.functional.translate(
                modelId = modelId,
                text = text,
                targetLanguage = targetLanguage,
                sourceLanguage = "",
            ).trim()
        } catch (e: TranslationNotSupportedException) {
            throw e
        } catch (e: Exception) {
            throw TranslationException(e.message ?: "Translation failed")
        }
    }
}
