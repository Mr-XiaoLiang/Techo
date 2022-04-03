package com.lollipop.recorder

interface RecorderListener {

    fun onRecord(data: ByteArray, begin: Int, end: Int)

    fun onStart()

    fun onStop()

}