package com.seazon.feedme.lib.rss.service.feedly.bo

import com.seazon.feedme.lib.rss.bo.RssTag
import kotlinx.serialization.Serializable

@Serializable
class FeedlyTag : RssTag() {
    fun parseLabelFromId(): String? {
        val i: Int = id?.lastIndexOf("/") ?: -1
        if (i == -1) return null

        return id?.substring(i + 1)
    }
}
