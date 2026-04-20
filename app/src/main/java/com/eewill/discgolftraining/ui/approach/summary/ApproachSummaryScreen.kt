package com.eewill.discgolftraining.ui.approach.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eewill.discgolftraining.ui.approach.map.ApproachMap
import com.eewill.discgolftraining.ui.approachRoundRepository
import com.eewill.discgolftraining.ui.simpleFactory
import com.google.android.gms.maps.model.LatLng
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
fun ApproachSummaryScreen(
    roundId: String,
    onHome: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: ApproachSummaryViewModel = viewModel(
        key = "approach-summary-$roundId",
        factory = simpleFactory {
            ApproachSummaryViewModel(roundId, context.approachRoundRepository())
        },
    )
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Approach Summary") }) },
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            state.round?.let {
                Text(
                    "Target: ${"%.0f".format(it.targetDistanceFeet)} ft",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Text(
                "Discs thrown: ${state.totalThrows}",
                style = MaterialTheme.typography.bodyMedium,
            )

            val roundTargetLatLng: LatLng? = state.round?.let { r ->
                val lat = r.targetLat
                val lng = r.targetLng
                if (lat != null && lng != null) LatLng(lat, lng) else null
            }
            val roundStartLatLng: LatLng? = state.round?.let { r ->
                val lat = r.startLat
                val lng = r.startLng
                if (lat != null && lng != null) LatLng(lat, lng) else null
            }
            val landingMarkers = state.landings.mapNotNull { it.latLng }
            if (roundTargetLatLng != null || landingMarkers.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                ) {
                    ApproachMap(
                        modifier = Modifier.fillMaxSize(),
                        targetLatLng = roundTargetLatLng,
                        startLatLng = roundStartLatLng,
                        targetCircleRadiusMeters = ((state.round?.targetSizeFeet ?: 0f) * 0.3048f).toDouble(),
                        throwMarkers = landingMarkers,
                        interactive = false,
                    )
                }
            }

            if (state.histogram.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text("Landing distances", style = MaterialTheme.typography.titleSmall)
                DistanceHistogram(bins = state.histogram)
            }

            if (state.landings.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text("By disc", style = MaterialTheme.typography.titleSmall)
                state.landings.forEach { landing ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(landing.disc.name, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    landing.disc.type.displayName(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                "${"%.1f".format(landing.distanceFeet)} ft",
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
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
private fun DistanceHistogram(bins: List<DistanceBin>) {
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(bins) {
        if (bins.isNotEmpty()) {
            modelProducer.runTransaction {
                columnSeries {
                    series(y = bins.map { it.count.toDouble() })
                }
            }
        }
    }
    val xFormatter = remember(bins) {
        CartesianValueFormatter { _, value, _ ->
            val i = value.toInt()
            bins.getOrNull(i)?.label ?: ""
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
