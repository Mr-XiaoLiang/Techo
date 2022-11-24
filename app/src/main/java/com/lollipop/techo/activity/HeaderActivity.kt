package com.lollipop.techo.activity

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.ViewPropertyTransition
import com.lollipop.base.ui.BaseActivity
import com.lollipop.base.util.*
import com.lollipop.pigment.Pigment
import com.lollipop.pigment.PigmentPage
import com.lollipop.pigment.PigmentParse
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

        /**
         * 当前的涂料颜色
         * 主题元素的颜色需要用它来进行渲染
         */
        private var currentPigment: Pigment? = null

    }

    private val viewBinding: ActivityHeaderBinding by lazyBind()

    abstract val contentView: View

    open val floatingView: View? = null

    protected open val showBackArrow = true

    protected open val optionsMenu = 0

    private var isBlurHeader = AppUtil.isBlurHeader

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
        viewBinding.backButton.setOnClickListener {
            notifyBackPress()
        }
        viewBinding.appBar.fixInsetsByMargin(WindowInsetsHelper.Edge.HEADER)
        viewBinding.contentScrollView.fixInsetsByMargin(
            WindowInsetsHelper.Edge(
                WindowInsetsHelper.EdgeStrategy.ACCUMULATE,
                WindowInsetsHelper.EdgeStrategy.ACCUMULATE,
                WindowInsetsHelper.EdgeStrategy.ACCUMULATE,
                WindowInsetsHelper.EdgeStrategy.ORIGINAL
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
        viewBinding.backButtonBackground.setBackgroundColor(pigment.primary)
        viewBinding.contentLoadingView.setIndicatorColor(
            pigment.primary,
            pigment.secondary,
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
        currentPigment?.let {
            onDecorationChanged(it)
        }
    }

    override fun requestPigment(page: PigmentPage) {
        currentPigment?.let {
            page.onDecorationChanged(it)
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

    private fun loadHeader(refreshPigment: Boolean) {
        val imageUrl = headerImageUrl
        if (imageUrl.isNotEmpty()) {
            onUrlLoaded(imageUrl, refreshPigment)
        } else {
            doAsync {
                val imageInfo = RequestService.getHeaderImageInfo()
                if (imageInfo.url.isNotEmpty()) {
                    headerImageUrl = imageInfo.fullUrl
                    onUI {
                        onUrlLoaded(headerImageUrl, refreshPigment)
                    }
                }
            }
        }
    }

    private fun onUrlLoaded(url: String, refreshPigment: Boolean) {
        var builder = Glide.with(viewBinding.headerBackground)
            .asBitmap()
            .load(url)
            .transition(GenericTransitionOptions.with(AlphaAnimator()))
        if (isBlurHeader) {
            builder = builder.apply(RequestOptions().transform(BlurTransformation.create()))
        }
        if (currentPigment == null || refreshPigment) {
            builder = builder.addListener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    e?.printStackTrace()
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    if (resource != null) {
                        onHeaderLoaded(resource)
                    }
                    return false
                }
            })
        }
        builder.into(viewBinding.headerBackground)
    }

    fun resultOk(callback: (Intent) -> Unit) {
        setResult(Activity.RESULT_OK, Intent().apply { callback(this) })
    }

    fun resultCanceled(callback: ((Intent) -> Unit)? = null) {
        setResult(Activity.RESULT_CANCELED, Intent().apply { callback?.invoke(this) })
    }

    private fun onHeaderLoaded(bitmap: Bitmap) {
        log("onHeaderLoaded")
        if (currentPigment == null) {
            PigmentParse.parse(bitmap) {
                currentPigment = it
                onDecorationChanged(it)
            }
        }
    }

    private class AlphaAnimator : ViewPropertyTransition.Animator {
        override fun animate(view: View?) {
            view ?: return
            view.alpha = 0F
            view.animate()?.alphaBy(1F)?.start()
        }
    }

}