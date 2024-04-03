package com.lollipop.punch2.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.sql.Date
import java.util.Calendar

/**
 * @date: 2019/02/04 23:31
 * @author: lollipop
 * 数据库辅助类
 */
class DBHelper private constructor(
    context: Context,
    dbName: String,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
) : SQLiteOpenHelper(context, dbName, factory, version) {

    companion object {

        const val DB_NAME = "LPunch.db"
        const val VERSION = 1

        fun read(context: Context): DBOperate {
            return DBOperate(DBHelper(context).readableDatabase)
        }

        fun write(context: Context): DBOperate {
            return DBOperate(DBHelper(context).writableDatabase)
        }

        private val tempStringBuilder: StringBuilder by lazy {
            StringBuilder()
        }

    }

    private constructor(context: Context) : this(context, DB_NAME, null, VERSION)

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(LPunch.CREATE_SQL)
        db?.execSQL(FLAG.CREATE_SQL)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    private interface TableColumn {
        val key: String
    }

    private class SimpleColumn(override val key: String) : TableColumn

    private object LPunch {

        const val TABLE = "LPunch_TABLE"

        enum class Column(override val key: String) : TableColumn {
            ID("ID"),
            PUNCH_TIME("PUNCH_TIME"),
            FLAG("FLAG"),
            YEAR("PUNCH_YEAR"),
            MONTH("PUNCH_MONTH"),
            DAY("PUNCH_DAY");

            companion object {
                val allColumns: String by lazy {
                    val builder = StringBuilder()
                    val columns = Column.entries
                    for (i in columns.indices) {
                        if (i != 0) {
                            builder.append(", ")
                        }
                        builder.append(columns[i].key)
                    }
                    builder.toString()
                }
            }

        }

        val CREATE_SQL = "CREATE TABLE $TABLE ( " +
                " ${Column.ID} INTEGER PRIMARY KEY, " +
                " ${Column.PUNCH_TIME} TimeStamp NOT NULL DEFAULT CURRENT_TIMESTAMP , " +
                " ${Column.FLAG} INTEGER , " +
                " ${Column.YEAR} INTEGER ," +
                " ${Column.MONTH} INTEGER ," +
                " ${Column.DAY} INTEGER " +
                " ); "
    }

    private object FLAG {

        const val TABLE = "FLAG_TABLE"

        enum class Column(override val key: String) : TableColumn {
            ID("ID"),
            NAME("NAME"),
            COLOR("COLOR"),
            CREATE_TIME("CREATE_TIME"),
            DISABLE("DISABLE"),
            POSITION("POSITION");

            companion object {
                val allColumns: String by lazy {
                    val builder = StringBuilder()
                    val columns = Column.entries
                    for (i in columns.indices) {
                        if (i != 0) {
                            builder.append(", ")
                        }
                        builder.append(columns[i].key)
                    }
                    builder.toString()
                }
            }
        }

        val CREATE_SQL = "CREATE TABLE $TABLE ( " +
                " ${Column.ID} INTEGER PRIMARY KEY, " +
                " ${Column.NAME} TEXT , " +
                " ${Column.COLOR} INTEGER , " +
                " ${Column.CREATE_TIME} TimeStamp NOT NULL DEFAULT CURRENT_TIMESTAMP , " +
                " ${Column.DISABLE} INTEGER DEFAULT 0 , " +
                " ${Column.POSITION} INTEGER " +
                " ); "
    }

    private object SQL {
        val COUNT = SimpleColumn("A")

        val SELECT_ALL_FLAG = " SELECT ${FLAG.Column.allColumns} " +
                " FROM ${FLAG.TABLE} " +
                " ORDER BY ${FLAG.Column.POSITION} "

        val SELECT_UN_PUNCH = " SELECT ${FLAG.Column.allColumns} " +
                " FROM ${FLAG.TABLE} " +
                " WHERE ${FLAG.Column.ID} NOT IN ( " +
                "   SELECT ${LPunch.Column.FLAG} " +
                "   FROM ${LPunch.TABLE} " +
                "   WHERE ${LPunch.Column.YEAR} = ? " +
                "       AND  ${LPunch.Column.MONTH} = ? " +
                "       AND  ${LPunch.Column.DAY} = ? " +
                "   GROUP BY ${LPunch.Column.FLAG} " +
                " ) AND ${FLAG.Column.DISABLE} = 0 " +
                " ORDER BY ${FLAG.Column.POSITION} "


        val SELECT_BY_MONTH = " SELECT ${LPunch.Column.YEAR} , ${LPunch.Column.MONTH}" +
                " COUNT(1) as $COUNT , " +
                " ${FLAG.Column.NAME}, " +
                " ${FLAG.Column.COLOR} " +
                " from ${LPunch.TABLE} p " +
                "LEFT JOIN ${FLAG.TABLE} f " +
                "ON p.${LPunch.Column.FLAG} = f.${FLAG.Column.ID} " +
                " WHERE p.${LPunch.Column.YEAR} = ? AND p.${LPunch.Column.MONTH} = ? " +
                " GROUP BY p.${LPunch.Column.FLAG}, " +
                " p.${LPunch.Column.YEAR}, " +
                " p.${LPunch.Column.MONTH} " +
                " ORDER BY p.${LPunch.Column.YEAR}, p.${LPunch.Column.MONTH} "

        val SELECT_COUNT = "SELECT COUNT(1) as $COUNT , " +
                " f.${FLAG.Column.NAME} , " +
                " f.${FLAG.Column.COLOR} " +
                " from ${LPunch.TABLE} p " +
                "LEFT JOIN ${FLAG.TABLE} f " +
                "ON p.${LPunch.Column.FLAG} = f.${FLAG.Column.ID} " +
                " WHERE p.${LPunch.Column.PUNCH_TIME} >= ? AND p.${LPunch.Column.PUNCH_TIME} < ?" +
                " GROUP BY p.${LPunch.Column.FLAG}"

        val FLAG_MAX_ID = "SELECT ${FLAG.Column.ID} " +
                "FROM ${FLAG.TABLE} " +
                "ORDER BY ${FLAG.Column.ID} DESC " +
                "LIMIT 1 "

    }

    class DBOperate(private val database: SQLiteDatabase) {

        private fun ContentValues.put(key: TableColumn, value: String) {
            put(key.key, value)
        }

        private fun ContentValues.put(key: TableColumn, value: Int) {
            put(key.key, value)
        }

        private fun ContentValues.put(key: TableColumn, value: Long) {
            put(key.key, value)
        }

        /**
         * 插入一个新的Flag
         * 新的Flag的序号为当前的ID
         */
        fun installFlag(info: FlagInfo) = tryDo {
            logD("installFlag()")
            var newIndex = 1L
            val cursor = database.rawQuery(SQL.FLAG_MAX_ID, null)
            if (cursor?.moveToFirst() == true) {
                newIndex = cursor.getLong(0) + 1
            }
            cursor.close()
            val values = ContentValues()
            values.put(FLAG.Column.NAME, info.name)
            values.put(FLAG.Column.COLOR, info.color)
            values.put(FLAG.Column.POSITION, newIndex)
            val id = database.insert(FLAG.TABLE, "", values)
            logD("installFlag: $id")
            BoolResult.result(id >= 0)
        }

        /**
         * 插入一条打卡记录
         */
        fun installPunch(
            info: FlagInfo,
            year: Int,
            month: Int,
            day: Int,
        ) = tryDo {
            logD("installPunch()")
            val values = ContentValues()
            values.put(LPunch.Column.FLAG, info.id)
            values.put(LPunch.Column.YEAR, year)
            values.put(LPunch.Column.MONTH, month)
            values.put(LPunch.Column.DAY, day)
            values.put(
                LPunch.Column.PUNCH_TIME,
                "${year.format(4)}-${month.format()}-${day.format()} ${timeString()}"
            )// YYYY-MM-DD HH:MM:SS.SSS
            val rows = database.insert(LPunch.TABLE, "", values)
            logD("installPunch: $rows")
            BoolResult.result(rows > 0)
        }

        private fun timeString(): String {
            val time = System.currentTimeMillis()
            val ss = time % 1000
            val s = time / 1000 % 60
            val m = time / 1000 / 60 % 60
            val h = time / 1000 / 60 / 60 % 24
            return "${h.toInt().format()}:${m.toInt().format()}:${s.toInt().format()}.${
                ss.toInt().format(3)
            }"
        }

        private fun Int.format(size: Int = 2): String {
            tempStringBuilder.clear()
            tempStringBuilder.append(this)
            while (tempStringBuilder.length < size) {
                tempStringBuilder.insert(0, "0")
            }
            return tempStringBuilder.toString()
        }

        /**
         * 移除一个Flag
         * 为了保证历史数据的完整，将只做逻辑删除
         */
        fun removeFlag(id: String) = tryDo {
            logD("removeFlag()")
            val values = ContentValues()
            values.put(FLAG.Column.DISABLE, 1)
            val rows = database.update(FLAG.TABLE, values, " ${FLAG.Column.ID} = ? ", arrayOf(id))
            logD("removeFlag: $rows")
            BoolResult.result(rows > 0)
        }

        /**
         * 找回一个Flag
         */
        fun retrieveFlag(id: String) = tryDo {
            logD("retrieveFlag()")
            val values = ContentValues()
            values.put(FLAG.Column.DISABLE, 0)
            val rows = database.update(FLAG.TABLE, values, " ${FLAG.Column.ID} = ? ", arrayOf(id))
            logD("retrieveFlag: $rows")
            BoolResult.result(rows > 0)
        }

        /**
         * 查询指定日期的标签集合
         */
        fun selectFlag(
            year: Int,
            month: Int,
            day: Int,
        ) = trySelect {
            logD("selectFlag()")
            val list = ArrayList<FlagInfo>()
            val cursor = database.rawQuery(SQL.SELECT_UN_PUNCH, arrayOf("$year", "$month", "$day"))
            while (cursor.moveToNext()) {
                list.add(parseFlag(cursor))
            }
            logD("selectFlag: size -> ${list.size}")
            cursor.close()
            ListResult.Success(list)
        }

        fun selectAllFlag() = trySelect {
            logD("selectAllFlag()")
            val list = ArrayList<FlagInfo>()
            val cursor = database.rawQuery(SQL.SELECT_ALL_FLAG, null)
            while (cursor.moveToNext()) {
                list.add(parseFlag(cursor))
            }
            logD("selectAllFlag: size -> ${list.size}")
            cursor.close()
            ListResult.Success(list)
        }

        private fun parseFlag(cursor: Cursor): FlagInfo {
            logD("parseFlag()")
            return FlagInfo(
                id = "" + cursor.findIntByName(FLAG.Column.ID),
                name = cursor.findStringByName(FLAG.Column.NAME),
                color = cursor.findIntByName(FLAG.Column.COLOR),
                position = cursor.findIntByName(FLAG.Column.POSITION),
                disable = cursor.findIntByName(FLAG.Column.DISABLE) == 1
            )
        }

        private fun Cursor.findIntByName(name: TableColumn, def: Int = 0): Int {
            val cursor = this
            val columnIndex = cursor.getColumnIndex(name.key)
            if (columnIndex < 0) {
                return def
            }
            return cursor.getInt(columnIndex)
        }

        private fun Cursor.findStringByName(name: TableColumn, def: String = ""): String {
            val cursor = this
            val columnIndex = cursor.getColumnIndex(name.key)
            if (columnIndex < 0) {
                return def
            }
            return cursor.getString(columnIndex)
        }

        /**
         * 改变Flag的顺序
         * 方法将启用数据库事务，一旦更新行数与请求数据数量不同
         * 将会驳回操作
         */
        fun moveFlag(list: ArrayList<FlagInfo>) = tryDo {
            logD("moveFlag(${list.size})")
            database.beginTransaction()
            val values = ContentValues()
            var rows = 0
            try {
                for (info in list) {
                    values.clear()
                    values.put(FLAG.Column.POSITION, info.position)
                    rows += database.update(
                        FLAG.TABLE,
                        values,
                        " ${FLAG.Column.ID} = ? ",
                        arrayOf(info.id)
                    )
                }
                if (rows == list.size) {
                    database.setTransactionSuccessful()
                }
                logD("moveFlag: rows -> $rows")
            } catch (e: Exception) {
                e.printStackTrace()
                logD("moveFlag: error -> $e")
            } finally {
                database.endTransaction()
            }
            BoolResult.result(rows == list.size)
        }

        /**
         * 查询指定时间段内的每月打卡次数
         */
        private fun selectByMonth(year: Int, month: Int) = trySelect {
            logD("selectByMonth($year, $month)")
            val list = ArrayList<CountInfo>()
            val cursor = database.rawQuery(SQL.SELECT_BY_MONTH, arrayOf("$year", "$month"))
            while (cursor.moveToNext()) {
                list.add(CountInfo(
                    name = cursor.findStringByName(FLAG.Column.NAME),
                    color = cursor.findIntByName(FLAG.Column.COLOR),
                    year = cursor.findIntByName(LPunch.Column.YEAR),
                    month = cursor.findIntByName(LPunch.Column.MONTH),
                    count = cursor.findIntByName(SQL.COUNT),
                ).apply {
                    logD("selectByMonth -> $this")
                })
            }
            logD("selectByMonth: size -> ${list.size}")
            cursor.close()
            ListResult.Success(list)
        }

        /**
         * 查询本月为止前12个月的打卡次数
         */
        fun selectFor12Month(
            thisYear: Int,
            thisMonth: Int,
        ) = trySelect {
            logD("selectFor12Month($thisYear, $thisMonth)")
            val calendar = Calendar.getInstance()
            calendar.set(thisYear, thisMonth - 1, 1)
            calendar.set(Calendar.HOUR, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val end = Date(calendar.timeInMillis)
            calendar.add(Calendar.MONTH, -12)
            val start = Date(calendar.timeInMillis)
            selectCountByDays(start, end)
        }

        /**
         * 查询指定时间段内，打卡次数统计
         */
        private fun selectCountByDays(
            start: Date,
            end: Date,
        ) = trySelect {
            logD("selectCountByDays()")
            val list = ArrayList<CountInfo>()
            val cursor = database.rawQuery(SQL.SELECT_COUNT, arrayOf("$start", "$end"))
            while (cursor.moveToNext()) {
                list.add(
                    CountInfo(
                        name = cursor.findStringByName(FLAG.Column.NAME),
                        color = cursor.findIntByName(FLAG.Column.COLOR),
                        count = cursor.findIntByName(SQL.COUNT)
                    )
                )
            }
            logD("selectCountByDays: size -> ${list.size}")
            cursor.close()
            ListResult.Success(list)
        }

        /**
         * 查询本周打卡次数统计
         */
        fun selectForThisWeek(
            thisYear: Int,
            thisMonth: Int,
            thisDay: Int,
        ) = trySelect {
            logD("selectForThisWeek()")
            val calendar = Calendar.getInstance()
            calendar.set(thisYear, thisMonth - 1, thisDay)
            calendar.set(Calendar.HOUR, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val week = calendar.get(Calendar.DAY_OF_WEEK)
            calendar.add(Calendar.DAY_OF_WEEK, week * -1)
            val start = Date(calendar.timeInMillis)
            calendar.add(Calendar.DAY_OF_WEEK, 7)
            val end = Date(calendar.timeInMillis)
            selectCountByDays(start, end)
        }

        /**
         * 查询本月的累计打卡次数
         */
        fun selectForThisMonth(year: Int, month: Int) = trySelect {
            logD("selectForThisMonth()")
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month - 1)
            calendar.set(Calendar.HOUR, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val start = Date(calendar.timeInMillis)
            calendar.add(Calendar.MONTH, 1)
            val end = Date(calendar.timeInMillis)
            selectCountByDays(start, end)
        }

        /**
         * 查询本年的累计打卡次数
         */
        fun selectForThisYear() = trySelect {
            logD("selectForThisYear()")
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            // 不设置年份，用默认
            // calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, 0)
            calendar.set(Calendar.HOUR, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val start = Date(calendar.timeInMillis)
            calendar.add(Calendar.YEAR, 1)
            val end = Date(calendar.timeInMillis)
            selectCountByDays(start, end)
        }

        /**
         * 回收销毁当前数据库操作连接
         */
        fun close() {
            database.close()
        }

        private fun logD(value: String) {
            Log.d("Lollipop", "DBOperate: $value")
        }

        private inline fun <reified T, reified D> T.trySelect(block: T.() -> ListResult<D>): ListResult<D> {
            return try {
                block()
            } catch (e: Throwable) {
                ListResult.Error(e)
            }
        }

        private inline fun <reified T> T.tryDo(block: T.() -> BoolResult): BoolResult {
            return try {
                block()
            } catch (e: Throwable) {
                BoolResult.Error(e)
            }
        }

    }

    sealed class ListResult<T> {
        class Success<T>(val data: List<T>) : ListResult<T>()

        class Error<T>(val error: Throwable) : ListResult<T>()
    }

    sealed class BoolResult {

        companion object {
            fun result(value: Boolean): BoolResult {
                return if (value) {
                    TRUE
                } else {
                    FALSE
                }
            }
        }

        data object TRUE : BoolResult()

        data object FALSE : BoolResult()

        class Error(val error: Throwable) : BoolResult()
    }

    private fun logD(value: String) {
        Log.d("Lollipop", "DBHelper: $value")
    }

}
