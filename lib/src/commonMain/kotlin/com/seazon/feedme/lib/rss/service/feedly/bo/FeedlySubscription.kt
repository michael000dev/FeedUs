package com.seazon.feedme.lib.rss.service.feedly.bo

import com.seazon.feedme.lib.rss.bo.Entity
import com.seazon.feedme.lib.rss.bo.RssCategory
import com.seazon.feedme.lib.rss.bo.RssFeed
import com.seazon.feedme.lib.rss.bo.RssTag
import com.seazon.feedme.lib.rss.service.feedly.FeedlyConstants
import kotlinx.serialization.Serializable

@Serializable
class FeedlySubscription : Entity() {
    /**
     * Sample : feed/http://daichuanqing.com/index.php/feed
     */
    var id: String = ""
    var title: String? = null
    var updated: Long = 0
    var categories: List<FeedlyCategory>? = null
    var website: String? = null
    var sortid: String? = null
    var visualUrl: String? = null

    fun convert(): RssFeed {
        val feed = RssFeed()
        feed.id = id
        feed.title = title
        feed.url = website
        feed.feedUrl = id.substring(5)
        feed.favicon = visualUrl
        feed.categories = categories?.map {
            RssCategory(it.id, it.label)
        }?.filter {
            !FeedlyConstants.isIgnoredTag(it.label) && !FeedlyConstants.isIgnoredForTag(it.label)
        } ?: ArrayList()
        return feed
    }
}

fun Collection<FeedlySubscription>.convert(): List<RssFeed> = map {
    it.convert()
}

fun Collection<FeedlyTag>.convert2(): List<RssTag> = map {
    RssTag(
        id = it.id,
        label = it.label ?: it.parseLabelFromId()
    )
}.filter {
    !FeedlyConstants.isIgnoredTag(it.label) && !FeedlyConstants.isIgnoredForTag(it.label)
}
