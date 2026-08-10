package de.tankzeit.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Query
import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
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

    @JsonAdapter(TankerkoenigPriceAdapter::class) val diesel: Double?,
    @JsonAdapter(TankerkoenigPriceAdapter::class) val e5: Double?,
    @JsonAdapter(TankerkoenigPriceAdapter::class) val e10: Double?,
    @JsonAdapter(TankerkoenigPriceAdapter::class) val price: Double?,
    val isOpen: Boolean
)

data class TankerkoenigPricesResponse(
    val ok: Boolean,
    val prices: Map<String, TankerkoenigPriceDto>?
)

data class TankerkoenigPriceDto(
    val status: String?,
    @JsonAdapter(TankerkoenigPriceAdapter::class) val diesel: Double?,
    @JsonAdapter(TankerkoenigPriceAdapter::class) val e5: Double?,
    @JsonAdapter(TankerkoenigPriceAdapter::class) val e10: Double?
)
/**
 * Tankerkönig liefert für diesel/e5/e10 den Wert `false`, wenn aktuell kein
 * Preis für die Station gemeldet ist - statt einer Zahl. Dieser Adapter
 * fängt das ab, statt beim Parsen der ganzen Liste abzustürzen.
 */
class TankerkoenigPriceAdapter : TypeAdapter<Double?>() {
    override fun write(out: JsonWriter, value: Double?) {
        if (value == null) out.nullValue() else out.value(value)
    }
    override fun read(reader: JsonReader): Double? {
        return when (reader.peek()) {
            JsonToken.NULL -> { reader.nextNull(); null }
            JsonToken.BOOLEAN -> { reader.nextBoolean(); null } // "false" = kein aktueller Preis
            JsonToken.NUMBER, JsonToken.STRING -> reader.nextDouble()
            else -> { reader.skipValue(); null }
        }
    }
}