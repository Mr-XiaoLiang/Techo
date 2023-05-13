package com.lollipop.pigment

import android.app.WallpaperColors
import android.app.WallpaperManager
import android.content.Context
import android.content.res.Configuration
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

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

    private fun isDarkMode(context: Context): Boolean {
        val uiMode = context.resources.configuration.uiMode
        return uiMode.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }

    fun init(context: Context) {
        val application = context.applicationContext
        val wallpaper = application.getSystemService(Context.WALLPAPER_SERVICE)
        if (wallpaper is WallpaperManager) {
            val wallpaperColors = wallpaper.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
            if (wallpaperColors != null) {
                parseColorToPigment(wallpaperColors, isDarkMode(application))
            }
            wallpaper.addOnColorsChangedListener(
                WallpaperManager.OnColorsChangedListener { colors, which ->
                    if (colors != null && which == WallpaperManager.FLAG_SYSTEM) {
                        parseColorToPigment(colors, isDarkMode(application))
                    }
                },
                Handler(Looper.getMainLooper())
            )
        }
    }

    private fun parseColorToPigment(wallpaperColors: WallpaperColors, darkMode: Boolean) {
        val newPigment = Pigment(
            primary = wallpaperColors.primaryColor,
            secondary = wallpaperColors.secondaryColor,
            if (darkMode) {
                BlendMode.Dark
            } else {
                BlendMode.Light
            }
        )
        onWallpaperPigmentChanged(newPigment)
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

    fun registerPigmentByLifecycle(lifecycle: Lifecycle, page: PigmentPage): LifecyclePigmentPage {
        return LifecyclePigmentPage(lifecycle, page)
    }

    class LifecyclePigmentPage internal constructor(
        lifecycle: Lifecycle,
        private val page: PigmentPage
    ) : LifecycleEventObserver, PigmentPage {

        init {
            val currentState = lifecycle.currentState
            if (currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                registerPigment(this)
            }
            lifecycle.addObserver(this)
        }

        override fun onDecorationChanged(pigment: Pigment) {
            page.onDecorationChanged(pigment)
        }

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    registerPigment(this)
                }

                Lifecycle.Event.ON_PAUSE -> {
                    unregisterPigment(this)
                }

                else -> {}
            }
        }
    }

}