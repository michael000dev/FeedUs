package com.seazon.feedus.ui.article

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalUriHandler
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import com.seazon.feedme.lib.rss.bo.Item
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ArticleDetailScreen(
    item: Item?,
    navBack: () -> Unit,
) {
    val viewModel = koinViewModel<ArticleDetailViewModel>()
    val toaster = rememberToasterState()
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(viewModel.eventFlow) {
        viewModel.eventFlow.collect {
            when (it) {
                is ArticleDetailEvent.GeneralErrorEvent -> toaster.show(it.message)
                else -> {}
            }
        }
    }

    viewModel.init(item)

    Toaster(state = toaster)
    ArticleDetailScreenComposable(
        stateFlow = viewModel.state,
        navBack = navBack,
        onToggleStar = { viewModel.toggleStar() },
        onOpenInBrowser = {
            val link = viewModel.state.value.item?.link.orEmpty()
            if (link.isNotEmpty()) uriHandler.openUri(link)
        },
        onLinkClick = { url ->
            if (url.isNotEmpty()) uriHandler.openUri(url)
        },
    )
}
