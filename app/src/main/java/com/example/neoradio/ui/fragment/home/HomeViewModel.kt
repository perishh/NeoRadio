package com.example.neoradio.ui.fragment.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neoradio.repository.HomeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(
) : ViewModel() {
    val featured = HomeRepository.featured

    init {
        viewModelScope.launch(Dispatchers.IO) {
            HomeRepository.loadHomePage()
        }
    }
}