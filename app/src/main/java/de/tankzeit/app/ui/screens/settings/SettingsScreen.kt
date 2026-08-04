package de.tankzeit.app.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.tankzeit.app.data.model.FuelType
import de.tankzeit.app.data.model.LocationMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Einstellungen") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            SectionTitle("Kraftstofftyp")
            FuelTypeSelector(selected = settings.fuelType, onSelect = viewModel::setFuelType)

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle("Standort")
            LocationModeSelector(selected = settings.locationMode, onSelect = viewModel::setLocationMode)
            Spacer(modifier = Modifier.height(12.dp))
            if (settings.locationMode == LocationMode.POSTAL_CODE) {
                var postalCodeText by remember(settings.postalCode) { mutableStateOf(settings.postalCode) }
                OutlinedTextField(
                    value = postalCodeText,
                    onValueChange = {
                        postalCodeText = it
                        if (it.length <= 5) viewModel.setPostalCode(it)
                    },
                    label = { Text("Postleitzahl") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = "Der aktuelle GPS-Standort wird beim Laden der Preise abgefragt.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle("Suchradius: ${settings.radiusKm.toInt()} km")
            Slider(
                value = settings.radiusKm.toFloat(),
                onValueChange = { viewModel.setRadiusKm(it.toDouble()) },
                valueRange = 1f..25f,
                steps = 23
            )

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle("API-Schlüssel")
            Spacer(modifier = Modifier.height(8.dp))
            ApiKeyField(
                label = "Tankerkönig API-Key",
                value = settings.tankerkoenigApiKey,
                onValueChange = viewModel::setTankerkoenigApiKey,
                helperText = "Kostenlos erhältlich auf creativecommons.tankerkoenig.de"
            )
            Spacer(modifier = Modifier.height(16.dp))
            ApiKeyField(
                label = "Alpha Vantage API-Key (optional)",
                value = settings.alphaVantageApiKey,
                onValueChange = viewModel::setAlphaVantageApiKey,
                helperText = "Kostenlos erhältlich auf alphavantage.co – verfeinert die Prognose mit dem Ölpreistrend."
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleMedium)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FuelTypeSelector(selected: FuelType, onSelect: (FuelType) -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        FuelType.values().forEachIndexed { index, type ->
            SegmentedButton(
                selected = selected == type,
                onClick = { onSelect(type) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = FuelType.values().size)
            ) {
                Text(type.label)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationModeSelector(selected: LocationMode, onSelect: (LocationMode) -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        SegmentedButton(
            selected = selected == LocationMode.POSTAL_CODE,
            onClick = { onSelect(LocationMode.POSTAL_CODE) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
        ) { Text("Postleitzahl") }
        SegmentedButton(
            selected = selected == LocationMode.GPS,
            onClick = { onSelect(LocationMode.GPS) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
        ) { Text("GPS") }
    }
}

@Composable
private fun ApiKeyField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    helperText: String
) {
    var text by remember(value) { mutableStateOf(value) }
    var visible by remember { mutableStateOf(false) }

    Column {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    onValueChange(it)
                },
                label = { Text(label) },
                visualTransformation = if (visible) androidx.compose.ui.text.input.VisualTransformation.None
                else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = helperText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        androidx.compose.material3.TextButton(onClick = { visible = !visible }) {
            Text(if (visible) "Verbergen" else "Anzeigen")
        }
    }
}
