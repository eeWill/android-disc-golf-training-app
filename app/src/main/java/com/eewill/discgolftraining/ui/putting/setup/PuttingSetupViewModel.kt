package com.eewill.discgolftraining.ui.putting.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eewill.discgolftraining.data.PuttingRoundRepository
import java.util.UUID
import kotlinx.coroutines.launch

class PuttingSetupViewModel(
    private val repository: PuttingRoundRepository,
) : ViewModel() {

    fun beginRound(
        minDistanceFeet: Float,
        maxDistanceFeet: Float,
        intervalFeet: Float,
        throwsPerPosition: Int,
        onReady: (String) -> Unit,
    ) {
        val id = UUID.randomUUID().toString()
        viewModelScope.launch {
            repository.createRound(
                id = id,
                minDistanceFeet = minDistanceFeet,
                maxDistanceFeet = maxDistanceFeet,
                intervalFeet = intervalFeet,
                throwsPerPosition = throwsPerPosition,
            )
            onReady(id)
        }
    }
}
