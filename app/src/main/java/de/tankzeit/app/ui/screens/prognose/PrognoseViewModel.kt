package de.tankzeit.app.ui.screens.prognose

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.tankzeit.app.data.model.ForecastResult
import de.tankzeit.app.data.model.LocationMode
import de.tankzeit.app.data.repository.ForecastRepository
import de.tankzeit.app.data.repository.FuelRepository
import de.tankzeit.app.data.repository.StationsResult
import de.tankzeit.app.data.settings.SettingsDataStore
import de.tankzeit.app.forecast.ForecastEngine
import de.tankzeit.app.location.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class PrognoseUiState(
    val isLoading: Boolean = false,
    val forecast: ForecastResult? = null,
    val currentAveragePrice: Double? = null,
    val oilTrendUsed: Boolean = false,
    val errorMessage: String? = null
)

class PrognoseViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsStore = SettingsDataStore(application)
    private val fuelRepository = FuelRepository()
    private val forecastRepository = ForecastRepository()
    private val locationHelper = LocationHelper(application)

    private val _uiState = MutableStateFlow(PrognoseUiState())
    val uiState: StateFlow<PrognoseUiState> = _uiState.asStateFlow()

    fun loadForecast() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val settings = settingsStore.settingsFlow.first()

            val location = try {
                when (settings.locationMode) {
                    LocationMode.GPS -> locationHelper.currentLocation()
                    LocationMode.POSTAL_CODE -> locationHelper.geocodePostalCode(settings.postalCode)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Standort konnte nicht ermittelt werden. Bitte in den Einstellungen prüfen."
                )
                return@launch
            }

            val stationsResult = fuelRepository.findStations(
                location = location,
                radiusKm = settings.radiusKm,
                fuelType = settings.fuelType,
                apiKey = settings.tankerkoenigApiKey
            )

            val averagePrice = when (stationsResult) {
                is StationsResult.Success -> stationsResult.stations.mapNotNull { it.price }
                    .takeIf { it.isNotEmpty() }?.average()
                is StationsResult.Error -> null
            } ?: fallbackAveragePrice(settings.fuelType)

            val oilTrend = forecastRepository.fetchOilTrend(settings.alphaVantageApiKey)
            val forecast = ForecastEngine.computeForecast(averagePrice, oilTrend)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                forecast = forecast,
                currentAveragePrice = averagePrice,
                oilTrendUsed = oilTrend != null,
                errorMessage = null
            )
        }
    }

    private fun fallbackAveragePrice(fuelType: de.tankzeit.app.data.model.FuelType): Double = when (fuelType) {
        de.tankzeit.app.data.model.FuelType.E5 -> 1.79
        de.tankzeit.app.data.model.FuelType.E10 -> 1.72
        de.tankzeit.app.data.model.FuelType.DIESEL -> 1.65
    }
}
