package de.tankzeit.app.ui.screens.preise

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.tankzeit.app.data.model.LocationMode
import de.tankzeit.app.ui.components.StationCard
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreiseScreen(viewModel: PreiseViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.refresh()
    }

    LaunchedEffect(uiState.settings.fuelType, uiState.settings.locationMode, uiState.settings.radiusKm) {
        if (uiState.settings.locationMode == LocationMode.GPS && !viewModel.hasLocationPermission()) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            viewModel.refresh()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preise") },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Aktualisieren")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.errorMessage != null -> ErrorState(uiState.errorMessage!!)
                else -> StationsList(
                    stations = uiState.stations,
                    isDemoData = uiState.isDemoData,
                    fuelLabel = uiState.settings.fuelType.label,
                    onStationClick = { station ->
                        val uri = String.format(
                            Locale.US,
                            "geo:%f,%f?q=%f,%f(%s)",
                            station.lat, station.lng,
                            station.lat, station.lng,
                            Uri.encode(station.brand + " " + station.name)
                        )
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
private fun StationsList(
    stations: List<de.tankzeit.app.data.model.Station>,
    isDemoData: Boolean,
    fuelLabel: String,
    onStationClick: (de.tankzeit.app.data.model.Station) -> Unit
) {
    val cheapestId = stations.filter { it.price != null }.minByOrNull { it.price!! }?.id
    Column(modifier = Modifier.fillMaxSize()) {
        if (isDemoData) {
            DemoDataBanner()
        }
        Text(
            text = "Aktuelle Preise für $fuelLabel",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
        if (stations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Keine Tankstellen im gewählten Umkreis gefunden.")
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(stations, key = { it.id }) { station ->
                    StationCard(
                        station = station,
                        isCheapest = station.id == cheapestId,
                        onClick = { onStationClick(station) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DemoDataBanner() {
    Surface(color = MaterialTheme.colorScheme.tertiaryContainer) {
        Text(
            text = "Demo-Daten – hinterlege in den Einstellungen deinen Tankerkönig API-Key für Live-Preise.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun ErrorState(message: String) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
