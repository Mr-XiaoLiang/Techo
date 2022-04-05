package com.lollipop.recorder

interface RecordStatusListener {

    fun onRecordStart()

    fun onRecordStop(result: RecordResult)

}