package com.seazon.feedme.lib.rss.service.folo.bo

import com.seazon.feedme.lib.rss.bo.RssItem
import com.seazon.feedme.lib.utils.DateUtil
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FoloEntries(
    val read: Boolean? = null,
    val view: Int? = null,
    val entries: FoloEntry? = null,
    val feeds: FoloFeed? = null,
    val subscriptions: FoloCategory? = null,
    val collections: FoloCollection? = null,
) {
    fun convert(): RssItem? {
        val entry = entries
        val feed = feeds
        return entry?.convert(
            feed = feed,
            category = subscriptions,
            read = read,
            collections = collections
        )
    }
}

@Serializable
data class FoloEntry(
    val title: String? = null,
    val url: String? = null,
    val content: String? = null,
    val description: String? = null,
    val id: String? = null,
    val author: String? = null,
    val publishedAt: String? = null,
    val media: List<FoloMedia>? = null,
    val attachments: List<FoloAttachment>? = null,
) {
    fun convert(
        feed: FoloFeed? = null,
        category: FoloCategory? = null,
        read: Boolean? = null,
        collections: FoloCollection? = null,
    ): RssItem {
        val audioAttachment = attachments?.firstOrNull { it.mimeType?.startsWith("audio") == true }
        val duration = attachments?.firstOrNull { it.durationInSeconds != null }?.durationInSeconds
        return RssItem(
            id = id,
            fid = feed?.id.orEmpty(),
            feed = feed?.convert(category),
            title = title.orEmpty(),
            link = url.orEmpty(),
            author = author,
            publisheddate = DateUtil.isoStringToTimestamp(publishedAt),
            updateddate = DateUtil.isoStringToTimestamp(publishedAt),
            description = content,
            tags = null,
            visual = media?.firstOrNull { it.type == "photo" }?.url,
            isUnread = !(read ?: false),
            isStar = collections != null,
            podcastUrl = audioAttachment?.url,
            podcastSize = audioAttachment?.sizeInBytes?.toLong(),
            duration = duration?.toLong(),
        )
    }
}

@Serializable
data class FoloAttachment(
    val url: String? = null,
    @SerialName("mime_type")
    val mimeType: String? = null,
    @SerialName("size_in_bytes")
    val sizeInBytes: String? = null,
    @SerialName("duration_in_seconds")
    val durationInSeconds: Int? = null,
)

@Serializable
data class FoloMedia(
    val url: String? = null,
    val type: String? = null, // photo
)

@Serializable
data class FoloCollection(
    val createdAt: String? = null,
)
