package com.lollipop.base.util.insets

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * @author lollipop
 * @date 4/19/21 22:40
 * Window缩进的辅助工具
 */
class WindowInsetsHelper(
    private val applyType: WindowInsetsApplyType,
    val windowInsetsOperator: WindowInsetsOperator,
    private val targetView: View? = null,
) : OnApplyWindowInsetsListener {

    companion object {
        fun getInsetsValue(
            insets: WindowInsetsCompat,
            type: WindowInsetsType = WindowInsetsType.SYSTEM_BARS,
        ): WindowInsetsValue {
            val typeMask = when (type) {
                WindowInsetsType.SYSTEM_BARS -> {
                    WindowInsetsCompat.Type.systemBars()
                }

                WindowInsetsType.DISPLAY_CUTOUT -> {
                    WindowInsetsCompat.Type.displayCutout()
                }

                WindowInsetsType.TAPPABLE_ELEMENT -> {
                    WindowInsetsCompat.Type.tappableElement()
                }

                WindowInsetsType.MANDATORY_SYSTEM_GESTURES -> {
                    WindowInsetsCompat.Type.mandatorySystemGestures()
                }

                WindowInsetsType.SYSTEM_GESTURES -> {
                    WindowInsetsCompat.Type.systemGestures()
                }

                WindowInsetsType.IME -> {
                    WindowInsetsCompat.Type.ime()
                }

                WindowInsetsType.CAPTION_BAR -> {
                    WindowInsetsCompat.Type.captionBar()
                }

                WindowInsetsType.NAVIGATION_BARS -> {
                    WindowInsetsCompat.Type.navigationBars()
                }

                WindowInsetsType.STATUS_BARS -> {
                    WindowInsetsCompat.Type.systemBars()
                }
            }
            return WindowInsetsValue(insets.getInsets(typeMask))
        }

        fun fitsSystemWindows(activity: Activity) {
            fitsSystemWindows(activity.window)
        }

        fun fitsSystemWindows(window: Window) {
            window.apply {
                statusBarColor = Color.TRANSPARENT
                navigationBarColor = Color.TRANSPARENT
            }
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        }

        fun setMargin(target: View, left: Int, top: Int, right: Int, bottom: Int) {
            WindowInsetsOperator.setMargin(target, left, top, right, bottom)
        }

        fun setPadding(target: View, left: Int, top: Int, right: Int, bottom: Int) {
            WindowInsetsOperator.setPadding(target, left, top, right, bottom)
        }

        fun setHeight(target: View, height: Int) {
            WindowInsetsOperator.setHeight(target, height)
        }

        fun setWidth(target: View, width: Int) {
            WindowInsetsOperator.setWidth(target, width)
        }

        fun getController(activity: Activity): WindowInsetsControllerCompat {
            return getController(activity.window)
        }

        fun getController(window: Window): WindowInsetsControllerCompat {
            return WindowCompat.getInsetsController(window, window.decorView)
        }

    }

    /**
     * 以快照的形式记录当前Margin值，并且以此为基础
     */
    fun snapshotMargin(target: View) {
        windowInsetsOperator.snapshotMargin(target)
    }

    /**
     * 以快照的形式记录当前Padding值，并且以此为基础
     */
    fun snapshotPadding(target: View) {
        windowInsetsOperator.snapshotPadding(target)
    }

    override fun onApplyWindowInsets(v: View, insets: WindowInsetsCompat): WindowInsetsCompat {
        return applyType.onWindowInsetsChanged(
            targetView ?: v,
            windowInsetsOperator,
            insets
        )
    }

    fun interface OnWindowInsetsChangedListener {
        fun onWindowInsetsChanged(
            v: View,
            operator: WindowInsetsOperator,
            insets: WindowInsetsCompat,
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
    listener: WindowInsetsHelper.OnWindowInsetsChangedListener,
): WindowInsetsHelper {
    return setWindowInsetsHelper(
        WindowInsetsApplyType.Custom(listener),
        WindowInsetsEdge.ALL,
        null
    )
}

fun View.fixInsetsByMargin(
    edge: WindowInsetsEdge = WindowInsetsEdge.ALL,
    target: View? = null,
): WindowInsetsHelper {
    return setWindowInsetsHelper(
        WindowInsetsApplyType.Margin,
        edge,
        target
    )
}

fun View.fixInsetsByMultiple(
    edge: WindowInsetsEdge = WindowInsetsEdge.ALL,
    type: MultipleInsetsDelegate.ApplyType,
    target: View? = null,
    vararg insetsType: WindowInsetsType,
): WindowInsetsHelper {
    return setWindowInsetsHelper(
        WindowInsetsApplyType.Custom(MultipleInsetsDelegate(type, target, *insetsType)),
        edge,
        null
    )
}

private fun View.setWindowInsetsHelper(
    type: WindowInsetsApplyType,
    edge: WindowInsetsEdge,
    customTarget: View?,
): WindowInsetsHelper {
    val windowInsetsHelper = WindowInsetsHelper(
        type,
        WindowInsetsOperator(edge),
        customTarget ?: this
    )
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
