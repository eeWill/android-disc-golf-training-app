package com.eewill.discgolftraining.data

enum class DiscDataMode {
    NONE,
    TYPE,
    DISC;

    fun displayName(): String = when (this) {
        NONE -> "None"
        TYPE -> "Type"
        DISC -> "Disc"
    }
}
