package com.lollipop.lqrdemo.floating

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lollipop.lqrdemo.databinding.FloatingScanButtonBinding
import com.lollipop.lqrdemo.floating.view.FloatingActionInvokeCallback
import com.lollipop.lqrdemo.floating.view.FloatingView
import com.lollipop.lqrdemo.floating.view.FloatingViewBerth
import com.lollipop.lqrdemo.floating.view.FloatingViewFactory
import com.lollipop.lqrdemo.floating.view.FloatingViewState

class FloatingActionButton(
    private val invokeCallback: FloatingActionInvokeCallback
) : FloatingView {

    companion object {

        var fabSizeDp = 48

        private const val DELAY_AUTO_HIDE = 3000L

    }

    private var binding: FloatingScanButtonBinding? = null
    private var berthWeight = 0.5f
    private var currentState = FloatingViewState.IDLE
    private var currentBerth = FloatingViewBerth.NONE
    private var lastChangeTime = 0L

    private val autoHideTask = Runnable { updateViewState() }

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
            newView.root.setOnClickListener { v ->
                v?.let {
                    invokeCallback.invoke(it.context)
                }
            }
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
        return (currentState == FloatingViewState.IDLE) && (now() - lastChangeTime > DELAY_AUTO_HIDE)
    }

    private fun postAutoHide() {
        binding?.root?.let {
            it.removeCallbacks(autoHideTask)
            if (currentState == FloatingViewState.IDLE) {
                it.postDelayed(autoHideTask, DELAY_AUTO_HIDE)
            }
        }
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
        postAutoHide()
    }

    override fun onStateChanged(state: FloatingViewState) {
        rememberViewStateChanged()
        currentState = state
        updateViewState()
        postAutoHide()
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
        override fun create(callback: FloatingActionInvokeCallback): FloatingView {
            return FloatingActionButton(callback)
        }
    }

}

