package com.lollipop.lqrdemo.base

import android.content.res.ColorStateList
import com.google.android.material.slider.Slider
import com.lollipop.base.util.changeAlpha
import com.lollipop.pigment.BlendMode
import com.lollipop.pigment.Pigment

object PigmentTheme {

    fun getForePanelBackground(pigment: Pigment, colorCallback: (bg: Int, btn: Int) -> Unit) {
        BlendMode.flow(pigment.primaryColor)
            .blend(pigment.extremeReversal)
            .blend(pigment.extreme, 0.8F) {
                colorCallback(it, BlendMode.titleOnColor(it))
            }
    }

    fun getForePanelInputBar(pigment: Pigment, colorCallback: (bg: Int, text: Int) -> Unit) {
        getForePanelBackground(pigment) { panel, _ ->
            val bg = BlendMode.blend(panel, pigment.primaryColor)
            val text = BlendMode.titleOnColor(bg)
            colorCallback(bg, text)
        }
    }

    fun updateSlider(slider: Slider, color: Int) {
        //        binding.slider.tickInactiveTintList = ColorStateList.valueOf(fg.changeAlpha(0.5F))
//        binding.slider.tickActiveTintList = ColorStateList.valueOf(fg)

        slider.trackActiveTintList = ColorStateList.valueOf(color)
        slider.trackInactiveTintList = ColorStateList.valueOf(color.changeAlpha(0.5F))

        slider.haloTintList = ColorStateList.valueOf(color.changeAlpha(0.24F))
        slider.thumbTintList = ColorStateList.valueOf(color)
    }

}