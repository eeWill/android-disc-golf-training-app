package com.eewill.discgolftraining.data

enum class DiscType {
    PUTTER,
    MIDRANGE,
    FAIRWAY_DRIVER,
    DRIVER,
    DISTANCE_DRIVER;

    fun displayName(): String = when (this) {
        PUTTER -> "Putter"
        MIDRANGE -> "Midrange"
        FAIRWAY_DRIVER -> "Fairway Driver"
        DRIVER -> "Driver"
        DISTANCE_DRIVER -> "Distance Driver"
    }
}
