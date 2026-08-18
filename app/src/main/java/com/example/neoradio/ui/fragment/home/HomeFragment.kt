package com.example.neoradio.ui.fragment.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.neoradio.player.PlayerController
import com.example.neoradio.rememberDominantColor
import com.example.neoradio.ui.component.radioListRow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeFragment(modifier: Modifier = Modifier, viewModel: HomeViewModel = koinViewModel()) {
    val controller = koinInject<PlayerController>()

    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val liked by viewModel.liked.collectAsStateWithLifecycle()
    val featured by viewModel.featured.collectAsStateWithLifecycle()

    val lists = remember(liked, featured) { listOf(Pair("ΑΓΑΠΗΜΕΝΑ", liked)) + featured }

    var visibleIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .map {
                it.visibleItemsInfo.lastOrNull {
                    it.offset < 500
                }?.index
            }.distinctUntilChanged().collect {
                it?.let {
                    visibleIndex = it
                }
            }
    }

    val dominantColor by rememberDominantColor(
        lists.getOrNull(visibleIndex)?.second?.firstOrNull()?.thumbnail
    )

    val animatedColor by animateColorAsState(dominantColor)

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0f),
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(
                        alpha = 0.95f
                    )
                ),
                navigationIcon = {
                    Icon(
                        Icons.Rounded.Radio,
                        contentDescription = null,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                },
                title = {
                    Text("NeoRadio")
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if (animatedColor != Color.Transparent) animatedColor.copy(alpha = 0.3f) else Color.Transparent,
                            animatedColor.copy(alpha = 0f)
                        ),
                        center = Offset(0f, 150f),
                        radius = 1000f
                    )
                ),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            contentPadding = innerPadding + PaddingValues(top = 16.dp)
        ) {
            if (featured.isEmpty()) {
                item {
                    LinearProgressIndicator(modifier = Modifier.fillMaxSize())
                }
            } else {
                radioListRow(lists) { controller.play(it) }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}