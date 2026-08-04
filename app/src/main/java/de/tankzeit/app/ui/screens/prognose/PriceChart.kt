package de.tankzeit.app.ui.screens.prognose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.tankzeit.app.data.model.HourlyForecastPoint
import de.tankzeit.app.data.model.WeeklyForecastPoint

/**
 * Zeichnet die stündliche Preiskurve als Liniendiagramm mit Markierung der
 * günstigsten Stunde.
 */
@Composable
fun HourlyPriceChart(points: List<HourlyForecastPoint>, cheapestHour: Int, modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val accent = MaterialTheme.colorScheme.secondary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier.fillMaxWidth().height(220.dp)) {
        if (points.isEmpty()) return@Canvas

        val leftPadding = 8.dp.toPx()
        val bottomPadding = 24.dp.toPx()
        val topPadding = 16.dp.toPx()
        val chartWidth = size.width - leftPadding * 2
        val chartHeight = size.height - bottomPadding - topPadding

        val minFactor = points.minOf { it.relativePriceFactor }
        val maxFactor = points.maxOf { it.relativePriceFactor }
        val range = (maxFactor - minFactor).takeIf { it > 0.0001 } ?: 0.01

        fun xFor(index: Int) = leftPadding + chartWidth * (index.toFloat() / (points.size - 1))
        fun yFor(factor: Double) = topPadding + chartHeight * (1f - ((factor - minFactor) / range).toFloat())

        // Gitterlinien
        for (i in 0..3) {
            val y = topPadding + chartHeight * (i / 3f)
            drawLine(
                color = gridColor,
                start = Offset(leftPadding, y),
                end = Offset(size.width - leftPadding, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Kurve
        val path = androidx.compose.ui.graphics.Path()
        points.forEachIndexed { index, point ->
            val x = xFor(index)
            val y = yFor(point.relativePriceFactor)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path = path, color = primary, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))

        // Markierung günstigste Stunde
        val cheapestIndex = points.indexOfFirst { it.hour == cheapestHour }.coerceAtLeast(0)
        val cx = xFor(cheapestIndex)
        val cy = yFor(points[cheapestIndex].relativePriceFactor)
        drawCircle(color = accent, radius = 6.dp.toPx(), center = Offset(cx, cy))

        // X-Achsen-Beschriftung (alle 4 Stunden)
        points.forEachIndexed { index, point ->
            if (point.hour % 4 == 0) {
                val label = "${point.hour}h"
                val textLayout = textMeasurer.measure(
                    text = label,
                    style = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, color = Color.Gray)
                )
                drawText(
                    textLayoutResult = textLayout,
                    topLeft = Offset(xFor(index) - textLayout.size.width / 2, size.height - bottomPadding + 4.dp.toPx())
                )
            }
        }
    }
}

/**
 * Zeichnet das relative Wochenmuster als Balkendiagramm.
 */
@Composable
fun WeeklyPatternChart(points: List<WeeklyForecastPoint>, modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val accent = MaterialTheme.colorScheme.secondary
    val textMeasurer = rememberTextMeasurer()
    val cheapestDay = points.minByOrNull { it.relativePriceFactor }?.dayOfWeek

    Canvas(modifier = modifier.fillMaxWidth().height(180.dp)) {
        if (points.isEmpty()) return@Canvas

        val bottomPadding = 24.dp.toPx()
        val topPadding = 8.dp.toPx()
        val chartHeight = size.height - bottomPadding - topPadding
        val barSlot = size.width / points.size
        val barWidth = barSlot * 0.55f

        val minFactor = points.minOf { it.relativePriceFactor }
        val maxFactor = points.maxOf { it.relativePriceFactor }
        val range = (maxFactor - minFactor).takeIf { it > 0.0001 } ?: 0.01

        points.forEachIndexed { index, point ->
            val normalizedHeight = ((point.relativePriceFactor - minFactor) / range).toFloat()
            val barHeight = (chartHeight * (0.25f + 0.75f * normalizedHeight))
            val left = index * barSlot + (barSlot - barWidth) / 2
            val top = topPadding + (chartHeight - barHeight)

            drawRoundRect(
                color = if (point.dayOfWeek == cheapestDay) accent else primary,
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )

            val textLayout = textMeasurer.measure(
                text = point.dayLabel,
                style = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = Color.Gray)
            )
            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(left + barWidth / 2 - textLayout.size.width / 2, size.height - bottomPadding + 4.dp.toPx())
            )
        }
    }
}
