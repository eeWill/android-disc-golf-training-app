package com.eewill.discgolftraining.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eewill.discgolftraining.data.PuttingResult
import com.eewill.discgolftraining.data.PuttingRoundRepository
import com.eewill.discgolftraining.data.PuttingThrowStatsRow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class PuttingDistancePoint(
    val distanceFeet: Float,
    val made: Int,
    val attempted: Int,
) {
    val percent: Float get() = if (attempted == 0) 0f else made * 100f / attempted
}

data class PuttingStatsResult(
    val perDistance: List<PuttingDistancePoint>,
    val overallMade: Int,
    val overallAttempted: Int,
) {
    val overallPercent: Float
        get() = if (overallAttempted == 0) 0f else overallMade * 100f / overallAttempted
}

data class PuttingStatsFilters(
    val startDateMillis: Long?,
    val endDateMillis: Long?,
    val minDistanceFeet: Float?,
    val maxDistanceFeet: Float?,
    val applied: Boolean,
) {
    companion object {
        private const val THIRTY_DAYS_MILLIS = 30L * 24 * 60 * 60 * 1000

        fun default(nowMillis: Long = System.currentTimeMillis()): PuttingStatsFilters =
            PuttingStatsFilters(
                startDateMillis = nowMillis - THIRTY_DAYS_MILLIS,
                endDateMillis = nowMillis,
                minDistanceFeet = null,
                maxDistanceFeet = null,
                applied = false,
            )
    }
}

class PuttingStatsViewModel(repository: PuttingRoundRepository) : ViewModel() {

    private val _filters = MutableStateFlow(PuttingStatsFilters.default())
    val filters: StateFlow<PuttingStatsFilters> = _filters.asStateFlow()

    private val allStats: StateFlow<List<PuttingThrowStatsRow>> =
        repository.getAllPuttingThrowStats().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val result: StateFlow<PuttingStatsResult> =
        combine(allStats, _filters) { rows, f ->
            if (!f.applied) return@combine PuttingStatsResult(emptyList(), 0, 0)
            val filtered = rows.asSequence()
                .filter { f.startDateMillis == null || it.createdAt >= f.startDateMillis }
                .filter { f.endDateMillis == null || it.createdAt <= f.endDateMillis }
                .filter { f.minDistanceFeet == null || it.distanceFeet >= f.minDistanceFeet }
                .filter { f.maxDistanceFeet == null || it.distanceFeet <= f.maxDistanceFeet }
                .toList()

            val perDistance = filtered
                .groupBy { it.distanceFeet }
                .toSortedMap()
                .map { (distance, throws) ->
                    PuttingDistancePoint(
                        distanceFeet = distance,
                        made = throws.count { it.result == PuttingResult.MADE },
                        attempted = throws.size,
                    )
                }
            PuttingStatsResult(
                perDistance = perDistance,
                overallMade = perDistance.sumOf { it.made },
                overallAttempted = perDistance.sumOf { it.attempted },
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PuttingStatsResult(emptyList(), 0, 0),
        )

    fun updateDateRange(start: Long?, end: Long?) {
        _filters.update { it.copy(startDateMillis = start, endDateMillis = end, applied = false) }
    }

    fun updateDistanceRange(min: Float?, max: Float?) {
        _filters.update { it.copy(minDistanceFeet = min, maxDistanceFeet = max, applied = false) }
    }

    fun apply() {
        _filters.update { it.copy(applied = true) }
    }
}
