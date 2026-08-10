package com.example.neoradio.ui.fragment.regions

import androidx.lifecycle.ViewModel
import com.example.neoradio.repository.HomeRepository

class RegionsViewModel(
    private val homeRepository: HomeRepository
) : ViewModel() {
    val regions = homeRepository.regions
}