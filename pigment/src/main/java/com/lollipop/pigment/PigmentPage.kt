package com.lollipop.pigment

interface PigmentPage {

    fun onDecorationChanged(pigment: Pigment)

    val currentPigment: Pigment?

}