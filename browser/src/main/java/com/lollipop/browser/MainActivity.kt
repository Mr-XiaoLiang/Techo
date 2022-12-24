package com.lollipop.browser

import android.os.Bundle
import android.util.TypedValue
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
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
//        binding.doneBtn.onClick {
//            binding.urlInput.closeBoard()
//            webHelper.loadUrl(binding.urlInput.text?.toString() ?: "")
//        }
        binding.pageGroup.previewInterval = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 20F, resources.displayMetrics
        ).toInt()
        binding.pageGroup.animationDuration = 500L
        var isPreview = false
        binding.previewBtn.onClick {
            isPreview = !isPreview
            binding.pageGroup.setMode(isPreview)
        }
    }
}