package com.example.neoradio.repository

import com.example.neoradio.model.RadioList
import com.example.neoradio.service.ERadioService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HomeRepository {
    private val _featured = MutableStateFlow<List<RadioList>>(emptyList())
    val featured = _featured.asStateFlow()

    private val _regions = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val regions = _regions.asStateFlow()

    suspend fun loadHomePage() {
        ERadioService.getHomePage().let { homePage ->
            _featured.update { homePage.radioLists }
            _regions.update { homePage.regions }
        }
    }
}