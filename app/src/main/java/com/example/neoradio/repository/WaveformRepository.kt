package com.example.neoradio.repository

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object WaveformRepository {
    private val _sampleFlow = MutableSharedFlow<FloatArray>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val sampleFlow = _sampleFlow.asSharedFlow()

    val hasObservers: Boolean get() = _sampleFlow.subscriptionCount.value > 0

    fun emitSample(sample: FloatArray) {
        _sampleFlow.tryEmit(sample)
    }
}