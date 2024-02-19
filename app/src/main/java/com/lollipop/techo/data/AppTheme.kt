package com.lollipop.techo.data

import android.content.Context
import com.lollipop.pigment.BlendMode
import com.lollipop.pigment.Pigment
import com.lollipop.pigment.PigmentWallpaperCenter

object AppTheme {

    private const val DEFAULT_PRIMARY = 0xFF00B78D.toInt()
    private const val DEFAULT_SECONDARY = 0xFFB75F00.toInt()
    private val DEFAULT = Pigment.valueOf(DEFAULT_PRIMARY, DEFAULT_SECONDARY, BlendMode.Light)

    val current: Pigment
        get() {
            // 一般情况下，我们认为壁纸的主题是有的，因为我们会设置默认的，实在不行，我们用完全缺省的
            return PigmentWallpaperCenter.pigment ?: DEFAULT
        }

    fun updateDefaultTheme(context: Context?) {
        val pigment = defaultByMode(context)
        TechoTheme.DEFAULT = if (pigment.blendMode == BlendMode.Dark) {
            TechoTheme.Base.DARK
        } else {
            TechoTheme.Base.LIGHT
        }
        PigmentWallpaperCenter.default(pigment)
    }

    private fun defaultByMode(context: Context?): Pigment {
        val mode = if (context != null) {
            Pigment.getBlendByNightMode(context)
        } else {
            BlendMode.Light
        }
        val wallpaperPigment = PigmentWallpaperCenter.wallpaperPigment
        if (wallpaperPigment != null) {
            // 如果捕获过颜色了，那么就使用它的颜色
            //  如果连通模式都一样，那就直接复用好了
            if (wallpaperPigment.blendMode == mode) {
                return wallpaperPigment
            }
            // 模式不一样，再创建新的
            return Pigment(
                wallpaperPigment.primary,
                wallpaperPigment.secondary,
                if (context != null) {
                    Pigment.getBlendByNightMode(context)
                } else {
                    BlendMode.Light
                }
            )
        }
        // 如果没有新的，那么就重新造一个吧
        return Pigment.valueOf(
            DEFAULT_PRIMARY,
            DEFAULT_SECONDARY,
            if (context != null) {
                Pigment.getBlendByNightMode(context)
            } else {
                BlendMode.Light
            }
        )
    }

}