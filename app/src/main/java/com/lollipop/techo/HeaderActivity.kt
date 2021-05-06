package com.lollipop.techo

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.lollipop.base.ui.BaseActivity
import com.lollipop.base.util.doAsync
import com.lollipop.base.util.lazyBind
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
        loadHeader()
    }

    private fun loadHeader() {
        doAsync {
            RequestService.getHeaderImageInfo().let { headerImageInfo ->
                headerImageInfo.images?.let { images ->
                    if (images.size > 0) {
                        images.get<HeaderImageInfo.ImageInfo>(0)?.let { imageInfo ->
                            if (imageInfo.url.isNotEmpty()) {
                                Glide.with(viewBinding.headerBackground)
                                    .load(imageInfo.fullUrl)
                                    .apply(
                                        RequestOptions().transform(
                                            BlurTransformation.create(this@HeaderActivity)))
                                    .into(viewBinding.headerBackground)
                            }
                        }
                    }
                }
            }
        }
    }

}