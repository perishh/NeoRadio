package com.example.neoradio.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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

fun LazyListScope.radioListRow(
    lists: List<RadioList>,
    onClick: (Station) -> Unit,
) {
    items(lists, key = { it.first }) { list ->
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
                items(list.second, key = { it.url }) { station ->
                    NetImage(
                        model = station.thumbnail,
                        contentDescription = station.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .animateItem()
                            .size(96.dp)
                            .shadow(elevation = 2.dp, shape = RoundedCornerShape(8.dp))
                            .background(
                                Color.White,
                                shape = RoundedCornerShape(8.dp)
                            ) // TODO: Make material
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onClick(station)
                            }
                    )
                }
            }
        }
    }
}