package com.lollipop.recorder.aac

import com.lollipop.recorder.util.ByteHeader

class AacHeader(
    val useMpeg4: Boolean = false,
    val hasCrc: Boolean = false,
) : ByteHeader() {

    val byteArray: ByteArray
        get() {
            val byteArray = ByteArray(7)
            putFixedHeader(byteArray)
            putVariableHeader(byteArray)
            return byteArray
        }

    private fun putFixedHeader(array: ByteArray) {
        // syncword：帧同步标识一个帧的开始，固定为0xFFF
        val syncWord = 0xFFF0
        // ID：MPEG 标示符。0表示MPEG-4，1表示MPEG-2
        val id = if (useMpeg4) { 0 } else { 0x8 }
        // layer：固定为'00'
        val layer = 0
        // protection_absent：标识是否进行误码校验。0表示有CRC校验，1表示没有CRC校验
        val protectionAbsent = if (hasCrc) { 0 } else { 0x1 }
        // 一般情况下：bit = 1111 1111 1111 1001
        array.putShort(0, syncWord or id or layer or protectionAbsent)


        // TODO
    }

    private fun putVariableHeader(array: ByteArray) {
        // TODO
    }

    /**
     * 0: Null
     * 1: AAC Main
     * 2: AAC LC (Low Complexity)
     * 3: AAC SSR (Scalable Sample Rate)
     * 4: AAC LTP (Long Term Prediction)
     * 5: SBR (Spectral Band Replication)
     * 6: AAC Scalable
     * 7: TwinVQ
     * 8: CELP (Code Excited Linear Prediction)
     * 9: HXVC (Harmonic Vector eXcitation Coding)
     * 10: Reserved
     * 11: Reserved
     * 12: TTSI (Text-To-Speech Interface)
     * 13: Main Synthesis
     * 14: Wavetable Synthesis
     * 15: General MIDI
     * 16: Algorithmic Synthesis and Audio Effects
     * 17: ER (Error Resilient) AAC LC
     * 18: Reserved
     * 19: ER AAC LTP
     * 20: ER AAC Scalable
     * 21: ER TwinVQ
     * 22: ER BSAC (Bit-Sliced Arithmetic Coding)
     * 23: ER AAC LD (Low Delay)
     * 24: ER CELP
     * 25: ER HVXC
     * 26: ER HILN (Harmonic and Individual Lines plus Noise)
     * 27: ER Parametric
     * 28: SSC (SinuSoidal Coding)
     * 29: PS (Parametric Stereo)
     * 30: MPEG Surround
     * 31: (Escape value)
     * 32: Layer-1
     * 33: Layer-2
     * 34: Layer-3
     * 35: DST (Direct Stream Transfer)
     * 36: ALS (Audio Lossless)
     * 37: SLS (Scalable LosslesS)
     * 38: SLS non-core
     * 39: ER AAC ELD (Enhanced Low Delay)
     * 40: SMR (Symbolic Music Representation) Simple
     * 41: SMR Main
     * 42: USAC (Unified Speech and Audio Coding) (no SBR)
     * 43: SAOC (Spatial Audio Object Coding)
     * 44: LD MPEG Surround
     * 45: USAC
     */
    enum class Profile(val id: Int) {
        AAC_MAIN(1),

        /**
         * Low Complexity
         */
        AAC_LC(2),

        /**
         * Scalable Sample Rate
         */
        AAC_SSR(3),

        /**
         * Long Term Prediction
         */
        AAC_LTP(4),
    }

}