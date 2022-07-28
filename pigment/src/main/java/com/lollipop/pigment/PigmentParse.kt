package com.lollipop.pigment

import android.graphics.Bitmap
import androidx.palette.graphics.Palette

object PigmentParse {

    private val EMPTY_PIGMENT = Pigment(
        0, 0, 0, 0,
        0, 0, 0, 0
    )

    private var basePigment = EMPTY_PIGMENT

    fun init(base: Pigment) {
        this.basePigment = base
    }

    fun parse(bitmap: Bitmap, callback: (Pigment) -> Unit) {
        Palette.from(bitmap).generate { palette ->
            if (palette != null) {
                if (basePigment == EMPTY_PIGMENT) {
                    throw IllegalArgumentException("需要初始化, 请先调用PigmentParse.init(Pigment)")
                }
                val base = basePigment
                val mutedSwatch = palette.mutedSwatch
                val darkMutedSwatch = palette.darkMutedSwatch
                val vibrantSwatch = palette.vibrantSwatch
                val darkVibrantSwatch = palette.darkVibrantSwatch
                callback(
                    Pigment(
                        primary = mutedSwatch?.rgb ?: base.primary,
                        primaryVariant = darkMutedSwatch?.rgb ?: base.primaryVariant,
                        onPrimaryTitle = mutedSwatch?.titleTextColor ?: base.onPrimaryTitle,
                        onPrimaryBody = mutedSwatch?.bodyTextColor ?: base.onPrimaryBody,
                        secondary = vibrantSwatch?.rgb ?: base.secondary,
                        secondaryVariant = darkVibrantSwatch?.rgb ?: base.secondaryVariant,
                        onSecondaryTitle = vibrantSwatch?.titleTextColor ?: base.onSecondaryTitle,
                        onSecondaryBody = vibrantSwatch?.bodyTextColor ?: base.onSecondaryBody
                    )
                )
            }
        }
    }

}