package com.lollipop.recorder.encode

import android.media.AudioFormat
import android.media.AudioRecord
import android.os.Build
import com.lollipop.recorder.RecorderConfig

class EncodeFormat(
    val config: RecorderConfig = RecorderConfig.DEFAULT,
    val audioFormat: Int = 0,
    val audioSessionId: Int = 0,
    val audioSource: Int = 0,
    val bufferSizeInFrames: Int = 0,
    val channelConfiguration: Int = 0,
    val channelCount: Int = 0,
    val format: AudioFormat = DEFAULT_AUDIO_FORMAT,
    val isPrivacySensitive: Boolean = false,
    val sampleRate: Int = 0
) {
    companion object {

        val EMPTY = EncodeFormat()

        val DEFAULT_AUDIO_FORMAT = AudioFormat.Builder().build()

        fun create(config: RecorderConfig, audioRecord: AudioRecord): EncodeFormat {
            val bufferSizeInFrames = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                audioRecord.bufferSizeInFrames
            } else {
                0
            }
            val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                audioRecord.format
            } else {
                DEFAULT_AUDIO_FORMAT
            }
            val isPrivacySensitive = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                audioRecord.isPrivacySensitive
            } else {
                false
            }
            return EncodeFormat(
                config = config,
                audioFormat = audioRecord.audioFormat,
                audioSessionId = audioRecord.audioSessionId,
                audioSource = audioRecord.audioSource,
                bufferSizeInFrames = bufferSizeInFrames,
                channelConfiguration = audioRecord.channelConfiguration,
                channelCount = audioRecord.channelCount,
                format = format,
                isPrivacySensitive = isPrivacySensitive,
                sampleRate = audioRecord.sampleRate
            )
        }
    }
}