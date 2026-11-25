package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.data.model.ChartAnalysis
import com.example.data.model.ZoneType
import com.example.ui.theme.BullionGold
import com.example.ui.theme.CrimsonBear
import com.example.ui.theme.EmeraldBull
import com.example.ui.theme.MonoLabelSmall
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.SlateTextSecondary
import kotlin.math.max
import kotlin.math.min

@Composable
fun PriceActionCanvas(
    analysis: ChartAnalysis,
    plannerEntry: Double?,
    plannerStopLoss: Double?,
    plannerTakeProfit: Double?,
    modifier: Modifier = Modifier
) {
    val points = analysis.chartPatternPoints
    val allValues = buildList {
        points.forEach { add(it.toDouble()) }
        analysis.keyLevels.forEach {
            add(it.lowPrice)
            add(it.highPrice)
        }
        add(analysis.currentPrice)
        plannerEntry?.takeIf { it > 0.0 }?.let { add(it) }
        plannerStopLoss?.takeIf { it > 0.0 }?.let { add(it) }
        plannerTakeProfit?.takeIf { it > 0.0 }?.let { add(it) }
    }

    val rawMin = allValues.minOrNull() ?: 3600.0
    val rawMax = allValues.maxOrNull() ?: 3690.0
    val pad = max((rawMax - rawMin) * 0.12, 0.0001)
    val minPrice = rawMin - pad
    val maxPrice = rawMax + pad
    val span = max(maxPrice - minPrice, 0.0001)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ObsidianSurface)
            .border(1.dp, ObsidianBorder, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "STRUCTURE & KEY ZONES",
                style = MaterialTheme.typography.labelMedium,
                color = SlateTextSecondary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LegendDot("Support", EmeraldBull)
                LegendDot("Resist", CrimsonBear)
                LegendDot("Price", BullionGold)
            }
        }

        Box(
            modifier = Modifier
                .padding(top = 10.dp)
                .fillMaxWidth()
                .height(210.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ObsidianSurfaceElevated)
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(vertical = 8.dp, horizontal = 4.dp)) {
                val w = size.width
                val h = size.height
                val chartW = w * 0.80f

                fun yForPrice(price: Double): Float {
                    val norm = ((price - minPrice) / span).toFloat().coerceIn(0f, 1f)
                    return h - (norm * h)
                }

                // Subtle grid lines
                val gridCount = 4
                for (i in 0..gridCount) {
                    val gy = h * (i.toFloat() / gridCount)
                    drawLine(
                        color = ObsidianBorder.copy(alpha = 0.5f),
                        start = Offset(0f, gy),
                        end = Offset(w, gy),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Shaded key zones
                analysis.keyLevels.forEach { zone ->
                    val yTop = yForPrice(zone.highPrice)
                    val yBottom = yForPrice(zone.lowPrice)
                    val zoneH = max(yBottom - yTop, 5.dp.toPx())
                    val zoneColor = when (zone.type) {
                        ZoneType.SUPPORT -> EmeraldBull
                        ZoneType.RESISTANCE -> CrimsonBear
                        ZoneType.IMBALANCE -> BullionGold
                        ZoneType.SESSION_EXTREME -> SlateTextSecondary
                    }
                    drawRect(
                        color = zoneColor.copy(alpha = 0.16f),
                        topLeft = Offset(0f, yTop),
                        size = Size(chartW, zoneH)
                    )
                    drawLine(
                        color = zoneColor.copy(alpha = 0.65f),
                        start = Offset(0f, (yTop + yBottom) / 2f),
                        end = Offset(chartW, (yTop + yBottom) / 2f),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Planner Entry / SL / TP dashed guides
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                plannerTakeProfit?.takeIf { it > 0.0 }?.let { tp ->
                    val y = yForPrice(tp)
                    drawLine(
                        color = EmeraldBull,
                        start = Offset(0f, y),
                        end = Offset(chartW, y),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = dashEffect
                    )
                }
                plannerStopLoss?.takeIf { it > 0.0 }?.let { sl ->
                    val y = yForPrice(sl)
                    drawLine(
                        color = CrimsonBear,
                        start = Offset(0f, y),
                        end = Offset(chartW, y),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = dashEffect
                    )
                }
                plannerEntry?.takeIf { it > 0.0 }?.let { entry ->
                    val y = yForPrice(entry)
                    drawLine(
                        color = Color.White.copy(alpha = 0.85f),
                        start = Offset(0f, y),
                        end = Offset(chartW, y),
                        strokeWidth = 1.4.dp.toPx(),
                        pathEffect = dashEffect
                    )
                }

                // Stylized price action line + gradient fill
                if (points.size >= 2) {
                    val linePath = Path()
                    val fillPath = Path()
                    val stepX = chartW / (points.size - 1)

                    points.forEachIndexed { idx, value ->
                        val x = idx * stepX
                        val y = yForPrice(value.toDouble())
                        if (idx == 0) {
                            linePath.moveTo(x, y)
                            fillPath.moveTo(x, h)
                            fillPath.lineTo(x, y)
                        } else {
                            linePath.lineTo(x, y)
                            fillPath.lineTo(x, y)
                        }
                    }
                    fillPath.lineTo(chartW, h)
                    fillPath.close()

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                BullionGold.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    )
                    drawPath(
                        path = linePath,
                        color = BullionGold,
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Current price pulse dot
                    val lastX = chartW
                    val lastY = yForPrice(analysis.currentPrice)
                    drawCircle(
                        color = BullionGold.copy(alpha = 0.28f),
                        radius = 8.dp.toPx(),
                        center = Offset(lastX, lastY)
                    )
                    drawCircle(
                        color = BullionGold,
                        radius = 4.dp.toPx(),
                        center = Offset(lastX, lastY)
                    )
                    drawLine(
                        color = BullionGold.copy(alpha = 0.55f),
                        start = Offset(0f, lastY),
                        end = Offset(w, lastY),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                }
            }

            // Right-side price scale labels
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp, top = 6.dp, bottom = 6.dp)
                    .height(196.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = String.format("%.${analysis.priceDecimals}f", maxPrice),
                    style = MonoLabelSmall,
                    color = SlateTextSecondary
                )
                Text(
                    text = String.format("%.${analysis.priceDecimals}f", analysis.currentPrice),
                    style = MonoLabelSmall,
                    color = BullionGold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(BullionGold.copy(alpha = 0.16f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                )
                Text(
                    text = String.format("%.${analysis.priceDecimals}f", minPrice),
                    style = MonoLabelSmall,
                    color = SlateTextSecondary
                )
            }
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(color)
                .padding(4.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = SlateTextSecondary
        )
    }
}
