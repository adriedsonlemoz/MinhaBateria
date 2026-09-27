package com.minhabateria.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import java.util.PriorityQueue

class VoiceAlertRuntime(context: Context) : VoiceAlertSink {
    private data class Queued(val event: VoiceAlertEvent, val sequence: Long, val force: Boolean)

    private val appContext = context.applicationContext
    private val preferences = VoiceAlertPreferences(appContext)
    private val handler = Handler(Looper.getMainLooper())
    private val queue = PriorityQueue<Queued>(compareByDescending<Queued> { it.event.priority }.thenBy { it.sequence })
    private val lastPlayedAt = mutableMapOf<VoiceAlertEvent, Long>()
    private var sequence = 0L
    private var player: MediaPlayer? = null
    private var current: VoiceAlertEvent? = null

    override fun play(event: VoiceAlertEvent, force: Boolean) {
        handler.post {
            if (!force && !canPlay(event)) return@post
            if (current == event || queue.any { it.event == event }) return@post
            if (queue.size >= MAX_QUEUE) {
                val weakest = queue.minByOrNull { it.event.priority }
                if (weakest != null && weakest.event.priority < event.priority) queue.remove(weakest) else return@post
            }
            queue.add(Queued(event, sequence++, force))
            startNextIfIdle()
        }
    }

    fun test(event: VoiceAlertEvent) = play(event, force = true)

    private fun canPlay(event: VoiceAlertEvent): Boolean {
        if (!preferences.isEnabled() || !preferences.isEventEnabled(event)) return false
        if (!preferences.allowBackground() && !AppVisibility.isForeground) return false
        val now = System.currentTimeMillis()
        val last = lastPlayedAt[event] ?: 0L
        return now - last >= event.cooldownMs
    }

    private fun startNextIfIdle() {
        if (player != null) return
        val queued = queue.poll() ?: return
        if (!queued.force && !canPlay(queued.event)) {
            startNextIfIdle()
            return
        }
        current = queued.event
        val custom = preferences.customUri(queued.event)
        val mediaPlayer = MediaPlayer()
        player = mediaPlayer
        runCatching {
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            val volume = preferences.volume() / 100f
            mediaPlayer.setVolume(volume, volume)
            if (custom != null) {
                mediaPlayer.setDataSource(appContext, custom)
            } else {
                val afd = appContext.resources.openRawResourceFd(queued.event.rawResId)
                    ?: error("Áudio padrão indisponível")
                afd.use { mediaPlayer.setDataSource(it.fileDescriptor, it.startOffset, it.length) }
            }
            mediaPlayer.setOnPreparedListener {
                lastPlayedAt[queued.event] = System.currentTimeMillis()
                it.start()
            }
            mediaPlayer.setOnCompletionListener { finishCurrent() }
            mediaPlayer.setOnErrorListener { _, _, _ ->
                val hadCustom = custom != null
                finishCurrent()
                if (hadCustom) {
                    preferences.clearCustomUri(queued.event)
                    play(queued.event, force = true)
                }
                true
            }
            mediaPlayer.prepareAsync()
        }.onFailure {
            val hadCustom = custom != null
            finishCurrent()
            if (hadCustom) {
                preferences.clearCustomUri(queued.event)
                play(queued.event, force = true)
            }
        }
    }

    private fun finishCurrent() {
        runCatching { player?.release() }
        player = null
        current = null
        startNextIfIdle()
    }

    companion object {
        private const val MAX_QUEUE = 4
    }
}
