package com.lollipop.filechooser

import android.net.Uri

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
    }

}