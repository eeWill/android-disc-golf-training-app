package com.eewill.discgolftraining.ui.stats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eewill.discgolftraining.ui.approachRoundRepository
import com.eewill.discgolftraining.ui.components.DiscTypeFilter
import com.eewill.discgolftraining.ui.discRepository
import com.eewill.discgolftraining.ui.repository
import com.eewill.discgolftraining.ui.simpleFactory

@Composable
fun DiscListStatsContent(
    onOpenDisc: (String) -> Unit,
) {
    val context = LocalContext.current
    val viewModel: DiscListStatsViewModel = viewModel(
        factory = simpleFactory {
            DiscListStatsViewModel(
                discRepo = context.discRepository(),
                roundRepo = context.repository(),
                approachRepo = context.approachRoundRepository(),
            )
        },
    )
    val items by viewModel.items.collectAsState()
    val typeFilter by viewModel.typeFilter.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DiscTypeFilter(
                selected = typeFilter,
                onChange = { viewModel.setTypeFilter(it) },
            )
        }

        if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (typeFilter == null) "No discs yet. Add some in Settings."
                    else "No discs of this type.",
                )
            }
        } else {
            items.forEach { item ->
                DiscStatsCard(item = item, onClick = { onOpenDisc(item.disc.id) })
            }
        }
    }
}

@Composable
private fun DiscStatsCard(
    item: DiscListStatsItem,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(item.disc.name, style = MaterialTheme.typography.titleSmall)
            Text(item.disc.type.displayName(), style = MaterialTheme.typography.bodySmall)
            Text(
                gapSummary(item),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                approachSummary(item),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private fun gapSummary(item: DiscListStatsItem): String {
    if (item.gapThrowCount == 0) return "Gap: no throws yet"
    val pct = item.gapHitPct?.let { "%.0f%%".format(it) } ?: "—"
    return "Gap: ${item.gapThrowCount} throws · hit $pct"
}

private fun approachSummary(item: DiscListStatsItem): String {
    if (item.approachThrowCount == 0) return "Approach: no throws yet"
    val avg = item.approachAvgDistFt?.let { "%.1f ft".format(it) } ?: "—"
    return "Approach: ${item.approachThrowCount} throws · avg $avg"
}
