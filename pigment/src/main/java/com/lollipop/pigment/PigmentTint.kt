package com.lollipop.pigment

import android.content.res.ColorStateList
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

fun FloatingActionButton.tint(pigment: Pigment) {
    backgroundTintList = ColorStateList.valueOf(pigment.secondary)
    imageTintList = ColorStateList.valueOf(pigment.onSecondary)
}

fun ExtendedFloatingActionButton.tint(pigment: Pigment) {
    backgroundTintList = ColorStateList.valueOf(pigment.secondary)
    iconTint = ColorStateList.valueOf(pigment.onSecondary)
    setTextColor(pigment.onSecondary)
}

