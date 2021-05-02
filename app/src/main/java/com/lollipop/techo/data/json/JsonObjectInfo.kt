package com.lollipop.techo.data.json

import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import kotlin.reflect.KProperty

/**
 * @author lollipop
 * @date 2020/5/26 23:44
 * 基础的信息类
 */
open class JsonObjectInfo : Convertible {

    var infoObject: JSONObject = JSONObject()
        private set

    protected val cache = HashMap<String, Any?>()

    inline fun <reified T : JsonObjectInfo> clone(): T {
        val newInfo = T::class.java.getConstructor().newInstance()
        newInfo.copy(this)
        return newInfo
    }

    fun copy(objectInfo: JsonObjectInfo) {
        copy(objectInfo.toString())
    }

    private fun copy(json: String) {
        cache.clear()
        try {
            val jsonTokener = JSONTokener(json)
            when (val nextValue = jsonTokener.nextValue()) {
                is JSONObject -> {
                    infoObject = nextValue
                }
                is JSONArray -> {
                    infoObject = JSONObject()
                    val length = nextValue.length()
                    for (index in 0 until length) {
                        infoObject.put(index.toString(), nextValue.opt(index))
                    }
                }
                is String -> {
                    copy(nextValue)
                }
            }
        } catch (e: Throwable) {
            infoObject = JSONObject()
        }
    }

    protected fun opt(key: String): Any? {
        return infoObject.opt(key)
    }

    protected inline operator fun <reified T : Any> get(key: String): T? {
        val valueOnly = getOnly<T>(key)
        set(key, valueOnly)
        return valueOnly
    }

    protected inline fun <reified T : Any> getOnly(key: String): T? {
        val cacheValue = cache[key]
        if (cacheValue is T) {
            return cacheValue
        }
        val opt = opt(key) ?: return null
        if (opt is T) {
            return opt
        }
        if (Convertible::class.java.isAssignableFrom(T::class.java)) {
            val newInstance = (T::class.java).getConstructor().newInstance()
            (newInstance as Convertible).parse(opt)
            return newInstance
        }
        when (T::class.java) {
            String::class.java -> {
                return infoObject.optString(key) as T
            }
            Boolean::class.java -> {
                return infoObject.optBoolean(key) as T
            }
            Int::class.java -> {
                return infoObject.optInt(key) as T
            }
            Long::class.java -> {
                return infoObject.optLong(key) as T
            }
            Float::class.java -> {
                infoObject.optDouble(key).let {
                    return if (it.isNaN()) {
                        null
                    } else {
                        it.toFloat() as T
                    }
                }
            }
            Double::class.java -> {
                infoObject.optDouble(key).let {
                    return if (it.isNaN()) {
                        null
                    } else {
                        it as T
                    }
                }
            }
        }
        return null
    }

    protected operator fun set(key: String, value: Any?) {
        when (value) {
            is JsonArrayInfo<*> -> {
                infoObject.put(key, value.infoArray)
            }
            is JsonObjectInfo -> {
                infoObject.put(key, value.infoObject)
            }
            else -> {
                infoObject.put(key, value)
            }
        }
        // 移除缓存
        cache[key] = value
    }

    val size: Int
        get() {
            return infoObject.length()
        }

    override fun parse(any: Any) {
        copy(if (any is String) {
            any
        } else {
            any.toString()
        })
    }

    override fun toString(): String {
        return infoObject.toString()
    }

    protected interface AnyDelegate<A : JsonObjectInfo, T : Any> {
        operator fun getValue(thisRef: A, property: KProperty<*>): T?

        operator fun setValue(thisRef: A, property: KProperty<*>, value: T?) {
            thisRef[property.name] = value
        }
    }

    protected interface AnyDelegateNoNull<A : JsonObjectInfo, T : Any> {
        operator fun getValue(thisRef: A, property: KProperty<*>): T

        operator fun setValue(thisRef: A, property: KProperty<*>, value: T?) {
            thisRef[property.name] = value
        }
    }

    protected inline fun <reified T : Any> withThis(): AnyDelegate<JsonObjectInfo, T> {
        return object : AnyDelegate<JsonObjectInfo, T> {
            override fun getValue(thisRef: JsonObjectInfo, property: KProperty<*>): T? {
                return thisRef[property.name]
            }
        }
    }

    protected inline fun <reified T : Any> withThis(def: T): AnyDelegateNoNull<JsonObjectInfo, T> {
        return object : AnyDelegateNoNull<JsonObjectInfo, T> {
            override fun getValue(thisRef: JsonObjectInfo, property: KProperty<*>): T {
                return thisRef[property.name]?:def
            }
        }
    }

    protected inline fun <reified T : Any> arrayWithThis() = withThis<JsonArrayInfo<T>>()

}