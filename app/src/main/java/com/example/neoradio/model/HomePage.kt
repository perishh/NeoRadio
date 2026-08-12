package com.example.neoradio.model

import kotlinx.serialization.Serializable

typealias RadioList = Pair<String, List<Station>>

@Serializable
data class HomePage(
    val regions: List<Pair<String, String>>,
    val categories: List<Genre>,
    val radioLists: List<RadioList>,
)
