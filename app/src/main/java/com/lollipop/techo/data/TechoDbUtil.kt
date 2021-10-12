package com.lollipop.techo.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.lang.StringBuilder

/**
 * @author lollipop
 * @date 2021/10/12 20:13
 */
class TechoDbUtil(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, VERSION) {

    companion object {
        private const val DB_NAME = "techo"
        private const val VERSION = 1
    }

    private enum class ColumnFormat {
        TEXT,
        INTEGER,
        FLOAT,
        BOOLEAN
    }

    private object FlagTable {

        const val NAME = "Flag"

        const val ID = "ID"

        enum class Column(val format: ColumnFormat) {
            NAME(ColumnFormat.TEXT),
            COLOR(ColumnFormat.INTEGER)
        }

        fun getCreateSql(): String {
            val sql = StringBuilder()
            sql.append("CREATE TABLE $NAME ( $ID INTEGER PRIMARY KEY AUTOINCREMENT ")

            Column.values().forEach {
                sql.append(", ")
                sql.append(it.name)
                sql.append(" ")
                sql.append(it.format)
            }

            sql.append(" ) ")
            return sql.toString()
        }

    }

    private object TechoTable {

        const val NAME = "Techo"

        const val ID = "ID"

        enum class Column(val format: ColumnFormat) {
            TITLE(ColumnFormat.TEXT),
            FLAG(ColumnFormat.INTEGER),
            VALUES(ColumnFormat.TEXT)
        }

        fun getCreateSql(): String {
            val sql = StringBuilder()
            sql.append("CREATE TABLE $NAME ( $ID INTEGER PRIMARY KEY AUTOINCREMENT ")

            Column.values().forEach {
                sql.append(", ")
                sql.append(it.name)
                sql.append(" ")
                sql.append(it.format)
            }

            sql.append(" ) ")
            return sql.toString()
        }

    }

    override fun onCreate(db: SQLiteDatabase?) {
        db ?: return
        db.execSQL(FlagTable.getCreateSql())
        db.execSQL(TechoTable.getCreateSql())
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

}