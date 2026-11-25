package com.example.podcast

import android.content.Context
import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class MurfGenerateRequest(
    val text: String,
    val voiceId: String,
    val style: String? = "Conversational",
    val variation: Int = 4,
    val rate: Int = -4,
    val pitch: Int = 0,
    val format: String = "MP3",
    val modelVersion: String = "GEN2",
    val sampleRate: Int = 44100,
    val channelType: String = "STEREO"
)

@JsonClass(generateAdapter = true)
data class MurfGenerateResponse(
    val audioFile: String? = null,
    val audioLengthInSeconds: Double? = null,
    val audioDuration: Double? = null,
    val errorMessage: String? = null,
    val message: String? = null
)

interface MurfApiService {
    @POST("v1/speech/generate")
    suspend fun generateSpeech(
        @Header("api-key") apiKey: String,
        @Body request: MurfGenerateRequest
    ): Response<MurfGenerateResponse>
}

class MurfAudioRepository(
    private val context: Context
) {
    // Versioned cache directory keyed by manifest track_id (POD-MONTH-XX / POD-DAY-XXX)
    private val cacheRoot: File = File(context.cacheDir, "sacred_podcasts_v3").apply {
        mkdirs()
        // Clean up legacy weekly podcast caches if present
        File(context.cacheDir, "sacred_podcasts").takeIf { it.exists() }?.deleteRecursively()
        File(context.cacheDir, "sacred_podcasts_v2").takeIf { it.exists() }?.deleteRecursively()
    }

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
            redactHeader("api-key")
        }
        OkHttpClient.Builder()
            .connectTimeout(25, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    private val api: MurfApiService by lazy {
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.murf.ai/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(MurfApiService::class.java)
    }

    fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.MURF_API_KEY.trim()
        return key.isNotBlank() && key != PLACEHOLDER_KEY
    }

    /**
     * Checks if a pre-rendered MP3 matching the manifest's `audio_base_path + audio_file`
     * has been bundled in `app/src/main/assets/media/audio/v1/...` and extracts it to cache
     * for MediaPlayer playback. Returns null when using on-demand Murf studio synthesis.
     */
    suspend fun resolveBundledTrackAsset(
        audioBasePath: String,
        relativeAudioFile: String,
        trackId: String
    ): File? = withContext(Dispatchers.IO) {
        val normalizedBase = audioBasePath.trimEnd('/')
        val normalizedRel = relativeAudioFile.trimStart('/')
        val assetPath = "$normalizedBase/$normalizedRel"
        val safeTrack = sanitizeKey(trackId)
        val cachedAssetFile = File(File(cacheRoot, safeTrack).apply { mkdirs() }, "bundled_full.mp3")
        if (cachedAssetFile.exists() && cachedAssetFile.length() > 0L) {
            return@withContext cachedAssetFile
        }
        runCatching {
            context.assets.open(assetPath).use { input ->
                cachedAssetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            if (cachedAssetFile.exists() && cachedAssetFile.length() > 0L) cachedAssetFile else null
        }.getOrNull()
    }

    fun cacheFileFor(trackId: String, turn: PodcastDialogueTurn): File {
        val trackDir = File(cacheRoot, sanitizeKey(trackId)).apply { mkdirs() }
        val safeVoice = sanitizeKey(turn.voiceId)
        val safeStyle = sanitizeKey(turn.style)
        return File(trackDir, "turn_${turn.index}_${safeVoice}_${safeStyle}.mp3")
    }

    fun isTurnCached(trackId: String, turn: PodcastDialogueTurn): Boolean {
        val file = cacheFileFor(trackId, turn)
        return file.exists() && file.length() > 0L
    }

    fun getCachedTurnIndices(trackId: String, turns: List<PodcastDialogueTurn>): Set<Int> {
        return turns.filter { isTurnCached(trackId, it) }.map { it.index }.toSet()
    }

    suspend fun getOrSynthesizeTurn(
        trackId: String,
        turn: PodcastDialogueTurn
    ): Result<File> = withContext(Dispatchers.IO) {
        val targetFile = cacheFileFor(trackId, turn)
        if (targetFile.exists() && targetFile.length() > 0L) {
            return@withContext Result.success(targetFile)
        }

        val apiKey = BuildConfig.MURF_API_KEY.trim()
        if (apiKey.isBlank() || apiKey == PLACEHOLDER_KEY) {
            return@withContext Result.failure(
                IllegalStateException(
                    "MURF_API_KEY is not configured. Add your Murf.ai key in the Secrets panel in AI Studio to synthesize podcast voices."
                )
            )
        }

        runCatching {
            val response = requestWithStyleFallback(apiKey = apiKey, turn = turn)

            if (!response.isSuccessful) {
                val errBody = response.errorBody()?.string()?.take(240).orEmpty()
                throw IOException("Murf voice request failed (${response.code()}): $errBody")
            }

            val audioUrl = response.body()?.audioFile
                ?: throw IOException(
                    response.body()?.errorMessage
                        ?: response.body()?.message
                        ?: "Murf API returned no audio URL."
                )

            downloadAudioToFile(audioUrl, targetFile)
            targetFile
        }
    }

    /**
     * Synthesizes using the turn's expressiveness profile (style, variation, unhurried rate, 44.1kHz stereo).
     * If Murf ever rejects a specific style for a voice with HTTP 400, gracefully falls back to Conversational.
     */
    private suspend fun requestWithStyleFallback(
        apiKey: String,
        turn: PodcastDialogueTurn
    ): Response<MurfGenerateResponse> {
        val primary = api.generateSpeech(
            apiKey = apiKey,
            request = MurfGenerateRequest(
                text = turn.spokenText,
                voiceId = turn.voiceId,
                style = turn.style,
                variation = turn.variation.coerceIn(0, 5),
                rate = turn.rate.coerceIn(-50, 50),
                pitch = turn.pitch.coerceIn(-50, 50),
                format = "MP3",
                modelVersion = "GEN2",
                sampleRate = 44100,
                channelType = "STEREO"
            )
        )
        if (primary.isSuccessful || primary.code() != 400) {
            return primary
        }

        if (!turn.style.equals("Conversational", ignoreCase = true)) {
            val conversational = api.generateSpeech(
                apiKey = apiKey,
                request = MurfGenerateRequest(
                    text = turn.spokenText,
                    voiceId = turn.voiceId,
                    style = "Conversational",
                    variation = turn.variation.coerceIn(0, 5),
                    rate = turn.rate.coerceIn(-50, 50),
                    pitch = turn.pitch.coerceIn(-50, 50),
                    format = "MP3",
                    modelVersion = "GEN2",
                    sampleRate = 44100,
                    channelType = "STEREO"
                )
            )
            if (conversational.isSuccessful || conversational.code() != 400) {
                return conversational
            }
        }

        return api.generateSpeech(
            apiKey = apiKey,
            request = MurfGenerateRequest(
                text = turn.spokenText,
                voiceId = turn.voiceId,
                style = null,
                variation = turn.variation.coerceIn(0, 5),
                rate = turn.rate.coerceIn(-50, 50),
                pitch = turn.pitch.coerceIn(-50, 50),
                format = "MP3",
                modelVersion = "GEN2",
                sampleRate = 44100,
                channelType = "STEREO"
            )
        )
    }

    private fun downloadAudioToFile(url: String, destination: File) {
        val downloadRequest = Request.Builder().url(url).get().build()
        okHttpClient.newCall(downloadRequest).execute().use { resp ->
            if (!resp.isSuccessful) {
                throw IOException("Failed to download synthesized podcast audio (${resp.code})")
            }
            val body = resp.body ?: throw IOException("Empty audio response from Murf")
            val tempFile = File(destination.parentFile, "${destination.name}.tmp")
            body.byteStream().use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            if (destination.exists()) {
                destination.delete()
            }
            if (!tempFile.renameTo(destination)) {
                tempFile.copyTo(destination, overwrite = true)
                tempFile.delete()
            }
        }
    }

    private fun sanitizeKey(raw: String): String =
        raw.replace(Regex("[^a-zA-Z0-9_-]"), "_")

    companion object {
        private const val PLACEHOLDER_KEY = "MY_MURF_API_KEY"
    }
}
