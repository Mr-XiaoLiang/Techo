package com.lollipop.recorder

import kotlin.math.min
object RecordHelper {

    var bigEndian = false

    fun byteToShort(byteFirst: Byte, byteSecond: Byte): Short {
        val first = byteFirst.toUInt()
        val second = byteSecond.toUInt()
        return if (bigEndian) {
            ((first shl 8) or second).toShort()
        } else {
            ((second shl 8) or first).toShort()
        }
    }

    fun shortToByte(s: Short): ByteArray {
        val src = s.toUInt()
        val byteMask = 0x00ff.toUInt()
        return if (bigEndian) {
            byteArrayOf(
                ((src shr 8) and byteMask).toByte(),
                (src and byteMask).toByte()
            )
        } else {
            byteArrayOf(
                (src and byteMask).toByte(),
                ((src shr 8) and byteMask).toByte()
            )
        }
    }

    fun readBy16Bit(channelCount: Int, data: ByteArray, begin: Int, end: Int): ShortResult {
        val stepSize = channelCount * 2
        val resultList = ArrayList<ShortArray>()
        for (block in begin until end step stepSize) {
            val blockEnd = (min(end, block + stepSize) - begin) / 2 * 2 + begin
            if (blockEnd <= block) {
                break
            }
            val array = ShortArray(channelCount)
            for (i in block until blockEnd step 2) {
                array[(i - block) / 2] = byteToShort(data[i], data[i + 1])
            }
            resultList.add(array)
        }
        return ShortResult(resultList.toTypedArray())
    }

    fun readBy8Bit(channelCount: Int, data: ByteArray, begin: Int, end: Int): ByteResult {
        val resultList = ArrayList<ByteArray>()
        for (index in begin until end step channelCount) {
            val channelEnd = min(data.size, index + channelCount)
            val array = ByteArray(channelCount)
            for (i in index until channelEnd) {
                array[i - index] = data[i]
            }
            resultList.add(array)
        }
        return ByteResult(resultList.toTypedArray())
    }

    class ShortResult(val data: Array<ShortArray>)

    class ByteResult(val data: Array<ByteArray>)

}