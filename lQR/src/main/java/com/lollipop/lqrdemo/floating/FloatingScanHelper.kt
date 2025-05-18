package com.lollipop.lqrdemo.floating

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lollipop.lqrdemo.PhotoScanActivity
import com.lollipop.lqrdemo.R
import com.lollipop.lqrdemo.floating.MediaProjectionHelper.LaunchResult
import java.io.File

class FloatingScanHelper(
    private val activity: AppCompatActivity,
    private val mpLauncher: MediaProjectionHelper.MPLauncher,
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

        private fun noNotificationPermissionTips(context: Context) {
            // TODO
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

        fun startScanResult(context: Context, path: String): ScanResultLaunchResult {
            if (path.isBlank()) {
                return ScanResultLaunchResult.PathEmpty
            }
            try {
                val fullScreenResult = NotificationHelper.startFullScreen(
                    context,
                    NotificationHelper.Channel.SCAN_RESULT,
                    PhotoScanActivity.getIntent(
                        context, Uri.fromFile(File(path)),
                    ),
                    R.string.notification_content_scan_result
                )
                return when (fullScreenResult) {
                    is NotificationHelper.FullScreenResult.Failed -> {
                        ScanResultLaunchResult.Failed(fullScreenResult.error)
                    }

                    NotificationHelper.FullScreenResult.ManagerNotFound -> {
                        ScanResultLaunchResult.ManagerNotFound
                    }

                    NotificationHelper.FullScreenResult.Success -> {
                        ScanResultLaunchResult.Success
                    }
                }
            } catch (e: Throwable) {
                return ScanResultLaunchResult.Failed(e)
            }
        }

    }

    private var isNotificationPermissionTipsShown = false

    private val notificationLauncher = NotificationHelper.registerPermissionLauncher(
        activity,
        ::onNotificationLauncherResult
    )

    private fun onNotificationLauncherResult(hasPermission: Boolean) {
        if (hasPermission) {
            return
        }
        if (isNotificationPermissionTipsShown) {
            return
        }
        noNotificationPermissionTips(activity)
        isNotificationPermissionTipsShown = true
    }

    fun start() {
        if (!notificationLauncher.hasPermission) {
            notificationLauncher.launch()
        }
        if (!checkFloatingPermission(activity)) {
            showFloatingPermissionDialog(activity)
            return
        }
        if (!MediaProjectionHelper.isServiceRunning) {
            showRecordingPermissionDialog(activity) {
                mpLauncher.launch()
            }
        }
    }

    sealed class ScanResultLaunchResult {
        object Success : ScanResultLaunchResult() {
            override fun toString(): String {
                return "ScanResultLaunchResult.Success"
            }
        }

        object PathEmpty : ScanResultLaunchResult() {
            override fun toString(): String {
                return "ScanResultLaunchResult.PathEmpty"
            }
        }

        object ManagerNotFound : ScanResultLaunchResult() {
            override fun toString(): String {
                return "ScanResultLaunchResult.ManagerNotFound"
            }
        }

        class Failed(val error: Throwable) : ScanResultLaunchResult() {
            override fun toString(): String {
                return "ScanResultLaunchResult.Failed(error=$error)"
            }
        }
    }

}