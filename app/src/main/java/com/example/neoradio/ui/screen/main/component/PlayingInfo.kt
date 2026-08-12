package com.example.neoradio.ui.screen.main.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Snooze
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.neoradio.controller.PlayerController
import com.example.neoradio.ui.component.NetImage
import com.example.neoradio.ui.component.Waveform
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.minutes

@Composable
fun PlayingInfo() {
    val controller = koinInject<PlayerController>()

    val media by controller.station.collectAsStateWithLifecycle()

    if (media == null) return

    val isPlaying by controller.isPlaying.collectAsStateWithLifecycle()
    val isBuffering by controller.isBuffering.collectAsStateWithLifecycle()

    var sleepTimerDisabled by remember { mutableStateOf(false) }

    val remainingTime by controller.remainingTime.collectAsStateWithLifecycle(null)
    LaunchedEffect(remainingTime) {
        sleepTimerDisabled = false
    }

    var isDialogOpen by remember { mutableStateOf(false) }

    if (isDialogOpen) {
        SleepTimerDialog(
            onConfirm = {
                it?.let {
                    if (it > 0L) {
                        controller.setSleepTimer(it.minutes)
                        sleepTimerDisabled = true
                    }
                }
            },
            onDismissRequest = {
                isDialogOpen = false
            }
        )
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NetImage(
            model = media!!.thumbnail,
            contentDescription = null,
            modifier = Modifier.size(170.dp),
            contentScale = ContentScale.Fit
        )

        Text(
            media!!.name,
            fontSize = 28.sp,
            fontWeight = FontWeight.W600,
            modifier = Modifier
                .fillMaxWidth()
                .basicMarquee()
                .padding(top = 32.dp),
            textAlign = TextAlign.Center
        )
        media!!.city?.let { city ->
            Text(
                city,
                fontSize = 18.sp,
                fontWeight = FontWeight.W500,
                modifier = Modifier
                    .fillMaxWidth()
                    .basicMarquee(),
                textAlign = TextAlign.Center
            )
        }

        Waveform(
            modifier = Modifier
                .padding(top = 48.dp)
                .height(48.dp)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )

        FilledTonalIconButton(
            modifier = Modifier
                .padding(top = 48.dp)
                .size(84.dp),
            enabled = !isBuffering,
            onClick = {
                if (isPlaying) {
                    controller.pause()
                } else {
                    controller.play()
                }
            }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AnimatedContent(
                    isPlaying,
                    modifier = Modifier.align(Alignment.Center)
                ) { isPlaying ->
                    if (isPlaying) {
                        Icon(
                            Icons.Rounded.Pause,
                            contentDescription = "Pause",
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        Icon(
                            Icons.Rounded.PlayArrow,
                            contentDescription = "Play",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                androidx.compose.animation.AnimatedVisibility(
                    isBuffering,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxSize()
                ) {
                    CircularProgressIndicator(modifier = Modifier.fillMaxSize())
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(top = 32.dp)
        ) {
            FilledTonalIconToggleButton(
                enabled = !sleepTimerDisabled,
                checked = remainingTime != null,
                onCheckedChange = {
                    if (remainingTime != null) {
                        controller.stopSleepTimer()
                        sleepTimerDisabled = true
                    } else {
                        isDialogOpen = true
                    }
                }
            ) {
                Icon(Icons.Rounded.Snooze, contentDescription = "Sleep timer")
            }
            AnimatedVisibility(remainingTime != null) {
                Text(
                    text = remainingTime?.toString() ?: "00:00",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W500,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }


    }
}