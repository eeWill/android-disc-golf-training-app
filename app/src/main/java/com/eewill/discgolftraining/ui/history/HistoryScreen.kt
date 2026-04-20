package com.eewill.discgolftraining.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.eewill.discgolftraining.data.ApproachRoundSummary
import com.eewill.discgolftraining.data.PuttingRoundSummary
import com.eewill.discgolftraining.data.RoundSummary
import com.eewill.discgolftraining.ui.approachRoundRepository
import com.eewill.discgolftraining.ui.puttingRoundRepository
import com.eewill.discgolftraining.ui.repository
import com.eewill.discgolftraining.ui.simpleFactory
import java.text.DateFormat
import java.util.Date

private data class PendingDelete(val id: String, val mode: HistoryMode)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onOpenGapRound: (String) -> Unit,
    onOpenApproachRound: (String) -> Unit,
    onOpenPuttingRound: (String) -> Unit,
) {
    val context = LocalContext.current
    val viewModel: HistoryViewModel = viewModel(
        factory = simpleFactory {
            HistoryViewModel(
                context.repository(),
                context.approachRoundRepository(),
                context.puttingRoundRepository(),
            )
        },
    )
    val mode by viewModel.mode.collectAsState()
    val gapRounds by viewModel.gapRounds.collectAsState()
    val approachRounds by viewModel.approachRounds.collectAsState()
    val puttingRounds by viewModel.puttingRounds.collectAsState()
    var pendingDelete by remember { mutableStateOf<PendingDelete?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { inner ->
        Column(
            modifier = Modifier.fillMaxSize().padding(inner),
        ) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                SegmentedButton(
                    selected = mode == HistoryMode.GAP,
                    onClick = { viewModel.setMode(HistoryMode.GAP) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                ) { Text("Gap") }
                SegmentedButton(
                    selected = mode == HistoryMode.APPROACH,
                    onClick = { viewModel.setMode(HistoryMode.APPROACH) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                ) { Text("Approach") }
                SegmentedButton(
                    selected = mode == HistoryMode.PUTTING,
                    onClick = { viewModel.setMode(HistoryMode.PUTTING) },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                ) { Text("Putting") }
            }

            when (mode) {
                HistoryMode.GAP -> GapList(
                    rounds = gapRounds,
                    onOpen = onOpenGapRound,
                    onRequestDelete = { id -> pendingDelete = PendingDelete(id, HistoryMode.GAP) },
                )
                HistoryMode.APPROACH -> ApproachList(
                    rounds = approachRounds,
                    onOpen = onOpenApproachRound,
                    onRequestDelete = { id -> pendingDelete = PendingDelete(id, HistoryMode.APPROACH) },
                )
                HistoryMode.PUTTING -> PuttingList(
                    rounds = puttingRounds,
                    onOpen = onOpenPuttingRound,
                    onRequestDelete = { id -> pendingDelete = PendingDelete(id, HistoryMode.PUTTING) },
                )
            }
        }
    }

    pendingDelete?.let { pd ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete round?") },
            text = { Text("This permanently removes the round and all its throws.") },
            confirmButton = {
                TextButton(onClick = {
                    when (pd.mode) {
                        HistoryMode.GAP -> viewModel.deleteGapRound(pd.id)
                        HistoryMode.APPROACH -> viewModel.deleteApproachRound(pd.id)
                        HistoryMode.PUTTING -> viewModel.deletePuttingRound(pd.id)
                    }
                    pendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun GapList(
    rounds: List<RoundSummary>,
    onOpen: (String) -> Unit,
    onRequestDelete: (String) -> Unit,
) {
    if (rounds.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text("No gap rounds yet.")
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(rounds, key = { it.id }) { r ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpen(r.id) },
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AsyncImage(
                        model = r.imagePath,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        contentScale = ContentScale.Crop,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        val total = r.hits + r.misses
                        val pct = if (total > 0) r.hits * 100f / total else 0f
                        Text(
                            text = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                                .format(Date(r.createdAt)),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            text = "${r.hits} hit / ${r.misses} miss  (${"%.0f".format(pct)}%)",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        if (!r.notes.isNullOrBlank()) {
                            Text(
                                text = r.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    IconButton(onClick = { onRequestDelete(r.id) }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete round",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PuttingList(
    rounds: List<PuttingRoundSummary>,
    onOpen: (String) -> Unit,
    onRequestDelete: (String) -> Unit,
) {
    if (rounds.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text("No putting rounds yet.")
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(rounds, key = { it.id }) { r ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpen(r.id) },
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                                .format(Date(r.createdAt)),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        val pct = if (r.attemptedCount > 0) r.madeCount * 100f / r.attemptedCount else 0f
                        val made = "${r.madeCount}/${r.attemptedCount} made (${"%.0f".format(pct)}%)"
                        Text(
                            text = "${"%.0f".format(r.minDistanceFeet)}–${"%.0f".format(r.maxDistanceFeet)} ft · step ${"%.0f".format(r.intervalFeet)} · ${r.throwsPerPosition}/pos · $made",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        if (!r.notes.isNullOrBlank()) {
                            Text(
                                text = r.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    IconButton(onClick = { onRequestDelete(r.id) }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete round",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ApproachList(
    rounds: List<ApproachRoundSummary>,
    onOpen: (String) -> Unit,
    onRequestDelete: (String) -> Unit,
) {
    if (rounds.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text("No approach rounds yet.")
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(rounds, key = { it.id }) { r ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpen(r.id) },
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                                .format(Date(r.createdAt)),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        val avgTxt = r.avgDistanceFeet?.let { "avg ${"%.1f".format(it)} ft" } ?: "no throws"
                        Text(
                            text = "Target ${"%.0f".format(r.targetDistanceFeet)} ft  ·  ${r.throwCount} discs  ·  $avgTxt",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        if (!r.notes.isNullOrBlank()) {
                            Text(
                                text = r.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    IconButton(onClick = { onRequestDelete(r.id) }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete round",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}
