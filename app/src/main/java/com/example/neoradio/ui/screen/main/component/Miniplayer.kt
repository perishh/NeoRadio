package com.example.neoradio.ui.screen.main.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.neoradio.controller.PlayerController
import com.example.neoradio.ui.screen.main.LocalBottomSheet
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun Miniplayer(modifier: Modifier = Modifier) {
    val controller = koinInject<PlayerController>()
    val sheetState = LocalBottomSheet.current

    val coroutineScope = rememberCoroutineScope()

    val station by controller.station.collectAsStateWithLifecycle()
    val isPlaying by controller.isPlaying.collectAsStateWithLifecycle()
    val isBuffering by controller.isBuffering.collectAsStateWithLifecycle()

    var isFullyExpanded by rememberSaveable { mutableStateOf(false) }
    var isToBeFullyExpanded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(sheetState) {
        snapshotFlow { sheetState.currentValue }
            .collect { state ->
                isFullyExpanded = state == SheetValue.Expanded
            }
    }
    LaunchedEffect(sheetState) {
        snapshotFlow { sheetState.targetValue }
            .collect { state ->
                isToBeFullyExpanded = state == SheetValue.Expanded
            }
    }

    BackHandler(isFullyExpanded) {
        coroutineScope.launch {
            sheetState.partialExpand()
        }
    }

    AnimatedVisibility(isToBeFullyExpanded, enter = expandVertically(), exit = shrinkVertically()) {
        Spacer(modifier = Modifier.statusBarsPadding())
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable {
                coroutineScope.launch {
                    sheetState.expand()
                }
            }
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        station?.let { station ->
            AsyncImage(
                model = station.thumbnail,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    station.name,
                    fontSize = 20.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.W500,
                    modifier = Modifier
                        .fillMaxWidth()
                        .basicMarquee()
                )
                station.city?.let { city ->
                    Text(
                        city,
                        fontSize = 14.sp,
                        lineHeight = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .basicMarquee()
                    )
                }
            }
            AnimatedContent(isBuffering) {
                if (it) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .size(32.dp)
                    )
                } else {
                    IconButton(
                        onClick = {
                            if (isPlaying) {
                                controller.pause()
                            } else {
                                controller.play()
                            }
                        }
                    ) {
                        AnimatedContent(
                            isPlaying,
                            transitionSpec = { slideInVertically() + fadeIn() togetherWith slideOutVertically { it / 2 } + fadeOut() }
                        ) {
                            if (it) {
                                Icon(Icons.Rounded.Pause, contentDescription = "Pause")
                            } else {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = "Play")
                            }
                        }
                    }
                }
            }
        } ?: Text(
            "Επιλέξτε σταθμό...",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}