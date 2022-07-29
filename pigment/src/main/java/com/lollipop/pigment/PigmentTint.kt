package com.lollipop.pigment

import android.content.res.ColorStateList
import android.widget.ImageView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

fun FloatingActionButton.tint(pigment: Pigment) {
    backgroundTintList = ColorStateList.valueOf(pigment.secondary)
    imageTintList = ColorStateList.valueOf(pigment.onSecondaryBody)
}

fun ExtendedFloatingActionButton.tint(pigment: Pigment) {
    backgroundTintList = ColorStateList.valueOf(pigment.secondary)
    iconTint = ColorStateList.valueOf(pigment.onSecondaryBody)
    setTextColor(pigment.onSecondaryBody)
}

fun ImageView.tintBodyIcon(pigment: Pigment) {
    imageTintList = ColorStateList.valueOf(pigment.onSecondaryBody)
}

fun ImageView.tintTitleIcon(pigment: Pigment) {
    imageTintList = ColorStateList.valueOf(pigment.onSecondaryTitle)
}

