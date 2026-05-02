package com.eewill.discgolftraining.ui.putting.active

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomAppBar
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eewill.discgolftraining.data.PuttingResult
import com.eewill.discgolftraining.ui.components.ExitRoundGuard
import com.eewill.discgolftraining.ui.puttingRoundRepository
import com.eewill.discgolftraining.ui.simpleFactory

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PuttingActiveScreen(
    roundId: String,
    onEndRound: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: PuttingActiveViewModel = viewModel(
        key = "putting-active-$roundId",
        factory = simpleFactory {
            PuttingActiveViewModel(roundId, context.puttingRoundRepository())
        },
    )
    val state by viewModel.state.collectAsState()
    val round = state.round
    val throwsPerPosition = round?.throwsPerPosition ?: 0
    val positions = state.positions

    ExitRoundGuard(onConfirmExit = onEndRound) { requestExit ->
    Scaffold(
        topBar = { TopAppBar(title = { Text("Putting") }) },
        bottomBar = {
            BottomAppBar {
                Button(
                    onClick = requestExit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) { Text("End round") }
            }
        },
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(count = positions.size, key = { it }) { positionIndex ->
                val distance = positions[positionIndex]
                val rowResults = (0 until throwsPerPosition).map { t ->
                    state.results[positionIndex to t]
                }
                val made = rowResults.count { it == PuttingResult.MADE }
                val missed = rowResults.count { it == PuttingResult.MISSED }
                val attempted = made + missed

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "${"%.0f".format(distance)} ft",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = "$made made · $missed missed · ${throwsPerPosition - attempted} remaining",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            for (t in 0 until throwsPerPosition) {
                                ResultCircle(
                                    result = rowResults[t],
                                    onTap = { viewModel.onTap(positionIndex, t) },
                                    onLongPress = { viewModel.onLongPress(positionIndex, t) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ResultCircle(
    result: PuttingResult?,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val fill: Color = when (result) {
        PuttingResult.MADE -> Color(0xFF2E7D32)
        PuttingResult.MISSED -> scheme.error
        null -> scheme.surfaceVariant
    }
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(fill)
            .border(1.dp, scheme.outline, CircleShape)
            .combinedClickable(
                onClick = onTap,
                onLongClick = onLongPress,
            ),
    )
}
