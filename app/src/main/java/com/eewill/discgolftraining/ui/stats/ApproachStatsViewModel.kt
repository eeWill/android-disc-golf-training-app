package com.eewill.discgolftraining.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eewill.discgolftraining.data.ApproachRoundRepository
import com.eewill.discgolftraining.data.ApproachThrowStatsRow
import com.eewill.discgolftraining.data.DiscType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class ApproachStatsPoint(
    val createdAt: Long,
    val avgDistance: Float,
)

data class ApproachStatsFilters(
    val startDateMillis: Long?,
    val endDateMillis: Long?,
    val includedDiscTypes: Set<DiscType>,
    val minTargetDistanceFeet: Float?,
    val maxTargetDistanceFeet: Float?,
    val applied: Boolean,
) {
    companion object {
        private const val THIRTY_DAYS_MILLIS = 30L * 24 * 60 * 60 * 1000

        fun default(nowMillis: Long = System.currentTimeMillis()): ApproachStatsFilters =
            ApproachStatsFilters(
                startDateMillis = nowMillis - THIRTY_DAYS_MILLIS,
                endDateMillis = nowMillis,
                includedDiscTypes = DiscType.entries.toSet(),
                minTargetDistanceFeet = null,
                maxTargetDistanceFeet = null,
                applied = false,
            )
    }
}

class ApproachStatsViewModel(repository: ApproachRoundRepository) : ViewModel() {

    private val _filters = MutableStateFlow(ApproachStatsFilters.default())
    val filters: StateFlow<ApproachStatsFilters> = _filters.asStateFlow()

    private val allStats: StateFlow<List<ApproachThrowStatsRow>> =
        repository.getAllApproachThrowStats().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val points: StateFlow<List<ApproachStatsPoint>> =
        combine(allStats, _filters) { rows, f ->
            if (!f.applied) return@combine emptyList()
            rows.asSequence()
                .filter { f.startDateMillis == null || it.createdAt >= f.startDateMillis }
                .filter { f.endDateMillis == null || it.createdAt <= f.endDateMillis }
                .filter { it.discType != null && it.discType in f.includedDiscTypes }
                .filter { f.minTargetDistanceFeet == null || it.targetDistanceFeet >= f.minTargetDistanceFeet }
                .filter { f.maxTargetDistanceFeet == null || it.targetDistanceFeet <= f.maxTargetDistanceFeet }
                .groupBy { it.roundId }
                .mapNotNull { (_, throws) ->
                    if (throws.isEmpty()) return@mapNotNull null
                    val createdAt = throws.first().createdAt
                    val avgDist = throws.map { it.distanceFeet }.average().toFloat()
                    ApproachStatsPoint(createdAt = createdAt, avgDistance = avgDist)
                }
                .sortedBy { it.createdAt }
                .toList()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun updateDateRange(start: Long?, end: Long?) {
        _filters.update { it.copy(startDateMillis = start, endDateMillis = end, applied = false) }
    }

    fun updateDistanceRange(min: Float?, max: Float?) {
        _filters.update { it.copy(minTargetDistanceFeet = min, maxTargetDistanceFeet = max, applied = false) }
    }

    fun toggleDiscType(type: DiscType) {
        _filters.update {
            val s = it.includedDiscTypes.toMutableSet()
            if (!s.add(type)) s.remove(type)
            it.copy(includedDiscTypes = s, applied = false)
        }
    }

    fun apply() {
        _filters.update { it.copy(applied = true) }
    }
}
