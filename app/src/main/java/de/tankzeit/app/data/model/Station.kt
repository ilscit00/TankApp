package de.tankzeit.app.data.model

/**
 * Repräsentiert eine Tankstelle mit aktuellem Preis für den gewählten Kraftstofftyp.
 */
data class Station(
    val id: String,
    val name: String,
    val brand: String,
    val street: String,
    val houseNumber: String,
    val postCode: String,
    val place: String,
    val lat: Double,
    val lng: Double,
    val distanceKm: Double,
    val price: Double?,
    val isOpen: Boolean
) {
    val address: String
        get() = "$street $houseNumber, $postCode $place".trim()
}

enum class FuelType(val apiKey: String, val label: String) {
    E5("e5", "Super E5"),
    E10("e10", "Super E10"),
    DIESEL("diesel", "Diesel")
}

enum class LocationMode {
    POSTAL_CODE,
    GPS
}
