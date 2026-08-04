package de.tankzeit.app.ui.screens.preise

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.tankzeit.app.data.model.LocationMode
import de.tankzeit.app.data.model.Station
import de.tankzeit.app.data.repository.FuelRepository
import de.tankzeit.app.data.repository.StationsResult
import de.tankzeit.app.data.settings.AppSettings
import de.tankzeit.app.data.settings.SettingsDataStore
import de.tankzeit.app.location.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class PreiseUiState(
    val isLoading: Boolean = false,
    val stations: List<Station> = emptyList(),
    val isDemoData: Boolean = false,
    val errorMessage: String? = null,
    val settings: AppSettings = AppSettings()
)

class PreiseViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsStore = SettingsDataStore(application)
    private val fuelRepository = FuelRepository()
    private val locationHelper = LocationHelper(application)

    private val _uiState = MutableStateFlow(PreiseUiState())
    val uiState: StateFlow<PreiseUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsStore.settingsFlow.collectLatest { settings ->
                _uiState.value = _uiState.value.copy(settings = settings)
            }
        }
    }

    fun refresh() {
        val settings = _uiState.value.settings
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val location = when (settings.locationMode) {
                    LocationMode.GPS -> locationHelper.currentLocation()
                    LocationMode.POSTAL_CODE -> {
                        if (settings.postalCode.isBlank()) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = "Bitte in den Einstellungen eine Postleitzahl eingeben."
                            )
                            return@launch
                        }
                        locationHelper.geocodePostalCode(settings.postalCode)
                    }
                }

                when (val result = fuelRepository.findStations(
                    location = location,
                    radiusKm = settings.radiusKm,
                    fuelType = settings.fuelType,
                    apiKey = settings.tankerkoenigApiKey
                )) {
                    is StationsResult.Success -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        stations = result.stations,
                        isDemoData = result.isDemoData,
                        errorMessage = null
                    )
                    is StationsResult.Error -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            } catch (e: SecurityException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Standortberechtigung wird benötigt, um Tankstellen in der Nähe zu finden."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Standort konnte nicht ermittelt werden."
                )
            }
        }
    }

    fun hasLocationPermission(): Boolean = locationHelper.hasLocationPermission()
}
