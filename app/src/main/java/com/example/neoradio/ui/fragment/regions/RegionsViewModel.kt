package com.example.neoradio.ui.fragment.regions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neoradio.model.RadioList
import com.example.neoradio.repository.HomeRepository
import com.example.neoradio.service.ERadioService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegionsViewModel(
    private val homeRepository: HomeRepository
) : ViewModel() {
    val regions = homeRepository.regions

    private val regionStations: MutableMap<String, MutableStateFlow<List<RadioList>>> =
        mutableMapOf()

    fun getStations(region: String): StateFlow<List<RadioList>> {
        if (regionStations.containsKey(region)) {
            return regionStations[region]!!
        }
        val sf = MutableStateFlow<List<RadioList>>(emptyList())
        regionStations[region] = sf
        viewModelScope.launch(Dispatchers.IO) {
            val res = ERadioService.getLocationStations(region)
            sf.update { res.groupBy { it.city ?: "ΆΛΛΟ" }.toList() }
        }
        return sf
    }

}