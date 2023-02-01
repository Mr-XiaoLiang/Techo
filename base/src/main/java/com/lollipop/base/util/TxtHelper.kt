package com.lollipop.base.util

import java.nio.file.Files
import java.nio.file.Paths

object TxtHelper {

    fun writeToFile(value: String, path: String) {
        Files.write(Paths.get(path), value.toByteArray())
    }

    fun readFromFile(path: String): String {
        val lines = Files.readAllLines(Paths.get(path))
        val stringBuilder = StringBuilder()
        lines.forEach {
            stringBuilder.append(it)
        }
        return stringBuilder.toString()
    }

}