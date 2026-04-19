package com.eewill.discgolftraining.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eewill.discgolftraining.data.DiscType
import com.eewill.discgolftraining.ui.approachRoundRepository
import com.eewill.discgolftraining.ui.simpleFactory
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import java.text.DateFormat
import java.util.Date

private val TapInColor = Color(0xFF66BB6A)
private val C1XColor = Color(0xFFFFA726)
private val C2Color = Color(0xFFEF5350)
private val AvgRawColor = Color(0xFF90A4AE)
private val AvgSmoothColor = Color(0xFF42A5F5)

private const val SMOOTH_WINDOW = 5

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ApproachStatsContent() {
    val context = LocalContext.current
    val viewModel: ApproachStatsViewModel = viewModel(
        factory = simpleFactory { ApproachStatsViewModel(context.approachRoundRepository()) },
    )
    val filters by viewModel.filters.collectAsState()
    val points by viewModel.points.collectAsState()

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    val dateFmt = remember { DateFormat.getDateInstance(DateFormat.SHORT) }

    val rangeValid = filters.startDateMillis == null ||
        filters.endDateMillis == null ||
        filters.startDateMillis!! <= filters.endDateMillis!!
    val canApply = rangeValid && filters.includedDiscTypes.isNotEmpty()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Date range", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = filters.startDateMillis?.let { dateFmt.format(Date(it)) } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Start") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(onClick = { showStartPicker = true }),
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = filters.endDateMillis?.let { dateFmt.format(Date(it)) } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("End") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(onClick = { showEndPicker = true }),
                )
            }
        }

        Text("Disc types", style = MaterialTheme.typography.titleSmall)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            DiscType.entries.forEach { type ->
                FilterChip(
                    selected = type in filters.includedDiscTypes,
                    onClick = { viewModel.toggleDiscType(type) },
                    label = { Text(type.displayName()) },
                )
            }
        }

        if (!rangeValid) {
            Text(
                "Start date must be on or before end date.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        if (filters.includedDiscTypes.isEmpty()) {
            Text(
                "Select at least one disc type.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(Modifier.height(4.dp))
        Button(
            onClick = { viewModel.apply() },
            enabled = canApply,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Show stats") }

        if (filters.applied) {
            Spacer(Modifier.height(8.dp))
            if (points.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("No rounds match these filters.")
                }
            } else {
                Text("Avg distance from target by round", style = MaterialTheme.typography.titleSmall)
                AvgDistanceLegend()
                AvgDistanceChart(points = points)
                val overallAvg = points.map { it.avgDistance }.average()
                Text(
                    "overall avg ${"%.1f".format(overallAvg)} ft",
                    style = MaterialTheme.typography.bodyMedium,
                )

                Spacer(Modifier.height(16.dp))
                Text("Zone % by round", style = MaterialTheme.typography.titleSmall)
                ZoneLegend()
                ApproachZoneChart(points = points)
                val avgTap = points.map { it.tapInPct }.average()
                val avgC1x = points.map { it.c1xPct }.average()
                val avgC2 = points.map { it.c2Pct }.average()
                Text(
                    "${points.size} rounds · tap-in ${"%.1f".format(avgTap)}% · C1X ${"%.1f".format(avgC1x)}% · C2 ${"%.1f".format(avgC2)}%",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }

    if (showStartPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = filters.startDateMillis)
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateDateRange(state.selectedDateMillis, filters.endDateMillis)
                    showStartPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = state)
        }
    }

    if (showEndPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = filters.endDateMillis)
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateDateRange(filters.startDateMillis, state.selectedDateMillis)
                    showEndPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = state)
        }
    }
}

@Composable
private fun AvgDistanceLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendSwatch(color = AvgRawColor, label = "Raw")
        LegendSwatch(color = AvgSmoothColor, label = "Smoothed ($SMOOTH_WINDOW-round)")
    }
}

@Composable
private fun AvgDistanceChart(points: List<ApproachZonePoint>) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(points) {
        if (points.isNotEmpty()) {
            val xs = points.indices.map { (it + 1).toDouble() }
            val raw = points.map { it.avgDistance.toDouble() }
            val smoothed = raw.movingAverage(SMOOTH_WINDOW)
            modelProducer.runTransaction {
                lineSeries {
                    series(x = xs, y = raw)
                    series(x = xs, y = smoothed)
                }
            }
        }
    }

    val xFormatter = remember {
        CartesianValueFormatter { _, value, _ -> value.toLong().toString() }
    }

    val rawLine = LineCartesianLayer.rememberLine(fill = LineCartesianLayer.LineFill.single(fill(AvgRawColor)))
    val smoothLine = LineCartesianLayer.rememberLine(fill = LineCartesianLayer.LineFill.single(fill(AvgSmoothColor)))

    Card(modifier = Modifier.fillMaxWidth()) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(rawLine, smoothLine),
                ),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = xFormatter),
            ),
            modelProducer = modelProducer,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(12.dp),
        )
    }
}

private fun List<Double>.movingAverage(window: Int): List<Double> {
    if (isEmpty() || window <= 1) return this
    return indices.map { i ->
        val start = (i - window + 1).coerceAtLeast(0)
        val slice = subList(start, i + 1)
        slice.average()
    }
}

@Composable
private fun ZoneLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendSwatch(color = TapInColor, label = "Tap-in")
        LegendSwatch(color = C1XColor, label = "C1X")
        LegendSwatch(color = C2Color, label = "C2")
    }
}

@Composable
private fun LegendSwatch(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color),
        )
        Spacer(Modifier.size(4.dp))
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ApproachZoneChart(points: List<ApproachZonePoint>) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(points) {
        if (points.isNotEmpty()) {
            val xs = points.indices.map { (it + 1).toDouble() }
            modelProducer.runTransaction {
                lineSeries {
                    series(x = xs, y = points.map { it.tapInPct.toDouble() })
                    series(x = xs, y = points.map { it.c1xPct.toDouble() })
                    series(x = xs, y = points.map { it.c2Pct.toDouble() })
                }
            }
        }
    }

    val xFormatter = remember {
        CartesianValueFormatter { _, value, _ -> value.toLong().toString() }
    }

    val tapLine = LineCartesianLayer.rememberLine(fill = LineCartesianLayer.LineFill.single(fill(TapInColor)))
    val c1xLine = LineCartesianLayer.rememberLine(fill = LineCartesianLayer.LineFill.single(fill(C1XColor)))
    val c2Line = LineCartesianLayer.rememberLine(fill = LineCartesianLayer.LineFill.single(fill(C2Color)))

    Card(modifier = Modifier.fillMaxWidth()) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(tapLine, c1xLine, c2Line),
                ),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = xFormatter),
            ),
            modelProducer = modelProducer,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(12.dp),
        )
    }
}
