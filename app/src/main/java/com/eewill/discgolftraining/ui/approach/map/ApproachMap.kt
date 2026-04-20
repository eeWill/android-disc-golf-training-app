package com.eewill.discgolftraining.ui.approach.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@Composable
fun ApproachMap(
    modifier: Modifier = Modifier,
    targetLatLng: LatLng?,
    startLatLng: LatLng?,
    targetCircleRadiusMeters: Double,
    throwMarkers: List<LatLng> = emptyList(),
    interactive: Boolean = true,
    startDraggable: Boolean = false,
    onTargetPlaced: (LatLng) -> Unit = {},
    onStartMoved: (LatLng) -> Unit = {},
    onLandingPlaced: ((LatLng) -> Unit)? = null,
) {
    val cameraPositionState = rememberCameraPositionState()
    val context = LocalContext.current
    val icons = remember(context) {
        MapsInitializer.initialize(context)
        ApproachMapIcons(
            square = buildSquareBitmapDescriptor(sizePx = 56, colorArgb = 0xFF2962FF.toInt()),
        )
    }

    var didInitialCenter by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(targetLatLng, startLatLng) {
        if (didInitialCenter) return@LaunchedEffect
        val center = targetLatLng ?: startLatLng ?: return@LaunchedEffect
        cameraPositionState.position = CameraPosition.fromLatLngZoom(center, 20f)
        didInitialCenter = true
    }

    val mapProperties = remember { MapProperties(mapType = MapType.SATELLITE) }
    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = true,
            zoomGesturesEnabled = true,
            compassEnabled = true,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false,
        )
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = uiSettings,
        onMapClick = { tap ->
            if (!interactive) return@GoogleMap
            val landingHandler = onLandingPlaced
            if (landingHandler != null) landingHandler(tap)
            else onTargetPlaced(tap)
        },
    ) {
        startLatLng?.let { pos ->
            val meState = rememberMarkerState(position = pos)
            LaunchedEffect(pos) {
                if (meState.position != pos) meState.position = pos
            }
            if (startDraggable) {
                LaunchedEffect(meState.isDragging) {
                    if (!meState.isDragging && meState.position != pos) {
                        onStartMoved(meState.position)
                    }
                }
            }
            Marker(
                state = meState,
                icon = icons.square,
                flat = true,
                anchor = Offset(0.5f, 0.5f),
                zIndex = 0f,
                title = "You",
                onClick = { true },
                draggable = interactive && startDraggable,
            )
        }

        targetLatLng?.let { t ->
            Circle(
                center = t,
                radius = 0.6,
                fillColor = Color.Yellow,
                strokeColor = Color.Black,
                strokeWidth = 3f,
                zIndex = 2f,
            )
            Circle(
                center = t,
                radius = targetCircleRadiusMeters,
                strokeColor = Color.Yellow,
                fillColor = Color(0x33FFFF00),
                strokeWidth = 4f,
                zIndex = 1f,
            )
        }

        throwMarkers.forEach { ll ->
            Circle(
                center = ll,
                radius = 0.4,
                fillColor = Color(0xFF2E7D32),
                strokeColor = Color.White,
                strokeWidth = 3f,
                zIndex = 3f,
            )
        }
    }
}

private data class ApproachMapIcons(
    val square: BitmapDescriptor,
)

private fun buildSquareBitmapDescriptor(sizePx: Int, colorArgb: Int): BitmapDescriptor {
    val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val fill = Paint().apply { color = colorArgb; isAntiAlias = true }
    val stroke = Paint().apply {
        color = AndroidColor.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }
    canvas.drawRect(0f, 0f, sizePx.toFloat(), sizePx.toFloat(), fill)
    canvas.drawRect(2f, 2f, sizePx - 2f, sizePx - 2f, stroke)
    return BitmapDescriptorFactory.fromBitmap(bmp)
}

private fun buildCircleBitmapDescriptor(
    sizePx: Int,
    fillArgb: Int,
    strokeArgb: Int,
): BitmapDescriptor {
    val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val fill = Paint().apply { color = fillArgb; isAntiAlias = true }
    val stroke = Paint().apply {
        color = strokeArgb
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
    }
    val cx = sizePx / 2f
    val cy = sizePx / 2f
    val r = (sizePx / 2f) - 2f
    canvas.drawCircle(cx, cy, r, fill)
    canvas.drawCircle(cx, cy, r, stroke)
    return BitmapDescriptorFactory.fromBitmap(bmp)
}
