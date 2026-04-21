package com.eewill.discgolftraining.data

enum class FlightModifier {
    HYZER_PLUS,
    ANHYZER_PLUS;

    fun displayName(): String = when (this) {
        HYZER_PLUS -> "Hyzer+"
        ANHYZER_PLUS -> "Anhyzer+"
    }
}
