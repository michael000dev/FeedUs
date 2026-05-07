package com.seazon.feedus.translation

import android.os.Build
import com.ai_model_hub.sdk.ModelAllowlist

actual class TranslationHelper actual constructor() {

    actual fun getAvailableModels(): List<String> =
        ModelAllowlist.models.map { it.name }

    actual suspend fun translate(text: String, targetLanguage: String, modelName: String): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            throw TranslationNotSupportedException(
                "Translation requires Android 12 (API 31) or higher."
            )
        }
        return try {
            com.ai_model_hub.sdk.functional.translate(
                modelName = modelName,
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
