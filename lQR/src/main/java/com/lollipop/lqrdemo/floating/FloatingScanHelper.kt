package com.lollipop.lqrdemo.floating

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri

object FloatingScanHelper {

    private fun requestFloatingPermission(context: Context) {
        // 引导用户去设置页面开启权限
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        intent.data = "package:${context.packageName}".toUri()
        context.startActivity(intent)
    }

    private fun checkFloatingPermission(context: Context): Boolean {
        return Settings.canDrawOverlays(context);
    }

    fun start(context: Context) {
        // TODO 启动扫描
    }

}