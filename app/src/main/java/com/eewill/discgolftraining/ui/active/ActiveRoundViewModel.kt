package com.eewill.discgolftraining.ui.active

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eewill.discgolftraining.data.DiscDataMode
import com.eewill.discgolftraining.data.DiscEntity
import com.eewill.discgolftraining.data.DiscRepository
import com.eewill.discgolftraining.data.DiscType
import com.eewill.discgolftraining.data.FlightModifier
import com.eewill.discgolftraining.data.RoundRepository
import com.eewill.discgolftraining.data.RoundWithThrows
import com.eewill.discgolftraining.data.ThrowEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class ActiveRoundViewModel(
    private val roundId: String,
    private val repository: RoundRepository,
    discRepository: DiscRepository,
) : ViewModel() {

    val state: StateFlow<RoundWithThrows?> =
        repository.getRoundWithThrows(roundId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    val discs: StateFlow<List<DiscEntity>> =
        combine(repository.getRoundDiscs(roundId), discRepository.getActiveDiscs()) { selected, active ->
            if (selected.isNotEmpty()) selected else active
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    private val _typeFilter = MutableStateFlow<DiscType?>(null)
    val typeFilter: StateFlow<DiscType?> = _typeFilter.asStateFlow()

    val visibleDiscs: StateFlow<List<DiscEntity>> =
        combine(discs, _typeFilter) { list, filter ->
            if (filter == null) list else list.filter { it.type == filter }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    private val _currentType = MutableStateFlow(DiscType.PUTTER)
    val currentType: StateFlow<DiscType> = _currentType.asStateFlow()

    private val _currentDiscId = MutableStateFlow<String?>(null)
    val currentDiscId: StateFlow<String?> = _currentDiscId.asStateFlow()

    private val _pendingFlightMod = MutableStateFlow<FlightModifier?>(null)
    val pendingFlightMod: StateFlow<FlightModifier?> = _pendingFlightMod.asStateFlow()

    fun selectType(type: DiscType) = _currentType.update { type }

    fun selectDisc(id: String) = _currentDiscId.update { id }

    fun setTypeFilter(type: DiscType?) = _typeFilter.update { type }

    fun togglePendingFlightMod(mod: FlightModifier) {
        _pendingFlightMod.update { if (it == mod) null else mod }
    }

    /** Ensure the current disc selection is valid given the latest visible discs list. */
    fun ensureDiscSelection(discs: List<DiscEntity>) {
        if (discs.isEmpty()) {
            _currentDiscId.update { null }
            return
        }
        val current = _currentDiscId.value
        if (current == null || discs.none { it.id == current }) {
            _currentDiscId.update { discs.first().id }
        }
    }

    fun recordThrow(normalized: Offset) {
        val current = state.value ?: return
        val rect = current.round.gapRect()
        val isHit = rect.contains(normalized)
        val nextIndex = (current.throws.maxOfOrNull { it.index } ?: -1) + 1

        val (throwDiscType, throwDiscId) = when (current.round.discDataMode) {
            DiscDataMode.NONE -> null to null
            DiscDataMode.TYPE -> _currentType.value to null
            DiscDataMode.DISC -> {
                val selectedId = _currentDiscId.value
                val disc = discs.value.firstOrNull { it.id == selectedId }
                disc?.type to disc?.id
            }
        }

        if (current.round.discDataMode == DiscDataMode.DISC) {
            val list = visibleDiscs.value
            if (list.isNotEmpty()) {
                val currentIdx = list.indexOfFirst { it.id == _currentDiscId.value }
                val nextIdx = if (currentIdx < 0) 0 else (currentIdx + 1) % list.size
                _currentDiscId.update { list[nextIdx].id }
            }
        }

        val flightMod = _pendingFlightMod.value
        _pendingFlightMod.value = null

        viewModelScope.launch {
            repository.insertThrow(
                ThrowEntity(
                    id = UUID.randomUUID().toString(),
                    roundId = roundId,
                    index = nextIndex,
                    x = normalized.x,
                    y = normalized.y,
                    isHit = isHit,
                    discType = throwDiscType,
                    discId = throwDiscId,
                    flightModifier = flightMod,
                )
            )
        }
    }

    fun deleteThrow(throwId: String) {
        viewModelScope.launch { repository.deleteThrow(throwId) }
    }

    fun undoLast() {
        val current = state.value ?: return
        val lastThrow = current.throws.maxByOrNull { it.index }
        if (current.round.discDataMode == DiscDataMode.DISC && lastThrow?.discId != null) {
            _currentDiscId.value = lastThrow.discId
        }
        viewModelScope.launch { repository.deleteLastThrow(roundId) }
    }
}

fun com.eewill.discgolftraining.data.RoundEntity.gapRect(): Rect =
    Rect(gapLeft, gapTop, gapRight, gapBottom)
