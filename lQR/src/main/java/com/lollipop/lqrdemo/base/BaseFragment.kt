package com.lollipop.lqrdemo.base

import androidx.fragment.app.Fragment
import com.lollipop.pigment.Pigment
import com.lollipop.pigment.PigmentPage

open class BaseFragment: Fragment(), PigmentPage {
    override fun onDecorationChanged(pigment: Pigment) {
    }
}