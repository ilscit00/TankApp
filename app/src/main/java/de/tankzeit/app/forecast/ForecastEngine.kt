package de.tankzeit.app.forecast

import de.tankzeit.app.data.model.ForecastResult
import de.tankzeit.app.data.model.HourlyForecastPoint
import de.tankzeit.app.data.model.OilTrend
import de.tankzeit.app.data.model.PriceTrend
import de.tankzeit.app.data.model.WeeklyForecastPoint
import java.time.LocalDate

/**
 * Berechnet die Tages- und Wochenprognose.
 *
 * Grundlage ist das von ADAC und der Markttransparenzstelle für Kraftstoffe
 * (MTS-K) dokumentierte Tagesmuster deutscher Kraftstoffpreise:
 * Morgens (ca. 6-8 Uhr) am teuersten, gefolgt von stufenweisen Preissenkungen
 * mit prägnanten Preisanpassungs-Sprüngen (insbesondere der markanten Erhöhung
 * um 12 Uhr sowie weiteren kleineren Wellen am Nachmittag).
 * Am günstigsten ist es meist am späten Abend (ca. 20-22 Uhr).
 *
 * Der optionale Brent-Rohölpreistrend (OilPriceAPI) wird als leicht
 * gedämpfter Faktor eingerechnet, damit kurzfristige Rohstoff-Volatilität
 * die robuste Tagesmusterkurve nicht überzeichnet.
 */
object ForecastEngine {

    // Relative Faktoren pro Stunde, angelehnt an aktuelle ADAC/MTS-K Auswertungen.
    // 1.0 = Tagesdurchschnitt.
    // Beachtet die gesetzlich/marktüblichen Preiswellen inklusive der Erhöhung um 12 Uhr.
    private val hourlyBasePattern: Map<Int, Double> = mapOf(
        0 to 0.978, 1 to 0.975, 2 to 0.972, 3 to 0.970, 4 to 0.972, 5 to 0.990,
        6 to 1.045, 7 to 1.050, 8 to 1.030, 9 to 1.015, 10 to 1.002, 11 to 0.992,
        12 to 1.032, 13 to 1.018, 14 to 1.008, 15 to 1.015, 16 to 1.008, 17 to 1.020,
        18 to 1.010, 19 to 0.998, 20 to 0.980, 21 to 0.968, 22 to 0.965, 23 to 0.972
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
        // Veraltete Trends (> 2 Tage) werden für die Preisberechnung ignoriert,
        // aber für die Anzeige der Rohdaten (oilPrice/Date) im Resultat behalten.
        val effectiveOilTrend = if (oilTrend?.isStale == true) null else oilTrend
        
        val oilInfluencePercent = effectiveOilTrend?.let {
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

        // Tendenz für morgen berechnen
        val today = LocalDate.now().dayOfWeek.value // 1 (Mon) to 7 (Sun)
        val tomorrow = if (today == 7) 1 else today + 1
        
        val factorToday = weeklyBasePattern[today] ?: 1.0
        val factorTomorrow = weeklyBasePattern[tomorrow] ?: 1.0
        
        // Ölpreis-Trend mit einbeziehen (falls aktuell)
        val oilTrendDaily = (effectiveOilTrend?.trendPercent ?: 0.0) / 30.0
        val adjustedFactorTomorrow = factorTomorrow * (1.0 + (oilTrendDaily * OIL_TREND_DAMPING / 100.0))

        val diff = adjustedFactorTomorrow - factorToday
        val nextDayTrend = when {
            diff > 0.002 -> PriceTrend.RISING
            diff < -0.002 -> PriceTrend.FALLING
            else -> PriceTrend.STABLE
        }

        val estimatedPriceTomorrow = roundTo3(currentAveragePrice * (adjustedFactorTomorrow / factorToday))

        return ForecastResult(
            hourly = hourly,
            weekly = weekly,
            cheapestHourToday = cheapestHour,
            cheapestWeekday = cheapestWeekday,
            oilTrendApplied = effectiveOilTrend != null,
            oilTrendDampingFactor = OIL_TREND_DAMPING,
            oilPrice = oilTrend?.latestValue,
            oilPriceDate = oilTrend?.asOfDate,
            nextDayTrend = nextDayTrend,
            estimatedPriceTomorrow = estimatedPriceTomorrow
        )
    }

    private fun roundTo3(value: Double): Double = Math.round(value * 1000.0) / 1000.0
}
