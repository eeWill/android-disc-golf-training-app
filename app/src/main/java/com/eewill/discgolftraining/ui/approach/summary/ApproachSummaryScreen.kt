package com.eewill.discgolftraining.ui.approach.summary

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eewill.discgolftraining.ui.approachRoundRepository
import com.eewill.discgolftraining.ui.simpleFactory

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

            Spacer(Modifier.height(4.dp))
            Text("Zones", style = MaterialTheme.typography.titleSmall)
            LandingZone.entries.forEach { zone ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(zone.label, style = MaterialTheme.typography.titleSmall)
                            Text(
                                zone.range,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = "${state.zoneCounts[zone] ?: 0}",
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                }
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
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "${"%.1f".format(landing.distanceFeet)} ft",
                                    style = MaterialTheme.typography.titleSmall,
                                )
                                Text(
                                    landing.zone.label,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Button(onClick = onHome, modifier = Modifier.fillMaxWidth()) { Text("Home") }
        }
    }
}
