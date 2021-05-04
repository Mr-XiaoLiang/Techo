package com.lollipop.techo.data.json

/**
 * @author lollipop
 * @date 4/30/21 20:33
 * Json信息的扩展方法
 */

inline fun <reified T : JsonObjectInfo> String.toJsonObjectInfo(): T {
    val newInstance = T::class.java.getConstructor().newInstance()
    newInstance.parse(this)
    return newInstance
}

inline fun <reified A: Any, reified T : JsonArrayInfo<A>> String.toJsonArrayInfo(): T {
    val newInstance = T::class.java.getConstructor().newInstance()
    newInstance.parse(this)
    return newInstance
}
