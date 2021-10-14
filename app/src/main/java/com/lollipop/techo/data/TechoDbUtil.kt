package com.lollipop.techo.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
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

        private fun Cursor.getText(enum: Enum<*>, def: String = ""): String {
            val columnIndex = getColumnIndex(enum.name)
            if (columnIndex < 0) {
                return def
            }
            return getString(columnIndex)
        }

        private fun ContentValues.put(enum: Enum<*>, value: String) {
            put(enum.name, value)
        }

        private fun ContentValues.put(enum: Enum<*>, value: Int) {
            put(enum.name, value)
        }

        private fun Cursor.getInt(enum: Enum<*>, def: Int = 0): Int {
            return getInt(enum.name, def)
        }

        private fun Cursor.getInt(name: String, def: Int = 0): Int {
            val columnIndex = getColumnIndex(name)
            if (columnIndex < 0) {
                return def
            }
            return getInt(columnIndex)
        }

        private fun Cursor.getFloat(enum: Enum<*>, def: Float = 0F): Float {
            val columnIndex = getColumnIndex(enum.name)
            if (columnIndex < 0) {
                return def
            }
            return getFloat(columnIndex)
        }
    }

    private val flagCache = ArrayList<TechoFlag>()

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(FlagTable.getCreateSql())
        db.execSQL(TechoTable.getCreateSql())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }

    private val readDb: SQLiteDatabase
        get() {
            return readableDatabase
        }

    private val writeDb: SQLiteDatabase
        get() {
            return writableDatabase
        }

    fun selectTechoById(id: Int): TechoInfo {
        TODO()
    }

    fun selectAllFlag(cache: Boolean = true): List<TechoFlag> {
        synchronized(FlagTable) {
            if (cache && flagCache.isNotEmpty()) {
                return ArrayList<TechoFlag>().apply {
                    addAll(flagCache)
                }
            }
            flagCache.clear()
            val allFlag = FlagTable.queryAll(readDb)
            flagCache.addAll(allFlag)
            return allFlag
        }
    }

    fun selectTecho(pageIndex: Int, pageSize: Int = 20): List<TechoInfo> {
        TODO()
    }

    fun insertFlag(flag: TechoFlag) {
        synchronized(FlagTable) {
            flagCache.add(flag.copyTo())
            FlagTable.insert(writeDb, flag)
        }
    }

    fun insertTecho(info: TechoInfo) {
        TODO()
    }

    fun updateFlag(flag: TechoFlag) {
        synchronized(FlagTable) {
            flagCache.forEach {
                if (it.id == flag.id) {
                    flag.copyTo(it)
                }
            }
            FlagTable.update(writeDb, flag)
        }
    }

    fun updateTecho(info: TechoInfo) {
        TODO()
    }

    fun deleteFlag(id: Int) {
        synchronized(FlagTable) {
            val iterator = flagCache.iterator()
            while (iterator.hasNext()) {
                val flag = iterator.next()
                if (flag.id == id) {
                    iterator.remove()
                }
            }
            FlagTable.delete(writeDb, id)
        }
    }

    fun deleteTecho(id: Int) {
        TODO()
    }

    private enum class ColumnFormat {
        TEXT,
        INTEGER,
        FLOAT
    }

    private object FlagTable {

        const val NAME = "Flag"

        const val ID = "ID"

        private val COLUMNS: Array<String> by lazy {
            Column.values().map { it.name }.toTypedArray()
        }

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

        fun queryAll(db: SQLiteDatabase): List<TechoFlag> {
            val list = ArrayList<TechoFlag>()
            val cursor = db.query(
                NAME,
                COLUMNS,
                null,
                null,
                null,
                null,
                "$ID DESC"
            )
            while (cursor.moveToNext()) {
                list.add(
                    TechoFlag(
                        name = cursor.getText(Column.NAME),
                        color = cursor.getInt(Column.COLOR),
                        id = cursor.getInt(ID),
                    )
                )
            }
            cursor.close()
            return list
        }

        fun insert(db: SQLiteDatabase, flag: TechoFlag) {
            db.insert(
                NAME,
                null,
                ContentValues().apply {
                    put(Column.NAME, flag.name)
                    put(Column.COLOR, flag.color)
                })
        }

        fun update(db: SQLiteDatabase, flag: TechoFlag) {
            db.update(
                NAME,
                ContentValues().apply {
                    put(Column.NAME, flag.name)
                    put(Column.COLOR, flag.color)
                },
                "$ID = ?",
                arrayOf(flag.id.toString())
            )
        }

        fun delete(db: SQLiteDatabase, id: Int) {
            db.delete(
                NAME,
                "$ID = ?",
                arrayOf(id.toString())
            )
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

}