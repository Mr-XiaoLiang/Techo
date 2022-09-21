package com.lollipop.palette

import android.content.Context
import com.lollipop.base.util.doAsync
import com.lollipop.base.util.onUI
import org.json.JSONArray
import java.io.*

class ColorHistoryHelper(private val context: Context) {

    private var mode = Impl.DEFAULT_MODE

    private val colorList = HashSet<Int>()

    val list: Set<Int>
        get() {
            return colorList
        }

    fun load(callback: () -> Unit) {
        doAsync {
            mode = Impl.lockRead(context, mode, colorList)
            onUI {
                callback()
            }
        }
    }

    fun save() {
        doAsync {
            mode = Impl.lockSave(context, mode, colorList)
        }
    }

    fun add(color: Int) {
        colorList.add(color)
    }

    private object Impl {
        const val DEFAULT_MODE = -1

        private var changedMode = DEFAULT_MODE

        private const val HISTORY_FILE_NAME = "colorHistory"

        val colorHistory = HashSet<Int>()

        private fun write(outputStream: OutputStream) {
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

        private fun read(inputStream: InputStream) {
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

        fun saveToFile(file: File) {
            if (file.exists() || file.isDirectory) {
                file.delete()
            }
            file.createNewFile()
            val fileOutputStream = BufferedOutputStream(FileOutputStream(file))
            write(fileOutputStream)
        }

        fun readFromFile(file: File, clean: Boolean = false) {
            if (clean) {
                colorHistory.clear()
            }
            if (file.isDirectory || !file.exists()) {
                return
            }
            val fileInputStream = BufferedInputStream(FileInputStream(file))
            read(fileInputStream)
        }

        private fun changeMode(): Int {
            if (changedMode == Int.MAX_VALUE) {
                changedMode = 0
            }
            return ++changedMode
        }

        private fun getFile(context: Context): File {
            return File(context.filesDir, HISTORY_FILE_NAME)
        }

        fun lockRead(context: Context, mode: Int, outList: MutableSet<Int>): Int {
            synchronized(ColorHistoryHelper::class.java) {
                if (mode != changedMode || changedMode == DEFAULT_MODE) {
                    readFromFile(getFile(context))
                    changeMode()
                }
                outList.clear()
                outList.addAll(colorHistory)
                return changedMode
            }
        }

        fun lockSave(context: Context, mode: Int, inList: Set<Int>): Int {
            synchronized(ColorHistoryHelper::class.java) {
                if (mode != changedMode) {
                    return changedMode
                }
                colorHistory.clear()
                colorHistory.addAll(inList)
                saveToFile(getFile(context))
                changeMode()
                return changedMode
            }
        }
    }

}