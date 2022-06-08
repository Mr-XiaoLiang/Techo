package com.lollipop.techo.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.lollipop.base.ui.BaseActivity
import com.lollipop.base.util.*
import com.lollipop.techo.data.RequestService
import com.lollipop.techo.databinding.ActivityHeaderBinding
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowInsetsHelper.initWindowFlag(this)
        setContentView(viewBinding.root)
        setSupportActionBar(viewBinding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(showBackArrow)
        viewBinding.contentRoot.addView(
            contentView,
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        floatingView?.let {
            viewBinding.floatingRoot.addView(
                it,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        viewBinding.appBar.fixInsetsByMargin(WindowInsetsHelper.Edge.HEADER, viewBinding.toolbar)
        loadHeader()
        hideLoading()
    }

    protected fun showLoading() {
        viewBinding.contentLoadingView.show()
    }

    protected fun hideLoading() {
        viewBinding.contentLoadingView.hide()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
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
                RequestService.getHeaderImageInfo().let { imageInfo ->
                    if (imageInfo.url.isNotEmpty()) {
                        headerImageUrl = imageInfo.fullUrl
                        onUI {
                            onUrlLoaded(headerImageUrl)
                        }
                    }
                }
            }
        }
    }

    private fun onUrlLoaded(url: String) {
        Glide.with(viewBinding.headerBackground)
            .load(url)
            .apply(
                RequestOptions().transform(
                    BlurTransformation.create()
                )
            ).into(viewBinding.headerBackground)
    }

    fun resultOk(callback: (Intent) -> Unit) {
        setResult(Activity.RESULT_OK, Intent().apply { callback(this) })
    }

    fun resultCanceled(callback: ((Intent) -> Unit)? = null) {
        setResult(Activity.RESULT_CANCELED, Intent().apply { callback?.invoke(this) })
    }

}