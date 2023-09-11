package com.lollipop.clip.squircle

class SquircleCorner(
    val inEdgeRadius: Float,
    val outEdgeRadius: Float,
    val inEdgeWeight: Float = 0.8F,
    val outEdgeWeight: Float = 0.8F,
) {

    fun clone(
        inRadius: Float = inEdgeRadius,
        outRadius: Float = outEdgeRadius,
        inWeight: Float = inEdgeWeight,
        outWeight: Float = outEdgeWeight
    ): SquircleCorner {
        return SquircleCorner(
            inEdgeRadius = inRadius,
            outEdgeRadius = outRadius,
            inEdgeWeight = inWeight,
            outEdgeWeight = outWeight
        )
    }

    fun offset(
        inRadiusOffset: Float = 0F,
        outRadiusOffset: Float = 0F,
        inWeightOffset: Float = 0F,
        outWeightOffset: Float = 0F
    ): SquircleCorner {
        return clone(
            inRadius = inEdgeRadius + inRadiusOffset,
            outRadius = outEdgeRadius + outRadiusOffset,
            inWeight = inEdgeWeight + inWeightOffset,
            outWeight = outEdgeWeight + outWeightOffset
        )
    }

}