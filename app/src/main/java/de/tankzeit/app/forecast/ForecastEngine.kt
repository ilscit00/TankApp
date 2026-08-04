package de.tankzeit.app.forecast

import de.tankzeit.app.data.model.ForecastResult
import de.tankzeit.app.data.model.HourlyForecastPoint
import de.tankzeit.app.data.model.OilTrend
import de.tankzeit.app.data.model.WeeklyForecastPoint

/**
 * Berechnet die Tages- und Wochenprognose.
 *
 * Grundlage ist das von ADAC und dem Marktransparenzstelle für Kraftstoffe
 * (MTS-K) wiederholt dokumentierte Tagesmuster deutscher Kraftstoffpreise:
 * morgens (ca. 6-9 Uhr) am teuersten, im Tagesverlauf mehrere kleinere
 * Wellen, am günstigsten meist spät abends / in der Nacht (ca. 20-24 Uhr).
 * Wochentagsmuster: Montag/Dienstag tendenziell günstiger, das Wochenende
 * (insbesondere Freitag-Sonntag) tendenziell teurer.
 *
 * Der optionale Brent-Rohölpreistrend (Alpha Vantage) wird nur als leicht
 * gedämpfter Faktor eingerechnet, damit kurzfristige Rohstoff-Volatilität
 * die robuste Tagesmusterkurve nicht überzeichnet.
 */
object ForecastEngine {

    // Relative Faktoren pro Stunde, empirisch an ADAC/MTS-K-Veröffentlichungen angelehnt.
    // 1.0 = Tagesdurchschnitt. Werte sind bewusst moderat gehalten.
    private val hourlyBasePattern: Map<Int, Double> = mapOf(
        0 to 0.975, 1 to 0.970, 2 to 0.968, 3 to 0.967, 4 to 0.970, 5 to 0.985,
        6 to 1.020, 7 to 1.040, 8 to 1.035, 9 to 1.015, 10 to 1.000, 11 to 0.998,
        12 to 1.005, 13 to 1.010, 14 to 1.008, 15 to 1.005, 16 to 1.010, 17 to 1.015,
        18 to 1.005, 19 to 0.995, 20 to 0.985, 21 to 0.978, 22 to 0.972, 23 to 0.970
    )

    // Relativer Faktor pro Wochentag (1=Montag ... 7=Sonntag).
    private val weeklyBasePattern: Map<Int, Double> = mapOf(
        1 to 0.985, 2 to 0.982, 3 to 0.990, 4 to 1.000, 5 to 1.015, 6 to 1.012, 7 to 1.008
    )

    private val weekdayLabels = mapOf(
        1 to "Mo", 2 to "Di", 3 to "Mi", 4 to "Do", 5 to "Fr", 6 to "Sa", 7 to "So"
    )

    // Ölpreistrend wird stark gedämpft eingerechnet: max. +/- 1.5% Verschiebung
    // der gesamten Kurve, selbst bei einem starken kurzfristigen Trend.
    private const val OIL_TREND_DAMPING = 0.06
    private const val MAX_OIL_INFLUENCE_PERCENT = 1.5

    fun computeForecast(currentAveragePrice: Double, oilTrend: OilTrend?): ForecastResult {
        val oilInfluencePercent = oilTrend?.let {
            (it.trendPercent * OIL_TREND_DAMPING).coerceIn(-MAX_OIL_INFLUENCE_PERCENT, MAX_OIL_INFLUENCE_PERCENT)
        } ?: 0.0
        val oilFactor = 1.0 + (oilInfluencePercent / 100.0)

        val hourly = hourlyBasePattern.entries.sortedBy { it.key }.map { (hour, factor) ->
            val adjustedFactor = factor * oilFactor
            HourlyForecastPoint(
                hour = hour,
                relativePriceFactor = adjustedFactor,
                estimatedPrice = roundTo3(currentAveragePrice * adjustedFactor)
            )
        }

        val weekly = weeklyBasePattern.entries.sortedBy { it.key }.map { (day, factor) ->
            WeeklyForecastPoint(
                dayOfWeek = day,
                dayLabel = weekdayLabels.getValue(day),
                relativePriceFactor = factor * oilFactor
            )
        }

        val cheapestHour = hourly.minByOrNull { it.relativePriceFactor }?.hour ?: 22
        val cheapestWeekday = weekly.minByOrNull { it.relativePriceFactor }?.dayLabel ?: "Di"

        return ForecastResult(
            hourly = hourly,
            weekly = weekly,
            cheapestHourToday = cheapestHour,
            cheapestWeekday = cheapestWeekday,
            oilTrendApplied = oilTrend != null,
            oilTrendDampingFactor = OIL_TREND_DAMPING
        )
    }

    private fun roundTo3(value: Double): Double = Math.round(value * 1000.0) / 1000.0
}
