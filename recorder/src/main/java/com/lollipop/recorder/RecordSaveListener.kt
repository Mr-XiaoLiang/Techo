package com.lollipop.recorder

interface RecordSaveListener {

    fun onSaveStart()

    /**
     * @param progress 进度为 0 ～ 1F
     */
    fun onSaveProgressChanged(progress: Float)

    fun onSaveEnd(result: RecordResult)

}