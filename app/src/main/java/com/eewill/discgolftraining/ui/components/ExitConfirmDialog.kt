package com.eewill.discgolftraining.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@Composable
fun KeepScreenOn() {
    val context = LocalContext.current
    DisposableEffect(context) {
        val window = context.findActivity()?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

private fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@Composable
fun ExitRoundGuard(
    onConfirmExit: () -> Unit,
    content: @Composable (requestExit: () -> Unit) -> Unit,
) {
    KeepScreenOn()

    var showDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = !showDialog) { showDialog = true }

    content { showDialog = true }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Exit round?") },
            text = { Text("This will end the current round. Your throws are saved.") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    onConfirmExit()
                }) { Text("Exit") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Keep playing") }
            },
        )
    }
}
