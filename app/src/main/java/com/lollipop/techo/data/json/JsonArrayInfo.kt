package com.lollipop.techo.data.json

import android.util.SparseArray
import androidx.core.util.set
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

/**
 * @author lollipop
 * @date 2020/5/26 23:44
 * 基础的信息类
 */
open class JsonArrayInfo: Convertible {

    var infoArray: JSONArray = JSONArray()
        private set

    protected val cache = SparseArray<Any>()

    inline fun <reified T : JsonArrayInfo> clone(): T {
        val newInfo = T::class.java.getConstructor().newInstance()
        newInfo.copy(this)
        return newInfo
    }

    fun copy(info: JsonArrayInfo) {
        copy(info.toString())
    }

    private fun copy(json: String) {
        cache.clear()
        try {
            val jsonTokener = JSONTokener(json)
            when (val nextValue = jsonTokener.nextValue()) {
                is JSONObject -> {
                    infoArray = nextValue.toJSONArray(nextValue.names())?: JSONArray()
                }
                is JSONArray -> {
                    infoArray = nextValue
                }
                is String -> {
                    copy(nextValue)
                }
            }
        } catch (e: Throwable) {
            infoArray = JSONArray()
        }
    }

    protected fun opt(key: Int): Any? {
        return infoArray.opt(key)
    }

    protected inline operator fun <reified T : Any> get(key: Int, def: T): T {
        val valueOnly = getOnly(key, def)
        set(key, valueOnly)
        return valueOnly
    }

    protected inline fun <reified T : Any> getOnly(key: Int, def: T): T {
        val cacheValue = cache[key]
        if (cacheValue is T) {
            return cacheValue
        }
        val opt = opt(key) ?: return def
        if (opt is T) {
            return opt
        }
        if (Convertible::class.java.isAssignableFrom(T::class.java)) {
            val newInstance = (T::class.java).getConstructor().newInstance()
            (newInstance as Convertible).parse(opt)
            return newInstance
        }
        when (def) {
            is String -> {
                return infoArray.optString(key) as T
            }
            is Boolean -> {
                return infoArray.optBoolean(key) as T
            }
            is Int -> {
                return infoArray.optInt(key) as T
            }
            is Long -> {
                return infoArray.optLong(key) as T
            }
            is Float -> {
                return infoArray.optDouble(key).let {
                    if (it.isNaN()) {
                        def
                    } else {
                        it.toFloat()
                    }
                } as T
            }
            is Double -> {
                return infoArray.optDouble(key).let {
                    if (it.isNaN()) {
                        def
                    } else {
                        it
                    }
                } as T
            }
        }
        return def
    }

    protected operator fun set(key: Int, value: Any) {
        when (value) {
            is JsonArrayInfo -> {
                infoArray.put(key, value.infoArray)
            }
            is JsonObjectInfo -> {
                infoArray.put(key, value.infoObject)
            }
            else -> {
                infoArray.put(key, value)
            }
        }
        // 更新数据后更新缓存
        cache[key] = value
    }

    fun put(value: Any) {
        if (checkPut(value)) {
            when (value) {
                is JsonArrayInfo -> {
                    infoArray.put(value.infoArray)
                }
                is JsonObjectInfo -> {
                    infoArray.put(value.infoObject)
                }
                else -> {
                    infoArray.put(value)
                }
            }
        }
    }

    fun remove(index: Int) {
        infoArray.remove(index)
    }

    val size: Int
        get() {
            return infoArray.length()
        }

    override fun parse(any: Any) {
        copy(if (any is String) {
            any
        } else {
            any.toString()
        })
    }

    override fun toString(): String {
        return infoArray.toString()
    }

    protected open fun checkPut(value: Any): Boolean {
        return true
    }

}