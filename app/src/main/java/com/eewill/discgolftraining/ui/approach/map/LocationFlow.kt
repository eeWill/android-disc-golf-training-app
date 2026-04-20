package com.eewill.discgolftraining.ui.approach.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng

@Composable
fun rememberLocationPermissionState(): Pair<Boolean, () -> Unit> {
    val context = LocalContext.current
    fun currentlyGranted(): Boolean =
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

    var granted by remember { mutableStateOf(currentlyGranted()) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        granted = result.values.any { it }
    }
    val request: () -> Unit = {
        launcher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        )
    }
    return granted to request
}

@SuppressLint("MissingPermission")
@Composable
fun rememberCurrentLatLng(hasPermission: Boolean): State<LatLng?> {
    val context = LocalContext.current
    val state = remember { mutableStateOf<LatLng?>(null) }
    DisposableEffect(hasPermission) {
        if (!hasPermission) {
            state.value = null
            return@DisposableEffect onDispose { }
        }
        val client = LocationServices.getFusedLocationProviderClient(context)
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2_000L)
            .setMinUpdateIntervalMillis(1_000L)
            .build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { state.value = LatLng(it.latitude, it.longitude) }
            }
        }
        client.lastLocation.addOnSuccessListener { loc ->
            if (state.value == null && loc != null) {
                state.value = LatLng(loc.latitude, loc.longitude)
            }
        }
        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
        onDispose { client.removeLocationUpdates(callback) }
    }
    return state
}
