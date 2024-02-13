package com.lollipop.pigment

import androidx.annotation.CallSuper

interface PigmentPage {

    @CallSuper
    fun onDecorationChanged(pigment: Pigment)

    val currentPigment: Pigment?

}