package com.eewill.discgolftraining.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import coil.compose.AsyncImage
import kotlin.math.max
import kotlin.math.min

@Composable
fun GapBoxDrawer(
    imagePath: String,
    modifier: Modifier = Modifier,
    gapRect: Rect?,
    onRectChanged: (Rect) -> Unit,
) {
    val bitmapSize = rememberBitmapSize(imagePath)
    val aspect = if (bitmapSize != null && bitmapSize.height > 0f) {
        bitmapSize.width / bitmapSize.height
    } else 1f

    BoxWithConstraints(modifier = modifier.aspectRatio(aspect)) {
        val density = LocalDensity.current
        val containerW = with(density) { maxWidth.toPx() }
        val containerH = with(density) { maxHeight.toPx() }
        val imageBounds = computeImageBounds(containerW, containerH, bitmapSize)

        var dragStart by remember { mutableStateOf<Offset?>(null) }
        var dragCurrent by remember { mutableStateOf<Offset?>(null) }

        AsyncImage(
            model = imagePath,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(imageBounds) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            if (imageBounds.contains(offset)) {
                                dragStart = offset
                                dragCurrent = offset
                            } else {
                                dragStart = null
                                dragCurrent = null
                            }
                        },
                        onDrag = { change, _ ->
                            val start = dragStart ?: return@detectDragGestures
                            val clamped = Offset(
                                change.position.x.coerceIn(imageBounds.left, imageBounds.right),
                                change.position.y.coerceIn(imageBounds.top, imageBounds.bottom),
                            )
                            dragCurrent = clamped
                            val a = toNormalized(start, imageBounds)
                            val b = toNormalized(clamped, imageBounds)
                            onRectChanged(normalizedRect(a, b))
                        },
                        onDragEnd = {
                            dragStart = null
                            dragCurrent = null
                        },
                        onDragCancel = {
                            dragStart = null
                            dragCurrent = null
                        },
                    )
                },
        ) {
            val rectToDraw = if (dragStart != null && dragCurrent != null) {
                Rect(
                    min(dragStart!!.x, dragCurrent!!.x),
                    min(dragStart!!.y, dragCurrent!!.y),
                    max(dragStart!!.x, dragCurrent!!.x),
                    max(dragStart!!.y, dragCurrent!!.y),
                )
            } else if (gapRect != null) {
                val tl = toPixels(Offset(gapRect.left, gapRect.top), imageBounds)
                val br = toPixels(Offset(gapRect.right, gapRect.bottom), imageBounds)
                Rect(tl.x, tl.y, br.x, br.y)
            } else null

            if (rectToDraw != null) {
                val size = Size(rectToDraw.width, rectToDraw.height)
                val topLeft = Offset(rectToDraw.left, rectToDraw.top)
                drawRect(color = Color(0x3300FF00), topLeft = topLeft, size = size)
                drawRect(color = Color(0xFF00AA00), topLeft = topLeft, size = size, style = Stroke(width = 4f))
            }
        }
    }
}
