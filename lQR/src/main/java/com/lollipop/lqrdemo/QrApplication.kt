package com.lollipop.lqrdemo

import android.app.Application
import com.lollipop.lqrdemo.creator.HistoryColor
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
        updateDefaultPigment()
        PigmentWallpaperCenter.init(this)
        val activityHelper = PigmentActivityHelper { onBackground ->
            if (!onBackground) {
                fetchPigment()
            }
        }
        PigmentWallpaperCenter.registerPigment(activityHelper)
        registerActivityLifecycleCallbacks(activityHelper)
        HistoryColor.init(this)
    }

    fun fetchPigment() {
        updateDefaultPigment()
        PigmentWallpaperCenter.fetch(this)
    }

    private fun updateDefaultPigment() {
        PigmentWallpaperCenter.default(
            Pigment.valueOf(
                0xFF00B78D.toInt(),
                0xFFB75F00.toInt(),
                Pigment.getBlendByNightMode(this)
            )
        )
    }

}