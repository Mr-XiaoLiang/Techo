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
import com.lollipop.lqrdemo.base.ColorModeActivity
import com.lollipop.lqrdemo.creator.QrAlignmentFragment
import com.lollipop.lqrdemo.creator.QrBackgroundFragment
import com.lollipop.lqrdemo.creator.QrContentValueFragment
import com.lollipop.lqrdemo.creator.QrDataPointFragment
import com.lollipop.lqrdemo.creator.QrPositionDetectionFragment
import com.lollipop.lqrdemo.databinding.ActivityCreatorBinding

class CreatorActivity : ColorModeActivity() {

    private val binding: ActivityCreatorBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        WindowInsetsHelper.initWindowFlag(this)
        binding.root.fixInsetsByPadding(WindowInsetsEdge.ALL)
        binding.subpageGroup.adapter = SubPageAdapter(this)
        TabLayoutMediator(
            binding.tabLayout,
            binding.subpageGroup,
            true
        ) { tab, position ->
            val subPage = SubPage.values()[position]
            tab.text = getString(subPage.tab)
        }.attach()
    }

    private class SubPageAdapter(
        fragmentActivity: FragmentActivity
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
        val fragment: Class<out Fragment>
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