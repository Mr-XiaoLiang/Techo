package com.lollipop.techo.activity

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.lollipop.base.ui.BaseActivity
import com.lollipop.base.util.*
import com.lollipop.techo.data.HeaderImageInfo
import com.lollipop.techo.data.RequestService
import com.lollipop.techo.databinding.ActivityHeaderBinding
import com.lollipop.techo.util.BlurTransformation

/**
 * 带有头部View的Activity
 * @author Lollipop
 * @date 2021/04/25
 */
abstract class HeaderActivity : BaseActivity() {

    private val viewBinding: ActivityHeaderBinding by lazyBind()

    abstract val contentView: View

    open val floatingView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowInsetsHelper.initWindowFlag(this)
        setContentView(viewBinding.root)
        setSupportActionBar(viewBinding.toolbar)
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
        viewBinding.appBar.fixInsetsByPadding { _, _, insets ->
            val insetsValue = WindowInsetsHelper.getInsetsValue(insets)
            WindowInsetsHelper.setMargin(
                viewBinding.toolbar,
                insetsValue.left,
                insetsValue.top,
                insetsValue.right,
                0
            )
            insets
        }
        loadHeader()
        hideLoading()
    }

    protected fun showLoading() {
        viewBinding.contentLoadingView.show()
    }

    protected fun hideLoading() {
        viewBinding.contentLoadingView.hide()
    }

    private fun loadHeader() {
        doAsync {
            RequestService.getHeaderImageInfo().images
                ?.getValue<HeaderImageInfo.ImageInfo>(0)
                ?.let { imageInfo ->
                    if (imageInfo.url.isNotEmpty()) {
                        onUI {
                            Glide.with(viewBinding.headerBackground)
                                .load(imageInfo.fullUrl)
                                .apply(
                                    RequestOptions().transform(
                                        BlurTransformation.create(this@HeaderActivity)
                                    )
                                ).into(viewBinding.headerBackground)
                        }
                    }
                }
        }
    }

}