package com.lollipop.techo.util

import android.annotation.SuppressLint
import android.text.Editable
import android.text.Layout
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import com.lollipop.base.util.ListenerManager
import com.lollipop.base.util.SingleTouchHelper
import kotlin.math.max
import kotlin.math.min

class TextSelectedHelper private constructor(
    private val option: Option
) : View.OnTouchListener, TextWatcher {

    companion object {
        fun create(): Builder {
            return Builder()
        }

        const val SELECTED_NONE = -1
    }

//    private val textView: TextView
//        get() {
//            return option.textView
//        }

    private var selectedStart = SELECTED_NONE
    private var selectedEnd = SELECTED_NONE

    private var maxLength = 0

    private val touchHelper = SingleTouchHelper()

    private val listenerManager = ListenerManager<OnSelectedRangChangedListener>()

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        // 拿不到View，放弃
        v ?: return false
        // View不是TextView，放弃
        if (v !is TextView) {
            return false
        }
        // 拿不到Text Layout放弃
        val textLayout = v.layout ?: return false
        touchHelper.onTouch(event)
        onTouchMove(v, textLayout, touchHelper.x, touchHelper.y)
        return true
    }

    private fun onTouchMove(view: TextView, textLayout: Layout, x: Float, y: Float) {
        val realX = x - view.totalPaddingLeft + view.scrollX
        val realY = (y - view.totalPaddingTop + view.scrollY).toInt()
        val lineForVertical = textLayout.getLineForVertical(realY)
        val offsetForHorizontal = textLayout.getOffsetForHorizontal(lineForVertical, realX)
        onTouchWithOffset(offsetForHorizontal)
    }

    private fun onTouchWithOffset(offset: Int) {
        val realOffset = max(0, min(offset, maxLength))
        if (selectedStart < 0 || selectedEnd < 0) {
            if (selectedStart < 0) {
                selectedStart = offset
            }
            if (selectedEnd < 0) {
                selectedEnd = offset
            }
        } else {
            if (realOffset < selectedStart) {
                TODO()
            }
        }
        Log.d("TextSelectedHelper", "onTouchWithOffset: $offset")
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    override fun afterTextChanged(s: Editable?) {
        maxLength = s?.length ?: 0
        if (option.resetWhenTextChanged) {
            reset()
        }
    }

    fun reset() {
        onRangeChanged(SELECTED_NONE, SELECTED_NONE)
    }

    private fun onRangeChanged(start: Int, end: Int) {
        selectedStart = start
        selectedEnd = end
        listenerManager.invoke { it.onSelectedRangChanged(start, end) }
    }

    fun interface OnSelectedRangChangedListener {
        fun onSelectedRangChanged(start: Int, end: Int)
    }

    class Builder {

        private var resetWhenTextChanged = true

        fun resetWhenTextChanged(enable: Boolean): Builder {
            resetWhenTextChanged = enable
            return this
        }

        private fun build(textView: TextView): Option {
            return Option(
                resetWhenTextChanged
            )
        }

        @SuppressLint("ClickableViewAccessibility")
        fun bind(textView: TextView): TextSelectedHelper {
            val selectedHelper = TextSelectedHelper(build(textView))
            textView.setOnTouchListener(selectedHelper)
            textView.addTextChangedListener(selectedHelper)
            return selectedHelper
        }

    }

    private class Option(
        val resetWhenTextChanged: Boolean
//        val textView: TextView
    )
}