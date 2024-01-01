package com.lollipop.pigment

import android.app.WallpaperColors
import android.app.WallpaperManager
import android.content.Context
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
        return Pigment.isNightMode(context)
    }

    fun init(context: Context) {
        val application = context.applicationContext
        val wallpaper = getWallpaperManager(application) ?: return
        fetchColor(application, wallpaper)
        wallpaper.addOnColorsChangedListener(
            { colors, which ->
                if (colors != null && which == WallpaperManager.FLAG_SYSTEM) {
                    parseColorToPigment(colors, isDarkMode(application))
                }
            },
            Handler(Looper.getMainLooper())
        )
    }

    fun fetch(context: Context) {
        val manager = getWallpaperManager(context) ?: return
        fetchColor(context, manager)
    }

    private fun fetchColor(context: Context, wallpaperManager: WallpaperManager) {
        wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)?.let {
            parseColorToPigment(it, isDarkMode(context))
        }
    }

    private fun getWallpaperManager(context: Context): WallpaperManager? {
        val application = context.applicationContext
        val wallpaper = application.getSystemService(Context.WALLPAPER_SERVICE)
        if (wallpaper is WallpaperManager) {
            return wallpaper
        }
        return null
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

        private var lastPigment: Pigment? = null

        init {
            val currentState = lifecycle.currentState
            if (currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                registerPigment(this)
            }
            lifecycle.addObserver(this)
        }

        override fun onDecorationChanged(pigment: Pigment) {
            lastPigment = pigment
            page.onDecorationChanged(pigment)
        }

        override val currentPigment: Pigment?
            get() {
                return lastPigment
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