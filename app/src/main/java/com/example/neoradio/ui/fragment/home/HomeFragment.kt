package com.example.neoradio.ui.fragment.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.neoradio.controller.PlayerController
import com.example.neoradio.ui.component.radioListRow
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeFragment(modifier: Modifier = Modifier, viewModel: HomeViewModel = koinViewModel()) {
    val controller = koinInject<PlayerController>()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val featured by viewModel.featured.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    Icon(
                        Icons.Rounded.Radio,
                        contentDescription = null,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                },
                title = {
                    Text("NeoRadio")
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            contentPadding = innerPadding
        ) {
            if (featured.isEmpty()) {
                item {
                    LinearProgressIndicator(modifier = Modifier.fillMaxSize())
                }
            } else {
                radioListRow(featured) { controller.play(it) }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}