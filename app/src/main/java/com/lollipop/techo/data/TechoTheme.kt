package com.lollipop.techo.data

enum class TechoTheme(
    val key: String
) {

    LIGHT("light"),
    DARK("dark");

    companion object {

        val DEFAULT = LIGHT

        fun find(key: String): TechoTheme {
            return entries.find { it.key == key } ?: DEFAULT
        }
    }

}