package com.lollipop.bigboom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

/**
 * @author lollipop
 * @date 2022/1/7 21:59
 * 大爆炸的View
 */
class BigBoomView(
    context: Context, attr: AttributeSet?, style: Int
) : ViewGroup(context, attr, style) {

    private val patchList = ArrayList<String>()
    private val selectedPatchSet = TreeSet<Int>()

    private val itemGroup = RecyclerView(context)

    private val quickSelectionHelper = QuickSelectionHelper().apply {
        onSelectedRangeAdd {
            notifySelectedAdd(it)
        }
        onSelectedRangeReduce {
            notifySelectedReduce(it)
        }
        getPositionByLocation { x, y ->
            findSelectPosition(x, y)
        }
    }

    private fun notifySelectedAdd(intRange: IntRange) {
        intRange.forEach {
            selectedPatchSet.add(it)
        }
        itemGroup.adapter?.notifyItemRangeChanged(
            intRange.first,
            intRange.last - intRange.first
        )
    }

    private fun notifySelectedReduce(intRange: IntRange) {
        intRange.forEach {
            selectedPatchSet.remove(it)
        }
        itemGroup.adapter?.notifyItemRangeChanged(
            intRange.first,
            intRange.last - intRange.first
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val childLeft = paddingLeft
        val childTop = paddingTop
        val childRight = width - paddingRight
        val childBottom = height - paddingBottom
        for (index in 0 until childCount) {
            getChildAt(index)?.layout(childLeft, childTop, childRight, childBottom)
        }
    }

    fun setPatches(value: Array<String>, defaultSelected: Array<Int> = emptyArray()) {
        patchList.clear()
        patchList.addAll(value)
        selectedPatchSet.clear()
        selectedPatchSet.addAll(defaultSelected)
        notifyPatchesChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun notifyPatchesChanged() {
        quickSelectionHelper.reset()
        itemGroup.adapter?.notifyDataSetChanged()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return quickSelectionHelper.onTouchEvent(event) || super.onTouchEvent(event)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return quickSelectionHelper.onInterceptTouchEvent(ev) || super.onInterceptTouchEvent(ev)
    }

    private fun findSelectPosition(x: Float, y: Float): Int {
        val itemView = itemGroup.findChildViewUnder(x, y) ?: return -1
        return itemGroup.findContainingViewHolder(itemView)?.adapterPosition ?: -1
    }

    private class QuickSelectionHelper(private val context: Context) {

        private var dragStartPosition = -1
        private var nowSelectedPosition = -1
        private var lastSelectedPosition = -1

        private var touchDownTime = 0L
        private var activeTouchId = -1
        private val lastTouchLocation = Point()

        private var status = Status.CANCEL

        private val longPressTimeout = ViewConfiguration.getLongPressTimeout()
        private val scaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop

        private var onSelectedRangeAddListener: ((IntRange) -> Unit)? = null
        private var onSelectedRangeReduceListener: ((IntRange) -> Unit)? = null
        private var positionProvider: ((x: Float, y: Float) -> Int)? = null

        fun onTouchEvent(event: MotionEvent?): Boolean {
            if (status.isCancel) {
                return false
            }
            event ?: return false
            return onTouch(event)
        }

        fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
            if (status.isCancel) {
                return false
            }
            event ?: return false
            return onTouch(event)
        }

        private fun onTouch(event: MotionEvent): Boolean {
            positionProvider ?: return false
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    onTouchDown(event)
                }
                MotionEvent.ACTION_MOVE -> {
                    onTouchMove(event)
                }
                MotionEvent.ACTION_UP -> {
                    TODO()
                }
                MotionEvent.ACTION_CANCEL -> {
                    TODO()
                }
                MotionEvent.ACTION_POINTER_UP -> {
                    getFocusTouchPosition(event)
                }
            }
            return status.isEnable
        }

        private fun onTouchDown(event: MotionEvent) {
            touchDownTime = System.currentTimeMillis()
            activeTouchId = event.getPointerId(0)
            val focus = focusXY(event)
            if (focus == null) {
                reset()
                return
            }
            notifyTouchMoved(focus[0], focus[1])
            status = Status.WAIT
        }

        private fun onTouchMove(event: MotionEvent) {
            val getPosition = positionProvider
            if (getPosition == null) {
                reset()
                return
            }
            val focus = focusXY(event)
            if (focus == null) {
                reset()
                return
            }
            val x = focus[0]
            val y = focus[1]
            if (status.isWait) {
                // 位置偏移是否符合，如果超过范围，那么认为是滑动，放弃事件
                if (abs(x - lastTouchLocation.x) > scaledTouchSlop
                    || abs(y - lastTouchLocation.y) > scaledTouchSlop) {
                    status = Status.CANCEL
                    return
                }
                val now = System.currentTimeMillis()
                // 是否达到长按时间，如果没有达到，那么就接着等待
                if ((now - touchDownTime) < longPressTimeout) {
                    return
                }
                // 达到长按时长，那么可以开始滑动操作了
                status = Status.ENABLE
                notifyTouchMoved(x, y)
                val position = getPosition(x, y)
                dragStartPosition = position
                nowSelectedPosition = position
                lastSelectedPosition = position
                if (position >= 0) {
                    notifyPositionAdd(position..position)
                }
                callEnable()
                return
            }
            // 以下逻辑都认为是激活状态
            TODO()
        }

        private fun callEnable() {
            val vibratorService = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibratorService?:return
            if (!vibratorService.hasVibrator()) {
                return
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibratorService.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else {
                vibratorService.vibrate(100L)
            }
        }

        private fun notifyPositionAdd(intRange: IntRange) {
            onSelectedRangeAddListener?.invoke(intRange)
        }

        private fun notifyPositionReduce(intRange: IntRange) {
            onSelectedRangeReduceListener?.invoke(intRange)
        }

        private fun focusXY(event: MotionEvent): Array<Float>? {
            val focusTouchPosition = getFocusTouchPosition(event)
            if (focusTouchPosition < 0) {
                return null
            }
            return arrayOf(
                event.getX(focusTouchPosition),
                event.getY(focusTouchPosition)
            )
        }

        private fun getFocusTouchPosition(event: MotionEvent): Int {
            val index = event.findPointerIndex(activeTouchId)
            if (index < 0) {
                activeTouchId = event.getPointerId(0)
                return 0
            }
            return index
        }

        private fun isTouchMoved(x: Float, y: Float): Boolean {
            return lastTouchLocation.x != x.toInt() || lastTouchLocation.y != y.toInt()
        }

        private fun notifyTouchMoved(x: Float, y: Float) {
            lastTouchLocation.set(x.toInt(), y.toInt())
        }

        fun reset() {
            dragStartPosition = -1
            nowSelectedPosition = -1
            lastSelectedPosition = -1
            touchDownTime = 0L
            status = Status.CANCEL
        }

        fun onSelectedRangeAdd(callback: ((IntRange) -> Unit)) {
            onSelectedRangeAddListener = callback
        }

        fun onSelectedRangeReduce(callback: ((IntRange) -> Unit)) {
            onSelectedRangeReduceListener = callback
        }

        fun getPositionByLocation(callback: (x: Float, y: Float) -> Int) {
            positionProvider = callback
        }

        enum class Status {
            WAIT,
            ENABLE,
            CANCEL;

            val isCancel: Boolean
                get() {
                    return this == CANCEL
                }
            val isEnable: Boolean
                get() {
                    return this == ENABLE
                }

            val isWait: Boolean
                get() {
                    return this == WAIT
                }
        }

    }

}