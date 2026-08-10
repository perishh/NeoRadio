package com.example.neoradio.ui.screen.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.neoradio.ui.miniplayer.Miniplayer
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(viewModel: HomeViewModel = koinViewModel()) {
    val density = LocalDensity.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val featured by viewModel.featured.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    var truePeekHeight by remember { mutableIntStateOf(85) }
    val animatedPeekHeight by animateDpAsState(targetValue = with(density) { truePeekHeight.toDp() })

    BottomSheetScaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
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
        },
        sheetPeekHeight = animatedPeekHeight + WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding(),
        sheetShadowElevation = 8.dp,
        sheetDragHandle = null,
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                Miniplayer(
                    modifier = Modifier.onSizeChanged { size ->
                        truePeekHeight = size.height
                    }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            contentPadding = innerPadding
        ) {
            items(featured) { list ->
                Column(
                    modifier = Modifier
                        .animateItem()
                        .fillMaxWidth()
                ) {
                    Text(
                        list.first,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.W600,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(list.second) { station ->
                            AsyncImage(
                                model = station.thumbnail,
                                contentDescription = station.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(96.dp)
                                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(8.dp))
                                    .background(
                                        Color.White,
                                        shape = RoundedCornerShape(8.dp)
                                    ) // TODO: Make material
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.play(station)
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}