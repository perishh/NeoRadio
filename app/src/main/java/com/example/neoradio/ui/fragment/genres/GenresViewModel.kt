package com.example.neoradio.ui.fragment.genres

import androidx.lifecycle.ViewModel
import com.example.neoradio.repository.GenresRepository
import com.example.neoradio.repository.HomeRepository

class GenresViewModel(
    private val genresRepository: GenresRepository
) : ViewModel() {
    val genres = HomeRepository.categories

    fun getStations(genre: String) = genresRepository.getStations(genre)

}