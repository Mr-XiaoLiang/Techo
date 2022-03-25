package com.lollipop.web.listener

interface LogPrinter {

    fun log(info: LogInfo)

    data class LogInfo(
        val message: String,
        val sourceId: String,
        val lineNumber: Int,
        val level: LogLevel
    )

    enum class LogLevel {
        TIP, LOG, WARNING, ERROR, DEBUG
    }

}