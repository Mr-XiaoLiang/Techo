package com.lollipop.techo.util.permission

sealed class PermissionResult {

    abstract fun isGranted(): Boolean

    class Single(val result: Boolean) : PermissionResult() {
        override fun isGranted(): Boolean {
            return result
        }

    }

    class MultipleByAnd(val result: Map<String, Boolean>) : PermissionResult() {
        override fun isGranted(): Boolean {
            val values = result.values
            for (b in values) {
                if (!b) {
                    return false
                }
            }
            return true
        }

    }

    class MultipleByOr(val result: Map<String, Boolean>) : PermissionResult() {
        override fun isGranted(): Boolean {
            val values = result.values
            for (b in values) {
                if (b) {
                    return true
                }
            }
            return false
        }

    }

}