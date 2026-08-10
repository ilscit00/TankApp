package de.tankzeit.app.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Zentrale Erzeugung der Retrofit-Clients. CORS spielt hier - anders als im
 * Browser-Prototyp - keine Rolle mehr, da OkHttp direkt auf Netzwerkebene arbeitet.
 */
object NetworkModule {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val tankerkoenigApi: TankerkoenigApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://creativecommons.tankerkoenig.de/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TankerkoenigApi::class.java)
    }

    val alphaVantageApi: AlphaVantageApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.alphavantage.co/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AlphaVantageApi::class.java)
    }
}
