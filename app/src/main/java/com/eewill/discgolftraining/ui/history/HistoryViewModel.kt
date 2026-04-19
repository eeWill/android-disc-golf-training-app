package com.eewill.discgolftraining.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eewill.discgolftraining.data.RoundRepository
import com.eewill.discgolftraining.data.RoundSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: RoundRepository) : ViewModel() {
    val rounds: StateFlow<List<RoundSummary>> =
        repository.getAllRoundsWithCounts().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun deleteRound(id: String) {
        viewModelScope.launch { repository.deleteRound(id) }
    }
}
