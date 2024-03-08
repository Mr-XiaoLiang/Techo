package com.lollipop.techo.activity

import android.os.Bundle
import com.lollipop.base.util.lazyBind
import com.lollipop.techo.databinding.ActivityWebBinding
import com.lollipop.web.WebHelper
import com.lollipop.web.WebHost

class WebActivity : BaseActivity(), WebHost {

    private val binding: ActivityWebBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val webHelper = WebHelper.bind(this, binding.webView)
        // 需要处理一下三方网页的状态栏兼容
    }
}