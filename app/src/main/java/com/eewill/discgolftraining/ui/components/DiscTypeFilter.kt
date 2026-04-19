package com.eewill.discgolftraining.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.eewill.discgolftraining.data.DiscType

@Composable
fun DiscTypeFilter(
    selected: DiscType?,
    onChange: (DiscType?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val label = selected?.displayName() ?: "All"
    Box(modifier = modifier) {
        AssistChip(
            onClick = { expanded = true },
            label = { Text("Type: $label ▼") },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("All") },
                onClick = {
                    onChange(null)
                    expanded = false
                },
            )
            DiscType.entries.forEach { t ->
                DropdownMenuItem(
                    text = { Text(t.displayName()) },
                    onClick = {
                        onChange(t)
                        expanded = false
                    },
                )
            }
        }
    }
}
