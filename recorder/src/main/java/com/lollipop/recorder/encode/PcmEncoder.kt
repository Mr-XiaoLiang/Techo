package com.lollipop.recorder.encode

import java.io.OutputStream

/**
 * PCM的格式化工具
 */
abstract class PcmEncoder {

    protected var format: EncodeFormat = EncodeFormat.EMPTY
        private set
    private var outputStream: OutputStream? = null

    fun init(format: EncodeFormat, outputStream: OutputStream) {
        this.format = format
        this.outputStream = outputStream
        onReady()
    }

    open fun onReady() {

    }

    abstract fun write(byteArray: ByteArray, offset: Int, count: Int)

    protected fun writeOut(byteArray: ByteArray, offset: Int, count: Int) {
        outputStream?.write(byteArray, offset, count)
    }

    open fun flush() {
        flushOut()
    }

    protected fun flushOut() {
        outputStream?.flush()
    }

    fun destroy() {
        onDestroy()
        outputStream = null
        format = EncodeFormat.EMPTY
    }

    open fun onDestroy() {}

}