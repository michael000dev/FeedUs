package com.seazon.feedus.translation

/**
 * Platform-specific translation helper backed by AiModelHub on Android,
 * and unsupported stubs on iOS / Desktop.
 */
expect class TranslationHelper() {

    /** Returns the list of available models from AiModelHub. */
    fun getAvailableModels(): List<ModelInfo>

    /**
     * Translates [text] from [sourceLanguage] to [targetLanguage] using the specified [modelId].
     * Both language parameters are full English names (e.g. "Chinese", "English").
     *
     * @throws TranslationNotSupportedException on platforms where translation is unavailable.
     * @throws TranslationException on all other translation errors.
     */
    suspend fun translate(text: String, targetLanguage: String, modelId: String): String
}

class TranslationNotSupportedException(message: String) : Exception(message)
class TranslationException(message: String) : Exception(message)

/** Maps ISO 639-1 language codes to the full English names used by AiModelHub translation. */
val LANGUAGE_CODE_TO_NAME: Map<String, String> = mapOf(
    "zh" to "Chinese",
    "en" to "English",
    "es" to "Spanish",
    "fr" to "French",
    "de" to "German",
    "ja" to "Japanese",
    "ko" to "Korean",
    "pt" to "Portuguese",
    "ru" to "Russian",
    "ar" to "Arabic",
    "it" to "Italian",
    "nl" to "Dutch",
    "pl" to "Polish",
    "sv" to "Swedish",
    "da" to "Danish",
    "fi" to "Finnish",
    "nb" to "Norwegian",
    "tr" to "Turkish",
    "cs" to "Czech",
    "hu" to "Hungarian",
    "ro" to "Romanian",
    "uk" to "Ukrainian",
    "id" to "Indonesian",
    "vi" to "Vietnamese",
    "th" to "Thai",
    "hi" to "Hindi",
)

fun languageCodeToName(code: String): String = LANGUAGE_CODE_TO_NAME[code] ?: "English"
