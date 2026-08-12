package com.example.neoradio.ui.fragment.genres

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
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.neoradio.ui.fragment.genres.component.GenreStations
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GenresFragment(modifier: Modifier = Modifier, viewModel: GenresViewModel = koinViewModel()) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val coroutineScope = rememberCoroutineScope()

    val categories by viewModel.genres.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState { categories.size }

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
                    Text("Κατηγορίες")
                }
            )
        }
    ) { innerPadding ->
        if (categories.isEmpty()) {
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
                    selectedTabIndex = pagerState.currentPage,
                    indicator = {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(
                                selectedTabIndex = pagerState.currentPage,
                                matchContentSize = false
                            )
                        )
                    }
                ) {
                    categories.forEachIndexed { i, region ->
                        Tab(
                            selected = i == pagerState.currentPage,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(i)
                                }
                            }
                        ) {
                            Text(
                                region.second,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                HorizontalPager(
                    state = pagerState,
                    beyondViewportPageCount = 0
                ) { page ->
                    GenreStations(
                        stations = viewModel.getStations(categories[page].first)
                    )
                }
            }
        }
    }
}