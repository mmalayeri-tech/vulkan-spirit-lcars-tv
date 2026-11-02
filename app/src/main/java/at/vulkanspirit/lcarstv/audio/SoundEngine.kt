package at.vulkanspirit.lcarstv.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.speech.tts.TextToSpeech
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

/** Plays the synthesized interface sounds through cached static AudioTracks. */
class SoundEngine {
    @Volatile var enabled = true
    @Volatile var navigationSounds = false
    @Volatile var volume = 0.7f

    private val tracks = ConcurrentHashMap<Sfx, AudioTrack>()
    private val executor = Executors.newSingleThreadExecutor()

    fun prepare() = executor.execute { Sfx.entries.forEach { runCatching { track(it) } } }

    fun play(sfx: Sfx) {
        if (!enabled) return
        if (sfx == Sfx.FOCUS && !navigationSounds) return
        executor.execute {
            runCatching {
                val t = track(sfx)
                if (t.playState != AudioTrack.PLAYSTATE_STOPPED) t.stop()
                t.reloadStaticData()
                if (sfx == Sfx.ALERT) t.setLoopPoints(0, ToneSynth.build(Sfx.ALERT).size, ALERT_REPEATS)
                t.setVolume(volume)
                t.play()
            }
        }
    }

    fun stopAlert() = executor.execute {
        runCatching { tracks[Sfx.ALERT]?.let { if (it.playState != AudioTrack.PLAYSTATE_STOPPED) it.stop() } }
    }

    fun release() {
        executor.execute {
            tracks.values.forEach { runCatching { it.release() } }
            tracks.clear()
        }
        executor.shutdown()
    }

    private fun track(sfx: Sfx): AudioTrack = tracks.getOrPut(sfx) {
        val pcm = ToneSynth.build(sfx)
        AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(ToneSynth.SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
            )
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(pcm.size * 2)
            .build()
            .also { it.write(pcm, 0, pcm.size) }
    }

    private companion object {
        /** The klaxon sounds 6 times (about 6 s), then the visual alert continues silently. */
        const val ALERT_REPEATS = 5
    }
}

/** Optional spoken computer responses through the device's text-to-speech engine. */
class VoiceEngine(context: Context) {
    @Volatile var enabled = false
    @Volatile private var ready = false

    private val tts: TextToSpeech = TextToSpeech(context.applicationContext) { status ->
        ready = status == TextToSpeech.SUCCESS
        if (ready) configure()
    }

    private fun configure() {
        runCatching {
            if (tts.isLanguageAvailable(Locale.US) >= TextToSpeech.LANG_AVAILABLE) tts.language = Locale.US
            tts.setPitch(0.92f)
            tts.setSpeechRate(0.95f)
        }
    }

    val isAvailable: Boolean get() = ready

    fun say(text: String, force: Boolean = false) {
        if ((!enabled && !force) || !ready) return
        runCatching { tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "lcars") }
    }

    fun shutdown() = runCatching { tts.shutdown() }
}
