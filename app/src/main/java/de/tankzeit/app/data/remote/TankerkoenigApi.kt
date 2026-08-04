package de.tankzeit.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Tankerkönig API (https://creativecommons.tankerkoenig.de)
 * Freier Tarif: max. 10 Anfragen pro Minute pro Key, Umkreissuche 1-25 km.
 */
interface TankerkoenigApi {

    @GET("json/list.php")
    suspend fun listStations(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("rad") radiusKm: Double,
        @Query("sort") sort: String = "price",
        @Query("type") type: String,
        @Query("apikey") apiKey: String
    ): TankerkoenigListResponse

    @GET("json/prices.php")
    suspend fun prices(
        @Query("ids") stationIds: String,
        @Query("apikey") apiKey: String
    ): TankerkoenigPricesResponse
}

data class TankerkoenigListResponse(
    val ok: Boolean,
    val license: String?,
    val data: String?,
    val status: String?,
    val message: String?,
    val stations: List<TankerkoenigStationDto>?
)

data class TankerkoenigStationDto(
    val id: String,
    val name: String,
    val brand: String?,
    val street: String?,
    val houseNumber: String?,
    val postCode: Int?,
    val place: String?,
    val lat: Double,
    val lng: Double,
    val dist: Double,
    val diesel: Double?,
    val e5: Double?,
    val e10: Double?,
    val isOpen: Boolean
)

data class TankerkoenigPricesResponse(
    val ok: Boolean,
    val prices: Map<String, TankerkoenigPriceDto>?
)

data class TankerkoenigPriceDto(
    val status: String?,
    val diesel: Double?,
    val e5: Double?,
    val e10: Double?
)
