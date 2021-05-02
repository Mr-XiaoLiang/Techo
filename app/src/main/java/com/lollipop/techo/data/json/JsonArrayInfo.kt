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
open class JsonArrayInfo<T: Any>: Convertible {

    var infoArray: JSONArray = JSONArray()
        private set

    val cache = SparseArray<Any?>()

    inline fun <reified A : JsonArrayInfo<T>> clone(): A {
        val newInfo = A::class.java.getConstructor().newInstance()
        newInfo.copy(this)
        return newInfo
    }

    fun copy(info: JsonArrayInfo<T>) {
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

    fun opt(key: Int): Any? {
        return infoArray.opt(key)
    }

    inline fun <reified A : T> get(key: Int, def: A? = null): A? {
        val valueOnly = getOnly(key, def)
        set(key, valueOnly)
        return valueOnly
    }

    inline fun <reified A : T> getOnly(key: Int, def: A?): A? {
        val cacheValue = cache[key]
        if (cacheValue is A) {
            return cacheValue
        }
        val opt = opt(key) ?: return def
        if (opt is A) {
            return opt
        }
        if (Convertible::class.java.isAssignableFrom(A::class.java)) {
            val newInstance = (A::class.java).getConstructor().newInstance()
            (newInstance as Convertible).parse(opt)
            return newInstance
        }
        when (def) {
            is String -> {
                return infoArray.optString(key) as A
            }
            is Boolean -> {
                return infoArray.optBoolean(key) as A
            }
            is Int -> {
                return infoArray.optInt(key) as A
            }
            is Long -> {
                return infoArray.optLong(key) as A
            }
            is Float -> {
                return infoArray.optDouble(key).let {
                    if (it.isNaN()) {
                        def
                    } else {
                        it.toFloat()
                    }
                } as A
            }
            is Double -> {
                return infoArray.optDouble(key).let {
                    if (it.isNaN()) {
                        def
                    } else {
                        it
                    }
                } as A
            }
        }
        return def
    }

    operator fun set(key: Int, value: Any?) {
        when (value) {
            is JsonArrayInfo<*> -> {
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

    fun put(value: T) {
        if (checkPut(value)) {
            when (value) {
                is JsonArrayInfo<*> -> {
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