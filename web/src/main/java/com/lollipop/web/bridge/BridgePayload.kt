package com.lollipop.web.bridge

import kotlin.reflect.KProperty

class BridgePayload(
    val action: String,
    val params: Map<String, String>,
    val callback: String
) {

    open class Delegate(val payload: BridgePayload) {

        protected fun string(customKey: String = ""): StringParamsEntrust {
            return if (customKey.isEmpty()) {
                StringParamsEntrust.DEFAULT
            } else {
                StringParamsEntrust(customKey)
            }
        }

        protected fun boolean(
            customKey: String = "",
            defValue: Boolean = false
        ): BooleanParamsEntrust {
            return if (customKey.isEmpty()) {
                if (defValue) {
                    BooleanParamsEntrust.DEFAULT_TRUE
                } else {
                    BooleanParamsEntrust.DEFAULT_FALSE
                }
            } else {
                BooleanParamsEntrust(customKey, defValue)
            }
        }

        protected fun int(
            customKey: String = "",
        ): IntParamsEntrust {
            return if (customKey.isEmpty()) {
                IntParamsEntrust.DEFAULT
            } else {
                IntParamsEntrust(customKey)
            }
        }

    }

    class StringParamsEntrust(val key: String) {

        companion object {
            val DEFAULT = StringParamsEntrust("")
        }

        operator fun getValue(delegate: Delegate, property: KProperty<*>): String {
            val paramsKey = key.ifEmpty { property.name }
            return delegate.payload.params[paramsKey] ?: ""
        }
    }

    class BooleanParamsEntrust(val key: String, val defValue: Boolean) {

        companion object {
            val DEFAULT_FALSE = BooleanParamsEntrust("", false)
            val DEFAULT_TRUE = BooleanParamsEntrust("", true)
            private const val TRUE = "true"
            private const val FALSE = "false"
        }

        operator fun getValue(delegate: Delegate, property: KProperty<*>): Boolean {
            val paramsKey = key.ifEmpty { property.name }
            val value = delegate.payload.params[paramsKey] ?: return defValue
            if (value.equals(TRUE, ignoreCase = true)) {
                return true
            }
            if (value.equals(FALSE, ignoreCase = true)) {
                return false
            }
            return defValue
        }
    }

    class IntParamsEntrust(val key: String) {

        companion object {
            val DEFAULT = IntParamsEntrust("")
        }

        operator fun getValue(delegate: Delegate, property: KProperty<*>): Int {
            val paramsKey = key.ifEmpty { property.name }
            val value = delegate.payload.params[paramsKey] ?: return 0
            try {
                return value.toIntOrNull() ?: 0
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            return 0
        }
    }

}