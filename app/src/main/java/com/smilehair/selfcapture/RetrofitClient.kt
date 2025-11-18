package com.smilehair.selfcapture

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .connectTimeout(90, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .writeTimeout(180, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .retryOnConnectionFailure(true)
            .build()
    }

    private var retrofit: Retrofit? = null
    private var cachedBaseUrl: String? = null

    init {
        ApiConfigManager.addListener {
            synchronized(this) {
                retrofit = null
                cachedBaseUrl = null
            }
        }
    }

    val apiService: ApiService
        get() = getRetrofit().create(ApiService::class.java)

    private fun getRetrofit(): Retrofit {
        val currentBaseUrl = ApiConfigManager.getBaseUrl()
        synchronized(this) {
            val existing = retrofit
            if (existing != null && cachedBaseUrl == currentBaseUrl) {
                return existing
            }

            val newRetrofit = Retrofit.Builder()
                .baseUrl(currentBaseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            retrofit = newRetrofit
            cachedBaseUrl = currentBaseUrl
            return newRetrofit
        }
    }

    fun getBaseUrl(): String = ApiConfigManager.getBaseUrl()
}
