package com.example.neoradio.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import com.example.neoradio.processor.WaveformAudioProcessor
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@UnstableApi
class PlaybackService : MediaSessionService(), MediaSession.Callback {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession

    private var sleepTimerJob: Job? = null
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

        player = ExoPlayer.Builder(this, renderersFactory)
            .setAudioAttributes(audioAttributes, true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .setHandleAudioBecomingNoisy(true)
            .build()

        mediaSession = MediaSession.Builder(this, player)
            .setCallback(this)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession =
        mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
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
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        setMediaNotificationProvider(
            DefaultMediaNotificationProvider.Builder(applicationContext)
                .setChannelId("playback_channel")
                .setNotificationId(1001)
                .build()
        )
    }

    override fun onDestroy() {
        sleepTimerJob?.cancel()
        serviceScope.cancel()
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