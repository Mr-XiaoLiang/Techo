package com.lollipop.pigment

import android.app.WallpaperColors
import android.app.WallpaperManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper

object PigmentWallpaperCenter {

    var wallpaperPigment: Pigment? = null
        private set

    var defaultPigment: Pigment? = null
        private set

    val pigment: Pigment?
        get() {
            return wallpaperPigment ?: defaultPigment
        }

    private val pigmentProviderHelper = PigmentProviderHelper()

    fun default(pigment: Pigment) {
        defaultPigment = pigment
        if (wallpaperPigment == null) {
            notifyPigmentChanged()
        }
    }

    fun init(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            val application = context.applicationContext
            val wallpaper = application.getSystemService(Context.WALLPAPER_SERVICE)
            if (wallpaper is WallpaperManager) {
                val wallpaperColors = wallpaper.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
                if (wallpaperColors != null) {
                    parseColorToPigment(wallpaperColors)
                }
                wallpaper.addOnColorsChangedListener(
                    WallpaperManager.OnColorsChangedListener { colors, which ->
                        if (colors != null && which == WallpaperManager.FLAG_SYSTEM) {
                            parseColorToPigment(colors)
                        }
                    },
                    Handler(Looper.getMainLooper())
                )
            }
        }
    }

    private fun parseColorToPigment(wallpaperColors: WallpaperColors) {
//        val newPigment = Pigment(
//            primary = wallpaperColors.primaryColor.toArgb(),
//
//        )
//        onPigmentChanged(newPigment)
        // 没想好怎么弄，颜色值不够
    }

    private fun onWallpaperPigmentChanged(newPigment: Pigment) {
        wallpaperPigment = newPigment
        notifyPigmentChanged()
    }

    private fun notifyPigmentChanged() {
        val p = pigment ?: return
        pigmentProviderHelper.onDecorationChanged(p)
    }

    fun registerPigment(page: PigmentPage) {
        pigmentProviderHelper.registerPigment(page)
        pigment?.let {
            page.onDecorationChanged(it)
        }
    }

    fun unregisterPigment(page: PigmentPage) {
        pigmentProviderHelper.unregisterPigment(page)
    }

}