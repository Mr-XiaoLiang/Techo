package com.lollipop.browser.main.launcher

import androidx.annotation.ColorInt
import java.io.File

class LauncherInfo(
    val id: Int,
    val label: String,
    val icon: File?,
    @ColorInt
    val iconTint: Int,
    val backgroundFile: File?,
    val backgroundColor: List<Int>,
    val url: String
)