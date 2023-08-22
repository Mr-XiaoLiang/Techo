package com.lollipop.lqrdemo.creator.background

import java.io.File

sealed class BackgroundInfo {

    fun getCornerOrNull(): BackgroundCorner? {
        if (this is CornerProvider) {
            return corner
        }
        return null
    }

    fun getGravityOrNull(): BackgroundGravity? {
        if (this is GravityProvider) {
            return gravity
        }
        return null
    }

    object None : BackgroundInfo()

    class Local(
        val file: File,
        override val gravity: BackgroundGravity,
        override val corner: BackgroundCorner?
    ) : BackgroundInfo(), GravityProvider, CornerProvider

//    class Remote(
//        val path: String,
//        override val gravity: BackgroundGravity,
//        override val corner: BackgroundCorner?
//    ) : BackgroundInfo(), GravityProvider, CornerProvider

    class Color(
        val color: Int,
        override val corner: BackgroundCorner?
    ) : BackgroundInfo(), CornerProvider

    interface CornerProvider {
        val corner: BackgroundCorner?
    }

    interface GravityProvider {
        val gravity: BackgroundGravity
    }

}