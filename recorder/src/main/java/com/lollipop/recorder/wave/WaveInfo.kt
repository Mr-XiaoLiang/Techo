package com.lollipop.recorder.wave

class WaveInfo(
    val left: Float,
    val right: Float
) {

    companion object {
        fun new(value: Float): WaveInfo {
            return WaveInfo(value, value)
        }
    }

}