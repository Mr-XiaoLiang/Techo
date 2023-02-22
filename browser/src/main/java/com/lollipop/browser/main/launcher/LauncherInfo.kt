package com.lollipop.browser.main.launcher

import java.io.File

class LauncherInfo(
    val id: Int,
    val label: String,
    val icon: File?,
    val backgroundFile: File?,
    val backgroundColor: List<Int>,
    val url: String
)