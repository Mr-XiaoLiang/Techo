package com.lollipop.techo

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.lollipop.base.ui.BaseActivity
import com.lollipop.base.util.lazyBind
import com.lollipop.techo.databinding.ActivityHeaderBinding

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
        viewBinding.contentRoot.addView(contentView,
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        floatingView?.let {
            viewBinding.floatingRoot.addView(it,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

}