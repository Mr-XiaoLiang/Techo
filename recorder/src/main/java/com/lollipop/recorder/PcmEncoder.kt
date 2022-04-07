package com.lollipop.recorder

import java.io.OutputStream

/**
 * PCM的格式化工具
 */
abstract class PcmEncoder(private val outputStream: OutputStream) {



    abstract fun write(byteArray: ByteArray, offset: Int, count: Int)

    protected fun writeOut(byteArray: ByteArray, offset: Int, count: Int) {
        outputStream.write(byteArray, offset, count)
    }

    open fun flush() {
        flushOut()
    }

    protected fun flushOut() {
        outputStream.flush()
    }

}