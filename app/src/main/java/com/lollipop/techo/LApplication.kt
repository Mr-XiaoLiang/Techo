package com.lollipop.techo

import android.app.Activity
import android.app.Application
import com.lollipop.base.request.PermissionFlow

/**
 * @author lollipop
 * @date 2021/5/13 22:23
 */
class LApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        PermissionFlow.globalRationaleCallback = PermissionRationaleCallback()
    }

    class PermissionRationaleCallback: PermissionFlow.RationaleCallback {
        override fun showRationale(
            activity: Activity,
            permissions: String,
            feedback: PermissionFlow.PermissionFeedback
        ) {
            // TODO
        }
    }

}