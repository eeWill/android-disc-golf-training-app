package com.eewill.discgolftraining.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eewill.discgolftraining.data.RoundRepository
import com.eewill.discgolftraining.data.RoundStatsRow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class ChartPoint(
    val createdAt: Long,
    val hitPercent: Float,
)

data class MissDirectionPoint(
    val createdAt: Long,
    val leftPct: Float,
    val rightPct: Float,
    val highPct: Float,
    val lowPct: Float,
)

data class StatsFilters(
    val startDateMillis: Long?,
    val endDateMillis: Long?,
    val minDistanceFeet: Float?,
    val maxDistanceFeet: Float?,
    val minGapWidthFeet: Float?,
    val maxGapWidthFeet: Float?,
    val applied: Boolean,
) {
    companion object {
        private const val THIRTY_DAYS_MILLIS = 30L * 24 * 60 * 60 * 1000

        fun default(nowMillis: Long = System.currentTimeMillis()): StatsFilters = StatsFilters(
            startDateMillis = nowMillis - THIRTY_DAYS_MILLIS,
            endDateMillis = nowMillis,
            minDistanceFeet = null,
            maxDistanceFeet = null,
            minGapWidthFeet = null,
            maxGapWidthFeet = null,
            applied = false,
        )
    }
}

class StatsViewModel(repository: RoundRepository) : ViewModel() {

    private val _filters = MutableStateFlow(StatsFilters.default())
    val filters: StateFlow<StatsFilters> = _filters.asStateFlow()

    private val allRounds: StateFlow<List<RoundStatsRow>> =
        repository.getAllRoundStats().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    private val filteredRounds: StateFlow<List<RoundStatsRow>> =
        combine(allRounds, _filters) { rounds, f ->
            if (!f.applied) emptyList()
            else rounds.asSequence()
                .filter { (it.hits + it.misses) > 0 }
                .filter { f.startDateMillis == null || it.createdAt >= f.startDateMillis }
                .filter { f.endDateMillis == null || it.createdAt <= f.endDateMillis }
                .filter { f.minDistanceFeet == null || it.distanceFeet >= f.minDistanceFeet }
                .filter { f.maxDistanceFeet == null || it.distanceFeet <= f.maxDistanceFeet }
                .filter { f.minGapWidthFeet == null || it.gapWidthFeet >= f.minGapWidthFeet }
                .filter { f.maxGapWidthFeet == null || it.gapWidthFeet <= f.maxGapWidthFeet }
                .toList()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val filteredPoints: StateFlow<List<ChartPoint>> =
        filteredRounds.map { rounds ->
            rounds.map {
                val total = it.hits + it.misses
                ChartPoint(
                    createdAt = it.createdAt,
                    hitPercent = it.hits * 100f / total,
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val missDirectionPoints: StateFlow<List<MissDirectionPoint>> =
        filteredRounds.map { rounds ->
            rounds.mapNotNull {
                if (it.misses <= 0) return@mapNotNull null
                val denom = it.misses.toFloat()
                MissDirectionPoint(
                    createdAt = it.createdAt,
                    leftPct = it.missLeft * 100f / denom,
                    rightPct = it.missRight * 100f / denom,
                    highPct = it.missHigh * 100f / denom,
                    lowPct = it.missLow * 100f / denom,
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun updateDateRange(start: Long?, end: Long?) {
        _filters.update { it.copy(startDateMillis = start, endDateMillis = end, applied = false) }
    }

    fun updateDistanceRange(min: Float?, max: Float?) {
        _filters.update { it.copy(minDistanceFeet = min, maxDistanceFeet = max, applied = false) }
    }

    fun updateGapWidthRange(min: Float?, max: Float?) {
        _filters.update { it.copy(minGapWidthFeet = min, maxGapWidthFeet = max, applied = false) }
    }

    fun apply() {
        _filters.update { it.copy(applied = true) }
    }
}
