package com.eewill.discgolftraining.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eewill.discgolftraining.ui.active.gapRect
import com.eewill.discgolftraining.ui.components.ImageWithOverlay
import com.eewill.discgolftraining.ui.components.ThrowMarker
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
        factory = simpleFactory { SummaryViewModel(roundId, context.repository()) },
    )
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Round Summary") }) },
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val round = state?.round
            val throws = state?.throws.orEmpty()
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
            } else {
                Text("Loading round…")
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
