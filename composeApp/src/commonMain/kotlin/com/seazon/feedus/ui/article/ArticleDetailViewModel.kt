package com.seazon.feedus.ui.article

import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.viewModelScope
import com.seazon.feedme.lib.rss.bo.Item
import com.seazon.feedus.cache.RssDatabase
import com.seazon.feedus.data.AppSettings
import com.seazon.feedus.data.RssSDK
import com.seazon.feedus.platform.getSystemLanguage
import com.seazon.feedus.translation.TranslationHelper
import com.seazon.feedus.translation.TranslationNotSupportedException
import com.seazon.feedus.translation.languageCodeToName
import com.seazon.feedus.ui.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class ArticleDetailEvent {
    data class GeneralErrorEvent(val message: String) : ArticleDetailEvent()
    data class TranslationErrorEvent(val message: String) : ArticleDetailEvent()
    object NavigateToTranslationSettings : ArticleDetailEvent()
}

class ArticleDetailViewModel(
    val rssSDK: RssSDK,
    val rssDatabase: RssDatabase,
    val appSettings: AppSettings,
    val translationHelper: TranslationHelper,
) : BaseViewModel() {

    private val _state = MutableStateFlow(ArticleDetailScreenState())
    val state: StateFlow<ArticleDetailScreenState> = _state

    private val _eventFlow = MutableStateFlow<ArticleDetailEvent?>(null)
    val eventFlow: StateFlow<ArticleDetailEvent?> = _eventFlow

    fun init(item: Item?) {
        if (_state.value.item != null || item == null) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, item = item) }
            try {
                val feed = rssDatabase.getFeedById(item.fid.orEmpty())
                val blocks = parseHtmlContent(item.description.orEmpty())
                _state.update {
                    it.copy(isLoading = false, feed = feed, contentBlocks = blocks)
                }
            } catch (e: Exception) {
                _eventFlow.value = ArticleDetailEvent.GeneralErrorEvent(e.message.orEmpty())
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun toggleStar() {
        val item = _state.value.item ?: return
        viewModelScope.launch {
            try {
                val api = rssSDK.getRssApi(false)
                val newItem = if (item.star == Item.STAR_STARRED) {
                    api.markUnstar(arrayOf(item.id))
                    item.copy(star = Item.STAR_UNSTAR)
                } else {
                    api.markStar(arrayOf(item.id))
                    item.copy(star = Item.STAR_STARRED)
                }
                rssDatabase.updateItemStar(newItem)
                _state.update { it.copy(item = newItem) }
            } catch (e: Exception) {
                _eventFlow.value = ArticleDetailEvent.GeneralErrorEvent(e.message.orEmpty())
            }
        }
    }

    fun consumeEvent() {
        _eventFlow.value = null
    }

    fun toTranslationSettings() {
        _eventFlow.value = ArticleDetailEvent.NavigateToTranslationSettings
    }

    fun translate() {
        val modelId = appSettings.getAppPreferences().translationModelId
        if (modelId.isBlank()) {
            toTranslationSettings()
            return
        }

        // If already translated, toggle visibility
        if (_state.value.translatedBlocks != null) {
            _state.update { it.copy(showTranslation = !it.showTranslation) }
            return
        }

        val item = _state.value.item ?: return
        val targetLanguage = languageCodeToName(getSystemLanguage())

        viewModelScope.launch {
            _state.update { it.copy(isTranslating = true) }
            try {
                val titleToTranslate = item.title.orEmpty()

                val translatedTitle = process {
                    if (titleToTranslate.isNotBlank())
                        translationHelper.translate(titleToTranslate, targetLanguage, modelId)
                    else ""
                }

                val translatedBlocks = _state.value.contentBlocks.map { block ->
                    when (block) {
                        is ContentBlock.Heading -> {
                            val t = process { translationHelper.translate(block.text, targetLanguage, modelId) }
                            block.copy(text = t)
                        }
                        is ContentBlock.Paragraph -> {
                            val t = process { translationHelper.translate(block.text.text, targetLanguage, modelId) }
                            block.copy(text = AnnotatedString(t))
                        }
                        is ContentBlock.Quote -> {
                            val t = process { translationHelper.translate(block.text.text, targetLanguage, modelId) }
                            block.copy(text = AnnotatedString(t))
                        }
                        is ContentBlock.ListItemBlock -> {
                            val t = process { translationHelper.translate(block.text.text, targetLanguage, modelId) }
                            block.copy(text = AnnotatedString(t))
                        }
                        // Images, code blocks and dividers are kept as-is
                        else -> block
                    }
                }

                _state.update {
                    it.copy(
                        isTranslating = false,
                        translatedTitle = translatedTitle,
                        translatedBlocks = translatedBlocks,
                        showTranslation = true,
                    )
                }
            } catch (e: TranslationNotSupportedException) {
                _eventFlow.value = ArticleDetailEvent.TranslationErrorEvent(e.message.orEmpty())
                _state.update { it.copy(isTranslating = false) }
            } catch (e: Exception) {
                _eventFlow.value = ArticleDetailEvent.TranslationErrorEvent(e.message.orEmpty())
                _state.update { it.copy(isTranslating = false) }
            }
        }
    }
}
