package com.lollipop.lqrdemo.floating

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lollipop.lqrdemo.databinding.FloatingScanButtonBinding
import com.lollipop.lqrdemo.floating.view.FloatingView
import com.lollipop.lqrdemo.floating.view.FloatingViewBerth
import com.lollipop.lqrdemo.floating.view.FloatingViewConfig
import com.lollipop.lqrdemo.floating.view.FloatingViewFactory
import com.lollipop.lqrdemo.floating.view.FloatingViewState

class FloatingActionButton : FloatingView {

    companion object {
        val CONFIG = FloatingViewConfig()
    }

    private var binding: FloatingScanButtonBinding? = null
    private var berthWeight = 0.5f
    private var currentState = FloatingViewState.IDLE
    private var currentBerth = FloatingViewBerth.NONE
    private var lastChangeTime = 0L

    override fun createView(
        context: Context,
        widthDp: Int,
        heightDp: Int,
        berthWeight: Float
    ): View {
        val oldView = this.binding
        if (oldView == null) {
            val newView = FloatingScanButtonBinding.inflate(LayoutInflater.from(context))
            this.binding = newView
            initView(newView, widthDp, heightDp, berthWeight)
            return newView.root
        }
        initView(oldView, widthDp, heightDp, berthWeight)
        return oldView.root
    }

    private fun initView(
        view: FloatingScanButtonBinding,
        widthDp: Int,
        heightDp: Int,
        berthWeight: Float
    ) {
        this.berthWeight = berthWeight
        val root = view.root
        val context = root.context
        root.layoutParams =
            ViewGroup.LayoutParams(dp2px(context, widthDp), dp2px(context, heightDp))
        rememberViewStateChanged()
        updateViewState()
    }

    private fun now(): Long {
        return System.currentTimeMillis()
    }

    private fun rememberViewStateChanged() {
        lastChangeTime = now()
    }

    private fun isIdle(): Boolean {
        return currentState == FloatingViewState.IDLE && now() - lastChangeTime > 500
    }

    private fun dp2px(context: Context, dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }

    override fun onBerthChanged(berth: FloatingViewBerth) {
        rememberViewStateChanged()
        currentBerth = berth
        updateViewState()
    }

    override fun onStateChanged(state: FloatingViewState) {
        rememberViewStateChanged()
        currentState = state
        updateViewState()
    }

    private fun updateViewState() {
        val alpha = if (isIdle()) {
            berthWeight
        } else {
            1f
        }
        binding?.root?.alpha = alpha
    }

    object Factory : FloatingViewFactory {
        override fun create(): FloatingView {
            return FloatingActionButton()
        }
    }

}

