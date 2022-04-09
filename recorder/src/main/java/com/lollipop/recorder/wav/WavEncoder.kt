package com.lollipop.recorder.wav

import android.media.AudioRecord
import com.lollipop.recorder.RecorderConfig
import com.lollipop.recorder.encode.PcmEncoder
import java.io.OutputStream

class WavEncoder(
    config: RecorderConfig,
    audioRecord: AudioRecord,
    outputStream: OutputStream
): PcmEncoder(outputStream) {

    override fun write(byteArray: ByteArray, offset: Int, count: Int) {
        writeOut(byteArray, offset, count)
    }



}