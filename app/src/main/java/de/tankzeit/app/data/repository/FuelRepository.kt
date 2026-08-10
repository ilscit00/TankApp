package de.tankzeit.app.data.repository

import de.tankzeit.app.data.model.FuelType
import de.tankzeit.app.data.model.Station
import de.tankzeit.app.data.remote.NetworkModule
import de.tankzeit.app.data.remote.TankerkoenigStationDto
import de.tankzeit.app.location.GeoPoint
import kotlin.math.roundToInt
import kotlin.random.Random

sealed class StationsResult {
    data class Success(val stations: List<Station>, val isDemoData: Boolean) : StationsResult()
    data class Error(val message: String) : StationsResult()
}

class FuelRepository {

    private val api = NetworkModule.tankerkoenigApi

    suspend fun findStations(
        location: GeoPoint,
        radiusKm: Double,
        fuelType: FuelType,
        apiKey: String
    ): StationsResult {
        if (apiKey.isBlank()) {
            return StationsResult.Success(demoStations(location, fuelType), isDemoData = true)
        }
        return try {
            val response = api.listStations(
                lat = location.lat,
                lng = location.lng,
                radiusKm = radiusKm,
                type = fuelType.apiKey,
                apiKey = apiKey
            )
            if (response.ok && response.stations != null) {
                StationsResult.Success(
                    response.stations.map { it.toStation(fuelType) }
                        .sortedWith(compareBy(nullsLast()) { it.price }),
                    isDemoData = false
                )
            } else {
                StationsResult.Error(response.message ?: "Unbekannter Fehler bei der Tankerkönig-Anfrage")
            }
        } catch (e: Exception) {
            StationsResult.Error(e.message ?: "Netzwerkfehler")
        }
    }

    private fun TankerkoenigStationDto.toStation(fuelType: FuelType): Station {
        // Falls wir eine Typsuche machen (e.g. type=diesel), liefert Tankerkönig den Preis
        // oft direkt im Feld "price". Falls nicht, nehmen wir das typspezifische Feld.
        val priceValue = price ?: when (fuelType) {
            FuelType.E5 -> e5
            FuelType.E10 -> e10
            FuelType.DIESEL -> diesel
        }
        return Station(
            id = id,
            name = name,
            brand = brand ?: name,
            street = street ?: "",
            houseNumber = houseNumber ?: "",
            postCode = postCode?.toString() ?: "",
            place = place ?: "",
            lat = lat,
            lng = lng,
            distanceKm = dist,
            price = priceValue,
            isOpen = isOpen
        )
    }

    /**
     * Erzeugt plausible, klar als Demo gekennzeichnete Beispieldaten für den
     * Fall, dass noch kein Tankerkönig API-Key hinterlegt ist.
     */
    private fun demoStations(location: GeoPoint, fuelType: FuelType): List<Station> {
        val basePrice = when (fuelType) {
            FuelType.E5 -> 1.79
            FuelType.E10 -> 1.72
            FuelType.DIESEL -> 1.65
        }
        val brands = listOf("Aral", "Shell", "Esso", "JET", "Star", "TotalEnergies", "HEM")
        return brands.mapIndexed { index, brand ->
            val jitter = Random.nextDouble(-0.12, 0.12)
            Station(
                id = "demo-$index",
                name = "$brand Tankstelle",
                brand = brand,
                street = "Musterstraße",
                houseNumber = (index + 1).toString(),
                postCode = "70000",
                place = "Demo-Ort",
                lat = location.lat + Random.nextDouble(-0.02, 0.02),
                lng = location.lng + Random.nextDouble(-0.02, 0.02),
                distanceKm = (Random.nextDouble(0.3, 9.5) * 10).roundToInt() / 10.0,
                price = ((basePrice + jitter) * 1000).roundToInt() / 1000.0,
                isOpen = true
            )
        }.sortedBy { it.price }
    }
}
