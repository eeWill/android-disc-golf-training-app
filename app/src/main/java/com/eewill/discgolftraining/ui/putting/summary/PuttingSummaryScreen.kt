package com.eewill.discgolftraining.ui.putting.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuttingSummaryScreen(
    roundId: String,
    onHome: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: PuttingSummaryViewModel = viewModel(
        key = "putting-summary-$roundId",
        factory = simpleFactory {
            PuttingSummaryViewModel(roundId, context.puttingRoundRepository())
        },
    )
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Putting Summary") }) },
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            state.round?.let { r ->
                Text(
                    text = "${"%.0f".format(r.minDistanceFeet)}–${"%.0f".format(r.maxDistanceFeet)} ft, step ${"%.0f".format(r.intervalFeet)} ft",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "${r.throwsPerPosition} throws per position",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            val chartRows = state.perDistance.filter { it.attempted > 0 }
            if (chartRows.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text("Made % by distance", style = MaterialTheme.typography.titleSmall)
                MadePercentChart(rows = chartRows)
            }

            if (state.perDistance.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        state.perDistance.forEach { row ->
                            StatRow(
                                label = "${"%.0f".format(row.distanceFeet)} ft",
                                made = row.made,
                                attempted = row.attempted,
                                bold = false,
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        StatRow(
                            label = "Overall",
                            made = state.overall.made,
                            attempted = state.overall.attempted,
                            bold = true,
                        )
                    }
                }
            }

            state.round?.let { r ->
                Spacer(Modifier.height(4.dp))
                var notesText by rememberSaveable(r.id) {
                    mutableStateOf(r.notes.orEmpty())
                }
                LaunchedEffect(r.id) { notesText = r.notes.orEmpty() }
                Text("Notes", style = MaterialTheme.typography.titleSmall)
                OutlinedTextField(
                    value = notesText,
                    onValueChange = {
                        notesText = it
                        viewModel.updateNotes(it.ifBlank { null })
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    minLines = 4,
                    placeholder = { Text("Add notes about this round…") },
                )
            }

            Spacer(Modifier.height(8.dp))
            Button(onClick = onHome, modifier = Modifier.fillMaxWidth()) { Text("Home") }
        }
    }
}

@Composable
private fun MadePercentChart(rows: List<PuttingDistanceStat>) {
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
                .height(220.dp)
                .padding(12.dp),
        )
    }
}

@Composable
private fun StatRow(label: String, made: Int, attempted: Int, bold: Boolean) {
    val pct = if (attempted > 0) made * 100f / attempted else 0f
    val style = if (bold) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = style, modifier = Modifier.weight(1f))
        Text(
            text = "$made/$attempted (${"%.0f".format(pct)}%)",
            style = style,
        )
    }
}
