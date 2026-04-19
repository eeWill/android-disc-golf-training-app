package com.eewill.discgolftraining

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.eewill.discgolftraining.ui.components.computeImageBounds
import com.eewill.discgolftraining.ui.components.normalizedRect
import com.eewill.discgolftraining.ui.components.toNormalized
import com.eewill.discgolftraining.ui.components.toPixels
import org.junit.Assert.assertEquals
import org.junit.Test

class CoordinateMappingTest {

    private val eps = 0.0001f

    @Test
    fun `computeImageBounds fills container when aspect ratios match`() {
        val bounds = computeImageBounds(
            containerW = 800f, containerH = 400f,
            bitmapSize = Size(400f, 200f), // same 2:1 aspect
        )
        assertEquals(0f, bounds.left, eps)
        assertEquals(0f, bounds.top, eps)
        assertEquals(800f, bounds.width, eps)
        assertEquals(400f, bounds.height, eps)
    }

    @Test
    fun `computeImageBounds letterboxes vertically when bitmap is wider than container`() {
        // container 600x600, bitmap 1000x500 (2:1) → fit width, image height = 300, centered vertically
        val bounds = computeImageBounds(600f, 600f, Size(1000f, 500f))
        assertEquals(0f, bounds.left, eps)
        assertEquals(150f, bounds.top, eps)
        assertEquals(600f, bounds.width, eps)
        assertEquals(300f, bounds.height, eps)
    }

    @Test
    fun `computeImageBounds letterboxes horizontally when bitmap is taller than container`() {
        // container 600x600, bitmap 500x1000 (1:2) → fit height, image width = 300, centered horizontally
        val bounds = computeImageBounds(600f, 600f, Size(500f, 1000f))
        assertEquals(150f, bounds.left, eps)
        assertEquals(0f, bounds.top, eps)
        assertEquals(300f, bounds.width, eps)
        assertEquals(600f, bounds.height, eps)
    }

    @Test
    fun `computeImageBounds falls back to container rect when bitmap size is null`() {
        val bounds = computeImageBounds(400f, 300f, null)
        assertEquals(0f, bounds.left, eps)
        assertEquals(0f, bounds.top, eps)
        assertEquals(400f, bounds.width, eps)
        assertEquals(300f, bounds.height, eps)
    }

    @Test
    fun `toNormalized maps interior point to fractional coordinates`() {
        val bounds = computeImageBounds(600f, 600f, Size(1000f, 500f)) // top=150, h=300
        val norm = toNormalized(Offset(300f, 300f), bounds)
        assertEquals(0.5f, norm.x, eps)
        assertEquals(0.5f, norm.y, eps)
    }

    @Test
    fun `toNormalized clamps to 0 to 1 range for out-of-bounds input`() {
        val bounds = computeImageBounds(600f, 600f, Size(1000f, 500f))
        val below = toNormalized(Offset(-50f, -50f), bounds)
        assertEquals(0f, below.x, eps)
        assertEquals(0f, below.y, eps)
        val above = toNormalized(Offset(10_000f, 10_000f), bounds)
        assertEquals(1f, above.x, eps)
        assertEquals(1f, above.y, eps)
    }

    @Test
    fun `toPixels is inverse of toNormalized for interior points`() {
        val bounds = computeImageBounds(800f, 400f, Size(1600f, 400f)) // letterbox vertical
        val original = Offset(400f, 120f)
        val normalized = toNormalized(original, bounds)
        val roundTripped = toPixels(normalized, bounds)
        assertEquals(original.x, roundTripped.x, eps)
        assertEquals(original.y, roundTripped.y, eps)
    }

    @Test
    fun `normalizedRect orders corners regardless of drag direction`() {
        val aToB = normalizedRect(Offset(0.2f, 0.3f), Offset(0.7f, 0.8f))
        val bToA = normalizedRect(Offset(0.7f, 0.8f), Offset(0.2f, 0.3f))
        assertEquals(aToB.left, bToA.left, eps)
        assertEquals(aToB.top, bToA.top, eps)
        assertEquals(aToB.right, bToA.right, eps)
        assertEquals(aToB.bottom, bToA.bottom, eps)
        assertEquals(0.2f, aToB.left, eps)
        assertEquals(0.3f, aToB.top, eps)
        assertEquals(0.7f, aToB.right, eps)
        assertEquals(0.8f, aToB.bottom, eps)
    }
}
