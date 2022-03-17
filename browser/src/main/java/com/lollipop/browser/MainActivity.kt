package com.lollipop.browser

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lollipop.base.util.lazyBind
import com.lollipop.browser.databinding.ActivityMainBinding
import com.lollipop.web.WebHelper
import com.lollipop.web.WebHost

class MainActivity : AppCompatActivity(), WebHost {

    private val binding: ActivityMainBinding by lazyBind()

    private val webHelper by lazy {
        WebHelper.bind(this, binding.webView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.doneBtn.setOnClickListener {
            webHelper.loadUrl(binding.urlInput.text?.toString() ?: "")
        }
    }
}