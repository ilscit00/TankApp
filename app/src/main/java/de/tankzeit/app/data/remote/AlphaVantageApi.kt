package de.tankzeit.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Alpha Vantage API (https://www.alphavantage.co) - optionale Datenquelle.
 * Liefert den Brent-Rohölpreis (BRENT), der als gedämpfter Einflussfaktor
 * in die Prognose einfließt. Freier Tarif: 25 Anfragen/Tag.
 */
interface AlphaVantageApi {

    @GET("query")
    suspend fun brentCrude(
        @Query("function") function: String = "BRENT",
        @Query("interval") interval: String = "daily",
        @Query("apikey") apiKey: String
    ): AlphaVantageResponse
}

data class AlphaVantageResponse(
    val name: String?,
    val interval: String?,
    val unit: String?,
    val data: List<AlphaVantageDataPoint>?,
    @com.google.gson.annotations.SerializedName("Error Message") val errorMessage: String?,
    @com.google.gson.annotations.SerializedName("Note") val note: String?
)

data class AlphaVantageDataPoint(
    val date: String,
    val value: String
)
