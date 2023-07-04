package com.lollipop.lqrdemo

import android.content.res.ColorStateList
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
import com.lollipop.lqrdemo.creator.QrContentInputPopupWindow
import com.lollipop.lqrdemo.creator.QrContentValueFragment
import com.lollipop.lqrdemo.creator.QrCreatorHelper
import com.lollipop.lqrdemo.creator.QrDataPointFragment
import com.lollipop.lqrdemo.creator.QrPositionDetectionFragment
import com.lollipop.lqrdemo.creator.bridge.OnCodeContentChangedListener
import com.lollipop.lqrdemo.creator.content.ContentBuilderActivity
import com.lollipop.lqrdemo.databinding.ActivityCreatorBinding
import com.lollipop.pigment.BlendMode
import com.lollipop.pigment.Pigment

class CreatorActivity : ColorModeActivity(), QrContentValueFragment.Callback,
    OnCodeContentChangedListener, QrCreatorHelper.OnLoadStatusChangedListener {

    private val binding: ActivityCreatorBinding by lazyBind()

    private val creatorHelper = QrCreatorHelper(this)

    private val contentBuilderLauncher = registerResult(ContentBuilderActivity.LAUNCHER) {
        onCodeContentChanged(it ?: "")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        WindowInsetsHelper.fitsSystemWindows(this)
        binding.root.fixInsetsByPadding(WindowInsetsEdge.HEADER)
//        binding.panelGroup.fixInsetsByPadding(WindowInsetsEdge.CONTENT)
//        binding.subpageGroup.adapter = SubPageAdapter(this)
//        TabLayoutMediator(
//            binding.tabLayout,
//            binding.subpageGroup,
//            true
//        ) { tab, position ->
//            tab.setText(SubPage.values()[position].tab)
//        }.attach()
        creatorHelper.addContentChangedListener(this)
    }

    /**
     *
     */
    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
        binding.root.setBackgroundColor(pigment.backgroundColor)
        binding.backButton.imageTintList = ColorStateList.valueOf(pigment.onBackgroundTitle)
        binding.shareButton.imageTintList = ColorStateList.valueOf(pigment.onBackgroundTitle)
        binding.titleView.setTextColor(pigment.onBackgroundTitle)
        binding.saveBtn.setBackgroundColor(pigment.secondaryVariant)
        binding.saveBtn.setTextColor(pigment.onSecondaryTitle)
//        binding.panelGroup.setBackgroundColor(pigment.extreme)
//        binding.tabLayout.setTabTextColors(pigment.onExtremeBody, pigment.primaryColor)
//        binding.tabLayout.setSelectedTabIndicatorColor(pigment.primaryColor)
//        binding.tabLayout.tabRippleColor = ColorStateList.valueOf(
//            BlendMode.blend(pigment.primaryColor, pigment.extreme, 0.8F)
//        )
    }

    override fun onCodeContentChanged(value: String) {
        creatorHelper.contentValue = value
        findContentValueFragment()?.onCodeContentChanged(value)
    }

    override fun onLoadStatusChanged(isLading: Boolean) {
        // TODO("Not yet implemented")
    }

    private fun openBuildPage() {
        contentBuilderLauncher.launch(null)
    }

//    private fun findFragment(position: Int = -1): Fragment? {
//        val pager2 = binding.subpageGroup
//        val adapter = pager2.adapter ?: return null
//        val itemCount = adapter.itemCount
//        if (itemCount < 1) {
//            return null
//        }
//        val pagePosition = if (position < 0 || position >= itemCount) {
//            pager2.currentItem
//        } else {
//            position
//        }
//        if (pagePosition < 0 || pagePosition >= itemCount) {
//            return null
//        }
//        val itemId = adapter.getItemId(pagePosition)
//        return supportFragmentManager.findFragmentByTag("f$itemId")
//    }

//    private inline fun <reified T : Any> findTypedFragment(position: Int = -1): T? {
//        val fragment = findFragment(position) ?: return null
//        if (fragment is T) {
//            return fragment
//        }
//        return null
//    }

    private fun findContentValueFragment(): QrContentValueFragment? {
        val fragment = supportFragmentManager.findFragmentByTag("contentValueFragment")
        if (fragment is QrContentValueFragment) {
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

    override fun getQrContentInfo(): String {
        return creatorHelper.contentValue
    }

    override fun requestChangeContent() {
        QrContentInputPopupWindow.show(
            this,
            getQrContentInfo(),
            ::openBuildPage,
            ::onCodeContentChanged
        )
    }

}