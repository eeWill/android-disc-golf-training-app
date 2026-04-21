package com.eewill.discgolftraining.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eewill.discgolftraining.data.DiscEntity
import com.eewill.discgolftraining.data.DiscRepository
import com.eewill.discgolftraining.data.RoundRepository
import com.eewill.discgolftraining.data.RoundWithThrows
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SummaryUiState(
    val roundWithThrows: RoundWithThrows? = null,
    val discsUsed: List<DiscEntity> = emptyList(),
    val shortDiscIds: Set<String> = emptySet(),
)

class SummaryViewModel(
    private val roundId: String,
    private val repository: RoundRepository,
    discRepository: DiscRepository,
) : ViewModel() {
    val state: StateFlow<SummaryUiState> = combine(
        repository.getRoundWithThrows(roundId),
        discRepository.getAllDiscs(),
        repository.getShortDiscIds(roundId),
    ) { roundWithThrows, allDiscs, shortIds ->
        val usedIds = roundWithThrows?.throws?.mapNotNull { it.discId }?.toSet().orEmpty()
        val discsUsed = allDiscs.filter { it.id in usedIds }
        SummaryUiState(
            roundWithThrows = roundWithThrows,
            discsUsed = discsUsed,
            shortDiscIds = shortIds.toSet(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SummaryUiState(),
    )

    fun updateNotes(notes: String?) {
        viewModelScope.launch { repository.updateNotes(roundId, notes) }
    }

    fun setDiscShort(discId: String, short: Boolean) {
        viewModelScope.launch { repository.setDiscShort(roundId, discId, short) }
    }
}
