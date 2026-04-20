package com.eewill.discgolftraining

import com.eewill.discgolftraining.data.puttingPositions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PuttingPositionsTest {

    @Test
    fun exactInterval_producesAllPositionsIncludingMax() {
        assertEquals(listOf(10f, 15f, 20f, 25f, 30f), puttingPositions(10f, 30f, 5f))
    }

    @Test
    fun trailingRemainder_isDropped() {
        assertEquals(listOf(10f, 15f, 20f, 25f), puttingPositions(10f, 28f, 5f))
    }

    @Test
    fun singlePosition_whenMaxEqualsMin() {
        assertEquals(listOf(15f), puttingPositions(15f, 15f, 5f))
    }

    @Test
    fun maxLessThanMin_returnsEmpty() {
        assertTrue(puttingPositions(30f, 10f, 5f).isEmpty())
    }

    @Test
    fun nonPositiveInterval_returnsEmpty() {
        assertTrue(puttingPositions(10f, 30f, 0f).isEmpty())
        assertTrue(puttingPositions(10f, 30f, -5f).isEmpty())
    }

    @Test
    fun floatingPointInterval_handlesEpsilon() {
        val positions = puttingPositions(10f, 20f, 2.5f)
        assertEquals(5, positions.size)
        assertEquals(10f, positions.first(), 0.001f)
        assertEquals(20f, positions.last(), 0.001f)
    }
}
