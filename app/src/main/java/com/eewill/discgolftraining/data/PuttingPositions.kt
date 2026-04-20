package com.eewill.discgolftraining.data

fun puttingPositions(
    minDistanceFeet: Float,
    maxDistanceFeet: Float,
    intervalFeet: Float,
): List<Float> {
    if (intervalFeet <= 0f || minDistanceFeet <= 0f || maxDistanceFeet < minDistanceFeet) {
        return emptyList()
    }
    val epsilon = intervalFeet * 1e-4f
    return generateSequence(minDistanceFeet) { it + intervalFeet }
        .takeWhile { it <= maxDistanceFeet + epsilon }
        .toList()
}
