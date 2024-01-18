package com.lollipop.browser.base

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.lollipop.base.util.insets.WindowInsetsEdge
import com.lollipop.base.util.insets.WindowInsetsEdgeStrategy
import com.lollipop.base.util.insets.WindowInsetsHelper
import com.lollipop.base.util.insets.fixInsetsByMargin
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.onClick
import com.lollipop.browser.databinding.ActivityHeaderBinding

abstract class HeaderActivity : AppCompatActivity() {

    private val viewBinding: ActivityHeaderBinding by lazyBind()

    abstract val contentView: View

    open val floatingView: View? = null

    open val headerView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        WindowInsetsHelper.fitsSystemWindows(this)
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
        createHeaderView()?.let {
            viewBinding.headerGroup.addView(
                it,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        viewBinding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
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

    protected open fun createHeaderView(): View? {
        return null
    }

    protected fun setOptionButton(callback: (ImageView) -> Unit) {
        viewBinding.optionButton.isVisible = true
        callback(viewBinding.optionButton)
    }

    override fun setTitle(title: CharSequence?) {
        viewBinding.titleView.text = title ?: ""
    }

    protected fun onTitleClick(callback: () -> Unit) {
        viewBinding.titleView.onClick { callback() }
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

}