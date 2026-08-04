package de.tankzeit.app.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.tankzeit.app.data.model.FuelType
import de.tankzeit.app.data.model.LocationMode
import de.tankzeit.app.data.settings.AppSettings
import de.tankzeit.app.data.settings.SettingsDataStore
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsStore = SettingsDataStore(application)

    val settings: StateFlow<AppSettings> = settingsStore.settingsFlow.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = AppSettings()
    )

    fun setFuelType(fuelType: FuelType) = viewModelScope.launch { settingsStore.setFuelType(fuelType) }
    fun setLocationMode(mode: LocationMode) = viewModelScope.launch { settingsStore.setLocationMode(mode) }
    fun setPostalCode(postalCode: String) = viewModelScope.launch { settingsStore.setPostalCode(postalCode) }
    fun setRadiusKm(radiusKm: Double) = viewModelScope.launch { settingsStore.setRadiusKm(radiusKm) }
    fun setTankerkoenigApiKey(key: String) = viewModelScope.launch { settingsStore.setTankerkoenigApiKey(key) }
    fun setAlphaVantageApiKey(key: String) = viewModelScope.launch { settingsStore.setAlphaVantageApiKey(key) }
}
