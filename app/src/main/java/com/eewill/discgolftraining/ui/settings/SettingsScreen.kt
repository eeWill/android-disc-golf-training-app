package com.eewill.discgolftraining.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import com.eewill.discgolftraining.data.DiscType
import com.eewill.discgolftraining.ui.discRepository
import com.eewill.discgolftraining.ui.simpleFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenDisc: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel(
        factory = simpleFactory { SettingsViewModel(context.discRepository()) },
    )
    val discs by viewModel.discs.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Discs") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { inner ->
        Column(
            modifier = Modifier.fillMaxSize().padding(inner),
        ) {
            if (discs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("No discs yet. Add one below.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(discs, key = { _, it -> it.id }) { index, disc ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenDisc(disc.id) },
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(disc.name, style = MaterialTheme.typography.titleSmall)
                                    Text(disc.type.displayName(), style = MaterialTheme.typography.bodySmall)
                                }
                                TextButton(
                                    onClick = { viewModel.moveUp(disc.id) },
                                    enabled = index > 0,
                                ) { Text("↑") }
                                TextButton(
                                    onClick = { viewModel.moveDown(disc.id) },
                                    enabled = index < discs.lastIndex,
                                ) { Text("↓") }
                                TextButton(onClick = { viewModel.deleteDisc(disc.id) }) {
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider()
            AddDiscForm(onAdd = viewModel::addDisc)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDiscForm(onAdd: (String, DiscType) -> Unit) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(DiscType.PUTTER) }
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Add a Disc", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                value = type.displayName(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth(),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                DiscType.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.displayName()) },
                        onClick = {
                            type = option
                            expanded = false
                        },
                    )
                }
            }
        }
        Button(
            onClick = {
                onAdd(name, type)
                name = ""
                type = DiscType.PUTTER
            },
            enabled = name.trim().isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Add Disc")
        }
    }
}
