package com.lollipop.recorder.encode

import android.media.AudioRecord
import com.lollipop.recorder.RecorderConfig
import java.io.OutputStream

class DefaultEncoderProvider: EncoderProvider {
    override fun createEncoder(
        config: RecorderConfig,
        audioRecord: AudioRecord,
        outputStream: OutputStream
    ): PcmEncoder {
        TODO("Not yet implemented")
    }
}