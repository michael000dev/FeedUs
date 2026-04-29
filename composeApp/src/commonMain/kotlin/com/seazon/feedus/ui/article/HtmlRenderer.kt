package com.seazon.feedus.ui.article

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser
import com.seazon.feedme.lib.utils.HtmlUtils

// ======================== Data model ========================

sealed class ContentBlock {
    data class Heading(val text: String, val level: Int) : ContentBlock()
    data class Paragraph(val text: AnnotatedString) : ContentBlock()
    data class ArticleImage(val url: String, val alt: String?) : ContentBlock()
    data class Quote(val text: AnnotatedString) : ContentBlock()
    data class CodeBlock(val text: String) : ContentBlock()
    data class ListItemBlock(val text: AnnotatedString, val ordered: Boolean, val index: Int) : ContentBlock()
    data object HorizontalRule : ContentBlock()
}

// ======================== HTML Parser ========================

fun parseHtmlContent(html: String): List<ContentBlock> {
    if (html.isBlank()) return emptyList()
    val blocks = mutableListOf<ContentBlock>()
    val state = ParseState(blocks)
    val handler = KsoupHtmlHandler.Builder()
        .onOpenTag { name, attributes, _ -> state.openTag(name.lowercase(), attributes) }
        .onCloseTag { name, _ -> state.closeTag(name.lowercase()) }
        .onText { text -> state.text(text) }
        .build()
    val parser = KsoupHtmlParser(handler = handler)
    parser.write(html)
    parser.end()
    state.finalize()
    return blocks
}

private class ParseState(private val blocks: MutableList<ContentBlock>) {

    private val tagStack = ArrayDeque<String>()
    private var blockType: String? = null
    private val buf = StringBuilder()
    private var inPre = false

    private val openSpans = ArrayDeque<OpenSpan>()
    private val closedSpans = mutableListOf<ClosedSpan>()

    private var listOrdered = false
    private var listItemIdx = 0

    private enum class SpanType { BOLD, ITALIC, LINK, INLINE_CODE }

    private data class OpenSpan(val type: SpanType, val start: Int, val href: String? = null)
    private data class ClosedSpan(val type: SpanType, val start: Int, val end: Int, val href: String? = null)

    fun openTag(tag: String, attrs: Map<String, String>) {
        tagStack.addLast(tag)
        when (tag) {
            "p" -> startBlock("p")
            "h1" -> startBlock("h1")
            "h2" -> startBlock("h2")
            "h3" -> startBlock("h3")
            "h4" -> startBlock("h4")
            "h5" -> startBlock("h5")
            "h6" -> startBlock("h6")
            "blockquote" -> startBlock("blockquote")
            "pre" -> {
                startBlock("pre")
                inPre = true
            }
            "li" -> {
                startBlock("li")
                listItemIdx++
            }
            "ul" -> {
                listOrdered = false
                listItemIdx = 0
            }
            "ol" -> {
                listOrdered = true
                listItemIdx = 0
            }
            "div", "article", "section" -> {
                if (blockType == null) blockType = "p"
                else {
                    flushBlock()
                    blockType = "p"
                }
            }
            "img" -> {
                flushBlock()
                val src = attrs["src"] ?: return
                if (src.isNotEmpty() && HtmlUtils.isHttpUrl(src)) {
                    blocks.add(ContentBlock.ArticleImage(src, attrs["alt"]))
                }
            }
            "strong", "b" -> openSpans.addLast(OpenSpan(SpanType.BOLD, buf.length))
            "em", "i" -> openSpans.addLast(OpenSpan(SpanType.ITALIC, buf.length))
            "a" -> {
                val href = attrs["href"]
                if (!href.isNullOrEmpty()) openSpans.addLast(OpenSpan(SpanType.LINK, buf.length, href))
            }
            "code" -> {
                if (!inPre) openSpans.addLast(OpenSpan(SpanType.INLINE_CODE, buf.length))
            }
            "br" -> buf.append("\n")
            "hr" -> {
                flushBlock()
                blocks.add(ContentBlock.HorizontalRule)
            }
        }
    }

