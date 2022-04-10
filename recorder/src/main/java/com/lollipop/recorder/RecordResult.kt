package com.lollipop.recorder

import android.media.AudioRecord

sealed class RecordResult {
    class Success : RecordResult()

    /**
     * 常规错误
     */
    class GenericError(
        val code: Int = AudioRecord.ERROR,
        val msg: String = "Denotes a generic operation failure.",
        val obj: Any? = null
    ) : RecordResult() {
        companion object {
            fun create(e: Throwable): GenericError {
                return GenericError(msg = e.message ?: e.localizedMessage ?: "", obj = e)
            }
        }
    }

    /**
     * 错误数据
     */
    class BadValueError(
        val code: Int = AudioRecord.ERROR_BAD_VALUE,
        val msg: String = "Denotes a failure due to the use of an invalid value.",
        val obj: Any? = null
    ) : RecordResult()

    /**
     * 无效操作
     */
    class InvalidOperationError(
        val code: Int = AudioRecord.ERROR_INVALID_OPERATION,
        val msg: String = "Denotes a failure due to the improper use of a method.",
        val obj: Any? = null
    ) : RecordResult()

    /**
     * 无效操作对象
     */
    class DeadObjectError(
        val code: Int = AudioRecord.ERROR_DEAD_OBJECT,
        val msg: String = "An error code indicating that the object reporting it is no longer valid and needs to be recreated.",
        val obj: Any? = null
    ) : RecordResult()

}