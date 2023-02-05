package com.lollipop.browser.history

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class HistoryInfo(
    @PrimaryKey val uid: Int = 0,
    @ColumnInfo(name = "url") val url: String = "",
    @ColumnInfo(name = "title") val title: String = "",
    @ColumnInfo(name = "time") val time: Long = 0L,
    @ColumnInfo(name = "favicon") val favicon: String = ""
)