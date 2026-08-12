package com.example.neoradio.repository

import com.example.neoradio.api.ERadio
import com.example.neoradio.model.RadioList
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class GenresRepository(
    private val coroutineScope: CoroutineScope
) {
    private val kv = MMKV.defaultMMKV()

    private val genreStations: MutableMap<String, MutableStateFlow<List<RadioList>>> =
        mutableMapOf()

    fun getStations(genre: String): StateFlow<List<RadioList>> {
        if (genreStations.containsKey(genre)) {
            return genreStations[genre]!!
        }

        val sf = MutableStateFlow<List<RadioList>>(emptyList())
        genreStations[genre] = sf

        coroutineScope.launch(Dispatchers.IO) {
            kv.decodeString("genre|$genre")?.let { encoded ->
                val res = Json.decodeFromString<List<RadioList>>(encoded)
                sf.update { res }
            }

            val res =
                ERadio.getCategoryStations(genre).groupBy { it.city ?: "ΆΛΛΟ" }.toList()
            sf.update { res }
            kv.encode("genre|$genre", Json.encodeToString(res))
        }

        return sf
    }

}