package com.seazon.feedus.ui.article

import androidx.lifecycle.viewModelScope
import com.seazon.feedme.lib.rss.bo.Item
import com.seazon.feedus.cache.RssDatabase
import com.seazon.feedus.data.RssSDK
import com.seazon.feedus.ui.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class ArticleDetailEvent {
    data class GeneralErrorEvent(val message: String) : ArticleDetailEvent()
}

class ArticleDetailViewModel(
    val rssSDK: RssSDK,
    val rssDatabase: RssDatabase,
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
}
