package com.lollipop.base.util.insets

import android.view.View
import androidx.core.view.WindowInsetsCompat
import kotlin.math.max

class MultipleInsetsDelegate(
    private val applyType: ApplyType,
    private val target: View? = null,
    private vararg val insetsType: WindowInsetsType,
) : WindowInsetsHelper.OnWindowInsetsChangedListener {

    override fun onWindowInsetsChanged(
        v: View,
        operator: WindowInsetsOperator,
        insets: WindowInsetsCompat,
    ): WindowInsetsCompat {
        var maxLeft = 0
        var maxTop = 0
        var maxRight = 0
        var maxBottom = 0
        insetsType.forEach {
            operator.insetsType = it
            val insetsValue = when (applyType) {
                ApplyType.PADDING -> {
                    operator.computeInsetsValueByPadding(insets)
                }

                ApplyType.MARGIN -> {
                    operator.computeInsetsValueByMargin(insets)
                }
            }
            maxLeft = max(maxLeft, insetsValue.left)
            maxTop = max(maxTop, insetsValue.top)
            maxRight = max(maxRight, insetsValue.right)
            maxBottom = max(maxBottom, insetsValue.bottom)
        }
        when (applyType) {
            ApplyType.PADDING -> {
                WindowInsetsHelper.setPadding(
                    target ?: v,
                    maxLeft,
                    maxTop,
                    maxRight,
                    maxBottom
                )
            }

            ApplyType.MARGIN -> {
                WindowInsetsHelper.setMargin(
                    target ?: v,
                    maxLeft,
                    maxTop,
                    maxRight,
                    maxBottom
                )
            }
        }
        return insets
    }

    enum class ApplyType {
        PADDING,
        MARGIN
    }

}