package com.lollipop.base.util

import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets

/**
 * @author lollipop
 * @date 4/19/21 22:40
 * Window缩进的辅助工具
 */
class WindowInsetsHelper(
        private val applyType: ApplyType,
        private val insetsListener: OnWindowInsetsChangedListener? = null
) : View.OnApplyWindowInsetsListener {

    /**
     * 基础的Margin值，它表示了最小的margin
     * 如果缩进尺寸小于此值，那么会保持在此值
     */
    private val baseMargin = Rect()

    /**
     * 基础的padding值，它代表了最小的padding值
     * 如果缩进尺寸小于此值，那么将会保持在此值
     */
    private val basePadding = Rect()

    /**
     * 以快照的形式记录当前Margin值，并且以此为基础
     */
    fun snapshotMargin(target: View) {
        target.layoutParams?.let { layoutParams ->
            if (layoutParams is ViewGroup.MarginLayoutParams) {
                baseMargin.set(
                        layoutParams.leftMargin,
                        layoutParams.topMargin,
                        layoutParams.rightMargin,
                        layoutParams.bottomMargin
                )
            }
        }
    }

    /**
     * 以快照的形式记录当前Padding值，并且以此为基础
     */
    fun snapshotPadding(target: View) {
        basePadding.set(
                target.paddingLeft,
                target.paddingTop,
                target.paddingRight,
                target.paddingBottom
        )
    }

    private fun setMargin(target: View, left: Int, top: Int, right: Int, bottom: Int) {
        target.layoutParams?.let { layoutParams ->
            if (layoutParams is ViewGroup.MarginLayoutParams) {
                layoutParams.setMargins(left, top, right, bottom)
            }
            target.layoutParams = layoutParams
        }
    }

    private fun setPadding(target: View, left: Int, top: Int, right: Int, bottom: Int) {
        target.setPadding(left, top, right, bottom)
    }

    override fun onApplyWindowInsets(v: View, insets: WindowInsets): WindowInsets {
        val listener = insetsListener
        if (listener != null) {
            return listener.onWindowInsetsChanged(v, applyType, insets)
        }
        val insetsValue = if (versionThen(Build.VERSION_CODES.R)) {
            val value = insets.getInsets(
                    WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
            InsetsValue(value.left, value.top, value.right, value.bottom)
        } else {
            InsetsValue(insets.systemWindowInsetLeft,
                    insets.systemWindowInsetTop,
                    insets.systemWindowInsetRight,
                    insets.systemWindowInsetBottom)
        }

        when (applyType) {
            ApplyType.Margin -> {
                insetsValue.max(baseMargin)
                setMargin(
                        v,
                        insetsValue.left,
                        insetsValue.top,
                        insetsValue.right,
                        insetsValue.bottom)
            }
            ApplyType.Padding -> {
                insetsValue.max(basePadding)
                setPadding(
                        v,
                        insetsValue.left,
                        insetsValue.top,
                        insetsValue.right,
                        insetsValue.bottom)
            }
        }
        return insets
    }

    private class InsetsValue(
            var left: Int,
            var top: Int,
            var right: Int,
            var bottom: Int
    ) {

        fun max(rect: Rect) {
            if (left < rect.left) {
                left = rect.left
            }
            if (top < rect.top) {
                top = rect.top
            }
            if (right < rect.right) {
                right = rect.right
            }
            if (bottom < rect.bottom) {
                bottom = rect.bottom
            }
        }
    }

    enum class ApplyType {
        Padding, Margin
    }

    fun interface OnWindowInsetsChangedListener {
        fun onWindowInsetsChanged(v: View, type: ApplyType, insets: WindowInsets): WindowInsets
    }

}

fun View.fixInsetsByPadding(
        listener: WindowInsetsHelper.OnWindowInsetsChangedListener? = null) {
    setOnApplyWindowInsetsListener(
            WindowInsetsHelper(WindowInsetsHelper.ApplyType.Padding, listener).apply {
                snapshotPadding(this@fixInsetsByPadding)
            }
    )
}

fun View.fixInsetsByMargin(
        listener: WindowInsetsHelper.OnWindowInsetsChangedListener? = null) {
    setOnApplyWindowInsetsListener(
            WindowInsetsHelper(WindowInsetsHelper.ApplyType.Margin, listener).apply {
                snapshotMargin(this@fixInsetsByMargin)
            }
    )
}
