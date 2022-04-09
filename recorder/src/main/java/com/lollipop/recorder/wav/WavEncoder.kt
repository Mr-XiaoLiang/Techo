package com.lollipop.recorder.wav

import com.lollipop.recorder.encode.PcmEncoder

class WavEncoder(): PcmEncoder() {

    override fun write(byteArray: ByteArray, offset: Int, count: Int) {
        writeOut(byteArray, offset, count)
    }

}