package com.lollipop.recorder.aac

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import com.lollipop.recorder.encode.PcmEncoder

class AacEncoder(
    private val bitRate: Int = 96000
) : PcmEncoder() {

    companion object {
        private const val MIME = MediaFormat.MIMETYPE_AUDIO_AAC
        private const val TIMEOUT_INFINITE = -1L
        private const val TIMEOUT_NOW = 0L
    }

    private var mediaCodec: MediaCodec? = null

    private var aacHeader = AacHeader.create()

    override fun onReady() {
        super.onReady()
        aacHeader = AacHeader.create(format, profile = AacHeader.Profile.AAC_LC)
        val codec = MediaCodec.createEncoderByType(MIME)
        codec.configure(getMediaFormat(), null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        mediaCodec = codec
        codec.start()
    }

    private fun getMediaFormat(): MediaFormat {
        val mediaFormat = MediaFormat.createAudioFormat(
            MIME,
            format.sampleRate,
            format.channelCount
        )
        mediaFormat.setInteger(
            MediaFormat.KEY_AAC_PROFILE,
            MediaCodecInfo.CodecProfileLevel.AACObjectLC
        )
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
        return mediaFormat
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaCodec?.release()
        mediaCodec = null
    }

    override fun write(byteArray: ByteArray, offset: Int, count: Int) {
        val codec = mediaCodec ?: return
        if (count <= 0) {
            return
        }
        val inputBufferIndex = codec.dequeueInputBuffer(TIMEOUT_INFINITE)
        if (inputBufferIndex >= 0) {
            codec.getInputBuffer(inputBufferIndex)?.let { inputBuffer ->
                inputBuffer.clear()
                inputBuffer.put(byteArray, offset, count)
                inputBuffer.limit(byteArray.size)
                codec.queueInputBuffer(inputBufferIndex, offset, count, 0, 0)
            }
        }
        output()
    }

    private fun output() {
        val codec = mediaCodec ?: return
        val bufferInfo = MediaCodec.BufferInfo()
        while (true) {
            val outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, TIMEOUT_NOW)
            if (outputBufferIndex < 0) {
                break
            }
            codec.getOutputBuffer(outputBufferIndex)?.let { outputBuffer ->
                outputBuffer.position(bufferInfo.offset)
                outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                val chunkAudio = ByteArray(bufferInfo.size + aacHeader.headerLength)
                addAdtsToPacket(chunkAudio, bufferInfo.size)
                outputBuffer.get(chunkAudio, aacHeader.headerLength, bufferInfo.size)
                outputBuffer.position(bufferInfo.offset)
                writeOut(chunkAudio, 0, chunkAudio.size)
                codec.releaseOutputBuffer(outputBufferIndex, false)
            }
        }
    }

    override fun flush() {
        super.flush()
        output()
    }

    private fun addAdtsToPacket(array: ByteArray, length: Int) {
        aacHeader.audioDataLength = length
        aacHeader.byteArray.forEachIndexed { index, byte ->
            array[index] = byte
        }
    }

}