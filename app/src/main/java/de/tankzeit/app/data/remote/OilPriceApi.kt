package de.tankzeit.app.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * OilPriceAPI (https://www.oilpriceapi.com)
 * Liefert den aktuellen Brent-Rohölpreis (BRENT_CRUDE_USD).
 * Header: Authorization: Token <API_KEY>
 */
interface OilPriceApi {

    @GET("v1/prices/latest")
    suspend fun getLatestPrice(
        @Header("Authorization") authHeader: String,
        @Query("by_code") byCode: String = "BRENT_CRUDE_USD"
    ): OilPriceResponse
}

data class OilPriceResponse(
    val status: String?,
    val data: OilPriceData?
)

data class OilPriceData(
    val code: String?,
    val price: Double?,
    val formatted: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    @SerializedName("as_of") val asOf: String?,
    val stale: Boolean?,
    val changes: OilPriceChanges?
)

data class OilPriceChanges(
    @SerializedName("24h") val h24: OilPriceChange24h?
)

data class OilPriceChange24h(
    val amount: Double?,
    val percent: Double?,
    @SerializedName("previous_price") val previousPrice: Double?
)
