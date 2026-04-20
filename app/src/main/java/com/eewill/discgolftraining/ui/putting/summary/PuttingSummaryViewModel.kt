package com.eewill.discgolftraining.ui.putting.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eewill.discgolftraining.data.PuttingResult
import com.eewill.discgolftraining.data.PuttingRoundEntity
import com.eewill.discgolftraining.data.PuttingRoundRepository
import com.eewill.discgolftraining.data.PuttingThrowEntity
import com.eewill.discgolftraining.data.puttingPositions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PuttingDistanceStat(
    val distanceFeet: Float,
    val made: Int,
    val attempted: Int,
) {
    val percent: Float get() = if (attempted == 0) 0f else made * 100f / attempted
}

data class PuttingSummaryState(
    val round: PuttingRoundEntity?,
    val perDistance: List<PuttingDistanceStat>,
    val overall: PuttingDistanceStat,
)

class PuttingSummaryViewModel(
    private val roundId: String,
    private val repository: PuttingRoundRepository,
) : ViewModel() {

    private val _round = MutableStateFlow<PuttingRoundEntity?>(null)

    init {
        viewModelScope.launch { _round.value = repository.getRound(roundId) }
    }

    fun updateNotes(notes: String?) {
        viewModelScope.launch {
            repository.updateRoundNotes(roundId, notes)
            _round.value = _round.value?.copy(notes = notes)
        }
    }

    val state: StateFlow<PuttingSummaryState> = combine(
        _round,
        repository.getRoundThrows(roundId),
    ) { round, throws ->
        buildState(round, throws)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PuttingSummaryState(null, emptyList(), PuttingDistanceStat(0f, 0, 0)),
    )

    private fun buildState(
        round: PuttingRoundEntity?,
        throws: List<PuttingThrowEntity>,
    ): PuttingSummaryState {
        val positions = round?.let {
            puttingPositions(it.minDistanceFeet, it.maxDistanceFeet, it.intervalFeet)
        }.orEmpty()
        val perDistance = positions.mapIndexed { i, distance ->
            val rowThrows = throws.filter { it.positionIndex == i }
            PuttingDistanceStat(
                distanceFeet = distance,
                made = rowThrows.count { it.result == PuttingResult.MADE },
                attempted = rowThrows.size,
            )
        }
        val overall = PuttingDistanceStat(
            distanceFeet = 0f,
            made = perDistance.sumOf { it.made },
            attempted = perDistance.sumOf { it.attempted },
        )
        return PuttingSummaryState(round, perDistance, overall)
    }
}
