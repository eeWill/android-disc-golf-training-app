package com.eewill.discgolftraining.ui.setup

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
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
    val minDistanceFeet: String = "",
    val gapRect: Rect? = null,
    val discDataMode: DiscDataMode = DiscDataMode.NONE,
    val selectedDiscIds: Set<String> = emptySet(),
) {
    val distance: Float? get() = distanceFeet.toFloatOrNull()
    val width: Float? get() = gapWidthFeet.toFloatOrNull()
    val minDistance: Float? get() = minDistanceFeet.toFloatOrNull()

    fun canBegin(hasDiscs: Boolean): Boolean =
        imagePath != null &&
            (distance ?: 0f) > 0f &&
            (width ?: 0f) > 0f &&
            gapRect != null &&
            gapRect.width > 0f && gapRect.height > 0f &&
            (minDistanceFeet.isBlank() || (minDistance ?: 0f) > 0f) &&
            (discDataMode != DiscDataMode.DISC || (hasDiscs && selectedDiscIds.isNotEmpty()))

    companion object {
        fun fromRound(round: RoundEntity, selectedDiscIds: Set<String> = emptySet()): SetupState = SetupState(
            imagePath = round.imagePath,
            distanceFeet = round.distanceFeet.trimmedString(),
            gapWidthFeet = round.gapWidthFeet.trimmedString(),
            minDistanceFeet = round.minDistanceFeet?.trimmedString().orEmpty(),
            gapRect = Rect(round.gapLeft, round.gapTop, round.gapRight, round.gapBottom),
            discDataMode = round.discDataMode,
            selectedDiscIds = selectedDiscIds,
        )
    }
}

private fun Float.trimmedString(): String =
    if (this % 1f == 0f) toInt().toString() else toString()

val SetupStateSaver: Saver<SetupState, Any> = mapSaver(
    save = { s ->
        mapOf(
            "imagePath" to s.imagePath,
            "distanceFeet" to s.distanceFeet,
            "gapWidthFeet" to s.gapWidthFeet,
            "minDistanceFeet" to s.minDistanceFeet,
            "gapLeft" to s.gapRect?.left,
            "gapTop" to s.gapRect?.top,
            "gapRight" to s.gapRect?.right,
            "gapBottom" to s.gapRect?.bottom,
            "discDataMode" to s.discDataMode.name,
            "selectedDiscIds" to ArrayList(s.selectedDiscIds),
        )
    },
    restore = { m ->
        val left = m["gapLeft"] as Float?
        val rect = if (left != null) Rect(
            left,
            m["gapTop"] as Float,
            m["gapRight"] as Float,
            m["gapBottom"] as Float,
        ) else null
        @Suppress("UNCHECKED_CAST")
        val ids = (m["selectedDiscIds"] as? List<String>)?.toSet() ?: emptySet()
        SetupState(
            imagePath = m["imagePath"] as String?,
            distanceFeet = m["distanceFeet"] as String,
            gapWidthFeet = m["gapWidthFeet"] as String,
            minDistanceFeet = m["minDistanceFeet"] as String,
            gapRect = rect,
            discDataMode = (m["discDataMode"] as? String)
                ?.let { runCatching { DiscDataMode.valueOf(it) }.getOrNull() }
                ?: DiscDataMode.NONE,
            selectedDiscIds = ids,
        )
    },
)

class SetupViewModel(
    private val repository: RoundRepository,
    discRepository: DiscRepository,
) : ViewModel() {

    val discs: StateFlow<List<DiscEntity>> =
        discRepository.getActiveDiscs().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    suspend fun loadRound(id: String): RoundEntity? = repository.getRound(id)

    suspend fun loadRoundDiscIds(id: String): List<String> = repository.getRoundDiscIdsOnce(id)

    fun beginRound(state: SetupState, onReady: (String) -> Unit) {
        val imagePath = state.imagePath ?: return
        val distance = state.distance ?: return
        val width = state.width ?: return
        val rect = state.gapRect ?: return

        val id = UUID.randomUUID().toString()
        val orderedDiscIds = if (state.discDataMode == DiscDataMode.DISC) {
            discs.value
                .filter { it.id in state.selectedDiscIds }
                .map { it.id }
        } else emptyList()
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
                    minDistanceFeet = state.minDistance?.takeIf { it > 0f },
                ),
                discIds = orderedDiscIds,
            )
            onReady(id)
        }
    }
}
