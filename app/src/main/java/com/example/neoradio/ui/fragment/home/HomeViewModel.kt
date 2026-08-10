package com.example.neoradio.ui.fragment.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neoradio.model.Station
import com.example.neoradio.repository.HomeRepository
import com.example.neoradio.repository.PlayerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(
    private val homeRepository: HomeRepository,
    private val playerRepository: PlayerRepository
) : ViewModel() {
    val featured = homeRepository.featured

    init {
        viewModelScope.launch(Dispatchers.IO) {
            homeRepository.loadHomePage()
        }
    }

    fun play(station: Station) = playerRepository.play(station)
}