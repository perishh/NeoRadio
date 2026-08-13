package com.example.neoradio.ui.fragment.genres.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.neoradio.model.RadioList
import com.example.neoradio.player.PlayerController
import com.example.neoradio.ui.component.radioListRow
import kotlinx.coroutines.flow.StateFlow
import org.koin.compose.koinInject

@Composable
fun GenreStations(
    stations: StateFlow<List<RadioList>>
) {
    val controller = koinInject<PlayerController>()
    val lists by stations.collectAsStateWithLifecycle()

    val filterListState = rememberLazyListState()

    val genres = remember(lists) { lists.flatMap { it.second.flatMap { it.genres } }.distinct() }

    var selectedGenre by rememberSaveable(genres) { mutableStateOf<String?>(null) }

    val filteredGenres = remember(genres, selectedGenre) {
        if (selectedGenre == null) {
            genres
        } else {
            val index = genres.indexOfFirst { it.first == selectedGenre }
            listOf(genres[index]) + genres.filterIndexed { i, _ -> i != index }
        }
    }

    val filtered = remember(selectedGenre, lists) {
        if (selectedGenre == null) lists else lists.map {
            Pair(
                it.first,
                it.second.filter { it.genres.any { it.first == selectedGenre } }
            )
        }.filter { it.second.isNotEmpty() }
    }

    LaunchedEffect(selectedGenre) {
        if (selectedGenre != null) {
            filterListState.animateScrollToItem(0)
        }
    }


    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(top = if (lists.isEmpty()) 0.dp else 8.dp, bottom = 24.dp)
    ) {
        if (lists.isEmpty()) {
            item {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        } else {
            item {
                LazyRow(
                    state = filterListState,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(filteredGenres, key = { it.first }) { genre ->
                        FilterChip(
                            modifier = Modifier.animateItem(),
                            selected = selectedGenre == genre.first,
                            onClick = {
                                selectedGenre = if (selectedGenre == genre.first) {
                                    null
                                } else {
                                    genre.first
                                }
                            },
                            label = { Text(genre.second) }
                        )
                    }
                }
            }
            radioListRow(filtered) {
                controller.play(it)
            }
        }
    }

}