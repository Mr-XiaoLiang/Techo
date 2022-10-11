package com.lollipop.recorder.visualizer

import com.lollipop.recorder.VisualizerHelper

interface Renderer {

    fun onRender(data: VisualizerHelper.Wave) {}

    fun onRender(data: VisualizerHelper.Fft) {}

    fun onRender(data: VisualizerHelper.Frequency) {}

}