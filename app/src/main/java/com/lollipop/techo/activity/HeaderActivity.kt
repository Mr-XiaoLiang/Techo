package com.lollipop.techo.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
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
import com.lollipop.base.util.onClick
import com.lollipop.base.util.onUI
import com.lollipop.pigment.BlendMode
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

    protected val scaffoldBinding: ActivityHeaderBinding by lazyBind()

    abstract val contentView: View

    open val floatingView: View? = null

    protected open val showBackArrow = true

    protected open val optionsMenu = 0

    private var isBlurHeader = AppUtil.isBlurHeader

    protected open val useCustomPigment = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(scaffoldBinding.root)
        WindowInsetsHelper.fitsSystemWindows(this)
        scaffoldBinding.contentRoot.addView(
            contentView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        floatingView?.let {
            scaffoldBinding.floatingRoot.addView(
                it,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        scaffoldBinding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        scaffoldBinding.appBar.fixInsetsByMargin(WindowInsetsEdge.HEADER)
        scaffoldBinding.contentScrollView.fixInsetsByMargin(
            WindowInsetsEdge(
                WindowInsetsEdgeStrategy.ACCUMULATE,
                WindowInsetsEdgeStrategy.ACCUMULATE,
                WindowInsetsEdgeStrategy.ACCUMULATE,
                WindowInsetsEdgeStrategy.ORIGINAL
            )
        )
        scaffoldBinding.headerBackground.onClick {
            changeBlurState()
        }
        loadHeader()
        hideLoading()
        setTitle("")
    }

    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
        if (!useCustomPigment) {
            scaffoldBinding.headerBackgroundMask.isVisible = pigment.blendMode == BlendMode.Dark
            scaffoldBinding.contentLoadingView.setIndicatorColor(
                pigment.primaryColor,
                pigment.secondaryColor,
                pigment.primaryVariant,
                pigment.secondaryVariant
            )
            scaffoldBinding.contentRoot.setBackgroundColor(pigment.backgroundColor)
        }
    }

    override fun setTitle(title: CharSequence?) {
        scaffoldBinding.titleView.isVisible = !TextUtils.isEmpty(title)
        scaffoldBinding.titleTextView.text = title ?: ""
    }

    protected fun showLoading() {
        scaffoldBinding.contentLoadingView.show()
    }

    override fun onResume() {
        super.onResume()
        if (isBlurHeader != AppUtil.isBlurHeader) {
            isBlurHeader = AppUtil.isBlurHeader
            loadHeader()
        }
    }

    private fun changeBlurState() {
        isBlurHeader = !isBlurHeader
        AppUtil.changeBlurHeader(this, isBlurHeader)
        loadHeader()
    }

    protected fun hideLoading() {
        scaffoldBinding.contentLoadingView.hide()
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
        var builder = Glide.with(scaffoldBinding.headerBackground)
            .asBitmap()
            .load(url)
            .transition(GenericTransitionOptions.with(AlphaAnimator()))
        if (isBlurHeader) {
            builder = builder.apply(RequestOptions().transform(BlurTransformation.create()))
        }
        builder.into(scaffoldBinding.headerBackground)
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