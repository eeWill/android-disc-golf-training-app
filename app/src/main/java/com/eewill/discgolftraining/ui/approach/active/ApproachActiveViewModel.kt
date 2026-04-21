package com.eewill.discgolftraining.ui.approach.active

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eewill.discgolftraining.data.ApproachRoundEntity
import com.eewill.discgolftraining.data.ApproachRoundRepository
import com.eewill.discgolftraining.data.ApproachThrowEntity
import com.eewill.discgolftraining.data.DiscEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ApproachActiveViewModel(
    private val roundId: String,
    private val repository: ApproachRoundRepository,
) : ViewModel() {

    private val _round = MutableStateFlow<ApproachRoundEntity?>(null)
    val round: StateFlow<ApproachRoundEntity?> = _round.asStateFlow()

    private val _placingDiscId = MutableStateFlow<String?>(null)
    val placingDiscId: StateFlow<String?> = _placingDiscId.asStateFlow()

    private val lastLandingByDisc = mutableMapOf<String, Pair<Double, Double>>()

    init {
        viewModelScope.launch { _round.value = repository.getRound(roundId) }
    }

    val discs: StateFlow<List<DiscEntity>> =
        repository.getRoundDiscs(roundId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val throws: StateFlow<List<ApproachThrowEntity>> =
        repository.getRoundThrows(roundId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun setThrow(
        discId: String,
        distanceFeet: Float,
        landingLat: Double? = null,
        landingLng: Double? = null,
    ) {
        val finalLat: Double?
        val finalLng: Double?
        if (landingLat != null && landingLng != null) {
            lastLandingByDisc[discId] = landingLat to landingLng
            finalLat = landingLat
            finalLng = landingLng
        } else {
            val existing = throws.value.firstOrNull { it.discId == discId }
            val bearingSource = existing?.let {
                val lat = it.landingLat
                val lng = it.landingLng
                if (lat != null && lng != null) lat to lng else null
            } ?: lastLandingByDisc[discId]
            val targetLat = _round.value?.targetLat
            val targetLng = _round.value?.targetLng
            if (bearingSource != null && targetLat != null && targetLng != null) {
                val bearing = initialBearingDeg(
                    targetLat, targetLng, bearingSource.first, bearingSource.second,
                )
                val distMeters = (distanceFeet / 3.28084f).toDouble()
                val repositioned = destinationPoint(targetLat, targetLng, bearing, distMeters)
                lastLandingByDisc[discId] = repositioned
                finalLat = repositioned.first
                finalLng = repositioned.second
            } else {
                finalLat = bearingSource?.first
                finalLng = bearingSource?.second
            }
        }
        val position = discs.value.indexOfFirst { it.id == discId }.coerceAtLeast(0)
        viewModelScope.launch {
            repository.insertThrow(
                ApproachThrowEntity(
                    id = throwId(discId),
                    roundId = roundId,
                    index = position,
                    discId = discId,
                    landingDistanceFeet = distanceFeet,
                    landingLat = finalLat,
                    landingLng = finalLng,
                )
            )
        }
    }

    private fun initialBearingDeg(
        lat1: Double, lng1: Double,
        lat2: Double, lng2: Double,
    ): Double {
        val results = FloatArray(2)
        Location.distanceBetween(lat1, lng1, lat2, lng2, results)
        return results[1].toDouble()
    }

    private fun destinationPoint(
        lat1: Double, lng1: Double,
        bearingDeg: Double, distanceMeters: Double,
    ): Pair<Double, Double> {
        val earthRadius = 6371000.0
        val brng = Math.toRadians(bearingDeg)
        val phi1 = Math.toRadians(lat1)
        val lambda1 = Math.toRadians(lng1)
        val delta = distanceMeters / earthRadius
        val phi2 = Math.asin(
            Math.sin(phi1) * Math.cos(delta) +
                Math.cos(phi1) * Math.sin(delta) * Math.cos(brng)
        )
        val lambda2 = lambda1 + Math.atan2(
            Math.sin(brng) * Math.sin(delta) * Math.cos(phi1),
            Math.cos(delta) - Math.sin(phi1) * Math.sin(phi2)
        )
        return Math.toDegrees(phi2) to Math.toDegrees(lambda2)
    }

    fun clearThrow(discId: String) {
        viewModelScope.launch { repository.deleteThrowForDisc(roundId, discId) }
    }

    fun updateTarget(lat: Double, lng: Double) {
        viewModelScope.launch {
            repository.updateRoundTarget(roundId, lat, lng)
            _round.value = _round.value?.copy(targetLat = lat, targetLng = lng)
        }
    }

    fun beginPlacingLanding(discId: String) {
        _placingDiscId.value = discId
    }

    fun cancelPlacingLanding() {
        _placingDiscId.value = null
    }

    private fun throwId(discId: String): String = "$roundId:$discId"
}
