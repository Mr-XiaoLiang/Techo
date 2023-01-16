package com.lollipop.browser

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.lollipop.base.util.dp2px
import com.lollipop.base.util.insets.*
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.lazyLogD
import com.lollipop.browser.databinding.ActivityMainBinding
import com.lollipop.web.WebHelper
import com.lollipop.web.WebHost
import kotlin.math.max

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

    private val log by lazyLogD()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowInsetsHelper.initWindowFlag(this)
        setContentView(binding.root)
        initBottomSheetPanel()
        binding.statusPanel.fixInsetsByListener(
            WindowInsetsEdgeViewDelegate(
                direction = WindowInsetsEdgeViewDelegate.Direction.TOP,
                minSize = 4.dp2px
            )
        )
        binding.statusPanel.setBackgroundColor(getColor(R.color.teal_700))

    }

    private fun initBottomSheetPanel() {
        val sheetBehavior = BottomSheetBehavior.from(binding.navBottomSheetPanel)
        sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        sheetBehavior.peekHeight = resources.getDimensionPixelSize(R.dimen.bottom_action_bar_height)
        sheetBehavior.isHideable = false

        val panelOperator = WindowInsetsOperator(WindowInsetsEdge.CONTENT)
        panelOperator.snapshotPadding(binding.navBottomSheetPanel)

        binding.root.fixInsetsByListener { v, _, insets ->
            val res = v.resources
            val actionHeight = res.getDimensionPixelSize(R.dimen.bottom_action_bar_height)

            // res.getDimensionPixelSize(R.dimen.mtrl_min_touch_target_size)
            val minHeight = 48.dp2px

            val value = WindowInsetsHelper.getInsetsValue(
                insets,
                WindowInsetsType.MANDATORY_SYSTEM_GESTURES
            )
            val peekHeight = max(value.bottom + minHeight - actionHeight, 0)
            WindowInsetsHelper.setHeight(binding.navigationBarBackgroundView, peekHeight)
            panelOperator.padding(
                binding.navBottomSheetPanel,
                bottom = panelOperator.basePadding.bottom + peekHeight
            )
            insets
        }
    }

    private fun createWebPage(url: String): View {
        val webView = WebView(this)
        WebHelper.bind(this, webView).init().loadUrl(url)
        return webView
    }

}