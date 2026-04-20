package com.eewill.discgolftraining.ui.stats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eewill.discgolftraining.ui.repository
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

private enum class StatsTab { GAP, APPROACH, PUTTING, PER_DISC }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StatsScreen(
    onBack: () -> Unit,
    onOpenDisc: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val viewModel: StatsViewModel = viewModel(
        factory = simpleFactory { StatsViewModel(context.repository()) },
    )
    val filters by viewModel.filters.collectAsState()
    val points by viewModel.filteredPoints.collectAsState()
    val missPoints by viewModel.missDirectionPoints.collectAsState()

    var minDistText by remember { mutableStateOf("") }
    var maxDistText by remember { mutableStateOf("") }
    var minGapText by remember { mutableStateOf("") }
    var maxGapText by remember { mutableStateOf("") }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var tab by remember { mutableStateOf(StatsTab.GAP) }

    val dateFmt = remember { DateFormat.getDateInstance(DateFormat.SHORT) }

    val rangeValid = filters.startDateMillis == null ||
        filters.endDateMillis == null ||
        filters.startDateMillis!! <= filters.endDateMillis!!

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                FilterChip(
                    selected = tab == StatsTab.GAP,
                    onClick = { tab = StatsTab.GAP },
                    label = { Text("Gap Throwing") },
                )
                FilterChip(
                    selected = tab == StatsTab.APPROACH,
                    onClick = { tab = StatsTab.APPROACH },
                    label = { Text("Approach Shot") },
                )
                FilterChip(
                    selected = tab == StatsTab.PUTTING,
                    onClick = { tab = StatsTab.PUTTING },
                    label = { Text("Putting") },
                )
                FilterChip(
                    selected = tab == StatsTab.PER_DISC,
                    onClick = { tab = StatsTab.PER_DISC },
                    label = { Text("Per Disc") },
                )
            }

            when (tab) {
                StatsTab.APPROACH -> {
                    ApproachStatsContent()
                    return@Column
                }
                StatsTab.PUTTING -> {
                    PuttingStatsContent()
                    return@Column
                }
                StatsTab.PER_DISC -> {
                    DiscListStatsContent(onOpenDisc = onOpenDisc)
                    return@Column
                }
                StatsTab.GAP -> Unit
            }

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
                    onValueChange = {
                        minDistText = it.filter { c -> c.isDigit() || c == '.' }
                        viewModel.updateDistanceRange(
                            min = minDistText.toFloatOrNull(),
                            max = maxDistText.toFloatOrNull(),
                        )
                    },
                    label = { Text("Min") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = maxDistText,
                    onValueChange = {
                        maxDistText = it.filter { c -> c.isDigit() || c == '.' }
                        viewModel.updateDistanceRange(
                            min = minDistText.toFloatOrNull(),
                            max = maxDistText.toFloatOrNull(),
                        )
                    },
                    label = { Text("Max") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }

            Text("Gap width (ft)", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = minGapText,
                    onValueChange = {
                        minGapText = it.filter { c -> c.isDigit() || c == '.' }
                        viewModel.updateGapWidthRange(
                            min = minGapText.toFloatOrNull(),
                            max = maxGapText.toFloatOrNull(),
                        )
                    },
                    label = { Text("Min") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = maxGapText,
                    onValueChange = {
                        maxGapText = it.filter { c -> c.isDigit() || c == '.' }
                        viewModel.updateGapWidthRange(
                            min = minGapText.toFloatOrNull(),
                            max = maxGapText.toFloatOrNull(),
                        )
                    },
                    label = { Text("Max") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
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

            Spacer(Modifier.height(4.dp))
            Button(
                onClick = { viewModel.apply() },
                enabled = rangeValid,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Show stats")
            }

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
                    Text("Hit % by round", style = MaterialTheme.typography.titleSmall)
                    HitPercentChart(points = points)
                    val avg = points.map { it.hitPercent }.average()
                    Text(
                        "${points.size} rounds · avg hit% ${"%.1f".format(avg)}%",
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    Spacer(Modifier.height(16.dp))
                    Text("Miss direction (% of misses)", style = MaterialTheme.typography.titleSmall)
                    if (missPoints.isEmpty()) {
                        Text(
                            "No misses in filtered rounds.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    } else {
                        MissDirectionLegend()
                        MissDirectionChart(points = missPoints, dateFormatter = dateFmt)
                    }
                }
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
private fun HitPercentChart(
    points: List<ChartPoint>,
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(points) {
        if (points.isNotEmpty()) {
            modelProducer.runTransaction {
                lineSeries {
                    series(
                        x = points.indices.map { (it + 1).toDouble() },
                        y = points.map { it.hitPercent.toDouble() },
                    )
                }
            }
        }
    }

    val xFormatter = remember {
        CartesianValueFormatter { _, value, _ -> value.toLong().toString() }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(),
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

private val MissLeftColor = Color(0xFFE57373)
private val MissRightColor = Color(0xFF64B5F6)
private val MissHighColor = Color(0xFF81C784)
private val MissLowColor = Color(0xFFFFB74D)

@Composable
private fun MissDirectionLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendSwatch(color = MissLeftColor, label = "Left")
        LegendSwatch(color = MissRightColor, label = "Right")
        LegendSwatch(color = MissHighColor, label = "High")
        LegendSwatch(color = MissLowColor, label = "Low")
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
private fun MissDirectionChart(
    points: List<MissDirectionPoint>,
    dateFormatter: DateFormat,
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(points) {
        if (points.isNotEmpty()) {
            val xs = points.map { it.createdAt.toDouble() }
            modelProducer.runTransaction {
                lineSeries {
                    series(x = xs, y = points.map { it.leftPct.toDouble() })
                    series(x = xs, y = points.map { it.rightPct.toDouble() })
                    series(x = xs, y = points.map { it.highPct.toDouble() })
                    series(x = xs, y = points.map { it.lowPct.toDouble() })
                }
            }
        }
    }

    val xFormatter = remember(dateFormatter) {
        CartesianValueFormatter { _, value, _ ->
            dateFormatter.format(Date(value.toLong()))
        }
    }

    val leftLine = LineCartesianLayer.rememberLine(fill = LineCartesianLayer.LineFill.single(fill(MissLeftColor)))
    val rightLine = LineCartesianLayer.rememberLine(fill = LineCartesianLayer.LineFill.single(fill(MissRightColor)))
    val highLine = LineCartesianLayer.rememberLine(fill = LineCartesianLayer.LineFill.single(fill(MissHighColor)))
    val lowLine = LineCartesianLayer.rememberLine(fill = LineCartesianLayer.LineFill.single(fill(MissLowColor)))

    Card(modifier = Modifier.fillMaxWidth()) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(
                        leftLine, rightLine, highLine, lowLine,
                    ),
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

