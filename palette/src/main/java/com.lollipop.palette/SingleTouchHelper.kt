package com.lollipop.palette

import android.graphics.PointF
import android.view.MotionEvent

internal class SingleTouchHelper(
    private val onTouchDown: (x: Float, y: Float) -> Unit,
    private val onTouchMove: (x: Float, y: Float, offsetX: Float, offsetY: Float) -> Unit,
    private var onTouchUp: (cancel: Boolean) -> Unit
) {

    private var touchDownPoint = PointF()
    private var lastPoint = PointF()
    private var activePointId = 0

    fun onTouchEvent(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                activePointId = event.getPointerId(0)
                val x = event.x
                val y = event.y
                resetPoint(x, y)
                onTouchDown(x, y)
            }
            MotionEvent.ACTION_MOVE -> {
                onTouchMove(event)
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val lastId = activePointId
                event.getPointIndex()
                // 如果ID被修改，那么就
                if (activePointId != lastId) {
                    resetPoint(event.activeX(), event.activeY())
                }
            }
            MotionEvent.ACTION_UP -> {
                onTouchMove(event)
                onTouchUp(false)
            }
            MotionEvent.ACTION_CANCEL -> {
                onTouchUp(true)
            }
        }
    }

    private fun resetPoint(x: Float, y: Float) {
        touchDownPoint.set(x, y)
        lastPoint.set(touchDownPoint)
    }

    private fun onTouchMove(event: MotionEvent) {
        val x = event.activeX()
        val y = event.activeY()
        val offsetX = x - lastPoint.x
        val offsetY = y - lastPoint.y
        lastPoint.set(x, y)
        onTouchMove(x, y, offsetX, offsetY)
    }

    private fun MotionEvent.getPointIndex(): Int {
        val index = findPointerIndex(activePointId)
        if (index >= 0) {
            return index
        }
        activePointId = getPointerId(0)
        return 0
    }

    private fun MotionEvent.activeX(): Float {
        return getX(getPointIndex())
    }

    private fun MotionEvent.activeY(): Float {
        return getY(getPointIndex())
    }

}