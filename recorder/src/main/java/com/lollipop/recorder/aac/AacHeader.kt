package com.lollipop.recorder.aac

import com.lollipop.recorder.encode.EncodeFormat
import com.lollipop.recorder.util.ByteHeader

class AacHeader(
    val useMpeg4: Boolean,
    val hasCrc: Boolean,
    val profile: Profile,
    val samplingFrequencies: SamplingFrequencies,
    val channel: ChannelConfiguration
) : ByteHeader() {

    companion object {
        fun create(
            format: EncodeFormat? = null,
            useMpeg4: Boolean = false,
            hasCrc: Boolean = false,
            profile: Profile = Profile.AAC_MAIN,
            samplingFrequencies: SamplingFrequencies? = null,
            channel: ChannelConfiguration? = null
        ): AacHeader {
            val sf = samplingFrequencies ?: if (format != null) {
                findSamplingFrequencies(format.sampleRate)
            } else {
                SamplingFrequencies.HZ_44100
            }
            val ch = channel ?: if (format != null) {
                findChannelConfiguration(format.channelCount)
            } else {
                ChannelConfiguration.CHANNEL_1
            }
            return AacHeader(
                useMpeg4 = useMpeg4,
                hasCrc = hasCrc,
                profile = profile,
                samplingFrequencies = sf,
                channel = ch
            )
        }

        private fun findSamplingFrequencies(hz: Int): SamplingFrequencies {
            SamplingFrequencies.values().forEach {
                if (it.hz == hz) {
                    return it
                }
            }
            return SamplingFrequencies.HZ_44100
        }

        private fun findChannelConfiguration(count: Int): ChannelConfiguration {
            ChannelConfiguration.values().forEach {
                if (it.count == count) {
                    return it
                }
            }
            return ChannelConfiguration.CHANNEL_1
        }
    }

    val headerLength: Int
        get() {
            return if (hasCrc) {
                9
            } else {
                7
            }
        }

    /**
     * 音频数据的长度，单位是byte，
     * 最大长度为：0x7FF, 即2047
     * 它不需要包含ADST头部的长度
     */
    var audioDataLength = 0

    /**
     * number_of_raw_data_blocks_in_frame
     * ADTS帧中的AAC帧数（raw data blocks），为获得最大兼容性，请始终为每个ADTS帧使用1个AAC帧
     */
    var rawBlocksSize = 1

    /**
     * 验证信息
     * 如果hasCrc为false，那么将不会添加这个字段
     * 如果hasCrc为true，那么将会添加到header中，但是可以选择不对它设置或者将它设置为0
     * 他的有效长度为2个byte，最大值为0xFFFF
     */
    var crc = 0

    val byteArray: ByteArray
        get() {
            val byteArray = ByteArray(headerLength)
            putFixedHeader(byteArray)
            putVariableHeader(byteArray)
            return byteArray
        }

    /**
     * 赋值固定消息头部分
     * 长度占用：28bit
     */
    private fun putFixedHeader(array: ByteArray) {
        val builder = buildByteArray(array, 0)
        // syncword：帧同步标识一个帧的开始，固定为0xFFF
        val syncWord = 0xFFF0
        // ID：MPEG 标示符。0表示MPEG-4，1表示MPEG-2
        val id = if (useMpeg4) {
            0
        } else {
            0x8
        }
        // layer：固定为'00'
        val layer = 0
        // protection_absent：标识是否进行误码校验。0表示有CRC校验，1表示没有CRC校验
        val protectionAbsent = if (hasCrc) {
            0
        } else {
            0x1
        }
        // 一般情况下：bit = 1111 1111 1111 1001
        builder.putShortBits(syncWord, id, layer, protectionAbsent)

        // profile 编码方式 有效位置：1100 0000
        val profileCode = profile.id.shl(6).and(0xC0)
        // sampling_frequency_index 采样率 有效位置 0011 1100
        val samplingFrequenciesCode = samplingFrequencies.id.shl(2).and(0x3C)
        //private_bit 专用位，保证MPEG永远不会使用，编码时设置为0，解码时忽略 有效位置 0000 0010
        val privateBit = 0
        // channel_configuration 声道数量 有效位置 0000 0001 1100 0000
        val channelId = channel.id
        // 有效位置 0000 0001
        val channelFirstByte = channelId.shr(2).and(0x1)
        // 有效位置 1100 0000
        val channelNextByte = channelId.shl(6).and(0xC0)

        // 有效位置 1111 1111
        builder.putByteBits(profileCode, samplingFrequenciesCode, privateBit, channelFirstByte)

        // original_copy 有效位置 0010 0000
        val originalCopy = 0
        // home 有效位置 0001 0000
        val home = 0
        // 有效位置 1111 0000
        builder.putByteBits(channelNextByte, originalCopy, home)

        // 以上，固定AAC头部信息的28bit赋值完毕
    }

    /**
     * 可变消息头部分
     * 长度占用：28bit
     */
    private fun putVariableHeader(array: ByteArray) {
        // 固定消息头有28bit，所以这里我们从第4个byte开始，有效位置从第4个byte的后4bit开始
        val builder = buildByteArray(array, 3)
        // 固定消息头的遗产，有效位置 1111 0000
        val fixedHeritage = array[4].toInt().and(0xF0)

        // copyrighted_id_bit 有效位置 0000 1000
        // 受版权保护的id位，集中注册的版权标识符的下一位，编码时设置为0，解码时忽略
        val copyrightedId = 0

        // copyrighted_id_start 有效位置 0000 0100
        // 版权id开始，表示此帧的版权id位是版权id的第一位，编码时设置为0，解码时忽略
        val copyrightedIdStart = 0

        // aac_frame_length ADTS帧长度包括ADTS长度和AAC声音数据长度的和
        // 有效位置 0000 0011  1111 1111  1110 0000
        // aac_frame_length = (protection_absent == 0 ? 9 : 7) + audio_data_length
        val aacFrameLength = headerLength + audioDataLength
        // 长度过长，切占据3个字节，所以拆分开来方便计算
        // 有效位置 0000 0011
        val aacFrameLengthFirst = aacFrameLength.shr(11).and(0x3)
        // 有效位置 1111 1111
        val aacFrameLengthNext = aacFrameLength.shr(3).and(0xFF)
        // 有效位置 1110 0000 0000 0000
        val aacFrameLengthEnd = aacFrameLength.shl(13).and(0xE0)

        builder.putByteBits(fixedHeritage, copyrightedId, copyrightedIdStart, aacFrameLengthFirst)
        builder.putByte(aacFrameLengthNext)

        // 固定为0x7FF。表示是码率可变的码流 有效位置 0001 1111 1111 1100
        val adtsBufferFullness = 0x7FF

        // number_of_raw_data_blocks_in_frame
        // ADTS帧中的AAC帧数（RDB）减去1，为获得最大兼容性，请始终为每个ADTS帧使用1个AAC帧
        // 有效位置 0000 0000 0000 0011
        val rawBlocksInFrame = (rawBlocksSize - 1).and(0x3)

        builder.putShortBits(aacFrameLengthEnd, adtsBufferFullness, rawBlocksInFrame)

        // 如果有crc验证位，那么放入crc验证信息
        if (hasCrc) {
            builder.putShort(crc)
        }

        // 以上，为ADTS的可变头部分的数据设置
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

    enum class SamplingFrequencies(val id: Int, val hz: Int) {
        HZ_96000(0, 96000),
        HZ_88200(1, 88200),
        HZ_64000(2, 64000),
        HZ_48000(3, 48000),
        HZ_44100(4, 44100),
        HZ_32000(5, 32000),
        HZ_24000(6, 24000),
        HZ_22050(7, 22050),
        HZ_16000(8, 16000),
        HZ_12000(9, 12000),
        HZ_11025(10, 11025),
        HZ_8000(11, 8000),
        HZ_7350(12, 7350);
    }

    enum class ChannelConfiguration(val id: Int, val count: Int) {
        /**
         * 1 channel: front-center
         */
        CHANNEL_1(1, 1),

        /**
         * 2 channels: front-left, front-right
         */
        CHANNEL_2(2, 2),

        /**
         * 3 channels: front-center, front-left, front-right
         */
        CHANNEL_3(3, 3),

        /**
         * 4 channels: front-center, front-left, front-right, back-center
         */
        CHANNEL_4(4, 4),

        /**
         * 5 channels: front-center, front-left, front-right, back-left, back-right
         */
        CHANNEL_5(5, 5),

        /**
         * 6 channels: front-center, front-left, front-right, back-left, back-right, LFE-channel
         */
        CHANNEL_6(6, 6),

        /**
         * 8 channels: front-center, front-left, front-right, side-left, side-right, back-left, back-right, LFE-channel
         */
        CHANNEL_8(7, 8);
    }

}