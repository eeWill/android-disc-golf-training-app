package com.eewill.discgolftraining.ui.replay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eewill.discgolftraining.data.RoundRepository
import com.eewill.discgolftraining.data.RoundWithThrows
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ReplayViewModel(
    roundId: String,
    repository: RoundRepository,
) : ViewModel() {
    val state: StateFlow<RoundWithThrows?> =
        repository.getRoundWithThrows(roundId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )
}
