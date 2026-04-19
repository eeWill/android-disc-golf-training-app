package com.eewill.discgolftraining

import com.eewill.discgolftraining.data.DirectionCounts
import com.eewill.discgolftraining.data.RoundEntity
import com.eewill.discgolftraining.data.ThrowEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class DirectionCountsTest {

    private fun round() = RoundEntity(
        id = "r1", createdAt = 0L, imagePath = "/tmp/x.jpg",
        distanceFeet = 40f, gapWidthFeet = 3f,
        gapLeft = 0.4f, gapTop = 0.4f, gapRight = 0.6f, gapBottom = 0.6f,
    )

    private fun t(id: String, x: Float, y: Float, isHit: Boolean) =
        ThrowEntity(id = id, roundId = "r1", index = 0, x = x, y = y, isHit = isHit)

    @Test
    fun `hit throws contribute no direction counts`() {
        val dirs = DirectionCounts.from(
            round(),
            listOf(
                t("a", 0.5f, 0.5f, isHit = true),
                t("b", 0.45f, 0.55f, isHit = true),
            ),
        )
        assertEquals(DirectionCounts(left = 0, right = 0, high = 0, low = 0), dirs)
    }

    @Test
    fun `pure directional misses increment exactly one bucket`() {
        val dirs = DirectionCounts.from(
            round(),
            listOf(
                t("left",  0.1f, 0.5f, isHit = false),
                t("right", 0.9f, 0.5f, isHit = false),
                t("high",  0.5f, 0.1f, isHit = false),
                t("low",   0.5f, 0.9f, isHit = false),
            ),
        )
        assertEquals(DirectionCounts(left = 1, right = 1, high = 1, low = 1), dirs)
    }

    @Test
    fun `low-left miss counts for both left and low`() {
        val dirs = DirectionCounts.from(
            round(),
            listOf(t("lowLeft", 0.1f, 0.9f, isHit = false)),
        )
        assertEquals(DirectionCounts(left = 1, right = 0, high = 0, low = 1), dirs)
    }

    @Test
    fun `high-right miss counts for both right and high`() {
        val dirs = DirectionCounts.from(
            round(),
            listOf(t("highRight", 0.9f, 0.1f, isHit = false)),
        )
        assertEquals(DirectionCounts(left = 0, right = 1, high = 1, low = 0), dirs)
    }

    @Test
    fun `mixed throws aggregate correctly`() {
        val dirs = DirectionCounts.from(
            round(),
            listOf(
                t("hit",        0.5f, 0.5f, isHit = true),
                t("low-left",   0.1f, 0.9f, isHit = false),
                t("low",        0.5f, 0.9f, isHit = false),
                t("right",      0.9f, 0.5f, isHit = false),
                t("high-right", 0.9f, 0.1f, isHit = false),
            ),
        )
        // low-left: left+low
        // low: low
        // right: right
        // high-right: right+high
        assertEquals(DirectionCounts(left = 1, right = 2, high = 1, low = 2), dirs)
    }

    @Test
    fun `empty throws list returns zeros`() {
        val dirs = DirectionCounts.from(round(), emptyList())
        assertEquals(DirectionCounts(0, 0, 0, 0), dirs)
    }
}
