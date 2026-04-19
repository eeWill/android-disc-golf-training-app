package com.eewill.discgolftraining

import androidx.compose.ui.geometry.Rect
import com.eewill.discgolftraining.data.DiscDataMode
import com.eewill.discgolftraining.data.RoundEntity
import com.eewill.discgolftraining.ui.setup.SetupState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SetupStateTest {

    private fun validRect() = Rect(0.2f, 0.3f, 0.7f, 0.6f)

    private fun validState() = SetupState(
        imagePath = "/tmp/a.jpg",
        distanceFeet = "40",
        gapWidthFeet = "3",
        gapRect = validRect(),
    )

    @Test
    fun `canBegin is false when no image`() {
        assertFalse(validState().copy(imagePath = null).canBegin(hasDiscs = true))
    }

    @Test
    fun `canBegin is false when distance is blank`() {
        assertFalse(validState().copy(distanceFeet = "").canBegin(hasDiscs = true))
    }

    @Test
    fun `canBegin is false when distance is zero`() {
        assertFalse(validState().copy(distanceFeet = "0").canBegin(hasDiscs = true))
    }

    @Test
    fun `canBegin is false when distance is unparseable`() {
        assertFalse(validState().copy(distanceFeet = "abc").canBegin(hasDiscs = true))
    }

    @Test
    fun `canBegin is false when gap width is zero`() {
        assertFalse(validState().copy(gapWidthFeet = "0").canBegin(hasDiscs = true))
    }

    @Test
    fun `canBegin is false when gap rect is missing`() {
        assertFalse(validState().copy(gapRect = null).canBegin(hasDiscs = true))
    }

    @Test
    fun `canBegin is false when gap rect is zero area`() {
        assertFalse(
            validState().copy(gapRect = Rect(0.5f, 0.5f, 0.5f, 0.5f))
                .canBegin(hasDiscs = true)
        )
    }

    @Test
    fun `canBegin is true when all fields are valid (None mode)`() {
        assertTrue(validState().canBegin(hasDiscs = false))
    }

    @Test
    fun `canBegin is true in Type mode regardless of disc library`() {
        val state = validState().copy(discDataMode = DiscDataMode.TYPE)
        assertTrue(state.canBegin(hasDiscs = false))
        assertTrue(state.canBegin(hasDiscs = true))
    }

    @Test
    fun `canBegin is false in Disc mode when disc library is empty`() {
        val state = validState().copy(discDataMode = DiscDataMode.DISC)
        assertFalse(state.canBegin(hasDiscs = false))
    }

    @Test
    fun `canBegin is true in Disc mode when disc library has entries`() {
        val state = validState().copy(discDataMode = DiscDataMode.DISC)
        assertTrue(state.canBegin(hasDiscs = true))
    }

    @Test
    fun `distance and width parse filtered numeric strings`() {
        val state = SetupState(distanceFeet = "42.5", gapWidthFeet = "2.75")
        assertEquals(42.5f, state.distance)
        assertEquals(2.75f, state.width)
    }

    @Test
    fun `fromRound rehydrates a valid setup state that can begin immediately`() {
        val round = RoundEntity(
            id = "abc",
            createdAt = 1_000L,
            imagePath = "/tmp/r.jpg",
            distanceFeet = 40f,
            gapWidthFeet = 3.5f,
            gapLeft = 0.2f, gapTop = 0.3f, gapRight = 0.7f, gapBottom = 0.6f,
            discDataMode = DiscDataMode.TYPE,
        )
        val state = SetupState.fromRound(round)
        assertEquals("/tmp/r.jpg", state.imagePath)
        assertEquals("40", state.distanceFeet)
        assertEquals("3.5", state.gapWidthFeet)
        assertEquals(Rect(0.2f, 0.3f, 0.7f, 0.6f), state.gapRect)
        assertEquals(DiscDataMode.TYPE, state.discDataMode)
        assertTrue(state.canBegin(hasDiscs = false))
    }
}
