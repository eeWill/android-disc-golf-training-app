package com.eewill.discgolftraining.data

data class DirectionCounts(
    val left: Int,
    val right: Int,
    val high: Int,
    val low: Int,
) {
    companion object {
        fun from(round: RoundEntity, throws: List<ThrowEntity>): DirectionCounts {
            var l = 0
            var r = 0
            var h = 0
            var lo = 0
            for (t in throws) {
                if (t.isHit) continue
                if (t.x < round.gapLeft) l++
                if (t.x > round.gapRight) r++
                if (t.y < round.gapTop) h++
                if (t.y > round.gapBottom) lo++
            }
            return DirectionCounts(l, r, h, lo)
        }
    }
}
