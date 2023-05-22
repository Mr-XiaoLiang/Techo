package com.lollipop.lqrdemo.other

import android.os.Bundle
import com.lollipop.base.util.lazyBind
import com.lollipop.lqrdemo.base.ColorModeActivity
import com.lollipop.lqrdemo.databinding.ActivityAboutBinding

class AboutActivity : ColorModeActivity() {

    private val binding: ActivityAboutBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}