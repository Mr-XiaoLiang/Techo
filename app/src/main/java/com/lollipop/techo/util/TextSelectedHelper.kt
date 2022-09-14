package com.lollipop.techo.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.Layout
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import com.lollipop.base.util.ListenerManager
import com.lollipop.base.util.SingleTouchHelper
import com.lollipop.techo.util.TextSelectedHelper.InvalidateCallback
import com.lollipop.techo.util.TextSelectedHelper.SelectTarget.*
import com.lollipop.techo.util.TextSelectedHelper.TextLayoutProvider
import kotlin.math.max
import kotlin.math.min

object TextSelectedHelper {

    const val SELECTED_NONE = -1

    fun selector(): Selector.Builder {
        return Selector.Builder()
    }

    fun printer(): Painter.Builder {
        return Painter.Builder()
    }

    class Painter private constructor(
        private val option: Option
    ) : Drawable(), OnSelectedRangeChangedListener {

        companion object {
            const val RADIUS_MAX = -1
            const val RADIUS_HALF = -2
        }

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

        fun setColor(color: Int) {
            option.color = color
            invalidateSelf()
        }

        override fun onSelectedRangeChanged(start: Int, end: Int) {
            setSelectedRange(start, end)
        }

        fun setSelectedRange(start: Int, end: Int) {
            selectedStart = start
            selectedEnd = end
            log("setSelectedRang:$start, $end")
            requestLayout()
            option.invalidateCallback.invalidate()
        }

        private fun requestLayout() {
            val textView = option.layoutProvider.getTextView()
            val textLayout = textView?.layout
            if (textView == null || textLayout == null) {
                pendingLayout = true
                rangePath.reset()
                return
            }
            rangePath.reset()
            if (selectedEnd - selectedStart < 1) {
                return
            }
            val startLine = textLayout.getLineForOffset(selectedStart)
            val endLine = textLayout.getLineForOffset(selectedEnd)
            val leftOffset = (textView.totalPaddingLeft - textView.scrollX).toFloat()
            val topOffset = (textView.totalPaddingTop - textView.scrollY).toFloat()
            log("requestLayout:startLine = $startLine, endLine = $endLine")
            val tempRect = RectF()
            val fontMetrics = textView.paint.fontMetrics
            val fontHeight = fontMetrics.bottom - fontMetrics.top
            for (line in startLine..endLine) {
                val left = if (line == startLine) {
                    textLayout.getPrimaryHorizontal(selectedStart)
                } else {
                    textLayout.getLineLeft(line)
                }
                val top = textLayout.getLineTop(line)
                val right = if (line == endLine) {
                    textLayout.getPrimaryHorizontal(selectedEnd)
                } else {
                    textLayout.getLineRight(line)
                }
                val bottom = top + fontHeight
                tempRect.set(left, top.toFloat(), right, bottom)
                tempRect.offset(leftOffset, topOffset)
                val radius = getRadius(tempRect.width(), tempRect.height())
                rangePath.addRoundRect(tempRect, radius, radius, Path.Direction.CW)
                log("requestLayout:$tempRect")
            }
            pendingLayout = false
        }

        private fun getRadius(rectWidth: Float, rectHeight: Float): Float {
            return when (option.radius) {
                RADIUS_MAX -> {
                    min(rectWidth, rectHeight) * 0.5F
                }
                RADIUS_HALF -> {
                    min(rectWidth, rectHeight) * 0.3F
                }
                else -> {
                    max(option.radius, 0).toFloat()
                }
            }
        }

        override fun draw(canvas: Canvas) {
            if (pendingLayout) {
                requestLayout()
            }
            if (rangePath.isEmpty) {
                return
            }
            canvas.drawPath(rangePath, paint)
        }

        override fun setAlpha(alpha: Int) {
            paint.alpha = alpha
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            paint.colorFilter = colorFilter
        }

        @Deprecated(
            "Deprecated in Java",
            ReplaceWith("PixelFormat.TRANSPARENT", "android.graphics.PixelFormat")
        )
        override fun getOpacity(): Int {
            return PixelFormat.TRANSPARENT
        }

        class Builder {
            @ColorInt
            private var color: Int = Color.BLACK
            private var radius: Int = 0
            private var layoutProvider: TextLayoutProvider? = null
            private var invalidateCallback: InvalidateCallback? = null

            fun setColor(
                @ColorInt
                c: Int
            ): Builder {
                this.color = c
                return this
            }

            fun setColor(context: Context, @ColorRes id: Int): Builder {
                return setColor(ContextCompat.getColor(context, id))
            }

            fun setRadius(r: Int): Builder {
                this.radius = r
                return this
            }

            fun maxRadius(): Builder {
                return setRadius(RADIUS_MAX)
            }

            fun halfRadius(): Builder {
                return setRadius(RADIUS_HALF)
            }

            fun setRadius(context: Context, @DimenRes id: Int): Builder {
                return setRadius(context.resources.getDimensionPixelSize(id))
            }

            fun setLayoutProvider(provider: TextLayoutProvider): Builder {
                this.layoutProvider = provider
                return this
            }

            fun notifyInvalidate(callback: InvalidateCallback): Builder {
                this.invalidateCallback = callback
                return this
            }

            fun build(): Painter {
                return Painter(
                    Option(
                        color = color,
                        radius = radius,
                        layoutProvider = layoutProvider ?: TextLayoutProvider { null },
                        invalidateCallback = invalidateCallback ?: InvalidateCallback { }
                    )
                )
            }

            fun bindTo(selector: Selector): Painter {
                return build().apply {
                    selector.addListener(this)
                }
            }

        }

        private class Option(
            @ColorInt
            var color: Int,
            val radius: Int,
            val layoutProvider: TextLayoutProvider,
            val invalidateCallback: InvalidateCallback
        )
    }

