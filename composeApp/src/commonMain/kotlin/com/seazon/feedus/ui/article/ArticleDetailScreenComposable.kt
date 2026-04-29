package com.seazon.feedus.ui.article

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.seazon.feedme.lib.utils.HtmlUtils
import com.seazon.feedme.lib.utils.orZero
import com.seazon.feedus.DateUtil
import com.seazon.feedus.ui.customize.LoadingView
import feedus.composeapp.generated.resources.Res
import feedus.composeapp.generated.resources.article_open_in_browser
import feedus.composeapp.generated.resources.article_star
import feedus.composeapp.generated.resources.article_unstar
import feedus.composeapp.generated.resources.ic_vec_star_fill
import feedus.composeapp.generated.resources.ic_vec_star_outline
import com.seazon.feedme.lib.rss.bo.Item
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ArticleDetailScreenComposable(
    stateFlow: StateFlow<ArticleDetailScreenState>,
    navBack: () -> Unit,
    onToggleStar: () -> Unit,
    onOpenInBrowser: () -> Unit,
    onLinkClick: (String) -> Unit,
) {
    val state by stateFlow.collectAsState()
    val item = state.item

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .statusBarsPadding()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = state.feed?.title ?: item?.title ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = navBack,
                    modifier = Modifier.padding(start = 2.dp, top = 8.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            },
            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
            elevation = 0.dp,
            actions = {
                if (item != null) {
                    val isStarred = item.star == Item.STAR_STARRED
                    IconButton(
                        onClick = onToggleStar,
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        Icon(
                            painter = painterResource(
                                if (isStarred) Res.drawable.ic_vec_star_fill
                                else Res.drawable.ic_vec_star_outline
                            ),
                            contentDescription = stringResource(
                                if (isStarred) Res.string.article_unstar
                                else Res.string.article_star
                            ),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
                IconButton(
                    onClick = onOpenInBrowser,
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInBrowser,
                        contentDescription = stringResource(Res.string.article_open_in_browser),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            },
        )

        if (state.isLoading && item == null) {
            LoadingView()
            return@Column
        }

        if (item == null) return@Column

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
            ) {
                // Hero image
                val visual = item.visual
                if (!visual.isNullOrEmpty() && HtmlUtils.isHttpUrl(visual)) {
                    AsyncImage(
                        model = visual,
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)),
                    )
                }

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Title
                    Text(
                        text = item.title.orEmpty(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Metadata row: feed name · author · date
                    val metaParts = listOfNotNull(
                        state.feed?.title,
                        item.author?.takeIf { it.isNotBlank() },
                        DateUtil.toXAgo(item.publishedDate.orZero()),
                    )
                    if (metaParts.isNotEmpty()) {
                        Row {
                            metaParts.forEachIndexed { idx, part ->
                                if (idx > 0) {
                                    Text(
                                        text = " · ",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.outline,
                                    )
                                }
                                Text(
                                    text = part,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.outline,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Article body
                    if (state.contentBlocks.isNotEmpty()) {
                        ArticleBody(
                            blocks = state.contentBlocks,
                            onLinkClick = onLinkClick,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else if (!state.isLoading) {
                        // No HTML content — show open-in-browser fallback
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onOpenInBrowser,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(Res.string.article_open_in_browser))
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
