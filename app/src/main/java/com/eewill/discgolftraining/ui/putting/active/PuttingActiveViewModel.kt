package com.eewill.discgolftraining.ui.putting.active

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

data class PuttingActiveState(
    val round: PuttingRoundEntity?,
    val positions: List<Float>,
    val results: Map<Pair<Int, Int>, PuttingResult>,
)

class PuttingActiveViewModel(
    private val roundId: String,
    private val repository: PuttingRoundRepository,
) : ViewModel() {

    private val _round = MutableStateFlow<PuttingRoundEntity?>(null)

    init {
        viewModelScope.launch { _round.value = repository.getRound(roundId) }
    }

    val state: StateFlow<PuttingActiveState> = combine(
        _round,
        repository.getRoundThrows(roundId),
    ) { round, throws ->
        val positions = round?.let {
            puttingPositions(it.minDistanceFeet, it.maxDistanceFeet, it.intervalFeet)
        }.orEmpty()
        val results = throws.associate { (it.positionIndex to it.throwIndex) to it.result }
        PuttingActiveState(round = round, positions = positions, results = results)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PuttingActiveState(null, emptyList(), emptyMap()),
    )

    fun onTap(positionIndex: Int, throwIndex: Int) {
        mark(positionIndex, throwIndex, PuttingResult.MADE)
    }

    fun onLongPress(positionIndex: Int, throwIndex: Int) {
        mark(positionIndex, throwIndex, PuttingResult.MISSED)
    }

    private fun mark(positionIndex: Int, throwIndex: Int, desired: PuttingResult) {
        val current = state.value
        val round = current.round ?: return
        val distance = current.positions.getOrNull(positionIndex) ?: return
        val existing = current.results[positionIndex to throwIndex]
        viewModelScope.launch {
            if (existing == desired) {
                repository.deleteThrowAt(roundId, positionIndex, throwIndex)
            } else {
                repository.upsertThrow(
                    PuttingThrowEntity(
                        id = PuttingThrowEntity.idFor(round.id, positionIndex, throwIndex),
                        roundId = round.id,
                        distanceFeet = distance,
                        positionIndex = positionIndex,
                        throwIndex = throwIndex,
                        result = desired,
                    )
                )
            }
        }
    }
}
