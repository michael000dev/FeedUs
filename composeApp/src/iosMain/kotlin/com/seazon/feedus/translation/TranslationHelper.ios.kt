package com.seazon.feedus.translation

actual class TranslationHelper actual constructor() {

    actual fun getAvailableModels(): List<String> = emptyList()

    actual suspend fun translate(text: String, targetLanguage: String, modelName: String): String {
        throw TranslationNotSupportedException(
            "Translation via AiModelHub is only supported on Android 12+."
        )
    }
}
