package com.lollipop.base.util.insets

import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat

class WindowInsetsOperator(
    val edge: WindowInsetsEdge
) {

    companion object {
        fun setMargin(target: View, left: Int, top: Int, right: Int, bottom: Int) {
            target.layoutParams?.let { layoutParams ->
                if (layoutParams is ViewGroup.MarginLayoutParams) {
                    layoutParams.setMargins(left, top, right, bottom)
                }
                target.layoutParams = layoutParams
            }
        }

        fun setPadding(target: View, left: Int, top: Int, right: Int, bottom: Int) {
            target.setPadding(left, top, right, bottom)
        }

        fun setHeight(target: View, height: Int) {
            var params = target.layoutParams
            if (params == null) {
                params = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
            }
            params.height = height
            target.layoutParams = params
        }

        fun setWidth(target: View, width: Int) {
            var params = target.layoutParams
            if (params == null) {
                params = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
            }
            params.width = width
            target.layoutParams = params
        }
    }

    /**
     * 基础的Margin值，它表示了最小的margin
     * 如果缩进尺寸小于此值，那么会保持在此值
     */
    var baseMargin: BoundsSnapshot = BoundsSnapshot.EMPTY
        private set

    /**
     * 基础的padding值，它代表了最小的padding值
     * 如果缩进尺寸小于此值，那么将会保持在此值
     */
    var basePadding: BoundsSnapshot = BoundsSnapshot.EMPTY
        private set

    /**
     * 缩进取值类型
     */
    var insetsType = WindowInsetsType.SYSTEM_BARS

    /**
     * 以快照的形式记录当前Margin值，并且以此为基础
     */
    fun snapshotMargin(target: View) {
        baseMargin = snapshotMarginInner(target)
    }

    private fun snapshotMarginInner(target: View): BoundsSnapshot {
        val layoutParams = target.layoutParams ?: return BoundsSnapshot.EMPTY
        if (layoutParams is ViewGroup.MarginLayoutParams) {
            return BoundsSnapshot(
                left = layoutParams.leftMargin,
                top = layoutParams.topMargin,
                right = layoutParams.rightMargin,
                bottom = layoutParams.bottomMargin
            )
        }
        return BoundsSnapshot.EMPTY
    }


    /**
     * 以快照的形式记录当前Padding值，并且以此为基础
     */
    fun snapshotPadding(target: View) {
        basePadding = snapshotPaddingInner(target)
    }

    private fun snapshotPaddingInner(target: View): BoundsSnapshot {
        return BoundsSnapshot(
            left = target.paddingLeft,
            top = target.paddingTop,
            right = target.paddingRight,
            bottom = target.paddingBottom
        )
    }

    fun computeInsetsValueByPadding(insets: WindowInsetsCompat): WindowInsetsValue {
        val insetsValue = WindowInsetsHelper.getInsetsValue(insets, insetsType)
        insetsValue.basePlus(basePadding, edge)
        return insetsValue
    }

    fun computeInsetsValueByMargin(insets: WindowInsetsCompat): WindowInsetsValue {
        val insetsValue = WindowInsetsHelper.getInsetsValue(insets, insetsType)
        insetsValue.basePlus(baseMargin, edge)
        return insetsValue
    }

    fun padding(
        target: View,
        left: Int = target.paddingLeft,
        top: Int = target.paddingTop,
        right: Int = target.paddingRight,
        bottom: Int = target.paddingBottom
    ) {
        setPadding(target, left, top, right, bottom)
    }

}