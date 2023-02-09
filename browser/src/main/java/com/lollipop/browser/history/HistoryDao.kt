package com.lollipop.browser.history

import android.content.ContentValues
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

    fun select(pageIndex: Int, pageSize: Int = 20): List<HistoryInfo> {
        return HistoryTable.selectByPage(readDb, pageIndex, pageSize)
    }

    fun insert(info: HistoryInfo, needId: Boolean = false): Int {
        return HistoryTable.insert(writeDb, info, needId)
    }

    fun delete(id: Int): Int {
        return HistoryTable.delete(writeDb, id)
    }

    fun deleteAll(db: SQLiteDatabase): Int {
        return HistoryTable.deleteAll(db)
    }

    private object HistoryTable : Table() {

        override val tableName: String = "History"
        override val columns: Array<out ColumnEnum>
            get() {
                return Column.values()
            }

        private val SELECT_ALL: String by lazy {
            val sqlBuilder = StringBuilder().append("SELECT $idColumn ")
            columns.forEach {
                sqlBuilder.append(", ")
                sqlBuilder.append(it.name)
            }
            sqlBuilder.append(" FROM ")
                .append(tableName)
                .append(" ORDER BY ")
                .append(idColumn)
                .append(" DESC LIMIT ? OFFSET ? ")
            sqlBuilder.toString()
        }

        fun selectByPage(db: SQLiteDatabase, pageIndex: Int, pageSize: Int): List<HistoryInfo> {
            return queryHistoryList(db) {
                it.rawQuery(
                    SELECT_ALL,
                    arrayOf(pageSize.toString(), (pageIndex * pageSize).toString())
                )
            }
        }

        fun insert(db: SQLiteDatabase, info: HistoryInfo, needId: Boolean = false): Int {
            val insert = db.insert(
                tableName,
                null,
                ContentValues().apply {
                    putValue(Column.URL, info.url)
                    putValue(Column.FAVICON, info.favicon)
                    putValue(Column.TIME, info.time)
                    putValue(Column.TITLE, info.title)
                }
            )
            if (needId && insert >= 0) {
                return selectLastId(tableName, idColumn, db)
            }
            return 0
        }

        fun delete(db: SQLiteDatabase, id: Int): Int {
            return db.delete(
                tableName,
                "$idColumn = ?",
                arrayOf(id.toString())
            )
        }

        fun deleteAll(db: SQLiteDatabase): Int {
            return db.delete(
                tableName,
                null,
                null
            )
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

        enum class Column(override val format: ColumnFormat) : ColumnEnum {
            TITLE(ColumnFormat.TEXT),
            URL(ColumnFormat.INTEGER),
            TIME(ColumnFormat.INTEGER),
            FAVICON(ColumnFormat.TEXT),
        }

    }

}