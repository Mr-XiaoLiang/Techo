package com.lollipop.browser.main.launcher

import android.os.Bundle
import android.view.View
import com.lollipop.base.util.lazyBind
import com.lollipop.browser.base.HeaderActivity
import com.lollipop.browser.databinding.ActivityLauncherEditBinding

class LauncherEditActivity : HeaderActivity() {

    private val binding: ActivityLauncherEditBinding by lazyBind()

    override val contentView: View
        get() {
            return binding.root
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    private fun initView() {

    }

}