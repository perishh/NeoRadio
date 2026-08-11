package com.example.neoradio.repository

import com.example.neoradio.model.RadioList
import com.example.neoradio.service.ERadioService
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RegionsRepository(
    private val coroutineScope: CoroutineScope
) {
    private val kv = MMKV.defaultMMKV()

    private val regionStations: MutableMap<String, MutableStateFlow<List<RadioList>>> =
        mutableMapOf()

    fun getStations(region: String): StateFlow<List<RadioList>> {
        if (regionStations.containsKey(region)) {
            return regionStations[region]!!
        }

        val sf = MutableStateFlow<List<RadioList>>(emptyList())
        regionStations[region] = sf

        coroutineScope.launch(Dispatchers.IO) {
            kv.decodeString("region|$region")?.let { encoded ->
                val res = Json.decodeFromString<List<RadioList>>(encoded)
                sf.update { res }
            }

            val res =
                ERadioService.getLocationStations(region).groupBy { it.city ?: "ΆΛΛΟ" }.toList()
            sf.update { res }
            kv.encode("region|$region", Json.encodeToString(res))
        }

        return sf
    }

}