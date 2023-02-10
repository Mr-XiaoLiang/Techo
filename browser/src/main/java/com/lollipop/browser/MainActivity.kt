package com.lollipop.browser

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.lollipop.base.util.dp2px
import com.lollipop.base.util.insets.*
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.lazyLogD
import com.lollipop.base.util.onClick
import com.lollipop.browser.databinding.ActivityMainBinding
import com.lollipop.browser.secret.SecretDialog
import com.lollipop.browser.web.WebPageFragment
import com.lollipop.browser.web.WebStatusManager
import com.lollipop.fragment.FragmentHelper
import com.lollipop.fragment.FragmentSwitcher
import com.lollipop.web.search.SearchSuggestion
import kotlin.math.max

class MainActivity : AppCompatActivity(), WebPageFragment.Callback {

    private val binding: ActivityMainBinding by lazyBind()

    private val log by lazyLogD()

    private var fragmentSwitcher: FragmentSwitcher? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowInsetsHelper.initWindowFlag(this)
        setContentView(binding.root)
        initBottomSheetPanel()
        initView()
        fragmentSwitcher = FragmentHelper.with(this).into(binding.webPageContainerView)
    }

    private fun initView() {
        binding.statusPanel.fixInsetsByListener(
            WindowInsetsEdgeViewDelegate(
                direction = WindowInsetsEdgeViewDelegate.Direction.TOP,
                minSize = 4.dp2px
            )
        )
        binding.statusPanel.setBackgroundColor(getColor(R.color.brand_4))
        binding.searchBar.onClick {
            SecretDialog.show(this) {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
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

    private fun findPage(pageId: String): WebPageFragment? {
        return fragmentSwitcher?.findTypedByTag(pageId)
    }

    override fun onLoadStart(pageId: String, url: String) {
        WebStatusManager.onLoadStart(pageId, url)
    }

    override fun onLoadProgressChanged(pageId: String, progress: Float) {
        WebStatusManager.onProgressChanged(pageId, progress)
    }

    override fun onTitleChanged(pageId: String, title: String) {
        WebStatusManager.onTitleChanged(pageId, title)
    }

    override fun onIconChanged(pageId: String, icon: Bitmap?) {
        WebStatusManager.onIconChanged(pageId, icon)
    }

    override fun onSearchRelevantResult(pageId: String, values: List<SearchSuggestion>) {
        if (values.isEmpty()) {
            return
        }
        // 默认实现，直接打开第一个
        findPage(pageId)?.load(values[0].url)
    }

}