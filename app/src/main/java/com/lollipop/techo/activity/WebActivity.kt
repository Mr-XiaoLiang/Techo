package com.lollipop.techo.activity

import android.os.Bundle
import com.lollipop.base.util.lazyBind
import com.lollipop.techo.databinding.ActivityWebBinding
import com.lollipop.web.WebHelper

class WebActivity : BaseActivity() {

    private val binding: ActivityWebBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
//        WebHelper.bind()
    }
}