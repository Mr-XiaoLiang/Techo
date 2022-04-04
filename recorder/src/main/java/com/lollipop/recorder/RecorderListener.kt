package com.lollipop.recorder

interface RecorderListener {

    fun onFormatChanged(is16Bit: Boolean, channelCount: Int)

    fun onRecord(data: ByteArray, begin: Int, end: Int)

}