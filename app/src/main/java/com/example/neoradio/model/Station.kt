package com.example.neoradio.model

import kotlinx.serialization.Serializable

@Serializable
data class Station(
    val url: String,
    val thumbnail: String,
    val name: String,
    val city: String?,
)
