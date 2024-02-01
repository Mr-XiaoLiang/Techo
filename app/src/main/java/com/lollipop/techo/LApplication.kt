package com.lollipop.techo

import android.app.Application
import com.lollipop.pigment.Pigment
import com.lollipop.pigment.PigmentActivityHelper
import com.lollipop.pigment.PigmentWallpaperCenter
import com.lollipop.techo.util.AppUtil
import com.lollipop.techo.util.FontHelper

/**
 * @author lollipop
 * @date 2021/5/13 22:23
 */
class LApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        FontHelper.init(this)
        AppUtil.init(this)
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