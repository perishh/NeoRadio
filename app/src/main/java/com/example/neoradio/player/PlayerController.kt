package com.example.neoradio.player

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionToken
import com.example.neoradio.model.Station
import com.example.neoradio.repository.StreamRepository
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class PlayerController(
    context: Context,
    private val coroutineScope: CoroutineScope,
) : Player.Listener {
    private val kv = MMKV.defaultMMKV()

    private val _controller = MutableStateFlow<MediaController?>(null)
    val controller get() = _controller.value

    init {
        // TODO: Implement reconnection strategy
        val sessionToken =
            SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture.addListener({
            _controller.value = controllerFuture.get().apply { addListener(this@PlayerController) }
        }, ContextCompat.getMainExecutor(context))
    }

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering = _isBuffering.asStateFlow()

    private val _station = MutableStateFlow<Station?>(null)
    val station = _station.asStateFlow()

    private var getStreamJob: Job? = null

    init {
        // Launch last played station
        coroutineScope.launch {
            kv.decodeString("last")?.let { last ->
                kv.decodeString("station|$last")?.let { last ->
                    val station = Json.decodeFromString<Station>(last)
                    _station.update { station }
                }
            }
        }
    }

    fun play(station: Station) {
        getStreamJob?.cancel()
        controller?.stop()

        _isBuffering.update { true }
        _station.update { station }

        coroutineScope.launch {
            kv.encode("station|${station.url}", Json.encodeToString(station))
            kv.encode("last", station.url)
        }

        getStreamJob = coroutineScope.launch {
            val stream = StreamRepository.getStream(station.url)
            _isBuffering.update { false }
            if (stream == null) {
                _station.update { null }
            } else {
                withContext(Dispatchers.Main) {
                    _controller.first { it != null }?.apply {
                        val mediaItem = MediaItem.Builder()
                            .setUri(stream)
                            .setMediaId(station.url)
                            .setMediaMetadata(
                                MediaMetadata.Builder()
                                    .setTitle(station.name)
                                    .setArtist(station.city)
                                    .setIsPlayable(true)
                                    .setArtworkUri(station.thumbnail.toUri())
                                    .build()
                            )
                            .setLiveConfiguration(
                                MediaItem.LiveConfiguration.Builder()
                                    .setTargetOffsetMs(5_000)
                                    .setMinPlaybackSpeed(0.95f)
                                    .setMaxPlaybackSpeed(1.05f)
                                    .build()
                            )
                            .build()
                        setMediaItem(mediaItem)
                        prepare()
                        playWhenReady = true
                    }
                }
            }
        }
    }

    fun play() {
        val loadedStation = _station.value
        if (loadedStation != null && controller?.currentMediaItem?.mediaId != loadedStation.url) {
            play(loadedStation)
        } else {
            controller?.seekToDefaultPosition()
            controller?.play()
        }
    }

    fun pause() {
        controller?.pause()
    }


    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    override fun onPlaybackStateChanged(playbackState: Int) {
        _isBuffering.update { playbackState == Player.STATE_BUFFERING }
    }

    override fun onIsLoadingChanged(isLoading: Boolean) {
        _isBuffering.update { isLoading }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _isPlaying.update { isPlaying }
    }

    // ----------------
    //   SLEEP TIMER
    // ----------------

    val remainingTime = flow {
        while (true) {
            controller?.let { controller ->
                val command = SessionCommand(
                    PlaybackService.ACTION_QUERY_SLEEP_TIMER,
                    Bundle.EMPTY
                )
                val result = controller.sendCustomCommand(command, Bundle.EMPTY).get()
                val remainingMs = result.extras.getLong(PlaybackService.KEY_SLEEP_DURATION_MS, -1L)
                if (remainingMs < 0L) {
                    emit(null)
                } else {
                    emit(remainingMs.milliseconds)
                }
            }
            delay(1.seconds)
        }
    }

    private fun sendSleepTimerCommand(duration: Duration) {
        val command = SessionCommand(
            PlaybackService.ACTION_START_SLEEP_TIMER, Bundle.EMPTY
        )

        val args = Bundle().apply {
            putLong(PlaybackService.KEY_SLEEP_DURATION_MS, duration.inWholeMilliseconds)
        }

        controller?.sendCustomCommand(command, args)
    }

    fun setSleepTimer(duration: Duration) {
        sendSleepTimerCommand(duration)
    }

    fun stopSleepTimer() {
        sendSleepTimerCommand(Duration.ZERO)
    }

    fun release() {
        getStreamJob?.cancel()
        controller?.release()
    }

}