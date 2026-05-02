package com.eewill.discgolftraining.ui.approach.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eewill.discgolftraining.data.ApproachRoundRepository
import com.eewill.discgolftraining.data.DiscEntity
import com.eewill.discgolftraining.data.DiscRepository
import com.google.android.gms.maps.model.LatLng
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ApproachSetupViewModel(
    private val approachRepo: ApproachRoundRepository,
    discRepo: DiscRepository,
) : ViewModel() {

    val discs: StateFlow<List<DiscEntity>> =
        discRepo.getActiveDiscs().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    private val _selectedDiscIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedDiscIds: StateFlow<Set<String>> = _selectedDiscIds.asStateFlow()

    private val _pendingTarget = MutableStateFlow<LatLng?>(null)
    val pendingTarget: StateFlow<LatLng?> = _pendingTarget.asStateFlow()

    private val _pendingStart = MutableStateFlow<LatLng?>(null)
    val pendingStart: StateFlow<LatLng?> = _pendingStart.asStateFlow()

    fun toggleDisc(id: String) {
        _selectedDiscIds.value = _selectedDiscIds.value.toMutableSet().also {
            if (!it.add(id)) it.remove(id)
        }
    }

    fun setSelection(ids: Collection<String>, selected: Boolean) {
        _selectedDiscIds.value = _selectedDiscIds.value.toMutableSet().also {
            if (selected) it.addAll(ids) else it.removeAll(ids.toSet())
        }
    }

    fun setPendingTarget(latLng: LatLng?) {
        _pendingTarget.value = latLng
    }

    fun setPendingStart(latLng: LatLng?) {
        _pendingStart.value = latLng
    }

    fun beginRound(
        targetDistanceFeet: Float,
        targetSizeFeet: Float?,
        onReady: (String) -> Unit,
    ) {
        val orderedIds = discs.value
            .filter { it.id in _selectedDiscIds.value }
            .map { it.id }
        if (orderedIds.isEmpty()) return
        val id = UUID.randomUUID().toString()
        val target = _pendingTarget.value
        val start = _pendingStart.value
        viewModelScope.launch {
            approachRepo.createRound(
                id = id,
                targetDistanceFeet = targetDistanceFeet,
                discIds = orderedIds,
                targetLat = target?.latitude,
                targetLng = target?.longitude,
                targetSizeFeet = targetSizeFeet,
                startLat = start?.latitude,
                startLng = start?.longitude,
            )
            onReady(id)
        }
    }
}
