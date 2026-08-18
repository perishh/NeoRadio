package com.example.neoradio.repository

import com.example.neoradio.api.ERadio
import com.example.neoradio.model.Stream
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

object StreamRepository {
    private val kv = MMKV.defaultMMKV()

    suspend fun getStream(url: String): Stream? = withContext(Dispatchers.IO) {
        // TODO: Check for streams requiring token renewal
        val cached = kv.decodeString("stream|$url")?.let { Json.decodeFromString<Stream>(it) }
        if (cached != null) {
            return@withContext cached
        } else {
            val res = ERadio.getStream(url)?.let {
                kv.encode("stream|$url", Json.encodeToString(it))
                it
            }
            return@withContext res
        }
    }

}