package com.lollipop.base.util

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import java.lang.ref.WeakReference

abstract class DatabaseHelper(
    context: Context,
    name: String,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
) : SQLiteOpenHelper(context, name, factory, version) {

    companion object {
        @JvmStatic
        protected fun ContentValues.putValue(enum: Enum<*>, value: String) {
            put(enum.name, value)
        }

        @JvmStatic
        protected fun ContentValues.putValue(enum: Enum<*>, value: Int) {
            put(enum.name, value)
        }

        @JvmStatic
        protected fun ContentValues.putValue(enum: Enum<*>, value: Long) {
            put(enum.name, value)
        }

        @JvmStatic
        protected fun Cursor.getIntByName(enum: Enum<*>, def: Int = 0): Int {
            return getIntByName(enum.name, def)
        }

        @JvmStatic
        protected fun Cursor.getIntByName(name: String, def: Int = 0): Int {
            val columnIndex = getColumnIndex(name)
            if (columnIndex < 0) {
                return def
            }
            return getInt(columnIndex)
        }

        @JvmStatic
        protected fun Cursor.getLongByName(enum: Enum<*>, def: Long = 0L): Long {
            return getLongByName(enum.name, def)
        }

        @JvmStatic
        protected fun Cursor.getLongByName(name: String, def: Long = 0L): Long {
            val columnIndex = getColumnIndex(name)
            if (columnIndex < 0) {
                return def
            }
            return getLong(columnIndex)
        }

        @JvmStatic
        protected fun Cursor.getTextByName(enum: Enum<*>, def: String = ""): String {
            return getTextByName(enum.name, def)
        }

        @JvmStatic
        protected fun Cursor.getTextByName(name: String, def: String = ""): String {
            val columnIndex = getColumnIndex(name)
            if (columnIndex < 0) {
                return def
            }
            return getString(columnIndex)
        }

        @JvmStatic
        protected fun Cursor.getFloatByName(enum: Enum<*>, def: Float = 0F): Float {
            return getFloatByName(enum.name, def)
        }

        @JvmStatic
        protected fun Cursor.getFloatByName(name: String, def: Float = 0F): Float {
            val columnIndex = getColumnIndex(name)
            if (columnIndex < 0) {
                return def
            }
            return getFloat(columnIndex)
        }

        @JvmStatic
        protected inline fun <reified T : Any> queryList(
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

        @JvmStatic
        protected fun selectLastId(table: String, idName: String, db: SQLiteDatabase): Int {
            try {
                val sql = "SELECT $idName FROM $table ORDER BY $idName DESC LIMIT 1"
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

        @JvmStatic
        protected fun createSql(
            table: String,
            idName: String,
            columns: Array<out ColumnEnum>
        ): String {
            val sql = StringBuilder()
            sql.append("CREATE TABLE $table ( $idName INTEGER PRIMARY KEY AUTOINCREMENT ")

            columns.forEach {
                sql.append(", ")
                sql.append(it.name)
                sql.append(" ")
                sql.append(it.format)
            }

            sql.append(" ) ")
            return sql.toString()
        }

        @JvmStatic
        protected fun Table.createSql(): String {
            val table = this
            return createSql(table.tableName, table.idColumn, table.columns)
        }

    }

    protected val readDb: SQLiteDatabase
        get() {
            return readableDatabase
        }

    protected val writeDb: SQLiteDatabase
        get() {
            return writableDatabase
        }

    private var lifecycleReference: WeakReference<Lifecycle>? = null

    private val lifecycleEventObserver = LifecycleEventObserver { source, event ->
        if (event == Lifecycle.Event.ON_DESTROY) {
            destroy()
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db ?: return
        getTableArray().forEach {
            db.execSQL(it.createSql())
        }
    }

    protected abstract fun getTableArray(): Array<Table>

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    fun bindLifecycle(lifecycle: Lifecycle) {
        lifecycleReference?.get()?.removeObserver(lifecycleEventObserver)
        lifecycle.addObserver(lifecycleEventObserver)
        lifecycleReference = WeakReference(lifecycle)
    }

    fun destroy() {
        lifecycleReference?.get()?.removeObserver(lifecycleEventObserver)
        lifecycleReference = null
        onDestroy()
        try {
            close()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    protected open fun onDestroy() {}

    abstract class Table {
        abstract val tableName: String
        open val idColumn: String = "ID"
        abstract val columns: Array<out ColumnEnum>
        open val columnsName: Array<String> by lazy {
            columns.map { it.name }.toTypedArray()
        }
    }

    enum class ColumnFormat {
        TEXT,
        INTEGER,
        FLOAT
    }

    interface ColumnEnum {
        val name: String
        val format: ColumnFormat
    }

}