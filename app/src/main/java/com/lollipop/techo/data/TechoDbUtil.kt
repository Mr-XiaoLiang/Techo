package com.lollipop.techo.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

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

        private fun selectLastId(table: String, idName: String, db: SQLiteDatabase): Int {
            try {
                val sql = "SELECT $idName from $table order by $idName DESC limit 1"
                val cursor = db.rawQuery(sql, null)
                if (cursor.moveToNext()) {
                    val index = cursor.getColumnIndex(idName)
                    val rowId = if (index < 0) {
                        0
                    } else {
                        cursor.getInt(index)
                    }
                    cursor.close()
                    return rowId
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            return 0
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

    fun selectTechoById(id: Int): TechoInfo? {
        return TechoTable.selectById(readDb, id)
    }

    fun selectAllFlag(cache: Boolean = true): List<TechoFlag> {
        synchronized(FlagTable) {
            if (cache && flagCache.isNotEmpty()) {
                return ArrayList<TechoFlag>().apply {
                    addAll(flagCache)
                }
            }
            flagCache.clear()
            val allFlag = FlagTable.selectAll(readDb)
            flagCache.addAll(allFlag)
            return allFlag
        }
    }

    fun selectTecho(pageIndex: Int, pageSize: Int = 20): List<TechoInfo> {
        return TechoTable.selectAll(readDb, pageIndex, pageSize)
    }

    fun insertFlag(flag: TechoFlag): Int {
        synchronized(FlagTable) {
            flagCache.add(flag.copyTo())
            return FlagTable.insert(writeDb, flag)
        }
    }

    fun insertTecho(info: TechoInfo): Int {
        return TechoTable.insert(writeDb, info)
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
        TechoTable.update(writeDb, info)
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
        TechoTable.delete(writeDb, id)
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

        fun selectAll(db: SQLiteDatabase): List<TechoFlag> {
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

        fun insert(db: SQLiteDatabase, flag: TechoFlag): Int {
            val insert = db.insert(
                NAME,
                null,
                ContentValues().apply {
                    put(Column.NAME, flag.name)
                    put(Column.COLOR, flag.color)
                })
            if (insert >= 0) {
                return selectLastId(NAME, ID, db)
            }
            return 0
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

        private val COLUMNS: Array<String> by lazy {
            Column.values().map { it.name }.toTypedArray()
        }

        enum class Column(val format: ColumnFormat) {
            TITLE(ColumnFormat.TEXT),
            FLAG(ColumnFormat.INTEGER),
            CONTENT(ColumnFormat.TEXT)
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

        fun selectById(db: SQLiteDatabase, id: Int): TechoInfo? {
            val cursor = db.query(
                NAME,
                COLUMNS,
                "$ID = ?",
                arrayOf(id.toString()),
                null,
                null,
                null
            )
            if (cursor.moveToNext()) {
                val content = cursor.getText(Column.CONTENT)
                return TechoInfo.fromJson(content).apply {
                    this.flag = TechoFlag(
                        name = cursor.getText(FlagTable.Column.NAME),
                        color = cursor.getInt(FlagTable.Column.COLOR),
                        id = cursor.getInt(FlagTable.ID),
                    )
                    this.id = cursor.getInt(ID)
                    this.title = cursor.getText(Column.TITLE)
                }
            }
            cursor.close()
            return null
        }

        fun selectAll(db: SQLiteDatabase, pageIndex: Int, pageSize: Int = 20): List<TechoInfo> {
            val list = ArrayList<TechoInfo>()
            val cursor = db.rawQuery(
                "SELECT t.$ID AS $ID, " +
                        "${Column.TITLE}, " +
                        "${Column.FLAG}, " +
                        "${Column.CONTENT}, " +
                        "${FlagTable.Column.NAME}, " +
                        "${FlagTable.Column.COLOR} " +
                        "FROM $NAME t LEFT JOIN ${FlagTable.NAME} f ON t.${Column.FLAG} = f.${FlagTable.ID} " +
                        "ORDER BY t.$ID DESC " +
                        "LIMIT ? OFFSET ?",
                arrayOf(
                    pageSize.toString(),
                    (pageIndex * pageSize).toString()
                )
            )
            while (cursor.moveToNext()) {
                val content = cursor.getText(Column.CONTENT)
                list.add(
                    TechoInfo.fromJson(content).apply {
                        flag = TechoFlag(
                            name = cursor.getText(FlagTable.Column.NAME),
                            color = cursor.getInt(FlagTable.Column.COLOR),
                            id = cursor.getInt(FlagTable.ID),
                        )
                        id = cursor.getInt(ID)
                        title = cursor.getText(Column.TITLE)
                    }
                )
            }
            cursor.close()
            return list
        }

        fun insert(db: SQLiteDatabase, info: TechoInfo): Int {
            val insert = db.insert(
                NAME,
                null,
                ContentValues().apply {
                    put(Column.TITLE, info.title)
                    put(Column.FLAG, info.flag.id)
                    put(Column.CONTENT, info.toJson().toString())
                })
            if (insert >= 0) {
                return selectLastId(NAME, ID, db)
            }
            return 0
        }

        fun update(db: SQLiteDatabase, info: TechoInfo) {
            db.update(
                NAME,
                ContentValues().apply {
                    put(Column.TITLE, info.title)
                    put(Column.FLAG, info.flag.id)
                    put(Column.CONTENT, info.toJson().toString())
                },
                "$ID = ?",
                arrayOf(info.id.toString())
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

}