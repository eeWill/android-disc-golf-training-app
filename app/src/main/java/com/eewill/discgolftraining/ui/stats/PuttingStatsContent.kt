package com.eewill.discgolftraining.ui.stats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eewill.discgolftraining.ui.puttingRoundRepository
import com.eewill.discgolftraining.ui.simpleFactory
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuttingStatsContent() {
    val context = LocalContext.current
    val viewModel: PuttingStatsViewModel = viewModel(
        factory = simpleFactory { PuttingStatsViewModel(context.puttingRoundRepository()) },
    )
    val filters by viewModel.filters.collectAsState()
    val result by viewModel.result.collectAsState()

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    val dateFmt = remember { DateFormat.getDateInstance(DateFormat.SHORT) }

    val rangeValid = filters.startDateMillis == null ||
        filters.endDateMillis == null ||
        filters.startDateMillis!! <= filters.endDateMillis!!

    var minDistText by remember { mutableStateOf(filters.minDistanceFeet?.fmt().orEmpty()) }
    var maxDistText by remember { mutableStateOf(filters.maxDistanceFeet?.fmt().orEmpty()) }
    val minDist = minDistText.toFloatOrNull()
    val maxDist = maxDistText.toFloatOrNull()
    val minDistBlank = minDistText.isBlank()
    val maxDistBlank = maxDistText.isBlank()
    val minDistParsed = minDistBlank || minDist != null
    val maxDistParsed = maxDistBlank || maxDist != null
    val distanceRangeValid = minDist == null || maxDist == null || minDist <= maxDist

    LaunchedEffect(minDistText, maxDistText) {
        if (minDistParsed && maxDistParsed) {
            viewModel.updateDistanceRange(
                if (minDistBlank) null else minDist,
                if (maxDistBlank) null else maxDist,
            )
        }
    }

    val canApply = rangeValid && distanceRangeValid && minDistParsed && maxDistParsed

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

        Text("Distance (ft)", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = minDistText,
                onValueChange = { minDistText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Min") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = maxDistText,
                onValueChange = { maxDistText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Max") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
            )
        }

        if (!rangeValid) {
            Text(
                "Start date must be on or before end date.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        if (!distanceRangeValid) {
            Text(
                "Min distance must be ≤ max distance.",
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
            if (result.overallAttempted == 0) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("No throws match these filters.")
                }
            } else {
                Text("Made % by distance", style = MaterialTheme.typography.titleSmall)
                PuttingMadeChart(rows = result.perDistance)
                Text(
                    "${result.overallMade}/${result.overallAttempted} made (${"%.1f".format(result.overallPercent)}%)",
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
private fun PuttingMadeChart(rows: List<PuttingDistancePoint>) {
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(rows) {
        if (rows.isNotEmpty()) {
            modelProducer.runTransaction {
                columnSeries {
                    series(y = rows.map { it.percent.toDouble() })
                }
            }
        }
    }
    val xFormatter = remember(rows) {
        CartesianValueFormatter { _, value, _ ->
            val i = value.toInt()
            rows.getOrNull(i)?.let { "${"%.0f".format(it.distanceFeet)}ft" } ?: ""
        }
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(),
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

private fun Float.fmt(): String =
    if (this % 1f == 0f) toInt().toString() else "%.1f".format(this)
