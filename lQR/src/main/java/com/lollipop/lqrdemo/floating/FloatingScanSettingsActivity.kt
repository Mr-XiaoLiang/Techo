package com.lollipop.lqrdemo.floating

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import com.lollipop.base.util.lazyBind
import com.lollipop.insets.WindowInsetsEdge
import com.lollipop.insets.fixInsetsByPadding
import com.lollipop.lqrdemo.base.ColorModeActivity
import com.lollipop.lqrdemo.databinding.ActivityFloatingScanSettingsBinding
import com.lollipop.pigment.Pigment

class FloatingScanSettingsActivity : ColorModeActivity() {

    private val binding: ActivityFloatingScanSettingsBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        binding.actionBar.fixInsetsByPadding(WindowInsetsEdge.HEADER)
        binding.contentGroup.fixInsetsByPadding(WindowInsetsEdge.CONTENT)
        bindByBack(binding.backButton)
    }

    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
        binding.root.setBackgroundColor(pigment.backgroundColor)
        binding.backButton.imageTintList = ColorStateList.valueOf(pigment.onBackgroundTitle)
        binding.titleView.setTextColor(pigment.onBackgroundTitle)

    }

}