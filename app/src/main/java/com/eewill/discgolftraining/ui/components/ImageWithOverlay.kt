package com.eewill.discgolftraining.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

data class ThrowMarker(val x: Float, val y: Float, val isHit: Boolean)

@Composable
fun ImageWithOverlay(
    imagePath: String,
    modifier: Modifier = Modifier,
    gapRect: Rect? = null,
    markers: List<ThrowMarker> = emptyList(),
    onTap: ((Offset) -> Unit)? = null,
) {
    val bitmapSize = rememberBitmapSize(imagePath)
    val aspect = if (bitmapSize != null && bitmapSize.height > 0f) {
        bitmapSize.width / bitmapSize.height
    } else 1f

    BoxWithConstraints(
        modifier = modifier.aspectRatio(aspect),
    ) {
        val density = LocalDensity.current
        val containerW = with(density) { maxWidth.toPx() }
        val containerH = with(density) { maxHeight.toPx() }
        val imageBounds = computeImageBounds(containerW, containerH, bitmapSize)

        AsyncImage(
            model = imagePath,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (onTap != null) Modifier.pointerInput(imageBounds) {
                        detectTapGestures { pos ->
                            if (imageBounds.contains(pos)) {
                                onTap(toNormalized(pos, imageBounds))
                            }
                        }
                    } else Modifier,
                ),
        ) {
            if (gapRect != null) {
                val tl = toPixels(Offset(gapRect.left, gapRect.top), imageBounds)
                val br = toPixels(Offset(gapRect.right, gapRect.bottom), imageBounds)
                val topLeft = Offset(min(tl.x, br.x), min(tl.y, br.y))
                val size = Size((br.x - tl.x).let { if (it < 0) -it else it }, (br.y - tl.y).let { if (it < 0) -it else it })
                drawRect(color = Color(0x3300FF00), topLeft = topLeft, size = size)
                drawRect(color = Color(0xFF00AA00), topLeft = topLeft, size = size, style = Stroke(width = 4f))
            }
            markers.forEach { m ->
                val center = toPixels(Offset(m.x, m.y), imageBounds)
                val color = if (m.isHit) Color(0xFF2E7D32) else Color(0xFFC62828)
                drawCircle(color = color, radius = 14f, center = center)
                drawCircle(color = Color.White, radius = 14f, center = center, style = Stroke(width = 3f))
            }
        }
    }
}

@Composable
fun rememberBitmapSize(imagePath: String): Size? {
    var size by remember(imagePath) { mutableStateOf<Size?>(null) }
    LaunchedEffect(imagePath) {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(imagePath, opts)
        if (opts.outWidth > 0 && opts.outHeight > 0) {
            size = Size(opts.outWidth.toFloat(), opts.outHeight.toFloat())
        }
    }
    return size
}

fun computeImageBounds(containerW: Float, containerH: Float, bitmapSize: Size?): Rect {
    if (bitmapSize == null || bitmapSize.width <= 0f || bitmapSize.height <= 0f) {
        return Rect(0f, 0f, containerW, containerH)
    }
    val scale = min(containerW / bitmapSize.width, containerH / bitmapSize.height)
    val w = bitmapSize.width * scale
    val h = bitmapSize.height * scale
    val left = (containerW - w) / 2f
    val top = (containerH - h) / 2f
    return Rect(left, top, left + w, top + h)
}

fun toNormalized(px: Offset, imageBounds: Rect): Offset {
    val w = imageBounds.width
    val h = imageBounds.height
    if (w <= 0f || h <= 0f) return Offset(0f, 0f)
    val nx = ((px.x - imageBounds.left) / w).coerceIn(0f, 1f)
    val ny = ((px.y - imageBounds.top) / h).coerceIn(0f, 1f)
    return Offset(nx, ny)
}

fun toPixels(norm: Offset, imageBounds: Rect): Offset {
    return Offset(
        imageBounds.left + norm.x * imageBounds.width,
        imageBounds.top + norm.y * imageBounds.height,
    )
}

fun normalizedRect(a: Offset, b: Offset): Rect {
    val left = min(a.x, b.x)
    val right = max(a.x, b.x)
    val top = min(a.y, b.y)
    val bottom = max(a.y, b.y)
    return Rect(left, top, right, bottom)
}
