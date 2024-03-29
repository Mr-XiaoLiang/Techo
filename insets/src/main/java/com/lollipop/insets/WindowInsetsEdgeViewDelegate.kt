package com.lollipop.insets

import android.view.View
import androidx.core.view.WindowInsetsCompat
import com.lollipop.insets.WindowInsetsEdgeViewDelegate.Direction.*
import kotlin.math.max

class WindowInsetsEdgeViewDelegate(
    private val direction: Direction,
    private val minSize: Int = 0,
    private val baseValue: Int = 0,
    private val insetsType: WindowInsetsType = WindowInsetsType.SystemBars
) : WindowInsetsHelper.OnWindowInsetsChangedListener {
    override fun onWindowInsetsChanged(
        v: View,
        operator: WindowInsetsOperator,
        insets: WindowInsetsCompat
    ): WindowInsetsCompat {
        val insetsValue = WindowInsetsHelper.getInsetsValue(insets, insetsType)
        val insetsSize = when (direction) {
            LEFT -> insetsValue.left
            TOP -> insetsValue.top
            RIGHT -> insetsValue.right
            BOTTOM -> insetsValue.bottom
        }
        val viewSize = max(insetsSize + baseValue, minSize)
        when (direction) {
            LEFT,
            RIGHT -> {
                WindowInsetsHelper.setWidth(v, viewSize)
            }
            TOP,
            BOTTOM -> {
                WindowInsetsHelper.setHeight(v, viewSize)
            }
        }
        return insets
    }

    enum class Direction {
        LEFT,
        TOP,
        RIGHT,
        BOTTOM;
    }

}