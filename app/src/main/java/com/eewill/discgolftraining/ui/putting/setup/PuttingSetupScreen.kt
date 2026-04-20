package com.eewill.discgolftraining.ui.putting.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eewill.discgolftraining.data.puttingPositions
import com.eewill.discgolftraining.ui.puttingRoundRepository
import com.eewill.discgolftraining.ui.simpleFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuttingSetupScreen(
    onBack: () -> Unit,
    onBegin: (String) -> Unit,
) {
    val context = LocalContext.current
    val viewModel: PuttingSetupViewModel = viewModel(
        factory = simpleFactory { PuttingSetupViewModel(context.puttingRoundRepository()) },
    )

    var minText by remember { mutableStateOf("10") }
    var maxText by remember { mutableStateOf("30") }
    var intervalText by remember { mutableStateOf("5") }
    var throwsText by remember { mutableStateOf("10") }

    val minDistance = minText.toFloatOrNull()
    val maxDistance = maxText.toFloatOrNull()
    val interval = intervalText.toFloatOrNull()
    val throws = throwsText.toIntOrNull()

    val positions = if (minDistance != null && maxDistance != null && interval != null) {
        puttingPositions(minDistance, maxDistance, interval)
    } else emptyList()

    val canBegin = minDistance != null && minDistance > 0f &&
        maxDistance != null && maxDistance >= minDistance &&
        interval != null && interval > 0f &&
        throws != null && throws >= 1 &&
        positions.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Putting — Setup") },
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
            OutlinedTextField(
                value = minText,
                onValueChange = { minText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Minimum distance (ft)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = intervalText,
                onValueChange = { intervalText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Distance interval (ft)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = maxText,
                onValueChange = { maxText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Maximum distance (ft)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = throwsText,
                onValueChange = { throwsText = it.filter { c -> c.isDigit() } },
                label = { Text("Throws per position") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(4.dp))
            val totalPutts = positions.size * (throws ?: 0)
            Text(
                text = if (positions.isNotEmpty() && throws != null && throws >= 1) {
                    "${positions.size} positions × $throws throws = $totalPutts putts"
                } else {
                    "Enter valid settings to preview positions."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    viewModel.beginRound(
                        minDistanceFeet = minDistance!!,
                        maxDistanceFeet = maxDistance!!,
                        intervalFeet = interval!!,
                        throwsPerPosition = throws!!,
                        onReady = onBegin,
                    )
                },
                enabled = canBegin,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Begin") }
        }
    }
}
