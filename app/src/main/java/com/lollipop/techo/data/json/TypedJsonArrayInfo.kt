package com.lollipop.techo.data.json

/**
 * @author lollipop
 * @date 2021/5/9 20:38
 */
abstract class TypedJsonArrayInfo<T: Any>: JsonArrayInfo() {

    abstract operator fun get(key: Int): T?

}