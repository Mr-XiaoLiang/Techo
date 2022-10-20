package com.lollipop.recorder

interface VisualizerRenderer {

    fun onRender(data: VisualizerHelper.Wave) {}

    fun onRender(data: VisualizerHelper.Fft) {}

    fun onRender(data: VisualizerHelper.Frequency) {}

}