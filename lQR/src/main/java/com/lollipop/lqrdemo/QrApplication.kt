package com.lollipop.lqrdemo

import android.app.Application
import com.lollipop.pigment.Pigment
import com.lollipop.pigment.PigmentActivityHelper
import com.lollipop.pigment.PigmentWallpaperCenter

class QrApplication : Application() {

    companion object {
        val APP: QrApplication
            get() {
                return appInner!!
            }

        private var appInner: QrApplication? = null
    }

    override fun onCreate() {
        super.onCreate()
        appInner = this
        updateDefaultFigment()
        PigmentWallpaperCenter.init(this)
        val activityHelper = PigmentActivityHelper { onBackground ->
            if (!onBackground) {
                fetchPigment()
            }
        }
        PigmentWallpaperCenter.registerPigment(activityHelper)
        registerActivityLifecycleCallbacks(activityHelper)
    }

    fun fetchPigment() {
        updateDefaultFigment()
        PigmentWallpaperCenter.fetch(this)
    }

    private fun updateDefaultFigment() {
        PigmentWallpaperCenter.default(
            Pigment.valueOf(
                0xFF00B78D.toInt(),
                0xFFB75F00.toInt(),
                Pigment.getBlendByNightMode(this)
            )
        )
    }

}