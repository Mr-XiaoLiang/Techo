package com.lollipop.base.util.insets

import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import com.lollipop.base.util.insets.WindowInsetsEdgeViewDelegate.Direction.*
import kotlin.math.max

class WindowInsetsEdgeViewDelegate(
    private val direction: Direction,
    private val minSize: Int = 0,
    private val baseValue: Int = 0
) : WindowInsetsHelper.OnWindowInsetsChangedListener {
    override fun onWindowInsetsChanged(
        v: View,
        option: WindowInsetsOption,
        insets: WindowInsetsCompat
    ): WindowInsetsCompat {
        val insetsValue = WindowInsetsHelper.getInsetsValue(insets)
        val insetsSize = when (direction) {
            LEFT -> insetsValue.left
            TOP -> insetsValue.top
            RIGHT -> insetsValue.right
            BOTTOM -> insetsValue.bottom
        }
        val viewSize = max(insetsSize + baseValue, minSize)

        var params = v.layoutParams
        if (params == null) {
            params = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
        }
        when (direction) {
            LEFT,
            RIGHT -> {
                params.width = viewSize
            }
            TOP,
            BOTTOM -> {
                params.height = viewSize
            }
        }
        v.layoutParams = params
        return insets
    }

    enum class Direction {
        LEFT,
        TOP,
        RIGHT,
        BOTTOM;
    }

}