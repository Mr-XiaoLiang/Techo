package com.lollipop.filechooser

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

sealed class FileChooseResult {

    object Empty : FileChooseResult() {
        override fun toString(): String {
            return "FileChooseResult.Empty"
        }
    }

    class Single(val uri: Uri) : FileChooseResult() {
        override fun toString(): String {
            return "FileChooseResult.Single($uri)"
        }

        fun open(context: Context): InputStream? {
            return FileChooser.open(context, uri)
        }

        fun save(context: Context, file: File) {
            FileChooser.save(context, uri, file)
        }

    }

    class Multiple(private val list: List<Uri>) : FileChooseResult(), List<Uri> by list {
        override fun toString(): String {
            val builder = StringBuilder("FileChooseResult.Multiple([\n")
            for (index in list.indices) {
                if (index != 0) {
                    builder.append(", \n")
                }
                builder.append(list[index].toString())
            }
            builder.append("\n])")
            return builder.toString()
        }

        fun open(context: Context, index: Int): InputStream? {
            if (index < 0 || index >= size) {
                return null
            }
            return FileChooser.open(context, get(index))
        }

        fun save(context: Context, index: Int, file: File) {
            FileChooser.save(open(context, index) ?: return, file)
        }

    }

}