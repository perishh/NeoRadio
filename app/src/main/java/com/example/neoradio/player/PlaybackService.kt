package com.example.neoradio.player

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import com.example.neoradio.api.ERadio
import com.example.neoradio.model.Song
import com.example.neoradio.model.Station
import com.example.neoradio.ui.activity.MainActivity
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@UnstableApi
class PlaybackService : MediaSessionService(), MediaSession.Callback, Player.Listener {
    private val kv = MMKV.defaultMMKV()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession

    private var timeTrackJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var metadataJob: Job? = null

    private val remainingTime = MutableStateFlow<Duration?>(null)

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        val audioProcessor = WaveformAudioProcessor()

        val renderersFactory = object : DefaultRenderersFactory(this) {
            override fun buildAudioSink(
                context: Context,
                enableFloatOutput: Boolean,
                enableAudioOutputPlaybackParams: Boolean
            ): AudioSink = DefaultAudioSink.Builder(this@PlaybackService)
                .setAudioProcessors(arrayOf(audioProcessor))
                .build()
        }

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()

        val mediaSourceFactory = DefaultMediaSourceFactory(this)
            .setDataSourceFactory(StreamResolvingDataSourceFactory(httpDataSourceFactory) { stream ->
                startMetadataJob(stream.history, stream.next)
            })
            .setLoadErrorHandlingPolicy(RetryErrorHandlingPolicy())

        player = ExoPlayer.Builder(this, renderersFactory)
            .setAudioAttributes(audioAttributes, true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .setHandleAudioBecomingNoisy(true)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()

        player.addListener(this)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("PROMPT_PLAYER", "")
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        mediaSession = MediaSession.Builder(this, player)
            .setCallback(this)
            .setSessionActivity(pendingIntent)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession =
        mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        player.stop()
        player.clearMediaItems()
        stopSelf()
    }

    private fun updateMetadata(song: Song?) {
        val currentItem = player.currentMediaItem ?: return
        val currentIndex = player.currentMediaItemIndex

        var station: Station? = null
        val bundle = currentItem.mediaMetadata.extras?.apply {
            station = getString("station")?.let { Json.decodeFromString(it) }
            putString("song", song?.let { Json.encodeToString(it) })
        }

        val updatedMetadata = currentItem.mediaMetadata.buildUpon()
            .setTitle(song?.title ?: station?.name)
            .setArtist(
                song?.artist ?: listOfNotNull(
                    station?.city,
                    station?.category?.second
                ).joinToString(" · ")
            )
            .setExtras(bundle)
            .build()

        val updatedMediaItem = currentItem.buildUpon()
            .setMediaMetadata(updatedMetadata)
            .build()

        player.replaceMediaItem(currentIndex, updatedMediaItem)
    }

