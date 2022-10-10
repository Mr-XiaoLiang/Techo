package com.lollipop.recorder

import android.media.audiofx.Visualizer
import com.lollipop.recorder.visualizer.Renderer
import kotlin.math.*

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

        fun getVolume(wave: Wave): Float {
            val waveform = wave.data
            var v = 0.0
            for (i in waveform.indices) {
                v += waveform[i].toDouble().pow(2.0)
            }
            val volume = 10 * log10(v / waveform.size)
            return volume.toFloat()
        }

        /**
         * 返回FFT对应的频率信息
         *
         * In order to obtain magnitude and phase values the following code can
         * be used:
         *
         *       int n = fft.size();
         *       float[] magnitudes = new float[n / 2 + 1];
         *       float[] phases = new float[n / 2 + 1];
         *       magnitudes[0] = (float)Math.abs(fft[0]);      // DC
         *       magnitudes[n / 2] = (float)Math.abs(fft[1]);  // Nyquist
         *       phases[0] = phases[n / 2] = 0;
         *       for (int k = 1; k &lt; n / 2; k++) {
         *           int i = k * 2;
         *           magnitudes[k] = (float)Math.hypot(fft[i], fft[i + 1]);
         *           phases[k] = (float)Math.atan2(fft[i + 1], fft[i]);
         *       }
         *
         */
        fun getFrequency(fft: Fft): Frequency {
            val data = fft.data
            val n = data.size
            val magnitudes = FloatArray(n / 2 + 1)
            val phases = FloatArray(n / 2 + 1)
            magnitudes[0] = abs(data[0].toFloat()) // DC
            magnitudes[n / 2] = abs(data[1].toFloat()) // Nyquist
            phases[0] = 0F
            phases[n / 2] = phases[0]
            for (k in 1 until n / 2) {
                val i = k * 2
                val rfk = data[i].toDouble()
                val ifk = data[i + 1].toDouble()
                magnitudes[k] = hypot(rfk, ifk).toFloat()
                phases[k] = atan2(ifk, rfk).toFloat()
            }
            return Frequency(magnitudes, phases, fft.samplingRate)
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
        visualizer?.enabled = false
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

    /**
     * Method called when a new frequency capture is available.
     * <p>Data in the fft buffer is valid only within the scope of the callback.
     * Applications which need access to the fft data after returning from the callback
     * should make a copy of the data instead of holding a reference.
     *
     * <p>In order to obtain magnitude and phase values the following formulas can
     * be used:
     *    <pre class="prettyprint">
     *       for (int i = 0; i &lt; fft.size(); i += 2) {
     *           float magnitude = (float)Math.hypot(fft[i], fft[i + 1]);
     *           float phase = (float)Math.atan2(fft[i + 1], fft[i]);
     *       }</pre>
     * @param visualizer Visualizer object on which the listener is registered.
     * @param fft array of bytes containing the frequency representation.
     *    The fft array only contains the first half of the actual
     *    FFT spectrum (frequencies up to Nyquist frequency), exploiting
     *    the symmetry of the spectrum. For each frequencies bin <code>i</code>:
     *    <ul>
     *      <li>the element at index <code>2*i</code> in the array contains
     *          the real part of a complex number,</li>
     *      <li>the element at index <code>2*i+1</code> contains the imaginary
     *          part of the complex number.</li>
     *    </ul>
     * @param samplingRate sampling rate of the visualized audio.
     */
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

    class Frequency(
        val magnitudes: FloatArray,
        val phases: FloatArray,
        val samplingRate: Int,
    ) {

        companion object {
            /**
             * byte x, y
             * Byte.Max = 127
             * sqrt(x^2 + y^2)
             * max = 179.605122421383071
             */
            const val MAX = 180
        }

    }

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