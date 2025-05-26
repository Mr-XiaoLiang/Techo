package com.lollipop.lqrdemo.floating

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import com.lollipop.base.util.lazyBind
import com.lollipop.insets.WindowInsetsEdge
import com.lollipop.insets.fixInsetsByPadding
import com.lollipop.lqrdemo.base.ColorModeActivity
import com.lollipop.lqrdemo.databinding.ActivityFloatingScanSettingsBinding

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

}