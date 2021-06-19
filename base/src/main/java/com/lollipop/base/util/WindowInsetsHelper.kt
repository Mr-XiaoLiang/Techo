package com.lollipop.base.util

import android.app.Activity
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager

/**
 * @author lollipop
 * @date 4/19/21 22:40
 * Window缩进的辅助工具
 */
class WindowInsetsHelper(
    val applyType: ApplyType,
    val edge: Edge = Edge.ALL,
    private val insetsListener: OnWindowInsetsChangedListener? = null
) : View.OnApplyWindowInsetsListener {

    companion object {
        fun getInsetsValue(insets: WindowInsets): InsetsValue {
            return if (versionThen(Build.VERSION_CODES.R)) {
                val value = insets.getInsets(
                    WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout()
                )
                InsetsValue(value.left, value.top, value.right, value.bottom)
            } else {
                InsetsValue(
                    insets.systemWindowInsetLeft,
                    insets.systemWindowInsetTop,
                    insets.systemWindowInsetRight,
                    insets.systemWindowInsetBottom
                )
            }

        }

        fun initWindowFlag(activity: Activity) {
            activity.window.apply {
                statusBarColor = Color.TRANSPARENT
                navigationBarColor = Color.TRANSPARENT
            }
            if (versionThen(Build.VERSION_CODES.R)) {
                activity.window.setDecorFitsSystemWindows(false)
            } else {
                var viewFlag = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    viewFlag = (viewFlag or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
                }
                activity.window.decorView.systemUiVisibility = viewFlag
            }
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        }

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
    }

    /**
     * 基础的Margin值，它表示了最小的margin
     * 如果缩进尺寸小于此值，那么会保持在此值
     */
    val baseMargin = Rect()

    /**
     * 基础的padding值，它代表了最小的padding值
     * 如果缩进尺寸小于此值，那么将会保持在此值
     */
    val basePadding = Rect()

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

    override fun onApplyWindowInsets(v: View, insets: WindowInsets): WindowInsets {
        val listener = insetsListener
        if (listener != null) {
            return listener.onWindowInsetsChanged(v, this, insets)
        }
        val insetsValue = getInsetsValue(insets)

        when (applyType) {
            ApplyType.Margin -> {
                insetsValue.basePlus(baseMargin, edge)
                setMargin(
                    v,
                    insetsValue.left,
                    insetsValue.top,
                    insetsValue.right,
                    insetsValue.bottom
                )
            }
            ApplyType.Padding -> {
                insetsValue.basePlus(basePadding, edge)
                setPadding(
                    v,
                    insetsValue.left,
                    insetsValue.top,
                    insetsValue.right,
                    insetsValue.bottom
                )
            }
        }
        return insets
    }

    class InsetsValue(
        var left: Int,
        var top: Int,
        var right: Int,
        var bottom: Int
    ) {
        fun basePlus(base: Rect, edge: Edge) {
            if (left < base.left || !edge.left) {
                left = base.left
            }
            if (top < base.top || !edge.top) {
                top = base.top
            }
            if (right < base.right || !edge.right) {
                right = base.right
            }
            if (bottom < base.bottom || !edge.bottom) {
                bottom = base.bottom
            }
        }
    }

    enum class ApplyType {
        Padding, Margin
    }

    fun interface OnWindowInsetsChangedListener {
        fun onWindowInsetsChanged(
            v: View,
            helper: WindowInsetsHelper,
            insets: WindowInsets
        ): WindowInsets
    }

    class Edge(
        var left: Boolean,
        var top: Boolean,
        var right: Boolean,
        var bottom: Boolean
    ) {
        companion object {
            val ALL = Edge(left = true, top = true, right = true, bottom = true)

            val HEADER = Edge(left = true, top = true, right = true, bottom = false)

            val CONTENT = Edge(left = true, top = false, right = true, bottom = true)
        }
    }

}

fun View.fixInsetsByPadding(
    edge: WindowInsetsHelper.Edge = WindowInsetsHelper.Edge.ALL,
) {
    setWindowInsetsHelper(
        WindowInsetsHelper.ApplyType.Padding,
        edge,
        null
    ).apply {
        snapshotPadding(this@fixInsetsByPadding)
    }
}

fun View.fixInsetsByPadding(
    listener: WindowInsetsHelper.OnWindowInsetsChangedListener? = null
) {
    setWindowInsetsHelper(
        WindowInsetsHelper.ApplyType.Padding,
        WindowInsetsHelper.Edge.ALL,
        listener
    ).apply {
        snapshotPadding(this@fixInsetsByPadding)
    }
}

fun View.fixInsetsByMargin(
    edge: WindowInsetsHelper.Edge = WindowInsetsHelper.Edge.ALL,
) {
    setWindowInsetsHelper(
        WindowInsetsHelper.ApplyType.Margin,
        edge,
        null
    ).apply {
        snapshotMargin(this@fixInsetsByMargin)
    }
}

fun View.fixInsetsByMargin(
    listener: WindowInsetsHelper.OnWindowInsetsChangedListener? = null
) {
    setWindowInsetsHelper(
        WindowInsetsHelper.ApplyType.Margin,
        WindowInsetsHelper.Edge.ALL,
        listener
    ).apply {
        snapshotMargin(this@fixInsetsByMargin)
    }
}

private fun View.setWindowInsetsHelper(
    type: WindowInsetsHelper.ApplyType,
    edge: WindowInsetsHelper.Edge,
    listener: WindowInsetsHelper.OnWindowInsetsChangedListener?
): WindowInsetsHelper {
    val windowInsetsHelper = WindowInsetsHelper(type, edge, listener)
    setOnApplyWindowInsetsListener(windowInsetsHelper)

    if (isAttachedToWindow) {
        requestApplyInsets()
    } else {
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                v.removeOnAttachStateChangeListener(this)
                v.requestApplyInsets()
            }

            override fun onViewDetachedFromWindow(v: View) = Unit
        })
    }

    return windowInsetsHelper
}

fun View.cleanWindowInsetHelper() {
    setOnApplyWindowInsetsListener(null)
}
