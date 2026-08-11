package com.example.neoradio.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.neoradio.repository.WaveformRepository
import kotlinx.coroutines.launch

@Composable
fun Waveform(
    modifier: Modifier = Modifier,
    barCount: Int = 32,
    barColor: Color = MaterialTheme.colorScheme.primary,
    barGap: Dp = 4.dp
) {
    val rawWaveform by WaveformRepository.sampleFlow.collectAsStateWithLifecycle(
        initialValue = FloatArray(
            barCount
        ) { 0.05f })

    val animatedBars = remember(barCount) {
        List(barCount) { Animatable(0.05f) }
    }

    LaunchedEffect(rawWaveform) {
        rawWaveform.forEachIndexed { index, value ->
            if (index < animatedBars.size) {
                launch {
                    animatedBars[index].animateTo(
                        targetValue = value,
                        animationSpec = tween(durationMillis = 60, easing = LinearEasing)
                    )
                }
            }
        }
    }

    Canvas(modifier = modifier) {
        val gapPx = barGap.toPx()
        val totalGap = gapPx * (barCount - 1)
        val barWidth = (size.width - totalGap) / barCount
        val maxHeight = size.height

        animatedBars.forEachIndexed { index, animatable ->
            val height = (animatable.value * maxHeight).coerceAtLeast(barWidth)
            val x = index * (barWidth + gapPx)
            val y = (maxHeight - height) / 2f // Centered alignment

            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, height),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}