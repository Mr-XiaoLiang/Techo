package com.lollipop.browser.history

data class HistoryInfo(
    val id: Int,
    val url: String,
    val title: String,
    val time: Long,
    val favicon: String
)