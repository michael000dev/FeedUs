package com.seazon.feedus.ui.article

import com.seazon.feedme.lib.rss.bo.Feed
import com.seazon.feedme.lib.rss.bo.Item

data class ArticleDetailScreenState(
    val isLoading: Boolean = false,
    val item: Item? = null,
    val feed: Feed? = null,
    val contentBlocks: List<ContentBlock> = emptyList(),
)
