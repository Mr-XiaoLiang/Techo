package com.lollipop.browser.utils

import android.content.Context
import android.graphics.Bitmap
import com.lollipop.base.util.TxtHelper
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

abstract class FileInfoManager {

    companion object {
        fun String.optFile(): File? {
            if (this.isEmpty()) {
                return null
            }
            val file = File(this)
            if (!file.exists()) {
                return null
            }
            return file
        }
    }

    abstract fun init(context: Context)

    protected fun readArrayFromFile(
        file: File,
        emptyData: (error: Boolean) -> Unit,
        itemReader: (item: JSONObject) -> Unit
    ) {
        try {
            if (!file.exists()) {
                emptyData(false)
                return
            }
            val value = TxtHelper.readFromFile(file)
            val jsonArray = JSONArray(value)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.optJSONObject(i) ?: continue
                try {
                    itemReader(obj)
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            emptyData(true)
        }
    }

    protected fun readObjectFromFile(
        file: File,
        emptyData: (error: Boolean) -> Unit,
        result: (JSONObject) -> Unit
    ) {
        try {
            if (!file.exists()) {
                emptyData(false)
                return
            }
            val value = TxtHelper.readFromFile(file)
            result(JSONObject(value))
        } catch (e: Throwable) {
            e.printStackTrace()
            emptyData(true)
        }
    }

    protected fun writeToFile(json: JSONArray, file: File) {
        if (file.exists()) {
            file.delete()
        }
        TxtHelper.writeToFile(json.toString(), file)
    }

    protected fun writeToFile(json: JSONObject, file: File) {
        if (file.exists()) {
            file.delete()
        }
        TxtHelper.writeToFile(json.toString(), file)
    }

    protected fun Bitmap.writeToFile(file: File) {
        if (file.exists()) {
            file.delete()
        }
        file.parentFile?.mkdirs()
        val bitmap = this
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(file)
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)) {
                out.flush()
                out.close()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {
            try {
                out?.close()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

}