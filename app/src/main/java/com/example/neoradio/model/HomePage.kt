package com.example.neoradio.model

typealias RadioList = Pair<String, List<Station>>

data class HomePage(
    val regions: List<Pair<String, String>>,
    val radioLists: List<RadioList>
)
