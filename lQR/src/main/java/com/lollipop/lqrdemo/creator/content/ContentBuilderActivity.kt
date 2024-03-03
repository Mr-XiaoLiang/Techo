package com.lollipop.lqrdemo.creator.content

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.lollipop.base.util.ActivityLauncherHelper
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.onClick
import com.lollipop.insets.MultipleInsetsDelegate
import com.lollipop.insets.WindowInsetsEdge
import com.lollipop.insets.WindowInsetsHelper
import com.lollipop.insets.WindowInsetsType
import com.lollipop.insets.fixInsetsByMultiple
import com.lollipop.lqrdemo.R
import com.lollipop.lqrdemo.base.ColorModeActivity
import com.lollipop.lqrdemo.creator.content.impl.CalendarEventContentBuilderPage
import com.lollipop.lqrdemo.creator.content.impl.ContactContentBuilderPage
import com.lollipop.lqrdemo.creator.content.impl.EmailContentBuilderPage
import com.lollipop.lqrdemo.creator.content.impl.GeoPointContentBuilderPage
import com.lollipop.lqrdemo.creator.content.impl.PhoneContentBuilderPage
import com.lollipop.lqrdemo.creator.content.impl.SmsContentBuilderPage
import com.lollipop.lqrdemo.creator.content.impl.WifiContentBuilderPage
import com.lollipop.lqrdemo.databinding.ActivityContentBuilderBinding
import com.lollipop.pigment.BlendMode
import com.lollipop.pigment.Pigment

class ContentBuilderActivity : ColorModeActivity() {

    companion object {

        private const val PARAMS_RESULT = "CONTENT_RESULT"

        private fun getResult(intent: Intent?): String? {
            return intent?.getStringExtra(PARAMS_RESULT)
        }

        val LAUNCHER: Class<out ActivityResultContract<Any?, String?>> = ResultContract::class.java

    }

    private val binding: ActivityContentBuilderBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        WindowInsetsHelper.fitsSystemWindows(this)
        binding.root.fixInsetsByMultiple(
            edge = WindowInsetsEdge.ALL,
            type = MultipleInsetsDelegate.ApplyType.PADDING,
            target = null,
            WindowInsetsType.Ime, WindowInsetsType.SystemBars
        )
        bindByBack(binding.backButton)
        binding.saveButton.onClick {
            setResult()
            finish()
        }
        binding.viewPager2.adapter = PageAdapter(this)
        TabLayoutMediator(
            binding.tabLayout,
            binding.viewPager2,
            true
        ) { tab, position ->
            tab.setText(SubPage.entries[position].tab)
        }.attach()
        // 默认是取消的
        setResult(RESULT_CANCELED)
    }

    private fun setResult() {
        val adapter = binding.viewPager2.adapter ?: return
        val itemId = adapter.getItemId(binding.viewPager2.currentItem)
        val fragment = supportFragmentManager.findFragmentByTag("f$itemId")
        if (fragment is ContentBuilder) {
            val contentValue = fragment.getContentValue()
            setResult(RESULT_OK, Intent().putExtra(PARAMS_RESULT, contentValue))
            return
        }
        setResult(RESULT_CANCELED)
    }

    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
        binding.root.setBackgroundColor(pigment.backgroundColor)
        binding.backButton.imageTintList = ColorStateList.valueOf(pigment.onBackgroundTitle)
        binding.saveButton.imageTintList = ColorStateList.valueOf(pigment.onBackgroundTitle)
        binding.titleView.setTextColor(pigment.onBackgroundTitle)
        updateTabLayoutPigment(pigment)
    }

    private fun updateTabLayoutPigment(pigment: Pigment) {
        binding.tabLayout.setTabTextColors(pigment.onBackgroundBody, pigment.primaryColor)
        binding.tabLayout.setSelectedTabIndicatorColor(pigment.primaryColor)
        binding.tabLayout.tabRippleColor = ColorStateList.valueOf(
            BlendMode.blend(pigment.primaryColor, pigment.backgroundColor, 0.8F)
        )
    }

    class ResultContract : ActivityLauncherHelper.Simple<Any?, String?>() {

        override val activityClass: Class<out Activity> = ContentBuilderActivity::class.java

        override fun parseResult(resultCode: Int, intent: Intent?): String? {
            intent ?: return null
            if (resultCode != Activity.RESULT_OK) {
                return null
            }
            return getResult(intent)
        }

    }

    private class PageAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

        private val data = SubPage.entries

        override fun getItemCount(): Int {
            return data.size
        }

        override fun createFragment(position: Int): Fragment {
            return data[position].fragment.getDeclaredConstructor().newInstance()
        }

    }

    private enum class SubPage(val tab: Int, val fragment: Class<out ContentBuilder>) {

        CalendarEvent(R.string.code_calendar_event, CalendarEventContentBuilderPage::class.java),

        Contact(R.string.code_contact, ContactContentBuilderPage::class.java),

        Email(R.string.code_email, EmailContentBuilderPage::class.java),

        GeoPoint(R.string.code_geo_point, GeoPointContentBuilderPage::class.java),

        Phone(R.string.code_phone, PhoneContentBuilderPage::class.java),

        Sms(R.string.code_sms, SmsContentBuilderPage::class.java),

        Wifi(R.string.code_wifi, WifiContentBuilderPage::class.java),

    }

}