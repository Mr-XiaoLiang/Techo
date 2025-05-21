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
import androidx.core.view.isVisible
import com.lollipop.lqrdemo.floating.view.FloatingView
import com.lollipop.lqrdemo.floating.view.FloatingViewBerth
import com.lollipop.lqrdemo.floating.view.FloatingViewConfig
import com.lollipop.lqrdemo.floating.view.FloatingViewFactory
import com.lollipop.lqrdemo.floating.view.FloatingViewState

class FloatingViewDelegate {

    private var floatingViewFactory: FloatingViewFactory? = null
    private var floatingView: FloatingView? = null
    private var currentView: View? = null

    private var config = FloatingViewConfig()

    fun attach(factory: FloatingViewFactory, config: FloatingViewConfig) {
        this.floatingViewFactory = factory
        this.config = config
    }

    fun detach() {
        clear()
        floatingViewFactory = null
    }

    fun show(context: Context) {
        if (floatingView != null && currentView != null) {
            currentView?.isVisible = true
            return
        }
        if (config.widthDp == 0 || config.heightDp == 0) {
            return
        }
        val factory = floatingViewFactory ?: return
        val newView = factory.create()
        floatingView = newView
        val view = newView.createView(
            context,
            config.widthDp,
            config.heightDp,
            config.berthWeight
        )
        attachToWindow(view)
    }

    fun hide() {
        currentView?.isVisible = false
    }

    fun remove() {
        clear()
    }

    private fun clear() {
        floatingView = null
        currentView?.let {
            removeView(it)
        }
        currentView = null
    }

    private fun removeView(view: View) {
        try {
            val parent = view.parent
            if (parent is ViewManager) {
                parent.removeView(view)
            }
        } catch (e: Throwable) {
            Log.e("FloatingViewDelegate", "removeView", e)
        }
    }

    private fun attachToWindow(view: View) {
        val context = view.context
        removeView(view)
        addAlertView(context, view) { wm, v, params ->
            params.x = 0
            params.y = 0
            params.gravity = 0
            params.flags = buildFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
            v.setOnTouchListener(ViewDragDelegate(view, wm, ::onBerthChanged, ::onStateChanged))
        }
    }

    private fun onBerthChanged(berth: FloatingViewBerth) {
        floatingView?.onBerthChanged(berth)
    }

    private fun onStateChanged(state: FloatingViewState) {
        floatingView?.onStateChanged(state)
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
            layoutParams.width = layoutParamsSize(context, config.widthDp)
            layoutParams.height = layoutParamsSize(context, config.heightDp)
            layoutParams.format = PixelFormat.TRANSPARENT;
            builder(windowManager, view, layoutParams)
            windowManager.addView(view, layoutParams);
        }
        return layoutParams
    }

    private fun layoutParamsSize(context: Context, dp: Int): Int {
        if (dp < 0) {
            return dp
        }
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }

    private class ViewDragDelegate(
        private val view: View,
        private val wm: WindowManager,
        private val onBerthChanged: (FloatingViewBerth) -> Unit,
        private val onStateChanged: (FloatingViewState) -> Unit
    ) : FloatingDragListener() {

        private var berth = FloatingViewBerth.NONE
        private var state = FloatingViewState.IDLE

        override fun onTouchDown() {
            super.onTouchDown()
            state = FloatingViewState.DRAGGING
            onStateChanged(state)
        }

        override fun onTouchUp() {
            super.onTouchUp()
            state = FloatingViewState.IDLE
            onStateChanged(state)
        }

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
                FloatingViewBerth.LEFT
            } else if (x == maxX) {
                FloatingViewBerth.RIGHT
            } else {
                FloatingViewBerth.NONE
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