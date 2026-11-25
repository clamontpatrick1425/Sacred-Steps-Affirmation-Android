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
    val format: String = "MP3",
    val modelVersion: String = "GEN2",
    val sampleRate: Int = 24000,
    val channelType: String = "MONO"
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
    context: Context
) {
    private val cacheRoot: File = File(context.cacheDir, "sacred_podcasts").apply { mkdirs() }

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

    fun cacheFileFor(weekNumber: Int, turn: PodcastDialogueTurn): File {
        val weekDir = File(cacheRoot, "week_$weekNumber").apply { mkdirs() }
        val safeVoice = turn.voiceId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        return File(weekDir, "turn_${turn.index}_${safeVoice}.mp3")
    }

    fun isTurnCached(weekNumber: Int, turn: PodcastDialogueTurn): Boolean {
        val file = cacheFileFor(weekNumber, turn)
        return file.exists() && file.length() > 0L
    }

    fun getCachedTurnIndices(weekNumber: Int, turns: List<PodcastDialogueTurn>): Set<Int> {
        return turns.filter { isTurnCached(weekNumber, it) }.map { it.index }.toSet()
    }

    suspend fun getOrSynthesizeTurn(
        weekNumber: Int,
        turn: PodcastDialogueTurn
    ): Result<File> = withContext(Dispatchers.IO) {
        val targetFile = cacheFileFor(weekNumber, turn)
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
            val response = api.generateSpeech(
                apiKey = apiKey,
                request = MurfGenerateRequest(
                    text = turn.spokenText,
                    voiceId = turn.voiceId,
                    format = "MP3",
                    modelVersion = "GEN2"
                )
            )

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

    companion object {
        private const val PLACEHOLDER_KEY = "MY_MURF_API_KEY"
    }
}
