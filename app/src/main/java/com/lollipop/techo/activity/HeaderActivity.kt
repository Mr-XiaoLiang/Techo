package com.lollipop.techo.activity

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.transition.ViewPropertyTransition
import com.lollipop.base.util.doAsync
import com.lollipop.base.util.insets.WindowInsetsEdge
import com.lollipop.base.util.insets.WindowInsetsEdgeStrategy
import com.lollipop.base.util.insets.WindowInsetsHelper
import com.lollipop.base.util.insets.fixInsetsByMargin
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.lazyLogD
import com.lollipop.base.util.onClick
import com.lollipop.base.util.onUI
import com.lollipop.pigment.Pigment
import com.lollipop.techo.data.RequestService
import com.lollipop.techo.databinding.ActivityHeaderBinding
import com.lollipop.techo.util.AppUtil
import com.lollipop.techo.util.BlurTransformation

/**
 * 带有头部View的Activity
 * @author Lollipop
 * @date 2021/04/25
 */
abstract class HeaderActivity : BaseActivity() {

    companion object {
        /**
         * 每次启动都保持不变吧
         */
        private var headerImageUrl = ""

    }

    private val viewBinding: ActivityHeaderBinding by lazyBind()

    abstract val contentView: View

    open val floatingView: View? = null

    protected open val showBackArrow = true

    protected open val optionsMenu = 0

    private var isBlurHeader = AppUtil.isBlurHeader

    private val log by lazyLogD()

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
        viewBinding.headerBackground.onClick {
            changeBlurState()
        }
        loadHeader(false)
        hideLoading()
    }

    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
        viewBinding.backButtonIcon.imageTintList = ColorStateList.valueOf(pigment.onPrimaryTitle)
        viewBinding.backButtonBackground.setBackgroundColor(pigment.primaryColor)
        viewBinding.contentLoadingView.setIndicatorColor(
            pigment.primaryColor,
            pigment.secondaryColor,
            pigment.primaryVariant,
            pigment.secondaryVariant
        )
    }

    override fun setTitle(title: CharSequence?) {
        viewBinding.titleView.text = title ?: ""
    }

    protected fun showLoading() {
        viewBinding.contentLoadingView.show()
    }

    override fun onResume() {
        super.onResume()
        if (isBlurHeader != AppUtil.isBlurHeader) {
            isBlurHeader = AppUtil.isBlurHeader
            loadHeader(true)
        }
    }

    private fun changeBlurState() {
        isBlurHeader = !isBlurHeader
        AppUtil.changeBlurHeader(this, isBlurHeader)
        loadHeader(true)
    }

    protected fun hideLoading() {
        viewBinding.contentLoadingView.hide()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (optionsMenu != 0) {
            menuInflater.inflate(optionsMenu, menu)
            return true
        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun loadHeader() {
        val imageUrl = headerImageUrl
        if (imageUrl.isNotEmpty()) {
            onUrlLoaded(imageUrl)
        } else {
            doAsync {
                val imageInfo = RequestService.getHeaderImageInfo()
                if (imageInfo.url.isNotEmpty()) {
                    headerImageUrl = imageInfo.fullUrl
                    onUI {
                        onUrlLoaded(headerImageUrl)
                    }
                }
            }
        }
    }

    private fun onUrlLoaded(url: String) {
        var builder = Glide.with(viewBinding.headerBackground)
            .asBitmap()
            .load(url)
            .transition(GenericTransitionOptions.with(AlphaAnimator()))
        if (isBlurHeader) {
            builder = builder.apply(RequestOptions().transform(BlurTransformation.create()))
        }
        builder.into(viewBinding.headerBackground)
    }

    fun resultOk(callback: (Intent) -> Unit) {
        setResult(Activity.RESULT_OK, Intent().apply { callback(this) })
    }

    fun resultCanceled(callback: ((Intent) -> Unit)? = null) {
        setResult(Activity.RESULT_CANCELED, Intent().apply { callback?.invoke(this) })
    }

    private class AlphaAnimator : ViewPropertyTransition.Animator {
        override fun animate(view: View?) {
            view ?: return
            view.alpha = 0F
            view.animate()?.alphaBy(1F)?.start()
        }
    }

}