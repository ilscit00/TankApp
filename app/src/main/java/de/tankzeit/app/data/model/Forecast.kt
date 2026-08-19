package de.tankzeit.app.data.model

/**
 * Ein Punkt in der Tagesprognose: Stunde (0-23) und relativer Preisfaktor
 * gegenüber dem aktuellen Durchschnittspreis (1.0 = Durchschnitt).
 */
data class HourlyForecastPoint(
    val hour: Int,
    val relativePriceFactor: Double,
    val estimatedPrice: Double
)

/**
 * Ein Punkt in der Wochenprognose: Wochentag (1=Montag..7=Sonntag) und
 * relativer Preisfaktor gegenüber dem Wochendurchschnitt.
 */
data class WeeklyForecastPoint(
    val dayOfWeek: Int,
    val dayLabel: String,
    val relativePriceFactor: Double
)

data class ForecastResult(
    val hourly: List<HourlyForecastPoint>,
    val weekly: List<WeeklyForecastPoint>,
    val cheapestHourToday: Int,
    val cheapestWeekday: String,
    val oilTrendApplied: Boolean,
    val oilTrendDampingFactor: Double,
    val oilPrice: Double? = null,
    val oilPriceDate: String? = null,
    val nextDayTrend: PriceTrend = PriceTrend.STABLE,
    val estimatedPriceTomorrow: Double? = null
)

enum class PriceTrend {
    RISING, FALLING, STABLE
}

/**
 * Brent-Rohöl Trend, optional von Alpha Vantage geladen.
 * trendPercent > 0 bedeutet steigender Ölpreis über die letzten Datenpunkte.
 */
data class OilTrend(
    val trendPercent: Double,
    val latestValue: Double,
    val asOfDate: String,
    val statusMessage: String? = null,
    val isStale: Boolean = false
)
