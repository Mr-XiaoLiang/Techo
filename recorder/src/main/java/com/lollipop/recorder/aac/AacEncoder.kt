package com.lollipop.recorder.aac

import android.media.MediaCodec
import android.media.MediaFormat
import com.lollipop.recorder.encode.PcmEncoder

class AacEncoder: PcmEncoder() {

    private var mediaCodec: MediaCodec? = null

    override fun onReady() {
        super.onReady()
        mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
    }


    override fun onDestroy() {
        super.onDestroy()
        mediaCodec?.release()
        mediaCodec = null
    }

    override fun write(byteArray: ByteArray, offset: Int, count: Int) {
        mediaCodec?:return
        TODO("Not yet implemented")
    }

}