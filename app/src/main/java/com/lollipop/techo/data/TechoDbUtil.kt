package com.lollipop.techo.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.lollipop.base.util.DatabaseHelper
import com.lollipop.techo.data.json.mapToJson
import com.lollipop.techo.data.json.toJsonObject

/**
 * @author lollipop
 * @date 2021/10/12 20:13
 */
class TechoDbUtil(context: Context) : DatabaseHelper(context, DB_NAME, null, VERSION) {

    companion object {
        private const val DB_NAME = "techo"
        private const val VERSION = 1
    }

    private val flagCache = ArrayList<TechoFlag>()

    override fun getTableArray(): Array<Table> {
        return arrayOf(FlagTable, TechoTable)
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

    private object FlagTable : Table() {

        override val tableName: String = "Flag"
        override val columns: Array<out ColumnEnum>
            get() {
                return Column.entries.toTypedArray()
            }

        enum class Column(override val format: ColumnFormat) : ColumnEnum {
            NAME(ColumnFormat.TEXT),
            COLOR(ColumnFormat.INTEGER)
        }

        private fun queryTechoFlagList(
            db: SQLiteDatabase,
            rawQueryCallback: (SQLiteDatabase) -> Cursor,
        ): List<TechoFlag> {
            return queryList(db, rawQueryCallback) {
                TechoFlag.create(
                    id = it.getIntByName(idColumn),
                    name = it.getTextByName(Column.NAME),
                    color = it.getIntByName(Column.COLOR),
                )
            }
        }

        fun selectByName(db: SQLiteDatabase, name: String): List<TechoFlag> {
            return queryTechoFlagList(db) {
                db.query(
                    tableName,
                    columnsName,
                    "${Column.NAME} LIKE ?",
                    arrayOf("%${name}%"),
                    null,
                    null,
                    "$idColumn DESC"
                )
            }
        }

        fun selectAll(db: SQLiteDatabase): List<TechoFlag> {
            return queryTechoFlagList(db) {
                db.query(
                    tableName,
                    columnsName,
                    null,
                    null,
                    null,
                    null,
                    "$idColumn DESC"
                )
            }
        }

        fun insert(db: SQLiteDatabase, flag: TechoFlag): Int {
            val insert = db.insert(
                tableName,
                null,
                ContentValues().apply {
                    putValue(Column.NAME, flag.name)
                    putValue(Column.COLOR, flag.color)
                })
            if (insert >= 0) {
                return selectLastId(tableName, idColumn, db)
            }
            return 0
        }

        fun update(db: SQLiteDatabase, flag: TechoFlag) {
            db.update(
                tableName,
                ContentValues().apply {
                    putValue(Column.NAME, flag.name)
                    putValue(Column.COLOR, flag.color)
                },
                "$idColumn = ?",
                arrayOf(flag.id.toString())
            )
        }

        fun delete(db: SQLiteDatabase, id: Int) {
            db.delete(
                tableName,
                "$idColumn = ?",
                arrayOf(id.toString())
            )
        }

    }

    private object TechoTable : Table() {

        override val tableName: String = "Techo"

        override val columns: Array<out ColumnEnum>
            get() {
                return Column.entries.toTypedArray()
            }

        enum class Column(override val format: ColumnFormat) : ColumnEnum {
            TITLE(ColumnFormat.TEXT),
            FLAG(ColumnFormat.INTEGER),
            CONTENT(ColumnFormat.TEXT),
            KEYWORD(ColumnFormat.TEXT),
        }

        val SELECT_ALL = "SELECT t.${idColumn} AS ${idColumn}, " +
                "${Column.TITLE}, " +
                "${Column.FLAG}, " +
                "${Column.CONTENT}, " +
                "${Column.KEYWORD}, " +
                "${FlagTable.Column.NAME}, " +
                "${FlagTable.Column.COLOR} " +
                "FROM $tableName t LEFT JOIN ${FlagTable.tableName} f ON t.${Column.FLAG} = f.${FlagTable.idColumn} " +
                "ORDER BY t.${idColumn} DESC " +
                "LIMIT ? OFFSET ?"

        val SELECT_BY_KEYWORD = "SELECT t.${idColumn} AS ${idColumn}, " +
                "${Column.TITLE}, " +
                "${Column.FLAG}, " +
                "${Column.CONTENT}, " +
                "${Column.KEYWORD}, " +
                "${FlagTable.Column.NAME}, " +
                "${FlagTable.Column.COLOR} " +
                "FROM $tableName t LEFT JOIN ${FlagTable.tableName} f ON t.${Column.FLAG} = f.${FlagTable.idColumn} " +
                "WHERE ${Column.KEYWORD} LIKE ? " +
                "ORDER BY t.${idColumn} DESC " +
                "LIMIT ? OFFSET ?"

        fun selectById(db: SQLiteDatabase, id: Int): TechoInfo? {
            val cursor = db.query(
                tableName,
                columnsName,
                "${idColumn} = ?",
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
                tableName,
                null,
                info.toContentValues()
            )
            if (insert >= 0) {
                return selectLastId(tableName, idColumn, db)
            }
            return 0
        }

        fun update(db: SQLiteDatabase, info: TechoInfo) {
            db.update(
                tableName,
                info.toContentValues(),
                "${idColumn} = ?",
                arrayOf(info.id.toString())
            )
        }

        fun delete(db: SQLiteDatabase, id: Int) {
            db.delete(
                tableName,
                "$idColumn = ?",
                arrayOf(id.toString())
            )
        }

        private fun Cursor.toTechoInfo(): TechoInfo {
            val cursor = this
            return TechoInfo().apply {
                parse(cursor.getTextByName(Column.CONTENT).toJsonObject())
                flag = TechoFlag.create(
                    id = cursor.getIntByName(FlagTable.idColumn),
                    name = cursor.getTextByName(FlagTable.Column.NAME),
                    color = cursor.getIntByName(FlagTable.Column.COLOR),
                )
                id = cursor.getIntByName(idColumn)
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