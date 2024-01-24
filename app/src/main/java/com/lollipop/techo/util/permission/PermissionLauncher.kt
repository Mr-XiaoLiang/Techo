package com.lollipop.techo.util.permission

import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.lollipop.techo.R
import java.lang.ref.WeakReference

class PermissionLauncher(
    activitySrc: AppCompatActivity,
    @StringRes
    private val rationaleMessage: Int,
    private val onResult: (Boolean) -> Unit
) {

    companion object {
        fun create(
            activity: AppCompatActivity,
            @StringRes
            rationaleMessage: Int,
            callback: (Boolean) -> Unit
        ): PermissionLauncher {
            return PermissionLauncher(activity, rationaleMessage, callback)
        }
    }

    private val requestPermissionLauncher = activitySrc.registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
        onResult
    )

    private val activityReference = WeakReference(activitySrc)

    fun request(permissions: String) {
        val activity = activityReference.get() ?: return
        when {
            isGranted(activity, permissions) -> {
                onResult(true)
            }

            ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions) -> {
                showRationale(activity, permissions)
            }

            else -> {
                launch(permissions)
            }
        }
    }

    private fun showRationale(activity: AppCompatActivity, permissions: String) {
        AlertDialog.Builder(activity)
            .setMessage(rationaleMessage)
            .setPositiveButton(R.string.granted) { dialog, _ ->
                dialog.dismiss()
                launch(permissions)
            }
            .setNegativeButton(R.string.denied) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun isGranted(activity: AppCompatActivity, permissions: String): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            permissions
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun launch(permissions: String) {
        requestPermissionLauncher.launch(permissions)
    }

}