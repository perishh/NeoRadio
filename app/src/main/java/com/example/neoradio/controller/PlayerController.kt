package com.example.neoradio.controller

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.neoradio.model.Station
import com.example.neoradio.service.ERadioService
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
    private val player = ExoPlayer.Builder(context).build().apply {
        addListener(this@PlayerController)
    }

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering = _isBuffering.asStateFlow()

    private val _station = MutableStateFlow<Station?>(null)
    val station = _station.asStateFlow()

    private var getStreamJob: Job? = null

    fun play(station: Station) {
        getStreamJob?.cancel()
        _isBuffering.update { true }
        _station.update { station }
        getStreamJob = coroutineScope.launch {
            val stream = ERadioService.getStream(station.url)
            if (stream == null) {
                _isBuffering.update { false }
                _station.update { null }
            } else {
                withContext(Dispatchers.Main) {
                    player.apply {
                        val mediaItem = MediaItem.fromUri(stream)
                        setMediaItem(mediaItem)
                        prepare()
                        playWhenReady = true
                    }
                }
            }
        }
    }

    fun play() {
        player.seekToDefaultPosition()
        player.play()
    }

    fun pause() {
        player.pause()
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
        getStreamJob?.cancel()
        player.removeListener(this@PlayerController)
        player.release()
    }
}