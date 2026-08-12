package com.example.neoradio.ui.screen.main.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import com.example.neoradio.ui.screen.main.LocalBottomSheet
import kotlinx.coroutines.launch

@Composable
fun BottomSheet(
    onSizeChanged: (Int) -> Unit
) {
    val sheetState = LocalBottomSheet.current

    val coroutineScope = rememberCoroutineScope()

    var isFullyExpanded by rememberSaveable { mutableStateOf(false) }
    var isToBeFullyExpanded by rememberSaveable { mutableStateOf(false) }
    var isAnimationRunning by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(sheetState) {
        snapshotFlow { sheetState.currentValue }
            .collect { state ->
                isFullyExpanded = state == SheetValue.Expanded
            }
    }

    LaunchedEffect(sheetState) {
        snapshotFlow { sheetState.isAnimationRunning }
            .collect { state ->
                isAnimationRunning = state
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

    Column(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            isToBeFullyExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Spacer(modifier = Modifier.statusBarsPadding())
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            AnimatedContent(
                targetState = isToBeFullyExpanded || (isFullyExpanded && !isAnimationRunning),
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220)) +
                            scaleIn(initialScale = 0.92f, animationSpec = tween(220)))
                        .togetherWith(fadeOut(animationSpec = tween(90)))
                }
            ) { isBig ->
                if (isBig) {
                    PlayingInfo {
                        coroutineScope.launch {
                            sheetState.partialExpand()
                        }
                    }
                } else {
                    Miniplayer(
                        modifier = Modifier.onSizeChanged { size ->
                            onSizeChanged(size.height)
                        }
                    ) {
                        coroutineScope.launch {
                            sheetState.expand()
                        }
                    }
                }
            }
        }
    }
}