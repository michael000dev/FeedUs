package com.seazon.feedme.lib.rss.service.folo

object FoloConstants {
    const val EXPIRED_TIMESTAMP = (7 * 24 * 3600).toLong() // seconds
    const val SCHEMA_HTTPS = "https://api.folo.is"
    const val API = ""
    const val TOKEN = "/better-auth/one-time-token/apply"
    const val HTTP_HEADER_AUTHORIZATION_KEY = "Authorization"
    const val URL_READS = "$API/reads"
    const val URL_SUBSCRIPTIONS = "$API/subscriptions"
    const val URL_COLLECTIONS = "$API/collections"
    const val URL_FEEDS = "$API/feeds"
    const val URL_DISCOVER = "$API/discover"
    const val URL_ENTRIES = "$API/entries"
    const val URL_STREAM = "$API/entries/stream"
}
