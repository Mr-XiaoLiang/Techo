package com.lollipop.lqrdemo.floating

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import com.lollipop.base.util.lazyBind
import com.lollipop.insets.WindowInsetsEdge
import com.lollipop.insets.fixInsetsByPadding
import com.lollipop.lqrdemo.base.ColorModeActivity
import com.lollipop.lqrdemo.databinding.ActivityFloatingScanSettingsBinding
import com.lollipop.lqrdemo.other.AppSettings
import com.lollipop.pigment.Pigment
import com.lollipop.pigment.PigmentTint

class FloatingScanSettingsActivity : ColorModeActivity() {

    private val binding: ActivityFloatingScanSettingsBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        binding.actionBar.fixInsetsByPadding(WindowInsetsEdge.HEADER)
        binding.contentGroup.fixInsetsByPadding(WindowInsetsEdge.CONTENT)
        bindByBack(binding.backButton)
        initView()
    }

    private fun initView() {
        val buttonEnable = AppSettings.ui.floatingScanButtonEnable
        binding.fabEnableSwitch.isChecked = buttonEnable
        binding.fabSizeSlider.isEnabled = buttonEnable
        val floatingScanButtonSize = AppSettings.ui.floatingScanButtonSize
        binding.fabSizeSlider.value = floatingScanButtonSize.toFloat()
        onFabSizeChanged(floatingScanButtonSize)
        binding.fabSizeSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                val valueDp = value.toInt()
                AppSettings.ui.floatingScanButtonSize = valueDp
                onFabSizeChanged(valueDp)
            }
        }
        binding.fabEnableSwitch.setOnCheckedChangeListener { _, isChecked ->
            AppSettings.ui.floatingScanButtonEnable = isChecked
            binding.fabSizeSlider.isEnabled = isChecked
        }
        binding.fabSizeSlider.setLabelFormatter { value: Float ->
            "${value.toInt()} DP"
        }
    }

    private fun onFabSizeChanged(dpValue: Int) {
        FloatingActionButton.update(binding.fabIconPreview, dpValue, dpValue)
    }

    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
        binding.root.setBackgroundColor(pigment.backgroundColor)
        binding.backButton.imageTintList = ColorStateList.valueOf(pigment.onBackgroundTitle)
        binding.titleView.setTextColor(pigment.onBackgroundTitle)
        binding.fabEnableItem.setBackgroundColor(pigment.extreme)
        binding.fabSizeItem.setBackgroundColor(pigment.extreme)

        binding.fabEnableSwitch.trackTintList = PigmentTint.getCheckStateList(
            pigment.primaryColor,
            pigment.extreme
        )
        binding.fabEnableSwitch.thumbTintList = ColorStateList.valueOf(pigment.secondaryColor)
        binding.fabSizeSlider.trackTintList = ColorStateList.valueOf(pigment.primaryColor)
        binding.fabSizeSlider.thumbTintList = ColorStateList.valueOf(pigment.secondaryColor)
    }

}