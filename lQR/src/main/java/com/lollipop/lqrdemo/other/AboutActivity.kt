package com.lollipop.lqrdemo.other

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import com.lollipop.insets.WindowInsetsEdge
import com.lollipop.insets.WindowInsetsHelper
import com.lollipop.insets.fixInsetsByPadding
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.richtext.RichText
import com.lollipop.base.util.versionName
import com.lollipop.insets.fitsSystemWindows
import com.lollipop.lqrdemo.base.ColorModeActivity
import com.lollipop.lqrdemo.databinding.ActivityAboutBinding
import com.lollipop.pigment.Pigment
import androidx.core.net.toUri


class AboutActivity : ColorModeActivity() {

    private val binding: ActivityAboutBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        binding.actionBar.fixInsetsByPadding(WindowInsetsEdge.HEADER)
        binding.contentGroup.fixInsetsByPadding(WindowInsetsEdge.CONTENT)
        binding.versionView.text = versionName()
        bindByBack(binding.backButton)

        val richFlow = RichText.startRichFlow()
        val linkColor = Color.BLUE // PigmentWallpaperCenter.pigment?.secondaryColor ?: Color.BLUE
        Icons.entries.forEachIndexed { index, icons ->
            if (index > 0) {
                richFlow.addInfo(", ")
            }
            richFlow.addClickInfo(icons.value, linkColor, icons.url, ::onLinkClick)
        }
        richFlow.into(binding.copyrightView)
    }

    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
        binding.root.setBackgroundColor(pigment.backgroundColor)
        binding.backButton.imageTintList = ColorStateList.valueOf(pigment.onBackgroundTitle)
        binding.titleView.setTextColor(pigment.onBackgroundTitle)
        binding.logoImageView.setBackgroundColor(pigment.secondaryColor)
        binding.logoImageView.imageTintList = ColorStateList.valueOf(pigment.onSecondaryTitle)
        binding.andrewView.setTextColor(pigment.onBackgroundBody)
        binding.lollipopView.setTextColor(pigment.onBackgroundBody)
        binding.versionView.setTextColor(pigment.onBackgroundBody)
    }

    private fun onLinkClick(link: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, link.toUri()))
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private enum class Icons(val value: String, val url: String) {

        Material("material", "https://m3.material.io/styles/icons/overview"),
//        Icons8("icons8", "https://icons8.com")

    }

}