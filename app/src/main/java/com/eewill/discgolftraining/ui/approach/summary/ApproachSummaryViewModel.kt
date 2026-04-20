package com.eewill.discgolftraining.ui.approach.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eewill.discgolftraining.data.ApproachRoundEntity
import com.eewill.discgolftraining.data.ApproachRoundRepository
import com.eewill.discgolftraining.data.ApproachThrowEntity
import com.eewill.discgolftraining.data.DiscEntity
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DiscLanding(
    val disc: DiscEntity,
    val distanceFeet: Float,
    val latLng: LatLng? = null,
)

data class DistanceBin(
    val label: String,
    val count: Int,
)

data class ApproachSummaryState(
    val round: ApproachRoundEntity?,
    val totalThrows: Int,
    val landings: List<DiscLanding>,
    val histogram: List<DistanceBin>,
)

private fun binIndexFor(distanceFeet: Float): Int =
    if (distanceFeet < 10f) 0 else 1 + ((distanceFeet - 10f) / 5f).toInt().coerceAtLeast(0)

private fun binLabel(index: Int): String =
    if (index == 0) "0-10" else {
        val lo = 10 + (index - 1) * 5
        "$lo-${lo + 5}"
    }

class ApproachSummaryViewModel(
    private val roundId: String,
    private val repository: ApproachRoundRepository,
) : ViewModel() {

    private val _round = MutableStateFlow<ApproachRoundEntity?>(null)

    init {
        viewModelScope.launch { _round.value = repository.getRound(roundId) }
    }

    fun updateNotes(notes: String?) {
        viewModelScope.launch {
            repository.updateRoundNotes(roundId, notes)
            _round.value = _round.value?.copy(notes = notes)
        }
    }

    val state: StateFlow<ApproachSummaryState> = combine(
        _round,
        repository.getRoundDiscs(roundId),
        repository.getRoundThrows(roundId),
    ) { round, discs, throws ->
        buildState(round, discs, throws)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ApproachSummaryState(null, 0, emptyList(), emptyList()),
    )

    private fun buildState(
        round: ApproachRoundEntity?,
        discs: List<DiscEntity>,
        throws: List<ApproachThrowEntity>,
    ): ApproachSummaryState {
        val landings = discs.mapNotNull { disc ->
            val t = throws.firstOrNull { it.discId == disc.id } ?: return@mapNotNull null
            val lat = t.landingLat
            val lng = t.landingLng
            DiscLanding(
                disc = disc,
                distanceFeet = t.landingDistanceFeet,
                latLng = if (lat != null && lng != null) LatLng(lat, lng) else null,
            )
        }
        val histogram = if (landings.isEmpty()) emptyList() else {
            val binCounts = mutableMapOf<Int, Int>()
            landings.forEach { binCounts.merge(binIndexFor(it.distanceFeet), 1, Int::plus) }
            val maxBin = binCounts.keys.max()
            (0..maxBin).map { i -> DistanceBin(binLabel(i), binCounts[i] ?: 0) }
        }
        return ApproachSummaryState(
            round = round,
            totalThrows = landings.size,
            landings = landings,
            histogram = histogram,
        )
    }
}
