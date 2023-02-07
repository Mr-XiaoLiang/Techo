package com.lollipop.base.util

import android.content.ContentValues
import android.database.Cursor

object DatabaseHelper {

    fun ContentValues.putValue(enum: Enum<*>, value: String) {
        put(enum.name, value)
    }

    fun ContentValues.putValue(enum: Enum<*>, value: Int) {
        put(enum.name, value)
    }

    fun Cursor.getIntByName(enum: Enum<*>, def: Int = 0): Int {
        return getIntByName(enum.name, def)
    }

    fun Cursor.getIntByName(name: String, def: Int = 0): Int {
        val columnIndex = getColumnIndex(name)
        if (columnIndex < 0) {
            return def
        }
        return getInt(columnIndex)
    }

    fun Cursor.getTextByName(enum: Enum<*>, def: String = ""): String {
        return getTextByName(enum.name, def)
    }

    fun Cursor.getTextByName(name: String, def: String = ""): String {
        val columnIndex = getColumnIndex(name)
        if (columnIndex < 0) {
            return def
        }
        return getString(columnIndex)
    }

    fun Cursor.getFloatByName(enum: Enum<*>, def: Float = 0F): Float {
        return getFloatByName(enum.name, def)
    }

    fun Cursor.getFloatByName(name: String, def: Float = 0F): Float {
        val columnIndex = getColumnIndex(name)
        if (columnIndex < 0) {
            return def
        }
        return getFloat(columnIndex)
    }

    enum class ColumnFormat {
        TEXT,
        INTEGER,
        FLOAT
    }


}