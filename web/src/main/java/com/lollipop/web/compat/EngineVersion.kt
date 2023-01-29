package com.lollipop.web.compat

data class EngineVersion(
    val name: String,
    val code: Long
) {

    companion object {
        val EMPTY = EngineVersion("", 0L)
    }

    val isEmpty = name.isEmpty() && code == 0L

}