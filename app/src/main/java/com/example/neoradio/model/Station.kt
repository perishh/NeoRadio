package com.example.neoradio.model

import android.os.Bundle
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

typealias Genre = Pair<String, String>

@Serializable
data class Station(
    val url: String,
    val thumbnail: String,
    val name: String,
    val city: String?,
    val category: Genre?,
    val genres: List<Genre>
) {
    fun toMediaItem(): MediaItem = MediaItem.Builder()
        .setUri(url)
        .setMediaId(url)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(name)
                .setArtist(listOfNotNull(city, category?.second).joinToString(" · "))
                .setStation(name)
                .setArtworkUri(thumbnail.toUri())
                .setGenre(category?.second)
                .setIsPlayable(true)
                .setExtras(
                    Bundle().apply {
                        putString("station", Json.encodeToString(this@Station))
                    }
                )
                .build()
        )
        .setLiveConfiguration(
            MediaItem.LiveConfiguration.Builder()
                .setTargetOffsetMs(5_000)
                .setMinPlaybackSpeed(0.95f)
                .setMaxPlaybackSpeed(1.05f)
                .build()
        )
        .build()
}

@Serializable
data class Stream(
    val url: String,
    val history: String?,
    val next: String?
)