    class Selector private constructor(
        private val option: Option
    ) : View.OnTouchListener, TextWatcher {

        private var selectedStart = SELECTED_NONE
        private var selectedEnd = SELECTED_NONE

        private val touchHelper = SingleTouchHelper()

        private val listenerManager = ListenerManager<OnSelectedRangeChangedListener>()

        var selectTarget = AUTO

        private var currentSelectTarget = AUTO

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
            when (event?.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    currentSelectTarget = selectTarget
                }
            }
            Log.d("Lollipop", "onTouch: $event")
            touchHelper.onTouch(event)
            onTouchMove(v, textLayout, touchHelper.x, touchHelper.y)
            return true
        }

        private fun onTouchMove(view: TextView, textLayout: Layout, x: Float, y: Float) {
            val realX = x - view.totalPaddingLeft + view.scrollX
            val realY = (y - view.totalPaddingTop + view.scrollY).toInt()
            val lineForVertical = textLayout.getLineForVertical(realY)
            val offsetForHorizontal = textLayout.getOffsetForHorizontal(lineForVertical, realX)
            val maxLength = textLayout.text.length
            onTouchWithOffset(offsetForHorizontal, maxLength)
        }

        private fun onTouchWithOffset(offset: Int, maxLength: Int) {
            log("onTouchWithOffset: $offset, $maxLength")
            val realOffset = max(0, min(offset, maxLength))
            var start: Int = selectedStart
            var end: Int = selectedEnd
            if (start > end) {
                val temp = end
                end = start
                start = temp
            }
            if (start < 0 || end < 0) {
                currentSelectTarget = END
                if (start < 0) {
                    start = offset
                }
                if (end < 0) {
                    end = offset
                }
            } else {
                when (currentSelectTarget) {
                    AUTO -> {
                        if (realOffset <= start) {
                            start = realOffset
                            currentSelectTarget = START
                        } else if (realOffset >= end) {
                            end = realOffset
                            currentSelectTarget = END
                        } else {
                            val startLength = realOffset - start
                            val endLength = end - realOffset
                            if (startLength > endLength) {
                                end = realOffset
                                currentSelectTarget = END
                            } else {
                                start = realOffset
                                currentSelectTarget = START
                            }
                        }
                    }
                    START -> {
                        start = offset
                    }
                    END -> {
                        end = offset
                    }
                }
            }
            if (start > end) {
                val temp = end
                end = start
                start = temp
            }
            onRangeChanged(start, end)
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        fun addListener(listener: OnSelectedRangeChangedListener) {
            listenerManager.addListener(listener)
        }

        fun removeListener(listener: OnSelectedRangeChangedListener) {
            listenerManager.removeListener(listener)
        }

        override fun afterTextChanged(s: Editable?) {
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
            listenerManager.invoke { it.onSelectedRangeChanged(start, end) }
        }

        class Builder {

            private var resetWhenTextChanged = true
            private var rangChangedListener: OnSelectedRangeChangedListener? = null

            fun resetWhenTextChanged(enable: Boolean): Builder {
                resetWhenTextChanged = enable
                return this
            }

            fun onSelectedChanged(listener: OnSelectedRangeChangedListener): Builder {
                rangChangedListener = listener
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
                rangChangedListener?.let {
                    selectedHelper.addListener(it)
                }
                return selectedHelper
            }

        }

        private class Option(
            val resetWhenTextChanged: Boolean
        )
    }

    fun interface OnSelectedRangeChangedListener {
        fun onSelectedRangeChanged(start: Int, end: Int)
    }

    fun interface TextLayoutProvider {
        fun getTextView(): TextView?
    }

    fun interface InvalidateCallback {
        fun invalidate()
    }

    enum class SelectTarget {
        AUTO,
        START,
        END
    }

    private fun log(value: String) {
        Log.d("TextSelectedHelper", value)
    }
}