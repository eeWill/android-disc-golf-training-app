package com.eewill.discgolftraining.ui.active

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eewill.discgolftraining.data.DirectionCounts
import com.eewill.discgolftraining.data.DiscDataMode
import com.eewill.discgolftraining.data.DiscType
import com.eewill.discgolftraining.ui.components.DiscTypeFilter
import com.eewill.discgolftraining.ui.components.ImageWithOverlay
import com.eewill.discgolftraining.ui.components.ThrowMarker
import com.eewill.discgolftraining.ui.discRepository
import com.eewill.discgolftraining.ui.repository
import com.eewill.discgolftraining.ui.simpleFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveRoundScreen(
    roundId: String,
    onEndRound: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: ActiveRoundViewModel = viewModel(
        key = "active-$roundId",
        factory = simpleFactory {
            ActiveRoundViewModel(roundId, context.repository(), context.discRepository())
        },
    )

    val state by viewModel.state.collectAsState()
    val visibleDiscs by viewModel.visibleDiscs.collectAsState()
    val typeFilter by viewModel.typeFilter.collectAsState()
    val currentType by viewModel.currentType.collectAsState()
    val currentDiscId by viewModel.currentDiscId.collectAsState()

    LaunchedEffect(visibleDiscs) { viewModel.ensureDiscSelection(visibleDiscs) }

    Scaffold(
        topBar = {
            val hits = state?.throws?.count { it.isHit } ?: 0
            val misses = state?.throws?.count { !it.isHit } ?: 0
            TopAppBar(title = { Text("Hits $hits  |  Misses $misses") })
        },
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val round = state?.round
            if (round != null) {
                when (round.discDataMode) {
                    DiscDataMode.TYPE -> TypeSelector(
                        current = currentType,
                        onSelect = viewModel::selectType,
                    )
                    DiscDataMode.DISC -> Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DiscSelector(
                            discs = visibleDiscs,
                            currentId = currentDiscId,
                            onSelect = viewModel::selectDisc,
                        )
                        DiscTypeFilter(
                            selected = typeFilter,
                            onChange = viewModel::setTypeFilter,
                        )
                    }
                    DiscDataMode.NONE -> Unit
                }

                val throws = state?.throws.orEmpty()
                ImageWithOverlay(
                    imagePath = round.imagePath,
                    modifier = Modifier.fillMaxWidth(),
                    gapRect = round.gapRect(),
                    markers = throws.map { ThrowMarker(it.x, it.y, it.isHit) },
                    onTap = { viewModel.recordThrow(it) },
                )
                val dirs = DirectionCounts.from(round, throws)
                Text("Misses — Left ${dirs.left}  ·  Right ${dirs.right}  ·  High ${dirs.high}  ·  Low ${dirs.low}")
                Text("Tap where the disc ended up. Distance ${round.distanceFeet.fmt()} ft · Gap ${round.gapWidthFeet.fmt()} ft")
            } else {
                Text("Loading round…")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = { viewModel.undoLast() },
                    modifier = Modifier.weight(1f),
                    enabled = (state?.throws?.isNotEmpty() == true),
                ) { Text("Undo Last Throw") }
                Button(
                    onClick = onEndRound,
                    modifier = Modifier.weight(1f),
                ) { Text("End Round") }
            }
        }
    }
}

@Composable
private fun TypeSelector(
    current: DiscType,
    onSelect: (DiscType) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        AssistChip(
            onClick = { expanded = true },
            label = { Text("Disc: ${current.displayName()} ▼") },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DiscType.entries.forEach { t ->
                DropdownMenuItem(
                    text = { Text(t.displayName()) },
                    onClick = {
                        onSelect(t)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun DiscSelector(
    discs: List<com.eewill.discgolftraining.data.DiscEntity>,
    currentId: String?,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val currentName = discs.firstOrNull { it.id == currentId }?.name ?: "(select)"
    Box(contentAlignment = Alignment.CenterStart) {
        AssistChip(
            onClick = { if (discs.isNotEmpty()) expanded = true },
            label = { Text("Disc: $currentName ▼") },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            discs.forEach { d ->
                DropdownMenuItem(
                    text = { Text("${d.name} · ${d.type.displayName()}") },
                    onClick = {
                        onSelect(d.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

private fun Float.fmt(): String =
    if (this % 1f == 0f) toInt().toString() else "%.1f".format(this)
