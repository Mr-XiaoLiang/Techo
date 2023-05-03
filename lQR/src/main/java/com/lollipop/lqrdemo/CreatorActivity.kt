package com.lollipop.lqrdemo

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.lollipop.base.util.insets.WindowInsetsEdge
import com.lollipop.base.util.insets.WindowInsetsHelper
import com.lollipop.base.util.insets.fixInsetsByPadding
import com.lollipop.base.util.lazyBind
import com.lollipop.lqrdemo.base.ColorModeActivity
import com.lollipop.lqrdemo.creator.QrContentValueFragment
import com.lollipop.lqrdemo.databinding.ActivityCreatorBinding

class CreatorActivity : ColorModeActivity() {

    private val binding: ActivityCreatorBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowInsetsHelper.initWindowFlag(this)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.root.fixInsetsByPadding(WindowInsetsEdge.ALL)
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

        CONTENT(R.string.tab_content, QrContentValueFragment::class.java)

    }

}