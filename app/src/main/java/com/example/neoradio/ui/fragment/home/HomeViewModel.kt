package com.example.neoradio.ui.fragment.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neoradio.repository.HomeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(
    private val homeRepository: HomeRepository
) : ViewModel() {
    val featured = homeRepository.featured

    init {
        viewModelScope.launch(Dispatchers.IO) {
            homeRepository.loadHomePage()
        }
    }
}