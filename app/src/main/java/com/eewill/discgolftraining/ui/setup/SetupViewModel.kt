package com.eewill.discgolftraining.ui.setup

import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eewill.discgolftraining.data.DiscDataMode
import com.eewill.discgolftraining.data.DiscEntity
import com.eewill.discgolftraining.data.DiscRepository
import com.eewill.discgolftraining.data.RoundEntity
import com.eewill.discgolftraining.data.RoundRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class SetupState(
    val imagePath: String? = null,
    val distanceFeet: String = "",
    val gapWidthFeet: String = "",
    val gapRect: Rect? = null,
    val discDataMode: DiscDataMode = DiscDataMode.NONE,
) {
    val distance: Float? get() = distanceFeet.toFloatOrNull()
    val width: Float? get() = gapWidthFeet.toFloatOrNull()

    fun canBegin(hasDiscs: Boolean): Boolean =
        imagePath != null &&
            (distance ?: 0f) > 0f &&
            (width ?: 0f) > 0f &&
            gapRect != null &&
            gapRect.width > 0f && gapRect.height > 0f &&
            (discDataMode != DiscDataMode.DISC || hasDiscs)

    companion object {
        fun fromRound(round: RoundEntity): SetupState = SetupState(
            imagePath = round.imagePath,
            distanceFeet = round.distanceFeet.trimmedString(),
            gapWidthFeet = round.gapWidthFeet.trimmedString(),
            gapRect = Rect(round.gapLeft, round.gapTop, round.gapRight, round.gapBottom),
            discDataMode = round.discDataMode,
        )
    }
}

private fun Float.trimmedString(): String =
    if (this % 1f == 0f) toInt().toString() else toString()

class SetupViewModel(
    private val repository: RoundRepository,
    discRepository: DiscRepository,
) : ViewModel() {

    val discs: StateFlow<List<DiscEntity>> =
        discRepository.getAllDiscs().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    suspend fun loadRound(id: String): RoundEntity? = repository.getRound(id)

    fun beginRound(state: SetupState, onReady: (String) -> Unit) {
        val imagePath = state.imagePath ?: return
        val distance = state.distance ?: return
        val width = state.width ?: return
        val rect = state.gapRect ?: return

        val id = UUID.randomUUID().toString()
        viewModelScope.launch {
            repository.insertRound(
                RoundEntity(
                    id = id,
                    createdAt = System.currentTimeMillis(),
                    imagePath = imagePath,
                    distanceFeet = distance,
                    gapWidthFeet = width,
                    gapLeft = rect.left,
                    gapTop = rect.top,
                    gapRight = rect.right,
                    gapBottom = rect.bottom,
                    discDataMode = state.discDataMode,
                )
            )
            onReady(id)
        }
    }
}
