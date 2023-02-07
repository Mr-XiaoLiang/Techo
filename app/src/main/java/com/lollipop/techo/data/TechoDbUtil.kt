package com.lollipop.techo.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.lollipop.base.util.DatabaseHelper
import com.lollipop.base.util.DatabaseHelper.getIntByName
import com.lollipop.base.util.DatabaseHelper.getTextByName
import com.lollipop.base.util.DatabaseHelper.putValue
import com.lollipop.techo.data.json.mapToJson
import com.lollipop.techo.data.json.toJsonObject

/**
 * @author lollipop
 * @date 2021/10/12 20:13
 */
class TechoDbUtil(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, VERSION) {

    companion object {
        private const val DB_NAME = "techo"
        private const val VERSION = 1

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

        private inline fun <reified T : Any> queryList(
            db: SQLiteDatabase,
            rawQueryCallback: (SQLiteDatabase) -> Cursor,
            mapCallback: (Cursor) -> T
        ): List<T> {
            val list = ArrayList<T>()
            val cursor = rawQueryCallback(db)
            while (cursor.moveToNext()) {
                list.add(mapCallback(cursor))
            }
            cursor.close()
            return list
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

    fun selectFlagByName(name: String): List<TechoFlag> {
        return FlagTable.selectByName(readDb, name)
    }

    fun selectTecho(pageIndex: Int, pageSize: Int = 20): List<TechoInfo> {
        return TechoTable.selectAll(readDb, pageIndex, pageSize)
    }

    fun selectTechoByKeyword(keyword: String, pageIndex: Int, pageSize: Int = 20): List<TechoInfo> {
        return TechoTable.selectByKeyword(readDb, keyword, pageIndex, pageSize)
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

    private object FlagTable {

        const val NAME = "Flag"

        const val ID = "ID"

        private val COLUMNS: Array<String> by lazy {
            Column.values().map { it.name }.toTypedArray()
        }

        enum class Column(val format: DatabaseHelper.ColumnFormat) {
            NAME(DatabaseHelper.ColumnFormat.TEXT),
            COLOR(DatabaseHelper.ColumnFormat.INTEGER)
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

        private fun queryTechoFlagList(
            db: SQLiteDatabase,
            rawQueryCallback: (SQLiteDatabase) -> Cursor,
        ): List<TechoFlag> {
            return queryList(db, rawQueryCallback) {
                TechoFlag.create(
                    id = it.getIntByName(ID),
                    name = it.getTextByName(Column.NAME),
                    color = it.getIntByName(Column.COLOR),
                )
            }
        }

        fun selectByName(db: SQLiteDatabase, name: String): List<TechoFlag> {
            return queryTechoFlagList(db) {
                db.query(
                    NAME,
                    COLUMNS,
                    "${Column.NAME} LIKE ?",
                    arrayOf("%${name}%"),
                    null,
                    null,
                    "$ID DESC"
                )
            }
        }

        fun selectAll(db: SQLiteDatabase): List<TechoFlag> {
            return queryTechoFlagList(db) {
                db.query(
                    NAME,
                    COLUMNS,
                    null,
                    null,
                    null,
                    null,
                    "$ID DESC"
                )
            }
        }

        fun insert(db: SQLiteDatabase, flag: TechoFlag): Int {
            val insert = db.insert(
                NAME,
                null,
                ContentValues().apply {
                    putValue(Column.NAME, flag.name)
                    putValue(Column.COLOR, flag.color)
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
                    putValue(Column.NAME, flag.name)
                    putValue(Column.COLOR, flag.color)
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

        enum class Column(val format: DatabaseHelper.ColumnFormat) {
            TITLE(DatabaseHelper.ColumnFormat.TEXT),
            FLAG(DatabaseHelper.ColumnFormat.INTEGER),
            CONTENT(DatabaseHelper.ColumnFormat.TEXT),
            KEYWORD(DatabaseHelper.ColumnFormat.TEXT),
        }

        val SELECT_ALL = "SELECT t.$ID AS $ID, " +
                "${Column.TITLE}, " +
                "${Column.FLAG}, " +
                "${Column.CONTENT}, " +
                "${Column.KEYWORD}, " +
                "${FlagTable.Column.NAME}, " +
                "${FlagTable.Column.COLOR} " +
                "FROM $NAME t LEFT JOIN ${FlagTable.NAME} f ON t.${Column.FLAG} = f.${FlagTable.ID} " +
                "ORDER BY t.$ID DESC " +
                "LIMIT ? OFFSET ?"

        val SELECT_BY_KEYWORD = "SELECT t.$ID AS $ID, " +
                "${Column.TITLE}, " +
                "${Column.FLAG}, " +
                "${Column.CONTENT}, " +
                "${Column.KEYWORD}, " +
                "${FlagTable.Column.NAME}, " +
                "${FlagTable.Column.COLOR} " +
                "FROM $NAME t LEFT JOIN ${FlagTable.NAME} f ON t.${Column.FLAG} = f.${FlagTable.ID} " +
                "WHERE ${Column.KEYWORD} LIKE ? " +
                "ORDER BY t.$ID DESC " +
                "LIMIT ? OFFSET ?"

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
            var info: TechoInfo? = null
            if (cursor.moveToNext()) {
                info = cursor.toTechoInfo()
            }
            cursor.close()
            return info
        }

        private fun queryTechoList(
            db: SQLiteDatabase,
            rawQueryCallback: (SQLiteDatabase) -> Cursor
        ): List<TechoInfo> {
            return queryList(db, rawQueryCallback) {
                it.toTechoInfo()
            }
        }

        fun selectByKeyword(
            db: SQLiteDatabase,
            keyword: String,
            pageIndex: Int,
            pageSize: Int = 20
        ): List<TechoInfo> {
            return queryTechoList(db) {
                db.rawQuery(
                    SELECT_BY_KEYWORD,
                    arrayOf(
                        "%${keyword}%",
                        pageSize.toString(),
                        (pageIndex * pageSize).toString()
                    )
                )
            }
        }

        fun selectAll(db: SQLiteDatabase, pageIndex: Int, pageSize: Int = 20): List<TechoInfo> {
            return queryTechoList(db) {
                db.rawQuery(
                    SELECT_ALL,
                    arrayOf(
                        pageSize.toString(),
                        (pageIndex * pageSize).toString()
                    )
                )
            }
        }

        fun insert(db: SQLiteDatabase, info: TechoInfo): Int {
            val insert = db.insert(
                NAME,
                null,
                info.toContentValues()
            )
            if (insert >= 0) {
                return selectLastId(NAME, ID, db)
            }
            return 0
        }

        fun update(db: SQLiteDatabase, info: TechoInfo) {
            db.update(
                NAME,
                info.toContentValues(),
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

        private fun Cursor.toTechoInfo(): TechoInfo {
            val cursor = this
            return TechoInfo().apply {
                parse(cursor.getTextByName(Column.CONTENT).toJsonObject())
                flag = TechoFlag.create(
                    id = cursor.getIntByName(FlagTable.ID),
                    name = cursor.getTextByName(FlagTable.Column.NAME),
                    color = cursor.getIntByName(FlagTable.Column.COLOR),
                )
                id = cursor.getIntByName(ID)
                title = cursor.getTextByName(Column.TITLE)
            }
        }

        private fun TechoInfo.toContentValues(): ContentValues {
            val info = this
            return ContentValues().apply {
                putValue(Column.TITLE, info.title)
                putValue(Column.FLAG, info.flag.id)
                putValue(Column.CONTENT, info.toJson().toString())
                putValue(Column.KEYWORD, info.keyWords.mapToJson().toString())
            }
        }
    }

}