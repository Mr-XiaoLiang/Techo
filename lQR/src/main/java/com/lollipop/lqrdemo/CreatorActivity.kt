package com.lollipop.lqrdemo

import android.os.Bundle
import com.lollipop.base.util.insets.WindowInsetsEdge
import com.lollipop.base.util.insets.WindowInsetsHelper
import com.lollipop.base.util.insets.fixInsetsByPadding
import com.lollipop.base.util.lazyBind
import com.lollipop.lqrdemo.base.ColorModeActivity
import com.lollipop.lqrdemo.databinding.ActivityCreatorBinding

class CreatorActivity : ColorModeActivity() {

    private val binding: ActivityCreatorBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowInsetsHelper.initWindowFlag(this)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.root.fixInsetsByPadding(WindowInsetsEdge.ALL)
    }
}