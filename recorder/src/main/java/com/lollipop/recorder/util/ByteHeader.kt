package com.lollipop.recorder.util

import kotlin.math.max
import kotlin.math.min

open class ByteHeader {

    companion object {
        /**
         * @param offset 放置的起始位置
         * @param value 放入value中的四个Byte
         */
        @JvmStatic
        protected fun ByteArray.putInt(offset: Int, value: Int): Int {
            return putBytes(offset, value, 4)
        }

        /**
         * @param offset 放置的起始位置
         * @param value 放入value中的最后两个Byte
         */
        @JvmStatic
        protected fun ByteArray.putShort(offset: Int, value: Int): Int {
            return putBytes(offset, value, 2)
        }

        /**
         * @param offset 放置的起始位置
         * @param value 放入value中的最后一个Byte
         */
        @JvmStatic
        protected fun ByteArray.putByte(offset: Int, value: Int): Int {
            return putBytes(offset, value, 1)
        }

        /**
         * @param offset 放置的起始位置
         * @param value 放入value中的最后几个Byte
         * @param count 放入的byte数量
         */
        @JvmStatic
        protected fun ByteArray.putBytes(offset: Int, value: Int, count: Int): Int {
            val end = max(min(count, 4), 0)
            for (i in 0 until end) {
                this[offset + i] = value.shr(i * 8).and(0xFF).toByte()
            }
            return count
        }
    }

    protected fun buildByteArray(array: ByteArray, offset: Int = 0): ByteArrayBuilder {
        return ByteArrayBuilder(array, offset)
    }

    protected class ByteArrayBuilder(
        val byteArray: ByteArray,
        private var offset: Int
    ) {

        fun putInt(value: Int): ByteArrayBuilder {
            offset += byteArray.putInt(offset, value)
            return this
        }

        fun putShort(value: Int): ByteArrayBuilder {
            offset += byteArray.putInt(offset, value)
            return this
        }

        fun putByte(value: Int): ByteArrayBuilder {
            offset += byteArray.putInt(offset, value)
            return this
        }

        fun putByteBits(vararg values: Int): ByteArrayBuilder {
            return putByte(getValue(values))
        }

        fun putShortBits(vararg values: Int): ByteArrayBuilder {
            return putShort(getValue(values))
        }

        fun putIntBits(vararg values: Int): ByteArrayBuilder {
            return putInt(getValue(values))
        }

        private fun getValue(array: IntArray): Int {
            var value = 0
            array.forEach {
                value = value.or(it)
            }
            return value
        }

    }

}