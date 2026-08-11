package com.example.neoradio.controller

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.neoradio.model.Station
import com.example.neoradio.repository.StreamRepository
import com.example.neoradio.service.PlaybackService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerController(
    context: Context,
    private val coroutineScope: CoroutineScope,
) : Player.Listener {
    private var controller: MediaController? = null

    init {
        // TODO: Implement reconnection strategy
        val sessionToken =
            SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture.addListener({
            controller = controllerFuture.get().apply { addListener(this@PlayerController) }
        }, ContextCompat.getMainExecutor(context))
    }

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering = _isBuffering.asStateFlow()

    private val _station = MutableStateFlow<Station?>(null)
    val station = _station.asStateFlow()

    private var getStreamJob: Job? = null

    fun play(context: Context, station: Station) {
        _isBuffering.update { true }
        _station.update { station }

        val serviceIntent = Intent(context, PlaybackService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)

        getStreamJob?.cancel()
        controller?.stop()

        getStreamJob = coroutineScope.launch {
            val stream = StreamRepository.getStream(station.url)
            if (stream == null) {
                _isBuffering.update { false }
                _station.update { null }
            } else {
                withContext(Dispatchers.Main) {
                    controller?.apply {
                        val mediaItem = MediaItem.Builder()
                            .setUri(stream)
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
                                    .setTargetOffsetMs(3000)
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
        controller?.seekToDefaultPosition()
        controller?.play()
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

    fun release() {
        controller?.release()
    }
}