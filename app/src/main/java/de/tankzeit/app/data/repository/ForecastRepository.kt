package de.tankzeit.app.data.repository

import de.tankzeit.app.data.model.OilTrend
import de.tankzeit.app.data.remote.NetworkModule
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class ForecastRepository {

    private val api = NetworkModule.oilPriceApi

    /**
     * Lädt den aktuellen Brent-Rohölpreis von OilPriceAPI und berechnet
     * den Trend anhand der 24h-Veränderung.
     */
    suspend fun fetchOilTrend(apiKey: String): OilTrend? {
        if (apiKey.isBlank()) return null
        return try {
            val authHeader = if (apiKey.trim().startsWith("Token ", ignoreCase = true)) apiKey.trim() else "Token ${apiKey.trim()}"
            val response = api.getLatestPrice(authHeader = authHeader)

            if (response.status != "success" || response.data == null) {
                return OilTrend(0.0, 0.0, "", response.status ?: "OilPriceAPI Fehler")
            }

            val data = response.data
            val price = data.price ?: return null

            val trendPercent = data.changes?.h24?.percent ?: run {
                val prev = data.changes?.h24?.previousPrice
                if (prev != null && prev > 0.0) {
                    ((price - prev) / prev) * 100.0
                } else 0.0
            }

            val rawAsOf = data.asOf ?: data.updatedAt ?: data.createdAt
            val asOfDate = rawAsOf?.take(10) ?: LocalDate.now().toString()

            val isStale = data.stale == true || try {
                val date = LocalDate.parse(asOfDate, DateTimeFormatter.ISO_LOCAL_DATE)
                ChronoUnit.DAYS.between(date, LocalDate.now()) > 3
            } catch (e: Exception) {
                false
            }

            val statusMsg = when {
                isStale -> "Daten veraltet (Stand $asOfDate)"
                else -> null
            }

            OilTrend(
                trendPercent = trendPercent,
                latestValue = price,
                asOfDate = asOfDate,
                statusMessage = statusMsg,
                isStale = isStale
            )
        } catch (e: Exception) {
            null
        }
    }
}
