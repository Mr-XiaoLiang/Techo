package com.lollipop.lqrdemo.creator

import com.lollipop.lqrdemo.base.BaseFragment
import com.lollipop.lqrdemo.base.PigmentTheme
import com.lollipop.pigment.Pigment

open class QrBaseSubpageFragment : BaseFragment() {

    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
        PigmentTheme.getForePanelBackground(pigment) { bg, btn ->
            view?.setBackgroundColor(bg)
        }
    }

}