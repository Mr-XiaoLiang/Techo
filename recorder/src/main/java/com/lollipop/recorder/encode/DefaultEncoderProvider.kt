package com.lollipop.recorder.encode

import com.lollipop.recorder.aac.AacEncoder

class DefaultEncoderProvider : EncoderProvider() {
    override fun createEncoder(): PcmEncoder {
        return AacEncoder()
    }
}