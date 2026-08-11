package com.example.neoradio.ui.fragment.regions

import androidx.lifecycle.ViewModel
import com.example.neoradio.repository.HomeRepository
import com.example.neoradio.repository.RegionsRepository

class RegionsViewModel(
    private val regionsRepository: RegionsRepository
) : ViewModel() {
    val regions = HomeRepository.regions

    fun getStations(region: String) = regionsRepository.getStations(region)

}