package com.example.neoradio.ui.fragment.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neoradio.model.Station
import com.example.neoradio.repository.HomeRepository
import com.example.neoradio.repository.LikedRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
) : ViewModel() {
    val featured = HomeRepository.featured

    private val _liked = MutableStateFlow<List<Station>>(emptyList())
    val liked = _liked.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            HomeRepository.loadHomePage()
        }
        viewModelScope.launch(Dispatchers.IO) {
            _liked.value = LikedRepository.getLikedStations()
        }
    }
}