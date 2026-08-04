package de.tankzeit.app.data.repository

import de.tankzeit.app.data.model.OilTrend
import de.tankzeit.app.data.remote.NetworkModule

class ForecastRepository {

    private val api = NetworkModule.alphaVantageApi

    /**
     * Lädt die letzten Brent-Rohöl-Datenpunkte und berechnet einen einfachen
     * Trend (Prozent-Änderung über die letzten 5 verfügbaren Werte). Liefert
     * null, wenn kein Key hinterlegt ist oder die Anfrage fehlschlägt - der
     * Aufrufer fällt dann auf die reine ADAC/MTS-K-Musterkurve zurück.
     */
    suspend fun fetchOilTrend(apiKey: String): OilTrend? {
        if (apiKey.isBlank()) return null
        return try {
            val response = api.brentCrude(apiKey = apiKey)
            val points = response.data?.mapNotNull { it.value.toDoubleOrNull()?.let { v -> it.date to v } }
                ?: return null
            if (points.size < 2) return null

            val recent = points.take(5)
            val newest = recent.first().second
            val oldest = recent.last().second
            if (oldest == 0.0) return null

            val trendPercent = ((newest - oldest) / oldest) * 100.0
            OilTrend(
                trendPercent = trendPercent,
                latestValue = newest,
                asOfDate = recent.first().first
            )
        } catch (e: Exception) {
            null
        }
    }
}
