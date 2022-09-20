package com.lollipop.palette

import java.io.InputStream
import java.io.OutputStream

class ColorHistoryHelper {

    companion object {

        val colorHistory = ArrayList<Int>()

        fun write(outputStream: OutputStream) {
            ColorHistoryWriter.write(colorHistory, outputStream)
        }

        fun read(inputStream: InputStream) {
            val read = ColorHistoryWriter.read(inputStream)
            colorHistory.clear()
            colorHistory.addAll(read)
        }

    }

}