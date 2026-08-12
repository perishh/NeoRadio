package com.example.neoradio.model

import kotlinx.serialization.Serializable

typealias Genre = Pair<String, String>

@Serializable
data class Station(
    val url: String,
    val thumbnail: String,
    val name: String,
    val city: String?,
    val category: Genre?,
    val genres: List<Genre>
)
