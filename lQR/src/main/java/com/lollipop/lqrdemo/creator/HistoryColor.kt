package com.lollipop.lqrdemo.creator

import android.content.Context
import android.content.SharedPreferences

object HistoryColor {

    private const val KEY_COLORS = "colors"
    private const val SEPARATOR = ";"

    private var sp: SharedPreferences? = null
    private const val HISTORY_MAX_COUNT = 9
    private val historyColor = mutableListOf<Int>()

    fun init(context: Context) {
        val app = context.applicationContext
        val preferences = app.getSharedPreferences("history_color", Context.MODE_PRIVATE)
        sp = preferences
        val value = preferences.getString(KEY_COLORS, "") ?: ""
        historyColor.clear()
        if (value.isNotEmpty()) {
            val list = deserialization(value)
            historyColor.addAll(list)
        }
    }

    private fun serialize(): String {
        val list = get()
        val builder = StringBuilder()
        list.forEach {
            if (builder.isNotEmpty()) {
                builder.append(SEPARATOR)
            }
            builder.append(it.toString(16))
        }
        return builder.toString()
    }

    private fun deserialization(value: String): List<Int> {
        val strings = value.split(SEPARATOR)
        val list = ArrayList<Int>()
        strings.forEach {
            try {
                if (it.isNotEmpty()) {
                    val int = it.toInt(16)
                    list.add(int)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
        return list
    }

    fun get(): List<Int> {
        return ArrayList(historyColor)
    }

    fun put(color: Int) {
        // 修改的时候加锁
        synchronized(historyColor) {
            // 检查颜色是否重复
            val index = historyColor.indexOf(color)
            if (index == 0) {
                // 点击第一个就没反应了
                return
            }
            if (index < 0) {
                // 不重复意味着需要插入，所以移除超过限制的颜色
                while (historyColor.isNotEmpty() && historyColor.size >= HISTORY_MAX_COUNT) {
                    // 移除最后的
                    historyColor.removeAt(historyColor.lastIndex)
                }
                // 添加到最前面
                historyColor.add(0, color)
            } else {
                // 如果已经存在了，那么直接移除已经存在的，并且添加到最前面
                historyColor.removeAt(index)
                historyColor.add(0, color)
            }
            // 保存一次
            sp?.edit()?.putString(KEY_COLORS, serialize())
        }
    }

}