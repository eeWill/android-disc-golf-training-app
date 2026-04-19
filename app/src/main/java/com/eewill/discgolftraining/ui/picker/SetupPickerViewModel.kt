package com.eewill.discgolftraining.ui.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eewill.discgolftraining.data.RoundEntity
import com.eewill.discgolftraining.data.RoundRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SetupPickerViewModel(repository: RoundRepository) : ViewModel() {
    val rounds: StateFlow<List<RoundEntity>> =
        repository.getAllRounds().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )
}
