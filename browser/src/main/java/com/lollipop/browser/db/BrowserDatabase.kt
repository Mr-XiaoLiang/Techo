package com.lollipop.browser.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lollipop.browser.history.HistoryDao
import com.lollipop.browser.history.HistoryInfo

@Database(entities = [HistoryInfo::class], version = 1)
abstract class BrowserDatabase : RoomDatabase() {

    companion object {
        fun create(context: Context): BrowserDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                BrowserDatabase::class.java, "browser"
            ).build()
        }
    }

    abstract fun historyDao(): HistoryDao

}


