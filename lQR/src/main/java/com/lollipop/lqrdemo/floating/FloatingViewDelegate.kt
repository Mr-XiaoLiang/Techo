package com.lollipop.lqrdemo.floating

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.util.TypedValue
import android.view.View
import android.view.ViewManager
import android.view.WindowManager

class FloatingViewDelegate {

    private var floatingView: FloatingView? = null
    private var currentView: View? = null

    fun attach(floatingView: FloatingView) {
        this.floatingView = floatingView
    }

    fun detach() {
        clear()
        floatingView = null
    }

    fun show(context: Context, config: Config) {
        // TODO
    }

    fun hide() {
        // TODO
    }

    private fun clear() {
        // TODO
    }

    private fun attachToWindow(view: View, config: Config) {
        val context = view.context
        try {
            view.parent?.let {
                if (it is ViewManager) {
                    it.removeView(view)
                }
            }
        } catch (e: Throwable) {
            Log.e("FloatingViewDelegate", "attachToWindow.clear", e)
        }
        addAlertView(context, view) { wm, v, params ->
            params.x = 0
            params.y = 0
            params.gravity = 0
            params.flags = buildFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
            v.setOnTouchListener(ViewDragDelegate(view, wm, ::onBerthChanged))
        }
    }

    private fun onBerthChanged(berth: Berth) {
        floatingView?.onBerthChanged(berth)
    }

    private fun buildFlags(vararg flags: Int): Int {
        var result = 0
        for (flag in flags) {
            result = result or flag
        }
        return result
    }


    private fun addAlertView(
        context: Context,
        view: View,
        builder: (WindowManager, View, WindowManager.LayoutParams) -> Unit
    ): WindowManager.LayoutParams {
        val layoutParams = WindowManager.LayoutParams()
        val windowManager = context.getSystemService(
            Context.WINDOW_SERVICE
        ) as? WindowManager
        if (windowManager != null) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.format = PixelFormat.TRANSPARENT;
            builder(windowManager, view, layoutParams)
            windowManager.addView(view, layoutParams);
        }
        return layoutParams
    }

    private fun convertDpToPx(context: Context, dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }

    interface FloatingView {

        fun createView(context: Context, widthDp: Int, heightDp: Int, berthWeight: Float): View

        fun onBerthChanged(berth: Berth)

    }

    enum class Berth {

        LEFT, RIGHT, NONE

    }

    enum class State {

        IDLE, DRAGGING,

    }

    class Config(
        val widthDp: Int = 0,
        val heightDp: Int = 0,
        val berthWeight: Float = 0.5f
    )

    private class ViewDragDelegate(
        private val view: View,
        private val wm: WindowManager,
        private val onBerthChanged: (Berth) -> Unit
    ) : FloatingDragListener() {

        private var berth = Berth.NONE

        override fun onMove(offsetX: Int, offsetY: Int) {
            val layoutParams = view.layoutParams
            if (layoutParams is WindowManager.LayoutParams) {
                layoutParams.x += offsetX
                layoutParams.y += offsetY
                val screenSize = getScreenSize(wm)
                val maxX = (screenSize.width - view.width) / 2
                val maxY = (screenSize.height - view.height) / 2
                val minX = maxX * -1
                val minY = maxY * -1
                if (layoutParams.x < minX) {
                    layoutParams.x = minX
                }
                if (layoutParams.y < minY) {
                    layoutParams.y = minY
                }
                if (layoutParams.x > maxX) {
                    layoutParams.x = maxX
                }
                if (layoutParams.y > maxY) {
                    layoutParams.y = maxY
                }
                checkBerth(layoutParams.x, minX, maxX)
                wm.updateViewLayout(view, layoutParams)
            }
        }

        private fun checkBerth(x: Int, minX: Int, maxX: Int) {
            val oldBerth = berth
            berth = if (x == minX) {
                Berth.LEFT
            } else if (x == maxX) {
                Berth.RIGHT
            } else {
                Berth.NONE
            }
            if (berth != oldBerth) {
                onBerthChanged(berth)
            }
        }

        private fun getScreenSize(windowManager: WindowManager): Size {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics = windowManager.currentWindowMetrics
                return Size(windowMetrics.bounds.width(), windowMetrics.bounds.height())
            } else {
                val displayMetrics = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(displayMetrics)
                return Size(displayMetrics.widthPixels, displayMetrics.heightPixels)
            }
        }

    }

}