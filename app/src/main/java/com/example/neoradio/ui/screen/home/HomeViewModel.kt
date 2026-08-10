package com.example.neoradio.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neoradio.model.Station
import com.example.neoradio.repository.PlayerRepository
import com.example.neoradio.service.ERadioService
import com.example.neoradio.service.RadioList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val playerRepository: PlayerRepository
) : ViewModel() {
    private val _featured = MutableStateFlow<List<RadioList>>(emptyList())
    val featured = _featured.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val lists = ERadioService.getFeatured()
            _featured.update { lists }
        }
    }

    fun play(station: Station) = playerRepository.play(station)
}