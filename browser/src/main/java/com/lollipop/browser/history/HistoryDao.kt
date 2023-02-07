package com.lollipop.browser.history

import com.lollipop.base.util.DatabaseHelper

class HistoryDao {

//    @Query("SELECT * FROM history Limit :pageSize Offset :pageOffset")
//    fun select(pageOffset: Int, pageSize: Int): List<HistoryInfo>
//
//    @Insert
//    fun insert(info: HistoryInfo)

    private object HistoryTable {
//        id: Int,
//        url: String,
//        title: String,
//        time: Long,
//        favicon: String

        const val NAME = "History"
        const val ID = "ID"

        enum class Column(val format: DatabaseHelper.ColumnFormat) {
            TITLE(DatabaseHelper.ColumnFormat.TEXT),
            URL(DatabaseHelper.ColumnFormat.INTEGER),
            TIME(DatabaseHelper.ColumnFormat.INTEGER),
            FAVICON(DatabaseHelper.ColumnFormat.TEXT),
        }
    }

}