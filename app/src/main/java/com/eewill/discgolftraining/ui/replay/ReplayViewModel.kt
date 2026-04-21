package com.eewill.discgolftraining.ui.replay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eewill.discgolftraining.data.RoundRepository
import com.eewill.discgolftraining.data.RoundWithThrows
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReplayViewModel(
    private val roundId: String,
    private val repository: RoundRepository,
) : ViewModel() {
    val state: StateFlow<RoundWithThrows?> =
        repository.getRoundWithThrows(roundId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    fun updateNotes(notes: String?) {
        viewModelScope.launch { repository.updateNotes(roundId, notes) }
    }
}
