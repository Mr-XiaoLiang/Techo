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
    private val permission: String,
) {

    companion object {
        fun create(
            activity: AppCompatActivity,
            @StringRes
            rationaleMessage: Int,
            permission: String
        ): PermissionLauncher {
            return PermissionLauncher(activity, rationaleMessage, permission)
        }
    }

    private var onResultCallback: ((Boolean) -> Unit)? = null

    private val requestPermissionLauncher = activitySrc.registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
        ::onRequestResult
    )

    private val activityReference = WeakReference(activitySrc)

    private fun onRequestResult(boolean: Boolean) {
        onResultCallback?.invoke(boolean)
        onResultCallback = null
    }

    fun request(callback: (Boolean) -> Unit) {
        val activity = activityReference.get() ?: return
        this.onResultCallback = callback
        when {
            isGranted(activity) -> {
                onRequestResult(true)
            }

            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission) -> {
                showRationale(activity)
            }

            else -> {
                launch()
            }
        }
    }

    private fun showRationale(activity: AppCompatActivity) {
        AlertDialog.Builder(activity)
            .setMessage(rationaleMessage)
            .setPositiveButton(R.string.granted) { dialog, _ ->
                dialog.dismiss()
                launch()
            }
            .setNegativeButton(R.string.denied) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun isGranted(activity: AppCompatActivity): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun launch() {
        requestPermissionLauncher.launch(permission)
    }

}