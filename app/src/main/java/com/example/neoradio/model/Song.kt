package com.example.neoradio.model

import kotlinx.serialization.Serializable

@Serializable
data class Song(
    val title: String,
    val artist: String,
    val startTime: String,
    val runTime: String
)