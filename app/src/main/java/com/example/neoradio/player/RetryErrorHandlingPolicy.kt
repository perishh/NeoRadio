package com.example.neoradio.player

import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy
import kotlin.math.min

@UnstableApi
class RetryErrorHandlingPolicy(
    private val initialRetryDelayMs: Long = 2_000L,
    private val maxRetryDelayMs: Long = 10_000L
) : DefaultLoadErrorHandlingPolicy() {
    override fun getRetryDelayMsFor(loadErrorInfo: LoadErrorHandlingPolicy.LoadErrorInfo): Long {
        val exception = loadErrorInfo.exception

        val errorCount = loadErrorInfo.errorCount
        val delay = initialRetryDelayMs * (1 shl min(errorCount - 1, 10))
        return min(delay, maxRetryDelayMs)
    }

    override fun getMinimumLoadableRetryCount(dataType: Int): Int {
        return Int.MAX_VALUE
    }
}