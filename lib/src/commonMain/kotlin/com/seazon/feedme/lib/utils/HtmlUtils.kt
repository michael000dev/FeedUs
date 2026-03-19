package com.seazon.feedme.lib.utils

import com.mohamedrejeb.ksoup.entities.KsoupEntities
import io.ktor.http.Url
import io.ktor.http.authority

object HtmlUtils {

    const val URL_DATA = "data:"
    const val URL_FILE = "file://"
    private const val SLASH = "/"

    fun getFirstImage(html: String?, htmlUrl: String?): String? {
        html ?: return null

        val imgSrcRegex = Regex(
            """<img\s+[^>]*?src\s*=\s*["']([^"']+)["']""",
            RegexOption.IGNORE_CASE
        )
        val url = imgSrcRegex.find(html)?.groupValues?.get(1)?.trim()
            ?.takeIf { it.isNotEmpty() } ?: return null

        return if (!isRightImageUrl(url) && !htmlUrl.isNullOrBlank()) {
            val baseUrl = getBaseUrl(html)
            if (url.startsWith("//")) {
                getScheme(htmlUrl) + url
            } else if (baseUrl.isNotEmpty()) {
                baseUrl + url
            } else {
                var slash = SLASH
                if (url.startsWith(SLASH)) {
                    slash = ""
                }
                getAuthority(htmlUrl) + slash + url
            }
        } else {
            url
        }
    }

    fun isHttpUrl(url: String?): Boolean {
        url ?: return false
        return url.startsWith("http://") || url.startsWith("https://")
    }

    private fun isRightImageUrl(url: String): Boolean {
        url.lowercase().run {
            return if (isHttpUrl(url)) {
                true
            } else url.startsWith(URL_DATA)
        }
    }

    private fun getBaseUrl(html: String): String {
        var baseUrl = ""
        val base_tag_start = "<base href=\""
        val base_tag_end = "\""
        val baseIndex = html.indexOf(base_tag_start, 0)
        if (baseIndex != -1) {
            baseUrl = html.substring(
                baseIndex + base_tag_start.length,
                html.indexOf(base_tag_end, baseIndex + base_tag_start.length)
            )
        }
        return baseUrl
    }

    private fun getAuthority(url: String): String {
        return try {
            val uri = Url(url)
            uri.protocol.name + "://" + uri.authority
        } catch (e: Exception) {
            url
        }
    }

    private fun getScheme(url: String): String {
        return try {
            val uri = Url(url)
            uri.protocol.name + ":"
        } catch (e: Exception) {
            url
        }
    }

    fun encode(s: String): String {
        return KsoupEntities.encodeHtml(s)
    }

    fun decode(s: String): String {
        return KsoupEntities.decodeHtml(s)
    }
}