    private fun startMetadataJob(history: String?, next: String?) {
        metadataJob?.cancel()

        if (history != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                metadataJob = serviceScope.launch {
                    val historyList = ERadio.parseHistory(history, next)

                    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")

                    while (isActive) {
                        val now = LocalTime.now()

                        var current: Song? = null
                        var next: Song? = null

                        // Parse string startTimes into LocalTime once to compare safely
                        for ((index, entry) in historyList.withIndex()) {
                            val entryTime = LocalTime.parse(entry.startTime, formatter)
                            if (entryTime.isAfter(now)) {
                                next = entry
                                current = historyList.getOrNull(index - 1)
                                break
                            }
                        }

                        // If no song is in the future, the current song is the last item
                        if (next == null) {
                            current = historyList.lastOrNull()
                        }

                        // Update UI if current track exists
                        if (current != null) {
                            withContext(Dispatchers.Main) {
                                updateMetadata(current)
                            }
                        }

                        // Stop if there is no upcoming track
                        if (next == null) break

                        // Calculate delay until the next track using java.time ChronoUnit
                        val nextTime = LocalTime.parse(next.startTime, formatter)
                        val millisToWait = ChronoUnit.MILLIS.between(now, nextTime)

                        if (millisToWait > 0) {
                            delay((millisToWait + 1000).milliseconds)
                        }
                    }
                }
            }
        }
    }

    private fun cancelMetadataJob() {
        metadataJob?.cancel()
    }

    private fun startTimeTracking(id: String) {
        timeTrackJob?.cancel()
        timeTrackJob = serviceScope.launch {
            while (isActive) {
                delay(1.minutes)
                launch(Dispatchers.IO) {
                    val savedTime = kv.getInt("time|$id", 0)
                    kv.putInt("time|$id", savedTime + 1)
                }
            }
        }
    }

    private fun cancelTimeTracking() {
        timeTrackJob?.cancel()
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (isPlaying) {
            player.currentMediaItem?.mediaId?.let { id ->
                startTimeTracking(id)
            }
        } else {
            cancelTimeTracking()
        }
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        cancelMetadataJob()
        if (mediaItem != null && player.isPlaying) {
            startTimeTracking(mediaItem.mediaId)
        } else {
            cancelTimeTracking()
        }
    }

    fun startSleepTimer(duration: Duration) {
        sleepTimerJob?.cancel()
        sleepTimerJob = serviceScope.launch {
            remainingTime.value = duration
            while (isActive && (remainingTime.value ?: Duration.ZERO) > Duration.ZERO) {
                delay(1.seconds)
                remainingTime.update {
                    it?.let { it - 1.seconds }
                }
            }
            val remaining = remainingTime.value
            if (remaining != null && remaining <= Duration.ZERO) {
                player.pause()
            }
            remainingTime.value = null
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        remainingTime.update { null }
    }

    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        val connectionResult = super.onConnect(session, controller)

        val startTimerCommand = SessionCommand(
            ACTION_START_SLEEP_TIMER,
            Bundle.EMPTY
        )
        val queryTimerCommand = SessionCommand(
            ACTION_QUERY_SLEEP_TIMER,
            Bundle.EMPTY
        )

        val sessionCommands = connectionResult.availableSessionCommands
            .buildUpon()
            .add(startTimerCommand)
            .add(queryTimerCommand)
            .build()

        val playbackCommands = connectionResult.availablePlayerCommands
            .buildUpon()
            .add(Player.COMMAND_PLAY_PAUSE)
            .add(Player.COMMAND_STOP)
            .add(Player.COMMAND_PREPARE)
            .add(Player.COMMAND_SET_MEDIA_ITEM)
            .add(Player.COMMAND_GET_CURRENT_MEDIA_ITEM)
            .add(Player.COMMAND_SEEK_TO_DEFAULT_POSITION)
            .add(Player.COMMAND_GET_METADATA)
            .build()

        return MediaSession.ConnectionResult.accept(
            sessionCommands,
            playbackCommands
        )
    }

    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle
    ): ListenableFuture<SessionResult> {
        when (customCommand.customAction) {
            ACTION_START_SLEEP_TIMER -> {
                val durationMs = args.getLong(KEY_SLEEP_DURATION_MS, 0L)
                if (durationMs > 0L) {
                    startSleepTimer(durationMs.milliseconds)
                } else {
                    cancelSleepTimer()
                }
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }

            ACTION_QUERY_SLEEP_TIMER -> {
                val bundle = Bundle().apply {
                    putLong(KEY_SLEEP_DURATION_MS, remainingTime.value?.inWholeMilliseconds ?: -1L)
                }
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS, bundle))
            }
        }
        return Futures.immediateFuture(SessionResult(SessionError.ERROR_NOT_SUPPORTED))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "playback_channel",
                "Playback Notifications",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        setMediaNotificationProvider(
            DefaultMediaNotificationProvider.Builder(applicationContext)
                .setChannelId("playback_channel")
                .setNotificationId(1001)
                .build()
        )
    }

    override fun onPlayerError(error: PlaybackException) {
        if (isNetworkError(error)) {
            player.prepare()
            player.play()
        }
    }

    private fun isNetworkError(error: PlaybackException): Boolean {
        return error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ||
                error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ||
                error.errorCode == PlaybackException.ERROR_CODE_IO_UNSPECIFIED
    }

    override fun onDestroy() {
        cancelMetadataJob()
        cancelTimeTracking()
        cancelSleepTimer()
        serviceScope.cancel()
        player.removeListener(this)
        mediaSession.run {
            player.release()
            release()
        }
        super.onDestroy()
    }


    companion object {
        const val ACTION_START_SLEEP_TIMER = "com.example.neoradio.ACTION_START_SLEEP_TIMER"
        const val ACTION_QUERY_SLEEP_TIMER = "com.example.neoradio.ACTION_QUERY_SLEEP_TIMER"
        const val KEY_SLEEP_DURATION_MS = "KEY_SLEEP_DURATION_MS"
    }
}