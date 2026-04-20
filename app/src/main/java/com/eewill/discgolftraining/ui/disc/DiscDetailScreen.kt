package com.eewill.discgolftraining.ui.disc

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eewill.discgolftraining.ui.approachRoundRepository
import com.eewill.discgolftraining.ui.discRepository
import com.eewill.discgolftraining.ui.repository
import com.eewill.discgolftraining.ui.simpleFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscDetailScreen(
    discId: String,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: DiscDetailViewModel = viewModel(
        key = "disc-detail-$discId",
        factory = simpleFactory {
            DiscDetailViewModel(
                discId = discId,
                discRepo = context.discRepository(),
                roundRepo = context.repository(),
                approachRepo = context.approachRoundRepository(),
            )
        },
    )
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Disc Details") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val disc = state.disc
            if (disc == null) {
                Text("Disc not found")
                return@Column
            }

            var editedName by rememberSaveable(disc.id) { mutableStateOf(disc.name) }
            LaunchedEffect(disc.id) { editedName = disc.name }

            Text(disc.type.displayName(), style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(
                value = editedName,
                onValueChange = {
                    editedName = it
                    viewModel.onNameChange()
                },
                label = { Text("Name") },
                singleLine = true,
                isError = state.nameError != null,
                supportingText = state.nameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = { viewModel.saveName(editedName) },
                enabled = !state.isSaving
                    && editedName.trim().isNotEmpty()
                    && editedName.trim() != disc.name,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (state.isSaving) "Saving…" else "Save")
            }

            GapStatsCard(state)
            ApproachStatsCard(state)
        }
    }
}

@Composable
private fun GapStatsCard(state: DiscDetailUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Gap Throws", style = MaterialTheme.typography.titleMedium)
            Text("Throws: ${state.gapThrowCount}")
            if (state.gapThrowCount == 0) {
                Text("No gap throws recorded yet.", style = MaterialTheme.typography.bodySmall)
            } else {
                val pct = state.gapHitPct
                Text("Hit %: ${if (pct != null) "%.0f".format(pct) else "—"}%")
                Text("Miss direction", style = MaterialTheme.typography.titleSmall)
                val dir = state.missDir
                if (dir == null) {
                    Text("No misses to break down.", style = MaterialTheme.typography.bodySmall)
                } else {
                    StatRow("Left", "%.0f%%".format(dir.left))
                    StatRow("Right", "%.0f%%".format(dir.right))
                    StatRow("High", "%.0f%%".format(dir.high))
                    StatRow("Low", "%.0f%%".format(dir.low))
                }
            }
        }
    }
}

@Composable
private fun ApproachStatsCard(state: DiscDetailUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Approach Throws", style = MaterialTheme.typography.titleMedium)
            Text("Throws: ${state.approachThrowCount}")
            if (state.approachThrowCount == 0) {
                Text("No approach throws recorded yet.", style = MaterialTheme.typography.bodySmall)
            } else {
                val avg = state.approachAvgDistFt
                Text("Avg distance from target: ${if (avg != null) "%.1f".format(avg) else "—"} ft")
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.weight(1f))
        Text(value)
    }
}
