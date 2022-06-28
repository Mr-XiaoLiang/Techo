package com.lollipop.techo.util

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.text.Editable
import android.text.Layout
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import com.lollipop.base.util.ListenerManager
import com.lollipop.base.util.SingleTouchHelper
import kotlin.math.max
import kotlin.math.min

object TextSelectedHelper {

    const val SELECTED_NONE = -1

    fun selector(): Selector.Builder {
        return Selector.Builder()
    }

    class Painter private constructor(
        private val option: Option
    ) {
        private var selectedStart: Int = SELECTED_NONE
        private var selectedEnd: Int = SELECTED_NONE

        private var pendingLayout = true
        private var rangePath = Path()

        private val paint by lazy {
            Paint().apply {
                isAntiAlias = true
                isDither = true
                style = Paint.Style.FILL
                color = option.color
            }
        }

        fun setSelectedRang(start: Int, end: Int) {
            selectedStart = start
            selectedEnd = end
            requestLayout()
        }

        private fun requestLayout() {
            val textLayout = option.layoutProvider.getTextLayout()
            if (textLayout == null) {
                pendingLayout = true
                rangePath.reset()
                return
            }

            // TODO
        }

        fun draw(canvas: Canvas) {
            if (pendingLayout) {
                requestLayout()
            }
            if (rangePath.isEmpty) {
                return
            }
            canvas.drawPath(rangePath, paint)
        }

        private class Option(
            @ColorInt
            val color: Int,
            val radius: Int,
            val layoutProvider: TextLayoutProvider
        )
    }

    class Selector private constructor(
        private val option: Option
    ) : View.OnTouchListener, TextWatcher {

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
            log("onTouchWithOffset: $offset")
            val realOffset = max(0, min(offset, maxLength))
            var start: Int = selectedStart
            var end: Int = selectedEnd
            if (start > end) {
                val temp = end
                end = start
                start = temp
            }
            if (start < 0 || end < 0) {
                if (start < 0) {
                    start = offset
                }
                if (end < 0) {
                    end = offset
                }
            } else {
                if (realOffset <= start) {
                    start = realOffset
                } else if (realOffset >= end) {
                    end = realOffset
                } else {
                    val startLength = realOffset - start
                    val endLength = end - realOffset
                    if (startLength > endLength) {
                        end = realOffset
                    } else {
                        start = realOffset
                    }
                }
            }
            onRangeChanged(start, end)
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
            fun bind(textView: TextView): Selector {
                val selectedHelper = Selector(build(textView))
                textView.setOnTouchListener(selectedHelper)
                textView.addTextChangedListener(selectedHelper)
                return selectedHelper
            }

        }

        private class Option(
            val resetWhenTextChanged: Boolean
        )
    }

    fun interface OnSelectedRangChangedListener {
        fun onSelectedRangChanged(start: Int, end: Int)
    }

    fun interface TextLayoutProvider {
        fun getTextLayout(): Layout?
    }

    private fun log(value: String) {
        Log.d("TextSelectedHelper", value)
    }
}