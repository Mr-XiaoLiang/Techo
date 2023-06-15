package com.lollipop.lqrdemo

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.lollipop.base.util.insets.WindowInsetsEdge
import com.lollipop.base.util.insets.WindowInsetsHelper
import com.lollipop.base.util.insets.fixInsetsByPadding
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.registerResult
import com.lollipop.lqrdemo.base.ColorModeActivity
import com.lollipop.lqrdemo.creator.QrAlignmentFragment
import com.lollipop.lqrdemo.creator.QrBackgroundFragment
import com.lollipop.lqrdemo.creator.QrContentValueFragment
import com.lollipop.lqrdemo.creator.QrDataPointFragment
import com.lollipop.lqrdemo.creator.QrPositionDetectionFragment
import com.lollipop.lqrdemo.creator.bridge.OnCodeContentChangedListener
import com.lollipop.lqrdemo.creator.content.ContentBuilderActivity
import com.lollipop.lqrdemo.databinding.ActivityCreatorBinding

class CreatorActivity : ColorModeActivity() {

    private val binding: ActivityCreatorBinding by lazyBind()

    private val contentBuilderLauncher = registerResult(ContentBuilderActivity.LAUNCHER) {
        onCodeContentChanged(it ?: "")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        WindowInsetsHelper.fitsSystemWindows(this)
        binding.root.fixInsetsByPadding(WindowInsetsEdge.HEADER)
        binding.panelGroup.fixInsetsByPadding(WindowInsetsEdge.CONTENT)
        binding.subpageGroup.adapter = SubPageAdapter(this)
        TabLayoutMediator(
            binding.tabLayout,
            binding.subpageGroup,
            true
        ) { tab, position ->
            tab.setText(SubPage.values()[position].tab)
        }.attach()

        contentBuilderLauncher.launch(null)
    }

    private fun onCodeContentChanged(value: String) {
        findTypedFragment<OnCodeContentChangedListener>()?.onCodeContentChanged(value)
    }

    private fun findFragment(position: Int = -1): Fragment? {
        val pager2 = binding.subpageGroup
        val adapter = pager2.adapter ?: return null
        val itemCount = adapter.itemCount
        if (itemCount < 1) {
            return null
        }
        val pagePosition = if (position < 0 || position >= itemCount) {
            pager2.currentItem
        } else {
            position
        }
        if (pagePosition < 0 || pagePosition >= itemCount) {
            return null
        }
        val itemId = adapter.getItemId(pagePosition)
        return supportFragmentManager.findFragmentByTag("f$itemId")
    }

    private inline fun <reified T : Any> findTypedFragment(position: Int = -1): T? {
        val fragment = findFragment(position) ?: return null
        if (fragment is T) {
            return fragment
        }
        return null
    }

    private class SubPageAdapter(
        fragmentActivity: FragmentActivity,
    ) : FragmentStateAdapter(fragmentActivity) {

        private val pageInfo = SubPage.values()

        override fun getItemCount(): Int {
            return pageInfo.size
        }

        override fun createFragment(position: Int): Fragment {
            return pageInfo[position].fragment.newInstance()
        }

    }

    private enum class SubPage(
        val tab: Int,
        val fragment: Class<out Fragment>,
    ) {

        CONTENT(R.string.tab_content, QrContentValueFragment::class.java),
        POSITION_DETECTION(
            R.string.tab_position_detection,
            QrPositionDetectionFragment::class.java
        ),
        ALIGNMENT(R.string.tab_alignment, QrAlignmentFragment::class.java),
        DATA_POINT(R.string.tab_data_point, QrDataPointFragment::class.java),
        BACKGROUND(R.string.tab_background, QrBackgroundFragment::class.java)

    }

}