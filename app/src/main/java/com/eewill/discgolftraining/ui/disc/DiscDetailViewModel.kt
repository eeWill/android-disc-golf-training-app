package com.eewill.discgolftraining.ui.disc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eewill.discgolftraining.data.ApproachRoundRepository
import com.eewill.discgolftraining.data.DiscEntity
import com.eewill.discgolftraining.data.DiscRepository
import com.eewill.discgolftraining.data.RoundRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MissDirPct(val left: Float, val right: Float, val high: Float, val low: Float)

data class DiscDetailUiState(
    val disc: DiscEntity? = null,
    val gapThrowCount: Int = 0,
    val gapHitPct: Float? = null,
    val missDir: MissDirPct? = null,
    val approachThrowCount: Int = 0,
    val approachAvgDistFt: Float? = null,
    val isSaving: Boolean = false,
    val nameError: String? = null,
)

class DiscDetailViewModel(
    private val discId: String,
    private val discRepo: DiscRepository,
    roundRepo: RoundRepository,
    approachRepo: ApproachRoundRepository,
) : ViewModel() {
    private val isSaving = MutableStateFlow(false)
    private val nameError = MutableStateFlow<String?>(null)

    val state: StateFlow<DiscDetailUiState> = combine(
        discRepo.observeDisc(discId),
        roundRepo.getGapStatsForDisc(discId),
        approachRepo.getApproachStatsForDisc(discId),
        isSaving,
        nameError,
    ) { disc, gap, approach, saving, err ->
        val gapTotal = gap.hits + gap.misses
        val hitPct = if (gapTotal > 0) gap.hits * 100f / gapTotal else null
        val dirTotal = gap.missLeft + gap.missRight + gap.missHigh + gap.missLow
        val missDir = if (dirTotal > 0) {
            MissDirPct(
                left = gap.missLeft * 100f / dirTotal,
                right = gap.missRight * 100f / dirTotal,
                high = gap.missHigh * 100f / dirTotal,
                low = gap.missLow * 100f / dirTotal,
            )
        } else null

        DiscDetailUiState(
            disc = disc,
            gapThrowCount = gapTotal,
            gapHitPct = hitPct,
            missDir = missDir,
            approachThrowCount = approach.throwCount,
            approachAvgDistFt = approach.avgDistanceFeet,
            isSaving = saving,
            nameError = err,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DiscDetailUiState(),
    )

    fun onNameChange() {
        if (nameError.value != null) nameError.value = null
    }

    fun saveName(newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) {
            nameError.value = "Name cannot be empty"
            return
        }
        val current = state.value.disc ?: return
        if (trimmed == current.name) return
        viewModelScope.launch {
            isSaving.value = true
            discRepo.updateDisc(current.copy(name = trimmed))
            isSaving.value = false
        }
    }
}
