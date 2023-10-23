package com.lollipop.base.util

object ClassHelper {

    fun checkExtends(target: Class<*>, other: Class<*>): Boolean {
        if (target === other) {
            return true
        }
        target.interfaces.forEach {
            if (it === other) {
                return true
            }
        }
        val superclass = target.superclass
        if (superclass === other) {
            return false
        }
        if (superclass == null) {
            return false
        }
        return checkExtends(superclass, other)
    }

}

fun Class<*>.checkExtends(other: Class<*>): Boolean {
    return ClassHelper.checkExtends(this, other)
}
