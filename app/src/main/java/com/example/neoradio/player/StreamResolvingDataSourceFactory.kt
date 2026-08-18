package com.example.neoradio.player

import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.ResolvingDataSource
import com.example.neoradio.model.Stream
import com.example.neoradio.repository.StreamRepository
import kotlinx.coroutines.runBlocking

@UnstableApi
class StreamResolvingDataSourceFactory(
    private val upstreamFactory: DataSource.Factory,
    private val onMetadataResolved: (Stream) -> Unit
) : DataSource.Factory {
    override fun createDataSource(): DataSource {
        val resolver = ResolvingDataSource.Resolver { dataSpec ->
            val url = dataSpec.uri.toString()

            val stream = runBlocking {
                StreamRepository.getStream(url)?.also {
                    onMetadataResolved(it)
                }
            }?.url ?: throw Exception("Couldn't get stream")

            dataSpec.buildUpon()
                .setUri(stream.toUri())
                .build()
        }

        return ResolvingDataSource(upstreamFactory.createDataSource(), resolver)
    }
}