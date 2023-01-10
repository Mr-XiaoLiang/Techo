package com.lollipop.base.util

import android.content.Context
import android.graphics.PointF
import android.view.MotionEvent
import android.view.ViewConfiguration
import com.lollipop.base.util.SingleTouchSlideHelper.Slider.Horizontally
import com.lollipop.base.util.SingleTouchSlideHelper.Slider.Vertically
import kotlin.math.abs

/**
 * @author Lollipop
 * 2022/10/16
 * 简单的单指头手势处理小工具
 */
class SingleTouchSlideHelper(
    context: Context,
    private val slider: Slider
) {

    private val scaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val touchHelper = SingleTouchHelper()

    private val touchDownLocation = PointF()
    private val touchLastLocation = PointF()
    private var direction = Direction.NONE

    private val touchMoveListener = ListenerManager<OnTouchMoveListener>()
    private val touchOffsetListener = ListenerManager<OnTouchOffsetListener>()
    private val touchEndListener = ListenerManager<OnTouchEndListener>()
    private val clickListener = ListenerManager<OnClickListener>()

    var activeTouchId = SingleTouchHelper.POINT_ID_NONE
        private set

    val isTouching: Boolean
        get() {
            return touchHelper.isTouching
        }

    fun onTouch(event: MotionEvent?): Boolean {
        event ?: return false
        touchHelper.onTouch(event)
        val isTouchIdChanged = activeTouchId != touchHelper.touchPointId
        activeTouchId = touchHelper.touchPointId
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchDownLocation.set(touchHelper.x, touchHelper.y)
                touchLastLocation.set(touchDownLocation)
                direction = Direction.PENDING
            }
            MotionEvent.ACTION_MOVE -> {
                checkDirection(touchHelper.x, touchHelper.y)
                onTouchMove()
            }
            MotionEvent.ACTION_POINTER_UP -> {
                if (isTouchIdChanged) {
                    touchDownLocation.set(touchHelper.x, touchHelper.y)
                    touchLastLocation.set(touchDownLocation)
                }
                onTouchMove()
            }
            MotionEvent.ACTION_UP -> {
                onTouchMove()
                onTouchEnd(false)
            }
            MotionEvent.ACTION_CANCEL -> {
                onTouchMove()
                onTouchEnd(true)
            }
        }
        return direction != Direction.NONE
    }

    private fun onTouchMove() {
        if (direction == Direction.PENDING || direction == Direction.NONE) {
            return
        }
        val x = touchHelper.x
        val y = touchHelper.y
        onTouchMove(x, y)
        val offsetX = x - touchLastLocation.x
        val offsetY = y - touchLastLocation.y
        touchLastLocation.set(x, y)
        onTouchOffset(offsetX, offsetY)
    }

    private fun onTouchMove(x: Float, y: Float) {
        touchMoveListener.invoke { it.onTouchMoved(x, y) }
    }

    private fun onTouchOffset(offsetX: Float, offsetY: Float) {
        touchOffsetListener.invoke { it.onTouchMoved(offsetX, offsetY) }
    }

    private fun onTouchEnd(isCancel: Boolean) {
        touchEndListener.invoke { it.onTouchEnd(isCancel) }
        if (!isCancel && direction == Direction.PENDING) {
            val x = touchHelper.x
            val y = touchHelper.y
            clickListener.invoke { it.onClick(x, y) }
        }
    }

    fun addMoveListener(listener: OnTouchMoveListener) {
        touchMoveListener.addListener(listener)
    }

    fun removeMoveListener(listener: OnTouchMoveListener) {
        touchMoveListener.removeListener(listener)
    }

    fun addOffsetListener(listener: OnTouchOffsetListener) {
        touchOffsetListener.addListener(listener)
    }

    fun removeOffsetListener(listener: OnTouchOffsetListener) {
        touchOffsetListener.removeListener(listener)
    }

    fun addEndListener(listener: OnTouchEndListener) {
        touchEndListener.addListener(listener)
    }

    fun removeEndListener(listener: OnTouchEndListener) {
        touchEndListener.removeListener(listener)
    }

    fun addClickListener(listener: OnClickListener) {
        clickListener.addListener(listener)
    }

    fun removeClickListener(listener: OnClickListener) {
        clickListener.removeListener(listener)
    }

    private fun checkDirection(x: Float, y: Float) {
        if (direction != Direction.PENDING) {
            return
        }
        val xOffset = abs(x - touchDownLocation.x)
        val yOffset = abs(y - touchDownLocation.y)
        when (slider) {
            Horizontally -> {
                if (xOffset >= scaledTouchSlop) {
                    direction = Direction.HORIZONTALLY
                } else if (yOffset >= scaledTouchSlop) {
                    direction = Direction.NONE
                }
            }
            Vertically -> {
                if (yOffset >= scaledTouchSlop) {
                    direction = Direction.VERTICALLY
                } else if (xOffset >= scaledTouchSlop) {
                    direction = Direction.NONE
                }
            }
        }
    }

    private enum class Direction {
        NONE,
        PENDING,
        HORIZONTALLY,
        VERTICALLY
    }

    enum class Slider {
        Horizontally,
        Vertically
    }

    fun interface OnTouchMoveListener {
        fun onTouchMoved(x: Float, y: Float)
    }

    fun interface OnTouchOffsetListener {
        fun onTouchMoved(offsetX: Float, offsetY: Float)
    }

    fun interface OnTouchEndListener {
        fun onTouchEnd(isCancel: Boolean)
    }

    fun interface OnClickListener {
        fun onClick(x: Float, y: Float)
    }

}