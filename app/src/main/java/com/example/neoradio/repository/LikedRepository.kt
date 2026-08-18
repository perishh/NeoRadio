package com.example.neoradio.repository

import com.example.neoradio.model.Station
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

object LikedRepository {
    val kv = MMKV.defaultMMKV()

    suspend fun getLikedStations(): List<Station> = withContext(Dispatchers.IO) {
        (kv.allKeys() ?: emptyArray<String>()).filter { it.startsWith("time|") }.mapNotNull { key ->
            val url = key.split("|", limit = 2).getOrNull(1) ?: return@mapNotNull null
            val time = kv.getInt(key, 0)
            kv.decodeString("station|$url")?.let { station ->
                Pair(Json.decodeFromString<Station>(station), time)
            }
        }.sortedByDescending { it.second }.map { it.first }
    }

}