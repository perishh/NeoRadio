package com.example.neoradio.ui.fragment.regions

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.neoradio.ui.fragment.regions.component.RegionStations
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegionsFragment(modifier: Modifier = Modifier, viewModel: RegionsViewModel = koinViewModel()) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val regions by viewModel.regions.collectAsStateWithLifecycle()
    var selectedRegion by remember(regions) { mutableIntStateOf(0) }
    val pagerState = rememberPagerState { regions.size }

    LaunchedEffect(selectedRegion) {
        if (pagerState.currentPage != selectedRegion) {
            pagerState.animateScrollToPage(selectedRegion)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect { page ->
                selectedRegion = page
            }
    }

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
        if (regions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                SecondaryScrollableTabRow(
                    selectedTabIndex = selectedRegion,
                ) {
                    regions.forEachIndexed { i, region ->
                        Tab(
                            selected = i == selectedRegion,
                            onClick = {
                                selectedRegion = i
                            }
                        ) {
                            Text(
                                region.second,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                            )
                        }
                    }
                }
                HorizontalPager(
                    state = pagerState,
                    beyondViewportPageCount = 0
                ) { page ->
                    RegionStations(
                        stations = viewModel.getStations(regions[page].first)
                    )
                }
            }
        }
    }
}