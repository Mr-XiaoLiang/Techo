package com.lollipop.pigment

import android.graphics.Color
import kotlin.math.max
import kotlin.math.min

object PigmentColorHelper {

    fun variant(src: Int): Int {
        return changeColorSV(src, 1F, 0.5F)
    }

    fun title(src: Int): Int {
        val hsv = colorToHsv(src)
        if (hsv[2] > 0.5F) {
            hsv[2] = 0.0F
        } else {
            hsv[2] = 1F
        }
        hsv[1] = 0.1F
        return Color.HSVToColor(hsv)
    }

    fun body(src: Int): Int {
        val hsv = colorToHsv(src)
        if (hsv[2] > 0.5F) {
            hsv[2] = 0.1F
        } else {
            hsv[2] = 0.9F
        }
        hsv[1] = 0.1F
        return Color.HSVToColor(hsv)
    }

    private fun colorToHsv(color: Int): FloatArray {
        val output = FloatArray(3)
        Color.colorToHSV(color, output)
        return output
    }

    private fun changeColorSV(color: Int, sWeight: Float, vWeight: Float): Int {
        val output = FloatArray(3)
        Color.colorToHSV(color, output)
        output[1] = (output[1] * sWeight).rangeWith(0F, 1F)
        output[2] = (output[2] * vWeight).rangeWith(0F, 1F)
        return Color.HSVToColor(output)
    }

    private fun Float.rangeWith(min: Float, max: Float): Float {
        return min(max(this, min), max)
    }

}