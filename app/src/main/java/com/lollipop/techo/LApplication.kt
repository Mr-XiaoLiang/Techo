package com.lollipop.techo

import android.Manifest
import android.app.Activity
import android.app.Application
import androidx.appcompat.app.AlertDialog
import com.lollipop.base.request.PermissionFlow
import com.lollipop.techo.util.FontHelper

/**
 * @author lollipop
 * @date 2021/5/13 22:23
 */
class LApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        PermissionFlow.globalRationaleCallback = PermissionRationaleCallback()
        FontHelper.init(this)
    }

    class PermissionRationaleCallback : PermissionFlow.RationaleCallback {
        override fun showRationale(
            activity: Activity,
            permissions: String,
            feedback: PermissionFlow.PermissionFeedback
        ) {
            val message = when (permissions) {
                Manifest.permission.READ_EXTERNAL_STORAGE -> {
                    R.string.permission_rationale_read_external_storage
                }
                else -> 0
            }
            if (message == 0) {
                return feedback.onFeedback(false)
            }
            AlertDialog.Builder(activity)
                .setMessage(message)
                .setPositiveButton(R.string.granted) { dialog, _ ->
                    feedback.onFeedback(true)
                    dialog.dismiss()
                }.setNegativeButton(R.string.denied) { dialog, _ ->
                    feedback.onFeedback(false)
                    dialog.dismiss()
                }.show()
        }
    }

}