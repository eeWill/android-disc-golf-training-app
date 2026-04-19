package com.eewill.discgolftraining.ui.approach.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eewill.discgolftraining.data.ApproachRoundRepository
import com.eewill.discgolftraining.data.DiscEntity
import com.eewill.discgolftraining.data.DiscRepository
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
        discRepo.getAllDiscs().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    private val _selectedDiscIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedDiscIds: StateFlow<Set<String>> = _selectedDiscIds.asStateFlow()

    fun toggleDisc(id: String) {
        _selectedDiscIds.value = _selectedDiscIds.value.toMutableSet().also {
            if (!it.add(id)) it.remove(id)
        }
    }

    fun beginRound(targetDistanceFeet: Float, onReady: (String) -> Unit) {
        val orderedIds = discs.value
            .filter { it.id in _selectedDiscIds.value }
            .map { it.id }
        if (orderedIds.isEmpty()) return
        val id = UUID.randomUUID().toString()
        viewModelScope.launch {
            approachRepo.createRound(id, targetDistanceFeet, orderedIds)
            onReady(id)
        }
    }
}
