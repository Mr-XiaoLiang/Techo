package com.lollipop.browser.history

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.lollipop.base.util.DatabaseHelper

class HistoryDao(context: Context) : DatabaseHelper(context, DB_NAME, null, VERSION) {

    companion object {
        private const val DB_NAME = "history"
        private const val VERSION = 1
    }

    override fun getTableArray(): Array<Table> {
        return arrayOf(HistoryTable)
    }

    private object HistoryTable: Table() {

        override val tableName: String = "History"
        override val columns: Array<out ColumnEnum>
            get() {
                return Column.values()
            }

        fun getCreateSql(): String {
            return createSql(tableName, idColumn, Column.values())
        }

        private fun queryHistoryList(
            db: SQLiteDatabase,
            rawQueryCallback: (SQLiteDatabase) -> Cursor,
        ): List<HistoryInfo> {
            return queryList(db, rawQueryCallback) {
                HistoryInfo(
                    id = it.getIntByName(idColumn),
                    url = it.getTextByName(Column.URL),
                    title = it.getTextByName(Column.TITLE),
                    favicon = it.getTextByName(Column.TITLE),
                    time = it.getLongByName(Column.TITLE),
                )
            }
        }

        enum class Column(override val format: ColumnFormat): ColumnEnum {
            TITLE(ColumnFormat.TEXT),
            URL(ColumnFormat.INTEGER),
            TIME(ColumnFormat.INTEGER),
            FAVICON(ColumnFormat.TEXT),
        }

    }

}