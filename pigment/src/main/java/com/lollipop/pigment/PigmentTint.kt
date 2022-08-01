package com.lollipop.pigment

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.button.MaterialButton
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

fun Drawable.tintBySelectState(pigment: Pigment, def: Int) {
    DrawableCompat.setTintList(this, getSelectStateList(pigment, def))
}

fun ImageView.tintBySelectState(pigment: Pigment, def: Int) {
    imageTintList = getSelectStateList(pigment, def)
}

fun MaterialButton.tintByHighlight(pigment: Pigment) {
    iconTint = ColorStateList.valueOf(pigment.onSecondaryBody)
    setTextColor(pigment.onSecondaryBody)
    backgroundTintList = ColorStateList.valueOf(pigment.secondary)
}

fun MaterialButton.tintByNotObvious(pigment: Pigment) {
    iconTint = ColorStateList.valueOf(pigment.secondary)
    setTextColor(pigment.secondary)
}

private fun getSelectStateList(pigment: Pigment, def: Int): ColorStateList {
    return ColorStateList(
        arrayOf(
            intArrayOf(android.R.attr.state_selected),
            intArrayOf()
        ),
        intArrayOf(
            pigment.secondary,
            def
        )
    )
}
