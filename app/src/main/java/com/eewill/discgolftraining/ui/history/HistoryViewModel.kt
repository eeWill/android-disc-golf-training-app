package com.eewill.discgolftraining.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eewill.discgolftraining.data.ApproachRoundRepository
import com.eewill.discgolftraining.data.ApproachRoundSummary
import com.eewill.discgolftraining.data.PuttingRoundRepository
import com.eewill.discgolftraining.data.PuttingRoundSummary
import com.eewill.discgolftraining.data.RoundRepository
import com.eewill.discgolftraining.data.RoundSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class HistoryMode { GAP, APPROACH, PUTTING }

class HistoryViewModel(
    private val roundRepository: RoundRepository,
    private val approachRepository: ApproachRoundRepository,
    private val puttingRepository: PuttingRoundRepository,
) : ViewModel() {

    private val _mode = MutableStateFlow(HistoryMode.GAP)
    val mode: StateFlow<HistoryMode> = _mode.asStateFlow()

    val gapRounds: StateFlow<List<RoundSummary>> =
        roundRepository.getAllRoundsWithCounts().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val approachRounds: StateFlow<List<ApproachRoundSummary>> =
        approachRepository.getAllApproachRoundSummaries().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val puttingRounds: StateFlow<List<PuttingRoundSummary>> =
        puttingRepository.getAllPuttingRoundSummaries().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun setMode(mode: HistoryMode) {
        _mode.value = mode
    }

    fun deleteGapRound(id: String) {
        viewModelScope.launch { roundRepository.deleteRound(id) }
    }

    fun deleteApproachRound(id: String) {
        viewModelScope.launch { approachRepository.deleteRound(id) }
    }

    fun deletePuttingRound(id: String) {
        viewModelScope.launch { puttingRepository.deleteRound(id) }
    }
}
