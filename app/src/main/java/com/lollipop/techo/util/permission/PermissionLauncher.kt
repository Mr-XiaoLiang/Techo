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

sealed class PermissionLauncher(
    activitySrc: AppCompatActivity,
    @StringRes
    protected val rationaleMessage: Int,
) {

    companion object {
        fun single(
            activity: AppCompatActivity,
            @StringRes
            rationaleMessage: Int,
            permission: String
        ): Single {
            return Single(activity, rationaleMessage, permission)
        }

        fun multipleByAnd(
            activity: AppCompatActivity,
            @StringRes
            rationaleMessage: Int,
            permissions: Array<String>
        ): MultipleByAnd {
            return MultipleByAnd(activity, rationaleMessage, permissions)
        }

        fun multipleByOr(
            activity: AppCompatActivity,
            @StringRes
            rationaleMessage: Int,
            permissions: Array<String>
        ): MultipleByOr {
            return MultipleByOr(activity, rationaleMessage, permissions)
        }
    }

    protected val activityReference = WeakReference(activitySrc)

    private var onResultCallback: ((PermissionResult) -> Unit)? = null

    protected fun bindCallback(callback: (PermissionResult) -> Unit) {
        this.onResultCallback = callback
    }

    protected fun invokeCallback(result: PermissionResult) {
        this.onResultCallback?.invoke(result)
        this.onResultCallback = null
    }

    protected fun showRationale(activity: AppCompatActivity) {
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

    protected abstract fun launch()

    abstract fun request(callback: (PermissionResult) -> Unit)

    class Single(
        activitySrc: AppCompatActivity,
        @StringRes
        rationaleMessage: Int,
        private val permission: String,
    ) : PermissionLauncher(activitySrc, rationaleMessage) {

        private val requestPermissionLauncher = activitySrc.registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
            ::onRequestResult
        )

        private fun onRequestResult(boolean: Boolean) {
            invokeCallback(PermissionResult.Single(boolean))
        }

        override fun request(callback: (PermissionResult) -> Unit) {
            val activity = activityReference.get() ?: return
            bindCallback(callback)
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

        private fun isGranted(activity: AppCompatActivity): Boolean {
            return ContextCompat.checkSelfPermission(
                activity,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }

        override fun launch() {
            requestPermissionLauncher.launch(permission)
        }

    }

    class MultipleByAnd(
        activitySrc: AppCompatActivity,
        @StringRes
        rationaleMessage: Int,
        private val permissions: Array<String>,
    ) : PermissionLauncher(activitySrc, rationaleMessage) {

        private val requestPermissionsLauncher = activitySrc.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
            ::onRequestResult
        )

        private fun onRequestResult(result: Map<String, Boolean>) {
            invokeCallback(PermissionResult.MultipleByAnd(result))
        }

        override fun request(callback: (PermissionResult) -> Unit) {
            val activity = activityReference.get() ?: return
            bindCallback(callback)
            var isAllGranted = true
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(
                        activity,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    isAllGranted = false
                    break
                }
            }
            if (isAllGranted) {
                onRequestResult(mockGrantedMap())
                return
            }
            var shouldShowRationale = false
            for (permission in permissions) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    shouldShowRationale = true
                    break
                }
            }
            if (shouldShowRationale) {
                showRationale(activity)
                return
            }
            launch()
        }

        private fun mockGrantedMap(): Map<String, Boolean> {
            val map = HashMap<String, Boolean>()
            permissions.forEach {
                map[it] = true
            }
            return map
        }

        override fun launch() {
            requestPermissionsLauncher.launch(permissions)
        }

    }


    class MultipleByOr(
        activitySrc: AppCompatActivity,
        @StringRes
        rationaleMessage: Int,
        private val permissions: Array<String>,

        ) : PermissionLauncher(activitySrc, rationaleMessage) {

        private val requestPermissionsLauncher = activitySrc.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
            ::onRequestResult
        )

        private fun onRequestResult(result: Map<String, Boolean>) {
            invokeCallback(PermissionResult.MultipleByOr(result))
        }

        override fun request(callback: (PermissionResult) -> Unit) {
            val activity = activityReference.get() ?: return
            bindCallback(callback)
            val grantedMap = HashMap<String, Boolean>()
            var hasGranted = false
            for (permission in permissions) {
                val granted = ContextCompat.checkSelfPermission(
                    activity,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
                if (granted) {
                    hasGranted = true
                }
                grantedMap[permission] = granted
            }
            if (hasGranted) {
                onRequestResult(grantedMap)
                return
            }
            var shouldShowRationale = false
            for (permission in permissions) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    shouldShowRationale = true
                    break
                }
            }
            if (shouldShowRationale) {
                showRationale(activity)
                return
            }
            launch()
        }

        override fun launch() {
            requestPermissionsLauncher.launch(permissions)
        }

    }

}