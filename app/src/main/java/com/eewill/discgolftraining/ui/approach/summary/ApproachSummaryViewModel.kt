package com.eewill.discgolftraining.ui.approach.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eewill.discgolftraining.data.ApproachRoundEntity
import com.eewill.discgolftraining.data.ApproachRoundRepository
import com.eewill.discgolftraining.data.ApproachThrowEntity
import com.eewill.discgolftraining.data.DiscEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LandingZone(val label: String, val range: String) {
    TAP_IN("Tap-in", "0–11 ft"),
    C1X("C1X", "11–33 ft"),
    C2("C2", "33+ ft");

    companion object {
        fun of(distanceFeet: Float): LandingZone = when {
            distanceFeet < 11f -> TAP_IN
            distanceFeet < 33f -> C1X
            else -> C2
        }
    }
}

data class DiscLanding(
    val disc: DiscEntity,
    val distanceFeet: Float,
    val zone: LandingZone,
)

data class ApproachSummaryState(
    val round: ApproachRoundEntity?,
    val totalThrows: Int,
    val zoneCounts: Map<LandingZone, Int>,
    val landings: List<DiscLanding>,
)

class ApproachSummaryViewModel(
    roundId: String,
    private val repository: ApproachRoundRepository,
) : ViewModel() {

    private val _round = MutableStateFlow<ApproachRoundEntity?>(null)

    init {
        viewModelScope.launch { _round.value = repository.getRound(roundId) }
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
        initialValue = ApproachSummaryState(null, 0, emptyMap(), emptyList()),
    )

    private fun buildState(
        round: ApproachRoundEntity?,
        discs: List<DiscEntity>,
        throws: List<ApproachThrowEntity>,
    ): ApproachSummaryState {
        val discById = discs.associateBy { it.id }
        val landings = discs.mapNotNull { disc ->
            val t = throws.firstOrNull { it.discId == disc.id } ?: return@mapNotNull null
            DiscLanding(
                disc = disc,
                distanceFeet = t.landingDistanceFeet,
                zone = LandingZone.of(t.landingDistanceFeet),
            )
        }
        val counts = LandingZone.entries.associateWith { z -> landings.count { it.zone == z } }
        return ApproachSummaryState(
            round = round,
            totalThrows = landings.size,
            zoneCounts = counts,
            landings = landings,
        )
    }
}
