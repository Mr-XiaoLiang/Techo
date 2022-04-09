package com.lollipop.recorder.encode

import com.lollipop.recorder.RecorderConfig

class EncodeFormat(
    val config: RecorderConfig,
    val audioFormat: Int,
    val audioSessionId: Int,
    val audioSource: Int,
    val bufferSizeInFrames: Int,
    val channelConfiguration: Int,
    val channelCount: Int,
    val format: Int,
    val isPrivacySensitive: Boolean,
)