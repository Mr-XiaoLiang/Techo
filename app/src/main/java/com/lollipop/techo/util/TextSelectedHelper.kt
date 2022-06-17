package com.lollipop.techo.util

import android.widget.TextView

class TextSelectedHelper private constructor(
    private val option: Option
) {

    private val textView: TextView
        get() {
            return option.textView
        }

    fun onTouchChanged(x: Int, y: Int) {
        textView.layout
    }

    private class Option(
        val textView: TextView
    )
}