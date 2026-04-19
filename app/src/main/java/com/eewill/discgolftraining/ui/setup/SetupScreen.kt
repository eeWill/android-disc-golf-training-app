package com.eewill.discgolftraining.ui.setup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eewill.discgolftraining.data.DiscDataMode
import com.eewill.discgolftraining.ui.components.GapBoxDrawer
import com.eewill.discgolftraining.ui.discRepository
import com.eewill.discgolftraining.ui.repository
import com.eewill.discgolftraining.ui.simpleFactory
import com.eewill.discgolftraining.util.ImageFiles
import java.io.File
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    onBack: () -> Unit,
    onOpenPicker: () -> Unit,
    onOpenSettings: () -> Unit,
    onBegin: (String) -> Unit,
    reusedRoundIdFlow: StateFlow<String?> = MutableStateFlow(null),
    onReusedRoundIdConsumed: () -> Unit = {},
) {
    val context = LocalContext.current
    val viewModel: SetupViewModel = viewModel(
        factory = simpleFactory { SetupViewModel(context.repository(), context.discRepository()) },
    )

    var state by remember { mutableStateOf(SetupState()) }
    var pendingFile by remember { mutableStateOf<File?>(null) }

    val reusedRoundId by reusedRoundIdFlow.collectAsState()
    val discs by viewModel.discs.collectAsState()

    LaunchedEffect(reusedRoundId) {
        val id = reusedRoundId ?: return@LaunchedEffect
        val round = viewModel.loadRound(id)
        if (round != null) {
            state = SetupState.fromRound(round)
        }
        onReusedRoundIdConsumed()
    }

    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success ->
        val file = pendingFile
        if (success && file != null && file.exists() && file.length() > 0L) {
            state = state.copy(imagePath = file.absolutePath, gapRect = null)
        } else {
            file?.delete()
        }
        pendingFile = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gap Throwing — Setup") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = {
                        val file = ImageFiles.createRoundImageFile(context)
                        pendingFile = file
                        val uri = ImageFiles.fileProviderUri(context, file)
                        takePicture.launch(uri)
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (state.imagePath == null) "Take Photo" else "Retake Photo")
                }
                OutlinedButton(
                    onClick = onOpenPicker,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Reuse Previous")
                }
            }

            state.imagePath?.let { path ->
                Text("Drag on the image to mark the gap.")
                GapBoxDrawer(
                    imagePath = path,
                    modifier = Modifier.fillMaxWidth(),
                    gapRect = state.gapRect,
                    onRectChanged = { rect -> state = state.copy(gapRect = rect) },
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = state.distanceFeet,
                    onValueChange = { state = state.copy(distanceFeet = it.filter { c -> c.isDigit() || c == '.' }) },
                    label = { Text("Distance (ft)") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                    ),
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = state.gapWidthFeet,
                    onValueChange = { state = state.copy(gapWidthFeet = it.filter { c -> c.isDigit() || c == '.' }) },
                    label = { Text("Gap width (ft)") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                    ),
                    modifier = Modifier.weight(1f),
                )
            }

            Text("Disc Data", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DiscDataMode.entries.forEach { mode ->
                    FilterChip(
                        selected = state.discDataMode == mode,
                        onClick = { state = state.copy(discDataMode = mode) },
                        label = { Text(mode.displayName()) },
                    )
                }
            }

            if (state.discDataMode == DiscDataMode.DISC && discs.isEmpty()) {
                Text(
                    "Add a disc in Settings to use this mode.",
                    style = MaterialTheme.typography.bodySmall,
                )
                TextButton(onClick = onOpenSettings) {
                    Text("Go to Settings")
                }
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { viewModel.beginRound(state, onBegin) },
                enabled = state.canBegin(hasDiscs = discs.isNotEmpty()),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Begin")
            }
        }
    }
}
