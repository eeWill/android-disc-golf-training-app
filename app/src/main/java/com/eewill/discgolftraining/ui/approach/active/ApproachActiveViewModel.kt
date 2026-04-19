package com.eewill.discgolftraining.ui.approach.active

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eewill.discgolftraining.data.ApproachRoundEntity
import com.eewill.discgolftraining.data.ApproachRoundRepository
import com.eewill.discgolftraining.data.ApproachThrowEntity
import com.eewill.discgolftraining.data.DiscEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ApproachActiveViewModel(
    private val roundId: String,
    private val repository: ApproachRoundRepository,
) : ViewModel() {

    val round: StateFlow<ApproachRoundEntity?> = MutableStateFlow<ApproachRoundEntity?>(null).also {
        viewModelScope.launch { it.value = repository.getRound(roundId) }
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

    fun setThrow(discId: String, distanceFeet: Float) {
        val position = discs.value.indexOfFirst { it.id == discId }.coerceAtLeast(0)
        viewModelScope.launch {
            repository.insertThrow(
                ApproachThrowEntity(
                    id = throwId(discId),
                    roundId = roundId,
                    index = position,
                    discId = discId,
                    landingDistanceFeet = distanceFeet,
                )
            )
        }
    }

    fun clearThrow(discId: String) {
        viewModelScope.launch { repository.deleteThrowForDisc(roundId, discId) }
    }

    private fun throwId(discId: String): String = "$roundId:$discId"
}
