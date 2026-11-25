package com.example.data.remote

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class EspUnlockPayload(
    val name: String,
    val email: String,
    @Json(name = "esp_provider")
    val espProvider: String = "Resend",
    @Json(name = "guide_requested")
    val guideRequested: String = "30-day-redemption-prayer-guide",
    val source: String = "sacred-steps-android",
    val timestamp: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class EspUnlockResponse(
    val success: Boolean? = null,
    val ok: Boolean? = null,
    val status: String = "",
    val provider: String = "",
    val message: String = "",
    val error: String = ""
)

interface EspWebhookApi {
    @POST
    suspend fun submitEmailUnlock(
        @Url url: String,
        @Body payload: EspUnlockPayload
    ): Response<EspUnlockResponse>
}

class EspWebhookClient {
    private val api: EspWebhookApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        val client = OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        Retrofit.Builder()
            .baseUrl("https://sacred-steps-affirmations.ai.studio/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(EspWebhookApi::class.java)
    }

    /**
     * Calls the handleAppSignup Firebase Cloud Function or Zapier Catch Hook configured via Secrets
     * (ESP_WEBHOOK_URL). All ESP API keys stay strictly server-side.
     */
    suspend fun sendPrayerGuideSignup(
        email: String,
        firstName: String,
        espProvider: String = "Resend"
    ): Boolean {
        val webhookUrl = BuildConfig.ESP_WEBHOOK_URL.trim()
        if (!webhookUrl.startsWith("http://") && !webhookUrl.startsWith("https://")) {
            return false
        }
        return runCatching {
            val response = api.submitEmailUnlock(
                url = webhookUrl,
                payload = EspUnlockPayload(
                    name = firstName.trim(),
                    email = email.trim(),
                    espProvider = espProvider
                )
            )
            val body = response.body()
            response.isSuccessful &&
                body?.success != false &&
                body?.ok != false &&
                !body?.status.equals("error", ignoreCase = true)
        }.getOrDefault(false)
    }
}
