package com.example.neoradio.ui.miniplayer

import androidx.lifecycle.ViewModel
import com.example.neoradio.repository.PlayerRepository

class MiniplayerViewModel(
    private val playerRepository: PlayerRepository
) : ViewModel() {
    val station = playerRepository.station
    val isPlaying = playerRepository.isPlaying
    val isBuffering = playerRepository.isBuffering
    fun play() = playerRepository.play()
    fun pause() = playerRepository.pause()
}