package com.lollipop.base.util

import android.view.MotionEvent

class SingleTouchHelper {

    companion object {
        const val POINT_ID_NONE = -1
    }

    var touchPointId = POINT_ID_NONE
        private set

    private var touchX = 0F
    private var touchY = 0F

    val x: Float
        get() {
            return touchX
        }
    val y: Float
        get() {
            return touchY
        }

    fun onTouch(event: MotionEvent?) {
        event ?: return
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchPointId = event.getPointerId(0)
                touchX = event.activeX()
                touchY = event.activeY()
            }
            MotionEvent.ACTION_UP -> {
                touchX = event.activeX()
                touchY = event.activeY()
                touchPointId = POINT_ID_NONE
            }
            else -> {
                touchX = event.activeX()
                touchY = event.activeY()
            }
        }
    }

    private fun MotionEvent.activeX(): Float {
        return getX(activeIndex())
    }

    private fun MotionEvent.activeY(): Float {
        return getY(activeIndex())
    }

    private fun MotionEvent.activeIndex(): Int {
        var pointerIndex = findPointerIndex(touchPointId)
        if (pointerIndex < 0) {
            pointerIndex = 0
            touchPointId = getPointerId(0)
        }
        return pointerIndex
    }

}