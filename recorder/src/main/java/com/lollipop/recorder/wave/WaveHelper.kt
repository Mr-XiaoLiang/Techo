package com.lollipop.recorder.wave

import com.lollipop.recorder.RecorderConfig
import com.lollipop.recorder.RecorderListener

/**
 * 波形监听器
 */
class WaveHelper(val config: RecorderConfig): RecorderListener {

    /**
     * 波形图的缓存大小
     */
    var waveBuffer = 100

    /**
     * 波形图的比重
     */
    var waveWeight = 300

    /**
     * 波形间隔
     */
    var waveInterval = 20L

    /**
     * 波形变化监听器
     */
    var waveListener: WaveListener? = null

    /**
     * 是否开启
     */
    var enable = false

    override fun onRecord(data: ByteArray, begin: Int, end: Int) {
        if (!enable) {
            return
        }
        TODO("Not yet implemented")
    }

    override fun onStart() {
        TODO("Not yet implemented")
    }

    override fun onStop() {
        TODO("Not yet implemented")
    }

}