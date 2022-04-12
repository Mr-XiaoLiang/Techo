package com.lollipop.recorder.util

import kotlin.math.max
import kotlin.math.min

open class ByteHeader {

    /**
     * @param offset 放置的起始位置
     * @param value 放入value中的四个Byte
     */
    protected fun ByteArray.putInt(offset: Int, value: Int): Int {
        return putBytes(offset, value, 4)
    }

    /**
     * @param offset 放置的起始位置
     * @param value 放入value中的最后两个Byte
     */
    protected fun ByteArray.putShort(offset: Int, value: Int): Int {
        return putBytes(offset, value, 2)
    }

    /**
     * @param offset 放置的起始位置
     * @param value 放入value中的最后一个Byte
     */
    protected fun ByteArray.putByte(offset: Int, value: Int): Int {
        return putBytes(offset, value, 1)
    }

    /**
     * @param offset 放置的起始位置
     * @param value 放入value中的最后几个Byte
     * @param count 放入的byte数量
     */
    protected fun ByteArray.putBytes(offset: Int, value: Int, count: Int): Int {
        val end = max(min(count, 4), 0)
        for (i in 0 until end) {
            this[offset+i] = value.shr(i * 8).and(0xFF).toByte()
        }
        return count
    }

}