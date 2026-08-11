package com.example.neoradio.repository

import com.example.neoradio.service.ERadioService
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object StreamRepository {
    private val kv = MMKV.defaultMMKV()

    suspend fun getStream(url: String): String? = withContext(Dispatchers.IO) {
        // TODO: Check for streams requiring token renewal
        val cached = kv.decodeString("stream|$url")
        if (cached != null) {
            return@withContext cached
        } else {
            val res = ERadioService.getStream(url)?.let {
                kv.encode("stream|$url", it)
                it
            }
            return@withContext res
        }
    }

}