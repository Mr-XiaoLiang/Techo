package com.lollipop.recorder

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder

class RecorderConfig private constructor(
    val source: Int,
    val sampleRateInHz: Int,
    val channelConfig: Int,
    val audioFormat: Int,
    val bufferSizeInBytes: Int
) {

    companion object {

        private val SAMPLE_RATE = intArrayOf(48000, 44100, 22050, 16000, 11025, 8000, 0)

        fun findMaxSampleRateAndMinBufferSize(channel: Int, encoding: Int): IntArray {
            for (i in SAMPLE_RATE.indices) {
                val sampleRate = SAMPLE_RATE[i]
                val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channel, encoding)
                if (bufferSize > 0) {
                    return intArrayOf(sampleRate, bufferSize)
                }
            }
            return intArrayOf(0, 0)
        }

        fun create(
            source: Int? = null,
            sampleRateInHz: Int? = null,
            channelConfig: Int? = null,
            audioFormat: Int? = null,
            bufferSizeInByte: Int? = null
        ): RecorderConfig {
            return RecorderConfig(
                source = source ?: DEFAULT.source,
                sampleRateInHz = sampleRateInHz ?: DEFAULT.sampleRateInHz,
                channelConfig = channelConfig ?: DEFAULT.channelConfig,
                audioFormat = audioFormat ?: DEFAULT.audioFormat,
                bufferSizeInBytes = bufferSizeInByte ?: DEFAULT.bufferSizeInBytes
            )
        }

        val DEFAULT by lazy {
            val channel = AudioFormat.CHANNEL_IN_STEREO
            val encoding = AudioFormat.ENCODING_PCM_16BIT
            val maxSampleRateAndMinBufferSize = findMaxSampleRateAndMinBufferSize(channel, encoding)
            val sampleRate = maxSampleRateAndMinBufferSize[0]
            val bufferSize = maxSampleRateAndMinBufferSize[1]
            RecorderConfig(
                source = MediaRecorder.AudioSource.MIC,
                sampleRateInHz = sampleRate,
                channelConfig = channel,
                audioFormat = AudioFormat.ENCODING_PCM_16BIT,
                bufferSizeInBytes = bufferSize
            )
        }

        private var GLOBAL_CONFIG: RecorderConfig? = null

        var GLOBAL: RecorderConfig
            set(value) {
                GLOBAL_CONFIG = value
            }
            get() {
                return GLOBAL_CONFIG ?: DEFAULT
            }
    }

    val is16Bit: Boolean
        get() {
            return audioFormat == AudioFormat.ENCODING_PCM_16BIT
        }

}