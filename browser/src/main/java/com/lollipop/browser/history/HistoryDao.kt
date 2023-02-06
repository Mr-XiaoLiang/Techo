package com.lollipop.browser.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history Limit :pageSize Offset :pageOffset")
    fun select(pageOffset: Int, pageSize: Int): List<HistoryInfo>

    @Insert
    fun insert(info: HistoryInfo)

}