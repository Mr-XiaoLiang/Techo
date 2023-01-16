package com.lollipop.base.util.insets

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

/**
 * @author lollipop
 * @date 4/19/21 22:40
 * Window缩进的辅助工具
 */
class WindowInsetsHelper(
    private val applyType: WindowInsetsApplyType,
    private val edge: WindowInsetsEdge = WindowInsetsEdge.ALL,
    private val targetView: View? = null
) : OnApplyWindowInsetsListener {

    companion object {
        fun getInsetsValue(insets: WindowInsetsCompat): WindowInsetsValue {
            return WindowInsetsValue(insets.getInsets(WindowInsetsCompat.Type.systemBars()))
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
    var baseMargin: BoundsSnapshot = BoundsSnapshot.EMPTY
        private set

    /**
     * 基础的padding值，它代表了最小的padding值
     * 如果缩进尺寸小于此值，那么将会保持在此值
     */
    var basePadding: BoundsSnapshot = BoundsSnapshot.EMPTY
        private set

    /**
     * 以快照的形式记录当前Margin值，并且以此为基础
     */
    fun snapshotMargin(target: View) {
        target.layoutParams?.let { layoutParams ->
            if (layoutParams is ViewGroup.MarginLayoutParams) {
                baseMargin = BoundsSnapshot(
                    left = layoutParams.leftMargin,
                    top = layoutParams.topMargin,
                    right = layoutParams.rightMargin,
                    bottom = layoutParams.bottomMargin
                )
            }
        }
    }

    /**
     * 以快照的形式记录当前Padding值，并且以此为基础
     */
    fun snapshotPadding(target: View) {
        basePadding = BoundsSnapshot(
            left = target.paddingLeft,
            top = target.paddingTop,
            right = target.paddingRight,
            bottom = target.paddingBottom
        )
    }

    override fun onApplyWindowInsets(v: View, insets: WindowInsetsCompat): WindowInsetsCompat {
        return applyType.onWindowInsetsChanged(
            targetView ?: v,
            edge,
            baseMargin,
            basePadding,
            insets
        )
    }

    fun interface OnWindowInsetsChangedListener {
        fun onWindowInsetsChanged(
            v: View,
            option: WindowInsetsOption,
            insets: WindowInsetsCompat
        ): WindowInsetsCompat
    }

}

fun View.fixInsetsByPadding(
    edge: WindowInsetsEdge = WindowInsetsEdge.ALL,
    target: View? = null,
): WindowInsetsHelper {
    return setWindowInsetsHelper(
        WindowInsetsApplyType.Padding,
        edge,
        target
    )
}

fun View.fixInsetsByListener(
    listener: WindowInsetsHelper.OnWindowInsetsChangedListener
): WindowInsetsHelper {
    return setWindowInsetsHelper(
        WindowInsetsApplyType.Custom(listener),
        WindowInsetsEdge.ALL,
        null
    )
}

fun View.fixInsetsByMargin(
    edge: WindowInsetsEdge = WindowInsetsEdge.ALL,
    target: View? = null
): WindowInsetsHelper {
    return setWindowInsetsHelper(
        WindowInsetsApplyType.Margin,
        edge,
        target
    )
}

private fun View.setWindowInsetsHelper(
    type: WindowInsetsApplyType,
    edge: WindowInsetsEdge,
    customTarget: View?
): WindowInsetsHelper {
    val windowInsetsHelper = WindowInsetsHelper(type, edge, customTarget ?: this)
    ViewCompat.setOnApplyWindowInsetsListener(this, windowInsetsHelper)

    windowInsetsHelper.snapshotPadding(this)
    windowInsetsHelper.snapshotMargin(this)

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
