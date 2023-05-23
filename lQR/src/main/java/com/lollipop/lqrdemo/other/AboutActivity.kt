package com.lollipop.lqrdemo.other

import android.content.res.ColorStateList
import android.os.Bundle
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.versionName
import com.lollipop.lqrdemo.base.ColorModeActivity
import com.lollipop.lqrdemo.databinding.ActivityAboutBinding
import com.lollipop.pigment.Pigment

class AboutActivity : ColorModeActivity() {

    private val binding: ActivityAboutBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.versionView.text = versionName()
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
}