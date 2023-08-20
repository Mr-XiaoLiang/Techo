package com.lollipop.lqrdemo.creator.background

import java.io.File

sealed class BackgroundInfo {

    object None: BackgroundInfo()

    class Local(val file: File, val gravity: BackgroundGravity, val corner: BackgroundCorner)

    class Remote(val path: String, val gravity: BackgroundGravity, val corner: BackgroundCorner)

    class Color(val color: Int, val corner: BackgroundCorner)


}