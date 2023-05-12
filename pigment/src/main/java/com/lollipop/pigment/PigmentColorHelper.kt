package com.lollipop.pigment

import android.graphics.Color

object PigmentColorHelper {

    fun variant(src: Int): Int {
        TODO()
    }

    fun title(src: Int): Int {
        TODO()
    }

    fun body(src: Int): Int {
        TODO()
    }

    private fun colorToHsv(color: Int): FloatArray {
        val output = FloatArray(3)
        Color.colorToHSV(color, output)
        return output
    }

}