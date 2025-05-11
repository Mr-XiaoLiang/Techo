package com.lollipop.lqrdemo.floating

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lollipop.lqrdemo.R
import com.lollipop.lqrdemo.floating.MediaProjectionHelper.LaunchResult

class FloatingScanHelper(
    private val activity: AppCompatActivity,
    private val launcher: MediaProjectionHelper.MPLauncher
) {

    companion object {
        fun register(
            activity: AppCompatActivity,
            resultCallback: (LaunchResult) -> Unit
        ): FloatingScanHelper {
            return FloatingScanHelper(
                activity,
                MediaProjectionHelper.register(
                    activity, resultCallback
                )
            )
        }

        private fun requestFloatingPermission(context: Context) {
            // 引导用户去设置页面开启权限
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = "package:${context.packageName}".toUri()
            context.startActivity(intent)
        }

        private fun checkFloatingPermission(context: Context): Boolean {
            return Settings.canDrawOverlays(context);
        }

        private fun showFloatingPermissionDialog(context: Context) {
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.dialog_title_floating_permission)
                .setMessage(R.string.dialog_summary_floating_permission)
                .setPositiveButton(R.string.dialog_agree) { d, w ->
                    d.dismiss()
                    requestFloatingPermission(context)
                }.setNegativeButton(R.string.dialog_refuse) { d, w ->
                    d.dismiss()
                }.show()
        }

        private fun showRecordingPermissionDialog(
            context: Context,
            requestRecording: () -> Unit
        ) {
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.dialog_title_recording_permission)
                .setMessage(R.string.dialog_summary_recording_permission)
                .setPositiveButton(R.string.dialog_agree) { d, w ->
                    d.dismiss()
                    requestRecording()
                }.setNegativeButton(R.string.dialog_refuse) { d, w ->
                    d.dismiss()
                }.show()
        }
    }

    fun start() {
        if (!checkFloatingPermission(activity)) {
            showFloatingPermissionDialog(activity)
            return
        }
        if (!MediaProjectionHelper.isServiceRunning) {
            showRecordingPermissionDialog(activity) {
                launcher.launch()
            }
        }
    }

}