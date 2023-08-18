package com.lollipop.lqrdemo.base

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

}