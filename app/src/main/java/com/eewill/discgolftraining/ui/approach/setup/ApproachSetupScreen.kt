package com.eewill.discgolftraining.ui.approach.setup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.setValue
import android.location.Location
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eewill.discgolftraining.data.DiscType
import com.eewill.discgolftraining.ui.approach.map.ApproachMap
import com.eewill.discgolftraining.ui.approach.map.rememberCurrentLatLng
import com.eewill.discgolftraining.ui.approach.map.rememberLocationPermissionState
import com.eewill.discgolftraining.ui.approachRoundRepository
import com.eewill.discgolftraining.ui.components.DiscTypeFilter
import com.eewill.discgolftraining.ui.discRepository
import com.eewill.discgolftraining.ui.simpleFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApproachSetupScreen(
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onBegin: (String) -> Unit,
) {
    val context = LocalContext.current
    val viewModel: ApproachSetupViewModel = viewModel(
        factory = simpleFactory {
            ApproachSetupViewModel(context.approachRoundRepository(), context.discRepository())
        },
    )
    val discs by viewModel.discs.collectAsState()
    val selected by viewModel.selectedDiscIds.collectAsState()
    val pendingTarget by viewModel.pendingTarget.collectAsState()
    val pendingStart by viewModel.pendingStart.collectAsState()

    var distanceText by remember { mutableStateOf("") }
    var distanceUserEdited by remember { mutableStateOf(false) }
    var targetSizeText by remember { mutableStateOf("33") }
    var typeFilter by remember { mutableStateOf<DiscType?>(null) }
    val distance = distanceText.toFloatOrNull()
    val targetSize = targetSizeText.toFloatOrNull()
    val canBegin = distance != null && distance > 0f && selected.isNotEmpty()
    val visibleDiscs = typeFilter?.let { t -> discs.filter { it.type == t } } ?: discs

    val (locationGranted, requestLocation) = rememberLocationPermissionState()
    LaunchedEffect(Unit) { if (!locationGranted) requestLocation() }
    val currentLatLng by rememberCurrentLatLng(locationGranted)

    LaunchedEffect(currentLatLng) {
        if (pendingStart == null && currentLatLng != null) {
            viewModel.setPendingStart(currentLatLng)
        }
    }

    LaunchedEffect(pendingTarget, pendingStart, distanceUserEdited) {
        if (distanceUserEdited) return@LaunchedEffect
        val target = pendingTarget ?: return@LaunchedEffect
        val start = pendingStart ?: return@LaunchedEffect
        val out = FloatArray(1)
        Location.distanceBetween(
            start.latitude, start.longitude,
            target.latitude, target.longitude,
            out,
        )
        val feet = out[0] * 3.28084f
        distanceText = if (feet % 1f == 0f) feet.toInt().toString() else "%.1f".format(feet)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Approach Shot — Setup") },
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Target", style = MaterialTheme.typography.titleSmall)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
            ) {
                ApproachMap(
                    modifier = Modifier.fillMaxSize(),
                    targetLatLng = pendingTarget,
                    startLatLng = pendingStart,
                    targetCircleRadiusMeters = ((targetSize ?: 0f) * 0.3048f).toDouble(),
                    startDraggable = true,
                    onTargetPlaced = { viewModel.setPendingTarget(it) },
                    onStartMoved = { viewModel.setPendingStart(it) },
                )
            }
            Text(
                text = if (pendingTarget != null) {
                    "Tap the map to move the target. Long-press anywhere to set your start (blue square)."
                } else {
                    "Tap the map to drop the target. Long-press anywhere to set your start (blue square)."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedTextField(
                value = targetSizeText,
                onValueChange = { targetSizeText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Target radius (ft)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = distanceText,
                onValueChange = {
                    distanceUserEdited = true
                    distanceText = it.filter { c -> c.isDigit() || c == '.' }
                },
                label = { Text("Distance to target (ft)") },
                supportingText = {
                    if (!distanceUserEdited && pendingTarget != null && pendingStart != null) {
                        Text("Auto-filled from start to target. Edit to override.")
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(4.dp))
            Text("Discs to throw", style = MaterialTheme.typography.titleSmall)

            if (discs.isEmpty()) {
                Text(
                    "Add discs in Settings before starting an approach round.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Open Settings") }
            } else {
                DiscTypeFilter(selected = typeFilter, onChange = { typeFilter = it })
                if (visibleDiscs.isEmpty()) {
                    Text(
                        "No discs of this type.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                visibleDiscs.forEach { disc ->
                    val checked = disc.id in selected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleDisc(disc.id) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { viewModel.toggleDisc(disc.id) },
                        )
                        Spacer(Modifier.padding(horizontal = 4.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(disc.name, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                disc.type.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    val d = distance ?: return@Button
                    viewModel.beginRound(d, targetSize, onBegin)
                },
                enabled = canBegin,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Begin") }
        }
    }
}
