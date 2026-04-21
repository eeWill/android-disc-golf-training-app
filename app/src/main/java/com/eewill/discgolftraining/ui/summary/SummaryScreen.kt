package com.eewill.discgolftraining.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eewill.discgolftraining.data.DiscDataMode
import com.eewill.discgolftraining.data.DiscEntity
import com.eewill.discgolftraining.ui.active.gapRect
import com.eewill.discgolftraining.ui.components.ImageWithOverlay
import com.eewill.discgolftraining.ui.components.ThrowMarker
import com.eewill.discgolftraining.ui.discRepository
import com.eewill.discgolftraining.ui.repository
import com.eewill.discgolftraining.ui.simpleFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    roundId: String,
    onHome: () -> Unit,
    onViewHistory: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: SummaryViewModel = viewModel(
        key = "summary-$roundId",
        factory = simpleFactory {
            SummaryViewModel(roundId, context.repository(), context.discRepository())
        },
    )
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Round Summary") }) },
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val round = state.roundWithThrows?.round
            val throws = state.roundWithThrows?.throws.orEmpty()
            val hits = throws.count { it.isHit }
            val misses = throws.count { !it.isHit }
            val total = throws.size
            val pct = if (total > 0) (hits * 100f / total) else 0f

            if (round != null) {
                ImageWithOverlay(
                    imagePath = round.imagePath,
                    modifier = Modifier.fillMaxWidth(),
                    gapRect = round.gapRect(),
                    markers = throws.map { ThrowMarker(it.x, it.y, it.isHit) },
                )
                Text("Hits: $hits / $total  (${"%.0f".format(pct)}%)", style = MaterialTheme.typography.titleMedium)
                Text("Misses: $misses", style = MaterialTheme.typography.bodyLarge)
                Text("Distance ${round.distanceFeet} ft  ·  Gap width ${round.gapWidthFeet} ft")
                round.minDistanceFeet?.let { Text("Minimum distance: $it ft") }
            } else {
                Text("Loading round…")
            }

            if (round != null
                && round.discDataMode == DiscDataMode.DISC
                && state.discsUsed.isNotEmpty()
            ) {
                DiscsUsedCard(
                    minDistanceFeet = round.minDistanceFeet,
                    discs = state.discsUsed,
                    shortDiscIds = state.shortDiscIds,
                    onToggleShort = viewModel::setDiscShort,
                )
            }

            if (round != null) {
                var notesText by rememberSaveable(round.id) {
                    mutableStateOf(round.notes.orEmpty())
                }
                LaunchedEffect(round.id) {
                    notesText = round.notes.orEmpty()
                }
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(onClick = onHome, modifier = Modifier.weight(1f)) { Text("Home") }
                Button(onClick = onViewHistory, modifier = Modifier.weight(1f)) { Text("View History") }
            }
        }
    }
}

@Composable
private fun DiscsUsedCard(
    minDistanceFeet: Float?,
    discs: List<DiscEntity>,
    shortDiscIds: Set<String>,
    onToggleShort: (String, Boolean) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Discs used", style = MaterialTheme.typography.titleSmall)
            if (minDistanceFeet != null) {
                Text(
                    "Check any that did not reach $minDistanceFeet ft",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            discs.forEach { disc ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (minDistanceFeet != null) {
                        Checkbox(
                            checked = disc.id in shortDiscIds,
                            onCheckedChange = { onToggleShort(disc.id, it) },
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(disc.name)
                        disc.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                            Text(
                                notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
