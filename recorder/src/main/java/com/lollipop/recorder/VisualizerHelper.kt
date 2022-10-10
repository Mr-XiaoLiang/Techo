package com.lollipop.recorder

import android.media.audiofx.Visualizer
import com.lollipop.recorder.visualizer.Renderer

class VisualizerHelper(
    private val capture: Capture,
    private val rate: Int = maxRate,
    private val captureSize: Int = captureSizeRange.last
) : AudioPlayerHelper.OnPlayerStateChangedListener,
    Visualizer.OnDataCaptureListener {

    companion object {
        val maxRate: Int
            get() {
                return Visualizer.getMaxCaptureRate()
            }

        val captureSizeRange: IntRange
            get() {
                val range = Visualizer.getCaptureSizeRange()
                return IntRange(range[0], range[1])
            }
    }

    private val rendererList = ArrayList<Renderer>()

    var wave: Wave? = null
        private set
    var fft: Fft? = null
        private set

    var visualizer: Visualizer? = null
        private set

    fun addRenderer(renderer: Renderer) {
        rendererList.add(renderer)
    }

    fun removeRenderer(renderer: Renderer) {
        rendererList.remove(renderer)
    }

    override fun onAudioPlayerStateChanged(
        helper: AudioPlayerHelper,
        state: AudioPlayerHelper.State
    ) {
        when (state) {
            AudioPlayerHelper.State.PREPARED -> {
                create(helper)
            }
            else -> {
                if (helper.lifecycle.isAtLeast(AudioPlayerHelper.Lifecycle.STARTED)) {
                    visualizer?.enabled = true
                } else if (helper.lifecycle.isAtLeast(AudioPlayerHelper.Lifecycle.PREPARED)) {
                    visualizer?.enabled = false
                } else {
                    release()
                }
            }
        }
    }

    fun release() {
        wave = null
        fft = null
        visualizer?.release()
        visualizer = null
    }

    private fun create(helper: AudioPlayerHelper) {
        release()
        val audioSessionId = helper.mediaPlayer?.audioSessionId ?: return
        val newVisualizer = Visualizer(audioSessionId)
        newVisualizer.setDataCaptureListener(this, rate, capture.hasWave, capture.hasFft)
        newVisualizer.captureSize = captureSize
    }

    override fun onWaveFormDataCapture(
        visualizer: Visualizer?,
        waveform: ByteArray?,
        samplingRate: Int
    ) {
        if (waveform == null) {
            wave = null
        } else {
            val newWave = Wave(waveform, samplingRate)
            wave = newWave
            rendererList.forEach { it.onRender(newWave) }
        }
    }

    override fun onFftDataCapture(visualizer: Visualizer?, fft: ByteArray?, samplingRate: Int) {
        if (fft == null) {
            this.fft = null
        } else {
            val newFft = Fft(fft, samplingRate)
            this.fft = newFft
            rendererList.forEach { it.onRender(newFft) }
        }
    }

    class Wave(
        val data: ByteArray,
        val samplingRate: Int
    )

    class Fft(
        val data: ByteArray,
        val samplingRate: Int
    )

    enum class Capture {
        WAVE,
        FFT,
        BOTH;

        val hasWave: Boolean
            get() {
                return this == WAVE || this == BOTH
            }

        val hasFft: Boolean
            get() {
                return this == FFT || this == BOTH
            }
    }

}