package com.example.neoradio.ui.fragment.regions.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.neoradio.controller.PlayerController
import com.example.neoradio.model.RadioList
import com.example.neoradio.ui.component.radioListRow
import kotlinx.coroutines.flow.StateFlow
import org.koin.compose.koinInject

@Composable
fun RegionStations(
    stations: StateFlow<List<RadioList>>
) {
    val controller = koinInject<PlayerController>()
    val lists by stations.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(top = if (lists.isEmpty()) 0.dp else 16.dp, bottom = 24.dp)
    ) {
        if (lists.isEmpty()) {
            item {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        } else {
            radioListRow(lists) {
                controller.play(it)
            }
        }
    }

}