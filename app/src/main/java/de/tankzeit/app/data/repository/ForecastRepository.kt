package de.tankzeit.app.data.repository

import de.tankzeit.app.data.model.OilTrend
import de.tankzeit.app.data.remote.NetworkModule
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class ForecastRepository {

    private val api = NetworkModule.alphaVantageApi

    /**
     * Lädt die letzten Brent-Rohöl-Datenpunkte und berechnet einen einfachen
     * Trend. Prüft auf API-Meldungen (Limit, Fehler) und Aktualität der Daten.
     */
    suspend fun fetchOilTrend(apiKey: String): OilTrend? {
        if (apiKey.isBlank()) return null
        return try {
            val response = api.brentCrude(apiKey = apiKey)

            // API Status prüfen
            val apiNote = response.errorMessage ?: response.note ?: response.information
            var statusMessage = when {
                apiNote?.contains("rate limit", ignoreCase = true) == true -> "API Limit erreicht"
                apiNote != null -> "AlphaVantage Info: $apiNote"
                else -> null
            }

            // Typ der Daten (Daily/Monthly) prüfen
            val isMonthly = response.interval?.contains("monthly", ignoreCase = true) == true
            if (isMonthly && statusMessage == null) {
                statusMessage = "Hinweis: Nur Monatsdaten verfügbar"
            }

            // Datenpunkte filtern und sortieren
            val points = response.data?.mapNotNull { 
                if (it.value == ".") null else it.value.toDoubleOrNull()?.let { v -> it.date to v } 
            }?.sortedByDescending { it.first }
                ?: return if (statusMessage != null) OilTrend(0.0, 0.0, "", statusMessage) else null

            if (points.isEmpty()) return if (statusMessage != null) OilTrend(0.0, 0.0, "", statusMessage) else null

            val recent = points.take(5)
            val newest = recent.first()
            val oldest = recent.last()

            // Aktualität prüfen (max 3 Tage alt, da am Wochenende oft keine Updates)
            val asOfDate = newest.first
            val isStale = try {
                val date = LocalDate.parse(asOfDate, DateTimeFormatter.ISO_LOCAL_DATE)
                val diff = ChronoUnit.DAYS.between(date, LocalDate.now())
                diff > 3
            } catch (e: Exception) {
                false
            }

            val trendPercent = if (recent.size >= 2 && oldest.second != 0.0) {
                ((newest.second - oldest.second) / oldest.second) * 100.0
            } else 0.0

            OilTrend(
                trendPercent = trendPercent,
                latestValue = newest.second,
                asOfDate = asOfDate,
                statusMessage = if (isStale) "Daten veraltet (Stand $asOfDate)" else statusMessage,
                isStale = isStale
            )
        } catch (e: Exception) {
            null
        }
    }
}
