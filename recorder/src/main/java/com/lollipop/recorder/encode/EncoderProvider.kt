package com.lollipop.recorder.encode

import android.media.AudioRecord
import com.lollipop.recorder.RecorderConfig
import java.io.OutputStream

abstract class EncoderProvider {

    fun getEncoder(
        config: RecorderConfig,
        audioRecord: AudioRecord,
        outputStream: OutputStream
    ): PcmEncoder {
        val encoder = createEncoder()
        init(encoder, config, audioRecord, outputStream)
        return encoder
    }

    abstract fun createEncoder(): PcmEncoder

    private fun init(
        encoder: PcmEncoder,
        config: RecorderConfig,
        audioRecord: AudioRecord,
        outputStream: OutputStream
    ) {
        encoder.init(EncodeFormat.create(config, audioRecord), outputStream)
    }

}