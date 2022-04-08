package com.lollipop.recorder.encode

import android.media.AudioRecord
import com.lollipop.recorder.RecorderConfig
import java.io.OutputStream

interface EncoderProvider {

    fun createEncoder(
        config: RecorderConfig,
        audioRecord: AudioRecord,
        outputStream: OutputStream
    ): PcmEncoder

    companion object {



    }

}