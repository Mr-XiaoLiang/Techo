package com.lollipop.base.util

import android.app.Activity
import android.graphics.Color
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import androidx.core.graphics.Insets
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.lollipop.base.util.WindowInsetsHelper.EdgeStrategy.*
import kotlin.math.max

/**
 * @author lollipop
 * @date 4/19/21 22:40
 * Window缩进的辅助工具
 */
class WindowInsetsHelper(
    val applyType: ApplyType,
    val edge: Edge = Edge.ALL,
    private val insetsListener: OnWindowInsetsChangedListener? = null,
    private val targetView: View? = null
) : View.OnApplyWindowInsetsListener {

    companion object {
        fun getInsetsValue(insets: WindowInsets): InsetsValue {
            return WindowInsetsCompat.toWindowInsetsCompat(insets)
                .getInsets(WindowInsetsCompat.Type.systemBars()).let {
                    InsetsValue(it)
                }
        }

        fun initWindowFlag(activity: Activity) {
            activity.window.apply {
                statusBarColor = Color.TRANSPARENT
                navigationBarColor = Color.TRANSPARENT
            }
            WindowCompat.setDecorFitsSystemWindows(activity.window, false)
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
                    targetView ?: v,
                    insetsValue.left,
                    insetsValue.top,
                    insetsValue.right,
                    insetsValue.bottom
                )
            }
            ApplyType.Padding -> {
                insetsValue.basePlus(basePadding, edge)
                setPadding(
                    targetView ?: v,
                    insetsValue.left,
                    insetsValue.top,
                    insetsValue.right,
                    insetsValue.bottom
                )
            }
        }
        return insets
    }

    class InsetsValue(insets: Insets) {

        var left = insets.left
        var top = insets.top
        var right = insets.right
        var bottom = insets.bottom

        fun basePlus(base: Rect, edge: Edge) {
            left = getNewValue(left, base.left, edge.left)
            top = getNewValue(top, base.top, edge.top)
            right = getNewValue(right, base.right, edge.right)
            bottom = getNewValue(bottom, base.bottom, edge.bottom)
        }

        private fun getNewValue(insets: Int, original: Int, s: EdgeStrategy): Int {
            return when (s) {
                ORIGINAL -> {
                    original
                }
                ACCUMULATE -> {
                    insets + original
                }
                COMPARE -> {
                    max(insets, original)
                }
                INSETS -> {
                    insets
                }
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
        val left: EdgeStrategy,
        val top: EdgeStrategy,
        val right: EdgeStrategy,
        val bottom: EdgeStrategy
    ) {
        companion object {
            val ALL = Edge(left = COMPARE, top = COMPARE, right = COMPARE, bottom = COMPARE)

            val HEADER = Edge(left = COMPARE, top = COMPARE, right = COMPARE, bottom = ORIGINAL)

            val CONTENT = Edge(left = COMPARE, top = ORIGINAL, right = COMPARE, bottom = COMPARE)
        }

        fun baseTo(
            left: EdgeStrategy = this.left,
            top: EdgeStrategy = this.top,
            right: EdgeStrategy = this.right,
            bottom: EdgeStrategy = this.bottom
        ): Edge {
            return Edge(left, top, right, bottom)
        }

    }

    enum class EdgeStrategy {
        /**
         * 累加
         */
        ACCUMULATE,

        /**
         * 比较
         */
        COMPARE,

        /**
         * 原始
         */
        ORIGINAL,

        /**
         * 缩紧
         */
        INSETS
    }

}

fun View.fixInsetsByPadding(
    edge: WindowInsetsHelper.Edge = WindowInsetsHelper.Edge.ALL,
    target: View? = null,
): WindowInsetsHelper {
    return setWindowInsetsHelper(
        WindowInsetsHelper.ApplyType.Padding,
        edge,
        null,
        target
    ).apply {
        snapshotPadding(this@fixInsetsByPadding)
    }
}

fun View.fixInsetsByPadding(
    listener: WindowInsetsHelper.OnWindowInsetsChangedListener? = null
): WindowInsetsHelper {
    return setWindowInsetsHelper(
        WindowInsetsHelper.ApplyType.Padding,
        WindowInsetsHelper.Edge.ALL,
        listener,
        null
    ).apply {
        snapshotPadding(this@fixInsetsByPadding)
    }
}

fun View.fixInsetsByMargin(
    edge: WindowInsetsHelper.Edge = WindowInsetsHelper.Edge.ALL,
    target: View? = null
): WindowInsetsHelper {
    return setWindowInsetsHelper(
        WindowInsetsHelper.ApplyType.Margin,
        edge,
        null,
        target
    ).apply {
        snapshotMargin(this@fixInsetsByMargin)
    }
}

fun View.fixInsetsByMargin(
    listener: WindowInsetsHelper.OnWindowInsetsChangedListener? = null
): WindowInsetsHelper {
    return setWindowInsetsHelper(
        WindowInsetsHelper.ApplyType.Margin,
        WindowInsetsHelper.Edge.ALL,
        listener,
        null
    ).apply {
        snapshotMargin(this@fixInsetsByMargin)
    }
}

private fun View.setWindowInsetsHelper(
    type: WindowInsetsHelper.ApplyType,
    edge: WindowInsetsHelper.Edge,
    listener: WindowInsetsHelper.OnWindowInsetsChangedListener?,
    customTarget: View?
): WindowInsetsHelper {
    val windowInsetsHelper = WindowInsetsHelper(type, edge, listener, customTarget)
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
