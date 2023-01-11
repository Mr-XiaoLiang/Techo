package com.lollipop.browser

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.onClick
import com.lollipop.browser.databinding.ActivityMainBinding
import com.lollipop.web.WebHelper
import com.lollipop.web.WebHost

class MainActivity : AppCompatActivity(), WebHost {

    private val binding: ActivityMainBinding by lazyBind()

//    private val webHelper by lazy {
//        WebHelper.bind(this, binding.webView)
//            .onTitleChanged {
//                onTitleChanged { iWeb, title ->
//                    Toast.makeText(this@MainActivity, title, Toast.LENGTH_SHORT).show()
//                }
//            }.onProgressChanged { _, progress ->
//                binding.progressBar.progress = progress
//            }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    private fun createWebPage(url: String): View {
        val webView = WebView(this)
        WebHelper.bind(this, webView).init().loadUrl(url)
        return webView
    }
}