    fun closeTag(tag: String) {
        tagStack.removeLastOrNull()
        when (tag) {
            "p", "h1", "h2", "h3", "h4", "h5", "h6", "blockquote", "li" -> flushBlock()
            "pre" -> {
                inPre = false
                flushBlock()
            }
            "div", "article", "section" -> flushBlock()
            "strong", "b" -> closeSpan(SpanType.BOLD)
            "em", "i" -> closeSpan(SpanType.ITALIC)
            "a" -> closeSpan(SpanType.LINK)
            "code" -> {
                if (!inPre) closeSpan(SpanType.INLINE_CODE)
            }
        }
    }

    fun text(rawText: String) {
        val text = if (inPre) rawText else rawText.replace(Regex("\\s+"), " ")
        if (blockType == null) {
            if (text.isNotBlank()) {
                blockType = "p"
                buf.append(text)
            }
        } else {
            buf.append(text)
        }
    }

    fun finalize() {
        flushBlock()
    }

    private fun startBlock(type: String) {
        flushBlock()
        blockType = type
    }

    private fun closeSpan(type: SpanType) {
        val idx = openSpans.indexOfLast { it.type == type }
        if (idx >= 0) {
            val open = openSpans.removeAt(idx)
            val end = buf.length
            if (open.start < end) {
                closedSpans.add(ClosedSpan(type, open.start, end, open.href))
            }
        }
    }

    private fun flushBlock() {
        val type = blockType ?: return
        blockType = null

        // Close any remaining open spans at the end of the block
        while (openSpans.isNotEmpty()) {
            val open = openSpans.removeLast()
            val end = buf.length
            if (open.start < end) {
                closedSpans.add(ClosedSpan(open.type, open.start, end, open.href))
            }
        }

        val rawText = buf.toString()
        buf.clear()

        val trimmedText = if (inPre) rawText else rawText.trim()
        if (trimmedText.isEmpty()) {
            closedSpans.clear()
            return
        }

        val leadingSpaces = rawText.length - rawText.trimStart().length

        val annotated = buildAnnotatedString {
            append(trimmedText)
            for (span in closedSpans) {
                val adjStart = (span.start - leadingSpaces).coerceIn(0, trimmedText.length)
                val adjEnd = (span.end - leadingSpaces).coerceIn(0, trimmedText.length)
                if (adjStart >= adjEnd) continue
                when (span.type) {
                    SpanType.BOLD -> addStyle(SpanStyle(fontWeight = FontWeight.Bold), adjStart, adjEnd)
                    SpanType.ITALIC -> addStyle(SpanStyle(fontStyle = FontStyle.Italic), adjStart, adjEnd)
                    SpanType.LINK -> {
                        addStyle(SpanStyle(textDecoration = TextDecoration.Underline), adjStart, adjEnd)
                        addStringAnnotation("URL", span.href ?: "", adjStart, adjEnd)
                    }
                    SpanType.INLINE_CODE -> addStyle(
                        SpanStyle(fontFamily = FontFamily.Monospace),
                        adjStart, adjEnd
                    )
                }
            }
        }
        closedSpans.clear()

        val decoded = HtmlUtils.decode(trimmedText)
        when (type) {
            "h1" -> blocks.add(ContentBlock.Heading(decoded, 1))
            "h2" -> blocks.add(ContentBlock.Heading(decoded, 2))
            "h3" -> blocks.add(ContentBlock.Heading(decoded, 3))
            "h4" -> blocks.add(ContentBlock.Heading(decoded, 4))
            "h5" -> blocks.add(ContentBlock.Heading(decoded, 5))
            "h6" -> blocks.add(ContentBlock.Heading(decoded, 6))
            "blockquote" -> blocks.add(ContentBlock.Quote(annotated))
            "pre" -> blocks.add(ContentBlock.CodeBlock(trimmedText))
            "li" -> blocks.add(ContentBlock.ListItemBlock(annotated, listOrdered, listItemIdx))
            else -> {
                // Decode entities in the annotated string text for paragraphs
                val decodedAnnotated = buildAnnotatedString {
                    append(HtmlUtils.decode(trimmedText))
                    for (span in annotated.spanStyles) {
                        addStyle(span.item, span.start, span.end)
                    }
                    for (annotation in annotated.getStringAnnotations("URL", 0, annotated.length)) {
                        addStringAnnotation("URL", annotation.item, annotation.start, annotation.end)
                    }
                }
                blocks.add(ContentBlock.Paragraph(decodedAnnotated))
            }
        }
    }
}

