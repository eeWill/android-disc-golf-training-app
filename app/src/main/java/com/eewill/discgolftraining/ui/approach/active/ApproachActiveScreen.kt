package com.eewill.discgolftraining.ui.approach.active

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eewill.discgolftraining.ui.approachRoundRepository
import com.eewill.discgolftraining.ui.simpleFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApproachActiveScreen(
    roundId: String,
    onEndRound: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: ApproachActiveViewModel = viewModel(
        key = "approach-active-$roundId",
        factory = simpleFactory {
            ApproachActiveViewModel(roundId, context.approachRoundRepository())
        },
    )
    val round by viewModel.round.collectAsState()
    val discs by viewModel.discs.collectAsState()
    val throws by viewModel.throws.collectAsState()

    val persistedByDisc = throws.associateBy { it.discId }
    val localText = remember { mutableStateMapOf<String, String>() }

    val targetLabel = round?.targetDistanceFeet?.let { "Target ${"%.0f".format(it)} ft" } ?: "Approach"
    val recordedCount = throws.size

    Scaffold(
        topBar = { TopAppBar(title = { Text(targetLabel) }) },
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "$recordedCount of ${discs.size} discs recorded",
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = "Walk to each landing spot, then enter the distance from the target. Discs can be entered in any order.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(discs, key = { it.id }) { disc ->
                    val persisted = persistedByDisc[disc.id]?.landingDistanceFeet?.fmt().orEmpty()
                    val value = localText[disc.id] ?: persisted
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(disc.name, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    disc.type.displayName(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            OutlinedTextField(
                                value = value,
                                onValueChange = { raw ->
                                    val filtered = raw.filter { c -> c.isDigit() || c == '.' }
                                    localText[disc.id] = filtered
                                    if (filtered.isEmpty()) {
                                        viewModel.clearThrow(disc.id)
                                    } else {
                                        filtered.toFloatOrNull()?.let {
                                            viewModel.setThrow(disc.id, it)
                                        }
                                    }
                                },
                                label = { Text("ft") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.width(120.dp),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onEndRound,
                enabled = recordedCount > 0,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Done") }
        }
    }
}

private fun Float.fmt(): String =
    if (this % 1f == 0f) toInt().toString() else "%.1f".format(this)
