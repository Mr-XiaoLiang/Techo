package com.lollipop.recorder.wave

import com.lollipop.recorder.RecordHelper
import com.lollipop.recorder.RecorderConfig
import com.lollipop.recorder.RecorderListener
import java.util.*

/**
 * 波形监听器
 */
class WaveHelper(private val config: RecorderConfig) : RecorderListener {

    /**
     * 波形图的缓存大小
     */
    var waveBuffer = 100

    /**
     * 波形图的采样率
     * 采样率的意义在于，按照一定的间隔摘取数据
     */
    var waveSamplingRate = 300

    /**
     * 波形变化监听器
     */
    var waveListener: WaveListener? = null

    /**
     * 是否开启
     */
    var enable = false

    /**
     * 是否是16位数据
     */
    var is16Bit = config.is16Bit
        private set

    /**
     * 声道数量
     */
    var channelCount = 1
        private set

    /**
     * 采样率的计数器
     */
    private var samplingRateIndex = 0

    /**
     * 内部缓存对象
     * 他是链表集合，用于快速增加和移除数据
     */
    private val cache = LinkedList<WaveInfo>()

    /**
     * 对外的缓存对象
     * 他是数组集合，用于快速读取
     */
    private val outCache = ArrayList<WaveInfo>()

    /**
     * 获取当前的缓存
     */
    fun getCache(): List<WaveInfo> {
        return outCache
    }

    override fun onFormatChanged(is16Bit: Boolean, channelCount: Int) {
        this.is16Bit = is16Bit
        this.channelCount = channelCount
    }

    override fun onRecord(data: ByteArray, begin: Int, end: Int) {
        if (!enable) {
            return
        }
        if (is16Bit) {
            val readBy16Bit = RecordHelper.readBy16Bit(channelCount, data, begin, end)
            val result = readBy16Bit.data
            sampling(result.size) {
                val info = result[it]
                if (info.size > 1) {
                    putWaveInfo(info[0], info[1])
                } else if (info.isNotEmpty()) {
                    putWaveInfo(info[0], info[0])
                }
            }
        } else {
            val readBy8Bit = RecordHelper.readBy8Bit(channelCount, data, begin, end)
            val result = readBy8Bit.data
            sampling(result.size) {
                val info = result[it]
                if (info.size > 1) {
                    putWaveInfo(info[0], info[1])
                } else if (info.isNotEmpty()) {
                    putWaveInfo(info[0], info[0])
                }
            }
        }
        outCache.clear()
        outCache.addAll(cache)
        waveListener?.onWaveChanged(outCache, channelCount > 1)
    }

    private fun sampling(dataSize: Int, callback: (Int) -> Unit) {
        val srStart = samplingRateIndex
        val sr = waveSamplingRate
        for (index in srStart until (dataSize + srStart) step sr) {
            val srIndex = index % sr
            samplingRateIndex = srIndex
            if (srIndex == 0) {
                callback(index - srStart)
            }
        }
    }

    private fun putWaveInfo(left: Short, right: Short) {
        putWaveInfo(
            WaveInfo(
                left = left * 1F / Short.MAX_VALUE,
                right = right * 1F / Short.MAX_VALUE
            )
        )
    }

    private fun putWaveInfo(left: Byte, right: Byte) {
        putWaveInfo(
            WaveInfo(
                left = left * 1F / Byte.MAX_VALUE,
                right = right * 1F / Byte.MAX_VALUE
            )
        )
    }

    private fun putWaveInfo(info: WaveInfo) {
        synchronized(cache) {
            cache.addLast(info)
            val buffer = waveBuffer
            while (cache.size > buffer) {
                cache.removeFirst()
            }
        }
    }

}