package com.eewill.discgolftraining.ui.approach.active

import android.location.Location
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.eewill.discgolftraining.ui.approach.map.ApproachMap
import com.eewill.discgolftraining.ui.approachRoundRepository
import com.eewill.discgolftraining.ui.simpleFactory
import com.google.android.gms.maps.model.LatLng

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
    val placingDiscId by viewModel.placingDiscId.collectAsState()

    val persistedByDisc = throws.associateBy { it.discId }
    val localText = remember { mutableStateMapOf<String, String>() }

    val targetLabel = round?.targetDistanceFeet?.let { "Target ${"%.0f".format(it)} ft" } ?: "Approach"
    val recordedCount = throws.size

    val targetLatLng: LatLng? = round?.let { r ->
        val lat = r.targetLat
        val lng = r.targetLng
        if (lat != null && lng != null) LatLng(lat, lng) else null
    }
    val targetCircleRadiusMeters =
        ((round?.targetSizeFeet ?: 0f) * 0.3048f).toDouble()

    val startLatLng: LatLng? = round?.let { r ->
        val lat = r.startLat
        val lng = r.startLng
        if (lat != null && lng != null) LatLng(lat, lng) else null
    }

    val placingDisc = placingDiscId?.let { id -> discs.firstOrNull { it.id == id } }

    val landingMarkers = throws.mapNotNull { t ->
        val lat = t.landingLat
        val lng = t.landingLng
        if (lat != null && lng != null) LatLng(lat, lng) else null
    }

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

            val mapBorder = if (placingDisc != null) {
                BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
            } else null
            Card(
                modifier = Modifier.fillMaxWidth().height(260.dp),
                border = mapBorder,
                colors = CardDefaults.cardColors(),
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    ApproachMap(
                        modifier = Modifier.fillMaxSize(),
                        targetLatLng = targetLatLng,
                        startLatLng = startLatLng,
                        targetCircleRadiusMeters = targetCircleRadiusMeters,
                        throwMarkers = landingMarkers,
                        onTargetPlaced = { },
                        onLandingPlaced = placingDisc?.let { disc ->
                            { tap ->
                                val target = targetLatLng ?: return@let
                                val out = FloatArray(1)
                                Location.distanceBetween(
                                    target.latitude, target.longitude,
                                    tap.latitude, tap.longitude,
                                    out,
                                )
                                val feet = (out[0] * 3.28084f)
                                viewModel.setThrow(
                                    discId = disc.id,
                                    distanceFeet = feet,
                                    landingLat = tap.latitude,
                                    landingLng = tap.longitude,
                                )
                                localText[disc.id] = feet.fmt()
                                val recorded = throws.mapNotNull { it.discId }.toMutableSet()
                                    .apply { add(disc.id) }
                                val nextId = discs
                                    .map { it.id }
                                    .firstOrNull { it !in recorded }
                                if (nextId != null) viewModel.beginPlacingLanding(nextId)
                                else viewModel.cancelPlacingLanding()
                            }
                        },
                    )
                }
            }

            if (placingDisc != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Tap map to mark where ${placingDisc.name} landed",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    TextButton(onClick = { viewModel.cancelPlacingLanding() }) { Text("Cancel") }
                }
            } else {
                Text(
                    text = if (targetLatLng == null) {
                        "Tap the map to set the target, then walk or tap to record each disc."
                    } else {
                        "Enter a distance below, or tap \"Place on map\" to drop the landing spot."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(discs, key = { it.id }) { disc ->
                    val persistedThrow = persistedByDisc[disc.id]
                    val persisted = persistedThrow?.landingDistanceFeet?.fmt().orEmpty()
                    val value = localText[disc.id] ?: persisted
                    val isPlacingThis = placingDiscId == disc.id
                    val hasLanding = persistedThrow?.landingLat != null && persistedThrow.landingLng != null
                    val cardBorder = if (isPlacingThis) {
                        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    } else null
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        border = cardBorder,
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        if (isPlacingThis) viewModel.cancelPlacingLanding()
                                        else viewModel.beginPlacingLanding(disc.id)
                                    },
                                    enabled = targetLatLng != null,
                                ) {
                                    Icon(Icons.Filled.Place, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        when {
                                            isPlacingThis -> "Tap map…"
                                            hasLanding -> "Re-place on map"
                                            else -> "Place on map"
                                        }
                                    )
                                }
                                if (targetLatLng == null) {
                                    Text(
                                        "Place target first",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
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
