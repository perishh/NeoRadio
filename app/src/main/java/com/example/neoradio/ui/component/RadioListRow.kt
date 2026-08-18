package com.example.neoradio.ui.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.neoradio.model.RadioList
import com.example.neoradio.model.Station

private val genreEmojis = mapOf(
    "ΑΓΑΠΗΜΕΝΑ" to "⭐",
    "ΕΛΛΗΝΙΚΕΣ ΕΠΙΤΥΧΙΕΣ" to "🇬🇷",
    "ΕΝΤΕΧΝΗ & ΜΠΑΛΑΝΤΕΣ" to "🎼",
    "ΞΕΝΑ HITS & DANCE" to "💃",
    "ΕΛΛΗΝΙΚΟ ΛΑΪΚΟ" to "🪕",
    "ΕΙΔΗΣΕΙΣ & ΜΟΥΣΙΚΗ" to "📰",
    "SOPHISTICATED" to "🎷",
    "ΘΡΗΣΚΕΥΤΙΚΑ" to "🙏",
    "ΑΘΛΗΤΙΚΑ" to "⚽",
    "ΡΟΚ" to "🎸",
    "ΚΥΠΡΟΣ" to "🇨🇾"
)

fun LazyListScope.radioListRow(
    lists: List<RadioList>,
    onClick: (Station) -> Unit,
) {
    items(lists, key = { it.first }) { list ->
        if (list.second.isNotEmpty()) {
            val listState = rememberLazyListState()

//            var visibleIndex by remember { mutableIntStateOf(0) }
//            LaunchedEffect(listState) {
//                snapshotFlow { listState.layoutInfo }
//                    .map {
//                        it.visibleItemsInfo.firstOrNull { it.offset >= 0 }?.index
//                    }
//                    .collect {
//                    visibleIndex = it ?: 0
//                }
//            }

            Column(
                modifier = Modifier
                    .animateItem()
                    .fillMaxWidth()
            ) {
                Text(
                    listOfNotNull(genreEmojis[list.first], list.first).joinToString("  "),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.W600,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                LazyRow(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .sizeIn(minHeight = 128.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(list.second.size, key = { i -> list.second[i].url }) { i ->
                        val station = list.second[i]

                        val isFirst by remember {
                            derivedStateOf {
                                listState.layoutInfo.visibleItemsInfo.firstOrNull { it.offset >= 0 }?.index == i
                            }
                        }

                        val size by animateDpAsState(
                            targetValue = if (isFirst) 128.dp else 96.dp,
                            animationSpec = tween(
                                durationMillis = 700
                            ),
                        )

                        NetImage(
                            model = station.thumbnail,
                            contentDescription = station.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .animateItem()
                                .padding(end = if (isFirst) 4.dp else 0.dp)
                                .size(size)
                                .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
                                .background(
                                    Color.White,
                                    shape = RoundedCornerShape(12.dp)
                                ) // TODO: Make material
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onClick(station)
                                }
                        )
                    }
                }
            }
        }
    }
}