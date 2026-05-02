package com.eewill.discgolftraining.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eewill.discgolftraining.data.DiscEntity
import com.eewill.discgolftraining.data.DiscRepository
import com.eewill.discgolftraining.data.DiscType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class SettingsViewModel(private val repository: DiscRepository) : ViewModel() {
    val discs: StateFlow<List<DiscEntity>> =
        repository.getAllDiscs().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun addDisc(name: String, type: DiscType) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val nextOrder = (discs.value.maxOfOrNull { it.sortOrder } ?: 0L) + 1L
            repository.insertDisc(
                DiscEntity(
                    id = UUID.randomUUID().toString(),
                    name = trimmed,
                    type = type,
                    createdAt = now,
                    sortOrder = nextOrder,
                )
            )
        }
    }

    fun deleteDisc(id: String) {
        viewModelScope.launch { repository.deleteDisc(id) }
    }

    fun setActive(id: String, active: Boolean) {
        val disc = discs.value.firstOrNull { it.id == id } ?: return
        if (disc.isActive == active) return
        viewModelScope.launch { repository.updateDisc(disc.copy(isActive = active)) }
    }

    fun setIncludeInStats(id: String, include: Boolean) {
        val disc = discs.value.firstOrNull { it.id == id } ?: return
        if (disc.includeInStats == include) return
        viewModelScope.launch { repository.updateDisc(disc.copy(includeInStats = include)) }
    }

    fun moveUp(id: String) = swapAdjacent(id, offset = -1)

    fun moveDown(id: String) = swapAdjacent(id, offset = +1)

    private fun swapAdjacent(id: String, offset: Int) {
        val list = discs.value
        val idx = list.indexOfFirst { it.id == id }
        val other = idx + offset
        if (idx < 0 || other !in list.indices) return
        val a = list[idx]
        val b = list[other]
        viewModelScope.launch {
            repository.updateDisc(a.copy(sortOrder = b.sortOrder))
            repository.updateDisc(b.copy(sortOrder = a.sortOrder))
        }
    }
}
