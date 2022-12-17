package com.lollipop.qr.writer

import androidx.lifecycle.LifecycleOwner
import com.lollipop.qr.comm.BarcodeInfo

class TypedBarcodeWriter(lifecycleOwner: LifecycleOwner) : BarcodeWriter(lifecycleOwner) {

    companion object {
        private val SPECIAL = arrayOf(
            Part("\\", "\\\\"),
            Part(";", "\\;"),
            Part(",", "\\,"),
            Part(":", "\\:")
        )

        fun String.encode(): String {
            var value = this
            if (value.isEmpty()) {
                return value
            }
            SPECIAL.forEach { special ->
                value = value.replace(special.key, special.value)
            }
            return value
        }

    }

    private class Part(
        val key: String,
        val value: String
    )

    fun encode(info: BarcodeInfo): Builder {
        return encode(info.getBarcodeValue())
    }

}