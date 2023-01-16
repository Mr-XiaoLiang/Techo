package com.lollipop.base.util.insets

import androidx.core.graphics.Insets
import kotlin.math.max

class WindowInsetsValue(insets: Insets) {

    var left = insets.left
    var top = insets.top
    var right = insets.right
    var bottom = insets.bottom

    fun basePlus(base: BoundsSnapshot, edge: WindowInsetsEdge) {
        left = getNewValue(left, base.left, edge.left)
        top = getNewValue(top, base.top, edge.top)
        right = getNewValue(right, base.right, edge.right)
        bottom = getNewValue(bottom, base.bottom, edge.bottom)
    }

    private fun getNewValue(insets: Int, original: Int, s: WindowInsetsEdgeStrategy): Int {
        return when (s) {
            WindowInsetsEdgeStrategy.ORIGINAL -> {
                original
            }
            WindowInsetsEdgeStrategy.ACCUMULATE -> {
                insets + original
            }
            WindowInsetsEdgeStrategy.COMPARE -> {
                max(insets, original)
            }
            WindowInsetsEdgeStrategy.INSETS -> {
                insets
            }
        }
    }
}