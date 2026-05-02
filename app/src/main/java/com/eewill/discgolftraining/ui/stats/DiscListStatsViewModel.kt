package com.eewill.discgolftraining.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eewill.discgolftraining.data.ApproachRoundRepository
import com.eewill.discgolftraining.data.DiscEntity
import com.eewill.discgolftraining.data.DiscRepository
import com.eewill.discgolftraining.data.DiscType
import com.eewill.discgolftraining.data.RoundRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DiscListStatsItem(
    val disc: DiscEntity,
    val gapThrowCount: Int,
    val gapHitPct: Float?,
    val approachThrowCount: Int,
    val approachAvgDistFt: Float?,
)

class DiscListStatsViewModel(
    discRepo: DiscRepository,
    roundRepo: RoundRepository,
    approachRepo: ApproachRoundRepository,
) : ViewModel() {
    private val _typeFilter = MutableStateFlow<DiscType?>(null)
    val typeFilter: StateFlow<DiscType?> = _typeFilter.asStateFlow()

    val items: StateFlow<List<DiscListStatsItem>> = combine(
        discRepo.getDiscsForStats(),
        roundRepo.getGapStatsGroupedByDisc(),
        approachRepo.getApproachStatsGroupedByDisc(),
        _typeFilter,
    ) { discs, gapRows, approachRows, filter ->
        val gapById = gapRows.associateBy { it.discId }
        val approachById = approachRows.associateBy { it.discId }
        discs
            .filter { filter == null || it.type == filter }
            .map { disc ->
                val gap = gapById[disc.id]
                val approach = approachById[disc.id]
                val gapTotal = (gap?.hits ?: 0) + (gap?.misses ?: 0)
                val hitPct = if (gapTotal > 0) (gap!!.hits * 100f / gapTotal) else null
                DiscListStatsItem(
                    disc = disc,
                    gapThrowCount = gapTotal,
                    gapHitPct = hitPct,
                    approachThrowCount = approach?.throwCount ?: 0,
                    approachAvgDistFt = approach?.avgDistanceFeet,
                )
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun setTypeFilter(type: DiscType?) {
        _typeFilter.value = type
    }
}