// ======================== Composable renderer ========================

@Composable
fun ArticleBody(
    blocks: List<ContentBlock>,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        for (block in blocks) {
            when (block) {
                is ContentBlock.Heading -> HeadingBlock(block)
                is ContentBlock.Paragraph -> ParagraphBlock(block, onLinkClick)
                is ContentBlock.ArticleImage -> ImageBlock(block)
                is ContentBlock.Quote -> QuoteBlock(block, onLinkClick)
                is ContentBlock.CodeBlock -> CodeBlockComposable(block)
                is ContentBlock.ListItemBlock -> ListItemComposable(block, onLinkClick)
                ContentBlock.HorizontalRule -> HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun HeadingBlock(block: ContentBlock.Heading) {
    val style = when (block.level) {
        1 -> MaterialTheme.typography.headlineMedium
        2 -> MaterialTheme.typography.headlineSmall
        3 -> MaterialTheme.typography.titleLarge
        else -> MaterialTheme.typography.titleMedium
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = block.text,
        style = style,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun ParagraphBlock(block: ContentBlock.Paragraph, onLinkClick: (String) -> Unit) {
    val linkColor = MaterialTheme.colorScheme.primary
    val style = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurface,
    )
    // Re-apply link color from theme at render time
    val styledText = buildAnnotatedString {
        append(block.text)
        for (annotation in block.text.getStringAnnotations("URL", 0, block.text.length)) {
            addStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline), annotation.start, annotation.end)
        }
    }
    @Suppress("DEPRECATION")
    ClickableText(
        text = styledText,
        style = style,
        onClick = { offset ->
            styledText.getStringAnnotations("URL", offset, offset)
                .firstOrNull()?.let { onLinkClick(it.item) }
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ImageBlock(block: ContentBlock.ArticleImage) {
    Spacer(modifier = Modifier.height(4.dp))
    AsyncImage(
        model = block.url,
        contentDescription = block.alt,
        contentScale = ContentScale.FillWidth,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
    )
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun QuoteBlock(block: ContentBlock.Quote, onLinkClick: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(height = with(androidx.compose.ui.platform.LocalDensity.current) {
                    (MaterialTheme.typography.bodyMedium.lineHeight.value * 1.2f).dp
                })
                .background(
                    MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(2.dp),
                )
        )
        Spacer(modifier = Modifier.width(8.dp))
        val linkColor = MaterialTheme.colorScheme.primary
        val styledText = buildAnnotatedString {
            append(block.text)
            for (annotation in block.text.getStringAnnotations("URL", 0, block.text.length)) {
                addStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline), annotation.start, annotation.end)
            }
        }
        @Suppress("DEPRECATION")
        ClickableText(
            text = styledText,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            onClick = { offset ->
                styledText.getStringAnnotations("URL", offset, offset)
                    .firstOrNull()?.let { onLinkClick(it.item) }
            },
        )
    }
}

@Composable
private fun CodeBlockComposable(block: ContentBlock.CodeBlock) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
    ) {
        Text(
            text = block.text,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ListItemComposable(block: ContentBlock.ListItemBlock, onLinkClick: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth()) {
        val bullet = if (block.ordered) "${block.index}." else "•"
        Text(
            text = bullet,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(24.dp),
        )
        val linkColor = MaterialTheme.colorScheme.primary
        val styledText = buildAnnotatedString {
            append(block.text)
            for (annotation in block.text.getStringAnnotations("URL", 0, block.text.length)) {
                addStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline), annotation.start, annotation.end)
            }
        }
        @Suppress("DEPRECATION")
        ClickableText(
            text = styledText,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
            ),
            onClick = { offset ->
                styledText.getStringAnnotations("URL", offset, offset)
                    .firstOrNull()?.let { onLinkClick(it.item) }
            },
            modifier = Modifier.weight(1f),
        )
    }
}
