package com.example.neoradio.repository

import com.example.neoradio.api.ERadio
import com.example.neoradio.model.HomePage
import com.example.neoradio.model.RadioList
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object HomeRepository {
    private val kv = MMKV.defaultMMKV()

    private val _featured = MutableStateFlow<List<RadioList>>(emptyList())
    val featured = _featured.asStateFlow()

    private val _regions = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val regions = _regions.asStateFlow()

    private val _categories = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val categories = _categories.asStateFlow()

    suspend fun loadHomePage() = withContext(Dispatchers.IO) {
        kv.decodeString("home")?.let { encoded ->
            val homePage = Json.decodeFromString<HomePage>(encoded)
            _featured.update { homePage.radioLists }
            _regions.update { homePage.regions }
            _categories.update { homePage.categories }
        }

        val homePage = ERadio.getHomePage()
        _featured.update { homePage.radioLists }
        _regions.update { homePage.regions }
        _categories.update { homePage.categories }

        kv.encode("home", Json.encodeToString(homePage))
    }
}