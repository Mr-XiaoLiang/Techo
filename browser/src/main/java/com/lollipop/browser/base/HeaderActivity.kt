package com.lollipop.browser.base

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.lollipop.base.util.insets.WindowInsetsEdge
import com.lollipop.base.util.insets.WindowInsetsEdgeStrategy
import com.lollipop.base.util.insets.WindowInsetsHelper
import com.lollipop.base.util.insets.fixInsetsByMargin
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.onUI
import com.lollipop.browser.databinding.ActivityHeaderBinding

abstract class HeaderActivity : AppCompatActivity() {

    private val viewBinding: ActivityHeaderBinding by lazyBind()

    abstract val contentView: View

    open val floatingView: View? = null

    open val headerView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowInsetsHelper.initWindowFlag(this)
        setContentView(viewBinding.root)
        viewBinding.contentRoot.addView(
            contentView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        floatingView?.let {
            viewBinding.floatingRoot.addView(
                it,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        headerView?.let {
            viewBinding.headerGroup.addView(
                it,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        viewBinding.backButton.setOnClickListener {
            notifyBackPress()
        }
        viewBinding.appBar.fixInsetsByMargin(WindowInsetsEdge.HEADER)
        viewBinding.contentScrollView.fixInsetsByMargin(
            WindowInsetsEdge(
                WindowInsetsEdgeStrategy.ACCUMULATE,
                WindowInsetsEdgeStrategy.ACCUMULATE,
                WindowInsetsEdgeStrategy.ACCUMULATE,
                WindowInsetsEdgeStrategy.ORIGINAL
            )
        )
        onDecorationChanged(
            viewBinding.backButtonIcon,
            viewBinding.backButtonBackground,
            viewBinding.contentLoadingView
        )
        hideLoading()
    }

    override fun setTitle(title: CharSequence?) {
        viewBinding.titleView.text = title ?: ""
    }

    protected open fun onDecorationChanged(
        backButtonIcon: ImageView,
        backButtonBackground: View,
        contentLoadingView: CircularProgressIndicator
    ) {
    }

    protected fun hideLoading() {
        viewBinding.contentLoadingView.hide()
    }

    protected fun showLoading() {
        viewBinding.contentLoadingView.show()
    }

    fun notifyBackPress() {
        onBackPressedDispatcher.onBackPressed()
    }

}