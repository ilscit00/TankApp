package de.tankzeit.app.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.tankzeit.app.data.model.FuelType
import de.tankzeit.app.data.model.LocationMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "tankzeit_settings")

/**
 * Persistiert die zuletzt genutzten Einstellungen: Kraftstofftyp, Standort
 * (PLZ oder GPS), Suchradius und die beiden API-Keys.
 */
class SettingsDataStore(private val context: Context) {

    private object Keys {
        val FUEL_TYPE = stringPreferencesKey("fuel_type")
        val LOCATION_MODE = stringPreferencesKey("location_mode")
        val POSTAL_CODE = stringPreferencesKey("postal_code")
        val RADIUS_KM = doublePreferencesKey("radius_km")
        val TANKERKOENIG_API_KEY = stringPreferencesKey("tankerkoenig_api_key")
        val ALPHA_VANTAGE_API_KEY = stringPreferencesKey("alpha_vantage_api_key")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            fuelType = FuelType.valueOf(prefs[Keys.FUEL_TYPE] ?: FuelType.E10.name),
            locationMode = LocationMode.valueOf(prefs[Keys.LOCATION_MODE] ?: LocationMode.POSTAL_CODE.name),
            postalCode = prefs[Keys.POSTAL_CODE] ?: "",
            radiusKm = prefs[Keys.RADIUS_KM] ?: 5.0,
            tankerkoenigApiKey = prefs[Keys.TANKERKOENIG_API_KEY] ?: "",
            alphaVantageApiKey = prefs[Keys.ALPHA_VANTAGE_API_KEY] ?: ""
        )
    }

    suspend fun setFuelType(fuelType: FuelType) {
        context.dataStore.edit { it[Keys.FUEL_TYPE] = fuelType.name }
    }

    suspend fun setLocationMode(mode: LocationMode) {
        context.dataStore.edit { it[Keys.LOCATION_MODE] = mode.name }
    }

    suspend fun setPostalCode(postalCode: String) {
        context.dataStore.edit { it[Keys.POSTAL_CODE] = postalCode }
    }

    suspend fun setRadiusKm(radiusKm: Double) {
        context.dataStore.edit { it[Keys.RADIUS_KM] = radiusKm.coerceIn(1.0, 25.0) }
    }

    suspend fun setTankerkoenigApiKey(key: String) {
        context.dataStore.edit { it[Keys.TANKERKOENIG_API_KEY] = key }
    }

    suspend fun setAlphaVantageApiKey(key: String) {
        context.dataStore.edit { it[Keys.ALPHA_VANTAGE_API_KEY] = key }
    }
}

data class AppSettings(
    val fuelType: FuelType = FuelType.E10,
    val locationMode: LocationMode = LocationMode.POSTAL_CODE,
    val postalCode: String = "",
    val radiusKm: Double = 5.0,
    val tankerkoenigApiKey: String = "",
    val alphaVantageApiKey: String = ""
) {
    val hasTankerkoenigKey: Boolean get() = tankerkoenigApiKey.isNotBlank()
    val hasAlphaVantageKey: Boolean get() = alphaVantageApiKey.isNotBlank()
}
