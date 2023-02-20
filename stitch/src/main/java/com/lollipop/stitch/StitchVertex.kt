package com.lollipop.stitch

class StitchVertex(
    val xWeight: Float,
    val yWeight: Float
) {

    fun x(width: Float): Float {
        return width * xWeight
    }

    fun y(height: Float): Float {
        return height * yWeight
    }

    override fun toString(): String {
        return "StitchVertex(x = $xWeight, y = $yWeight)"
    }

}