package com.lollipop.techo

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.lollipop.base.ui.BaseActivity
import com.lollipop.base.util.bind
import com.lollipop.base.util.lazyBind
import com.lollipop.techo.databinding.ActivityHeaderBinding

/**
 * 带有头部View的Activity
 * @author Lollipop
 * @date 2021/04/25
 */
abstract class HeaderActivity : BaseActivity() {

    private val viewBinding: ActivityHeaderBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        setSupportActionBar(viewBinding.toolbar)
        viewBinding.contentRoot.addView(getContentView(),
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        getFloatingView()?.let {
            viewBinding.floatingRoot.addView(it,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

    abstract fun getContentView(): View

    open fun getFloatingView(): View? {
        return null
    }

}