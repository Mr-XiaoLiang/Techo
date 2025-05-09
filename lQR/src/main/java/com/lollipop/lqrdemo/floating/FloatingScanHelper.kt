package com.lollipop.lqrdemo.floating

import android.content.Context

object FloatingScanHelper {

    fun start(context: Context) {
        if (!FloatingPermissionActivity.check(context)) {
            return
        }
        // TODO 启动扫描
    }

}