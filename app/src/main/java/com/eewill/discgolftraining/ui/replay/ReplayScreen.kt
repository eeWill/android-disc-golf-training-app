package com.eewill.discgolftraining.ui.replay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.eewill.discgolftraining.data.DirectionCounts
import com.eewill.discgolftraining.ui.active.gapRect
import com.eewill.discgolftraining.ui.components.ImageWithOverlay
import com.eewill.discgolftraining.ui.components.ThrowMarker
import com.eewill.discgolftraining.ui.repository
import com.eewill.discgolftraining.ui.simpleFactory
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReplayScreen(
    roundId: String,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: ReplayViewModel = viewModel(
        key = "replay-$roundId",
        factory = simpleFactory { ReplayViewModel(roundId, context.repository()) },
    )
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Replay") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val round = state?.round
            val throws = state?.throws.orEmpty()
            if (round != null) {
                ImageWithOverlay(
                    imagePath = round.imagePath,
                    modifier = Modifier.fillMaxWidth(),
                    gapRect = round.gapRect(),
                    markers = throws.map { ThrowMarker(it.x, it.y, it.isHit) },
                )
                val hits = throws.count { it.isHit }
                val total = throws.size
                val pct = if (total > 0) hits * 100f / total else 0f
                Text(
                    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                        .format(Date(round.createdAt)),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text("Hits: $hits / $total  (${"%.0f".format(pct)}%)", style = MaterialTheme.typography.titleMedium)
                val dirs = DirectionCounts.from(round, throws)
                Text("Misses — Left ${dirs.left}  ·  Right ${dirs.right}  ·  High ${dirs.high}  ·  Low ${dirs.low}")
                Text("Distance ${round.distanceFeet} ft  ·  Gap width ${round.gapWidthFeet} ft")
            } else {
                Text("Loading round…")
            }
        }
    }
}
