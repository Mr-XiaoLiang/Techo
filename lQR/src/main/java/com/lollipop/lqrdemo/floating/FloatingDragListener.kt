package com.lollipop.lqrdemo.floating

import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import kotlin.math.abs

abstract class FloatingDragListener() : View.OnTouchListener {

    companion object {
        fun offsetView(view: View, windowManager: WindowManager, offsetX: Int, offsetY: Int) {
            val layoutParams = view.layoutParams ?: return
            when (layoutParams) {
                is WindowManager.LayoutParams -> {
                    layoutParams.x += offsetX
                    layoutParams.y += offsetY
                    windowManager.updateViewLayout(view, layoutParams)
                }

                else -> {
                    view.offsetLeftAndRight(offsetX)
                    view.offsetTopAndBottom(offsetY)
                }
            }
        }
    }

    private var lastTouchX = 0F
    private var lastTouchY = 0F
    private var touchDownX = 0F
    private var touchDownY = 0F

    private var dragEnable = false

    private fun MotionEvent.activeX(): Float {
        return rawX
    }

    private fun MotionEvent.activeY(): Float {
        return rawY
    }

    abstract fun onMove(offsetX: Int, offsetY: Int)

    protected open fun onTouchDown() {}
    protected open fun onTouchUp() {}

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        v ?: return false
        event ?: return false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x
                val y = event.y
                if (x < 0 || x > v.width || y < 0 || y > v.height) {
                    dragEnable = false
                    return false
                }
                dragEnable = true
                lastTouchX = event.activeX()
                lastTouchY = event.activeY()
                touchDownX = lastTouchX
                touchDownY = lastTouchY
                onTouchDown()
                Log.d(
                    "FloatingDragListener",
                    "DOWN: [$touchDownX, $touchDownY] ==> [${event.x}, ${event.y}]"
                )
            }

            MotionEvent.ACTION_MOVE -> {
                if (!dragEnable) {
                    return false
                }
                val x = event.activeX()
                val y = event.activeY()
                val offerX = x - lastTouchX
                val offerY = y - lastTouchY
                lastTouchX = x
                lastTouchY = y
                val oxi = offerX.toInt()
                val oyi = offerY.toInt()
                // 小数点偏差补齐
                lastTouchX -= offerX - oxi
                lastTouchY -= offerY - oyi
                Log.d(
                    "FloatingDragListener",
                    "MOVE: [$x, $y] ==> [$oxi, $oyi]"
                )
                onMove(oxi, oyi)
            }

            MotionEvent.ACTION_UP -> {
                if (!dragEnable) {
                    return false
                }
                onTouchUp()
                dragEnable = false
                val x = event.activeX()
                val y = event.activeY()
                val touchSlop = ViewConfiguration.get(v.context).scaledTouchSlop
                val totalOffsetX = touchDownX - x
                val totalOffsetY = touchDownY - y
                if (abs(totalOffsetX) < touchSlop && abs(totalOffsetY) < touchSlop) {
                    // 轻触
                    v.performClick()
                } else {
                    // 拖拽
                    v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                if (dragEnable) {
                    onTouchUp()
                }
                dragEnable = false
            }
        }
        return dragEnable
    }

}

class SimpleDragListener(
    val moveTo: (Int, Int) -> Unit
) : FloatingDragListener() {
    override fun onMove(offsetX: Int, offsetY: Int) {
        moveTo(offsetX, offsetY)
    }
}