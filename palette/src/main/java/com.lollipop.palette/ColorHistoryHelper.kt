package com.lollipop.palette

import org.json.JSONArray
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

class ColorHistoryHelper {

    companion object {

        val colorHistory = ArrayList<Int>()

        fun write(outputStream: OutputStream) {
            var inputStream: InputStream? = null
            try {
                val jsonArray = JSONArray()
                colorHistory.forEach {
                    jsonArray.put(it)
                }
                val input = ByteArrayInputStream(jsonArray.toString().toByteArray())
                inputStream = input
                val output = outputStream

                val buffer = ByteArray(1024 * 4)
                do {
                    val length = input.read(buffer)
                    if (length < 0) {
                        break
                    }
                    output.write(buffer, 0, length)
                } while (true)
                output.flush()
            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
                try {
                    inputStream?.close()
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
                try {
                    outputStream.close()
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }

        fun read(inputStream: InputStream) {
            val tempList = ArrayList<Int>()
            var outputStream: OutputStream? = null
            try {
                val output = ByteArrayOutputStream()
                outputStream = output

                val input = inputStream
                val buffer = ByteArray(1024 * 4)
                do {
                    val length = input.read(buffer)
                    if (length < 0) {
                        break
                    }
                    output.write(buffer, 0, length)
                } while (true)
                output.flush()

                val jsonValue = outputStream.toString()
                if (jsonValue.isNotEmpty()) {
                    val jsonArray = JSONArray(jsonValue)
                    for (index in 0 until jsonArray.length()) {
                        val c = jsonArray.optInt(index)
                        if (c != 0) {
                            tempList.add(c)
                        }
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
                try {
                    inputStream.close()
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
                try {
                    outputStream?.close()
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }

            colorHistory.clear()
            colorHistory.addAll(tempList)
        }

    }

}