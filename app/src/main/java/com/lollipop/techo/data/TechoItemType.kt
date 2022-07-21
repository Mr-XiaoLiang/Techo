package com.lollipop.techo.data

enum class TechoItemType {
    Title,
    Text,
    Number,
    CheckBox,
    Photo,
    Split;

    companion object {
        fun opt(name: String): TechoItemType {
            try {
                return valueOf(name)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            return Text
        }
    }
}