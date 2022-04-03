package com.lollipop.recorder.wave

interface WaveListener {

    fun onWaveChanged(info: List<WaveInfo>, stereo: Boolean)

}