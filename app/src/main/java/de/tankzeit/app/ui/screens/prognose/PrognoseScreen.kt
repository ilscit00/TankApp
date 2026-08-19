package de.tankzeit.app.ui.screens.prognose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.tankzeit.app.data.model.PriceTrend
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrognoseScreen(viewModel: PrognoseViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadForecast() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prognose") },
                actions = {
                    IconButton(onClick = { viewModel.loadForecast() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Aktualisieren")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.errorMessage != null -> Text(
                    text = uiState.errorMessage!!,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp)
                )
                uiState.forecast != null -> PrognoseContent(
                    forecast = uiState.forecast!!,
                    oilTrendUsed = uiState.oilTrendUsed,
                    oilTrendStatus = uiState.oilTrendStatus,
                    oilTrendIsStale = uiState.oilTrendIsStale
                )
            }
        }
    }
}

@Composable
private fun PrognoseContent(
    forecast: de.tankzeit.app.data.model.ForecastResult,
    oilTrendUsed: Boolean,
    oilTrendStatus: String? = null,
    oilTrendIsStale: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (oilTrendStatus != null) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (oilTrendIsStale) MaterialTheme.colorScheme.errorContainer 
                                    else MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Text(
                    text = oilTrendStatus,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (oilTrendIsStale) MaterialTheme.colorScheme.onErrorContainer 
                            else MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }

        MarketAnalysisCard(forecast)
        
        Spacer(modifier = Modifier.height(24.dp))

        forecast.estimatedPriceTomorrow?.let { price ->
            Text(
                text = "Erwarteter Durchschnittspreis morgen:",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = String.format(Locale.GERMANY, "ca. %.3f €", price),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Tagesverlauf (24h)", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                HourlyPriceChart(points = forecast.hourly, cheapestHour = forecast.cheapestHourToday)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Wochenmuster (günstigster Tag: ${forecast.cheapestWeekday})",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                WeeklyPatternChart(points = forecast.weekly)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (oilTrendUsed) {
                "Basis: dokumentiertes ADAC/MTS-K Tagesmuster, leicht angepasst durch den aktuellen Brent-Rohölpreistrend."
            } else {
                "Basis: dokumentiertes ADAC/MTS-K Tagesmuster. Hinterlege einen Alpha-Vantage-Key für eine zusätzliche Ölpreis-Anpassung."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MarketAnalysisCard(forecast: de.tankzeit.app.data.model.ForecastResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Börsen-Check",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = forecast.oilPrice?.let { String.format(Locale.GERMANY, "Brent Öl: %.2f $", it) } ?: "Ölpreis: N/A",
                    style = MaterialTheme.typography.titleLarge
                )
                forecast.oilPriceDate?.let {
                    Text(
                        text = "Stand: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Trend morgen",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (icon, color, label) = when (forecast.nextDayTrend) {
                        PriceTrend.RISING -> Triple(Icons.Default.TrendingUp, Color(0xFFD32F2F), "Steigend")
                        PriceTrend.FALLING -> Triple(Icons.Default.TrendingDown, Color(0xFF388E3C), "Fallend")
                        PriceTrend.STABLE -> Triple(Icons.Default.TrendingFlat, Color(0xFF757575), "Stabil")
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium,
                        color = color
                    )
                }
            }
        }
    }
}
