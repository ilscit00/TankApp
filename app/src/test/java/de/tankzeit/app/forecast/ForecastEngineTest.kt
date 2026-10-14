package de.tankzeit.app.forecast

import de.tankzeit.app.data.model.OilTrend
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ForecastEngineTest {

    @Test
    fun testComputeForecast_containsHourlyPatternAndMiddayPeak() {
        val forecast = ForecastEngine.computeForecast(
            currentAveragePrice = 1.750,
            oilTrend = OilTrend(
                trendPercent = -0.84,
                latestValue = 105.9,
                asOfDate = "2026-10-13",
                statusMessage = null,
                isStale = false
            )
        )

        assertNotNull(forecast)
        assertEquals(24, forecast.hourly.size)

        // Verify morning peak around 7 AM
        val point7am = forecast.hourly.first { it.hour == 7 }
        // Verify midday peak at 12 PM
        val point12pm = forecast.hourly.first { it.hour == 12 }
        val point11am = forecast.hourly.first { it.hour == 11 }
        
        // Midday price at 12 PM should be higher than 11 AM due to 12 Uhr Preisanpassung
        assertTrue("12 PM price should show price adaptation jump compared to 11 AM", point12pm.estimatedPrice > point11am.estimatedPrice)
        // Evening price around 10 PM (22 Uhr) should be among the lowest
        val point22pm = forecast.hourly.first { it.hour == 22 }
        assertTrue("22 PM price should be cheaper than morning peak", point22pm.estimatedPrice < point7am.estimatedPrice)
    }
}
