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
        val format = EncodeFormat.create(config, audioRecord)
        encoder.init(format, outputStream)
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