package com.lollipop.recorder.wav

import com.lollipop.recorder.util.ByteHeader

/**
 * WAV的头信息
 */
class WavHeader(
    /**
     * sampling frequency in Hz (e.g. 44100).
     * 采样率
     */
    private val sampleRate: Int = 0,
    /**
     * number of channels.
     * 声道数量
     */
    private val channels: Int = 0,
    /**
     * total number of samples per channel.
     * 样本数量，数据块的数量
     * 一块数据 = 2 * 声道数量
     */
    private val mumSamples: Int = 0,
): ByteHeader() {

    /**
     * number of bytes per sample, all channels included.
     */
    private val numBytesPerSample: Int = 2 * channels

    /**
     *  the complete header.
     */
    val header: ByteArray by lazy {
        createHeader()
    }

    override fun toString(): String {
        var str = ""
        val num32bitsPerLines = 8
        header.forEachIndexed { index, byte ->
            val breakLine = index > 0 && index % (num32bitsPerLines * 4) == 0
            val insertSpace = index > 0 && index % 4 == 0 && !breakLine
            if (breakLine) {
                str += '\n'
            }
            if (insertSpace) {
                str += ' '
            }
            str += String.format("%02X", byte)
        }
        return str
    }

    private fun createHeader(): ByteArray {
        val header = ByteArray(44)
        var offset = 0
        // set the RIFF chunk
        System.arraycopy(
            byteArrayOf('R'.code.toByte(), 'I'.code.toByte(), 'F'.code.toByte(), 'F'.code.toByte()),
            0,
            header,
            offset,
            4
        )
        offset += 4
        offset += header.putInt(offset, 36 + mumSamples * numBytesPerSample)

        // set the WAVE chunk
        System.arraycopy(
            byteArrayOf('W'.code.toByte(), 'A'.code.toByte(), 'V'.code.toByte(), 'E'.code.toByte()),
            0,
            header,
            offset,
            4
        )
        offset += 4

        // set the fmt chunk
        System.arraycopy(
            byteArrayOf('f'.code.toByte(), 'm'.code.toByte(), 't'.code.toByte(), ' '.code.toByte()),
            0,
            header,
            offset,
            4
        )
        offset += 4
        // chunk size = 16
        System.arraycopy(byteArrayOf(0x10, 0, 0, 0), 0, header, offset, 4)
        offset += 4
        // format = 1 for PCM
        System.arraycopy(byteArrayOf(1, 0), 0, header, offset, 2)
        offset += 2
        offset += header.putShort(offset, channels)
        offset += header.putInt(offset, sampleRate)
        offset += header.putInt(offset, sampleRate * numBytesPerSample)
        offset += header.putShort(offset, numBytesPerSample)
        System.arraycopy(byteArrayOf(0x10, 0), 0, header, offset, 2)
        offset += 2

        // set the beginning of the data chunk
        System.arraycopy(
            byteArrayOf('d'.code.toByte(), 'a'.code.toByte(), 't'.code.toByte(), 'a'.code.toByte()),
            0,
            header,
            offset,
            4
        )
        offset += 4
        offset += header.putInt(offset, mumSamples * numBytesPerSample)
        return header
    }

}