package com.eewill.discgolftraining.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStartGapPractice: () -> Unit,
    onStartApproachPractice: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Disc Golf Training") }) },
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Practice drills", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onStartGapPractice,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Gap Throwing Practice")
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onStartApproachPractice,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Approach Shot Practice")
            }
            Spacer(Modifier.height(32.dp))
            OutlinedButton(
                onClick = onOpenHistory,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("History")
            }
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = onOpenStats,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Statistics")
            }
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = onOpenSettings,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Settings")
            }
        }
    }
}
