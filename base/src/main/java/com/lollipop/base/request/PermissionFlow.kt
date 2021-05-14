package com.lollipop.base.request

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.lollipop.base.ui.BaseFragment
import java.lang.RuntimeException
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author lollipop
 * @date 2021/5/12 22:08
 */
class PermissionFlow(
    private val activity: Activity,
    private val requestLauncher: RequestLauncher
) {

    companion object {
        var globalRationaleCallback: RationaleCallback? = null
    }

    private var rationaleCallback: RationaleCallback? = globalRationaleCallback
    private var permissionCallback: PermissionCallback? = null

    private var permissionsList = HashSet<String>()

    private val shouldShowRationale = LinkedList<String>()
    private val pendingRequestPermissions = ArrayList<String>()
    private val grantedPermissions = ArrayList<String>()

    private val selfPermissionCallback = PermissionCallback { result ->
        val allPermissionList = ArrayList<String>()
        val allResult = ArrayList<Int>()
        allPermissionList.addAll(result.permissions)
        result.grantResults.forEach {
            allResult.add(it)
        }
        grantedPermissions.forEach {
            allPermissionList.add(it)
            allResult.add(PermissionResult.GRANTED)
        }
        permissionCallback?.onPermissionsResult(
            PermissionResult(
                allPermissionList.toTypedArray(),
                allResult.toIntArray()
            )
        )
    }

    fun onNeedRationale(callback: RationaleCallback): PermissionFlow {
        this.rationaleCallback = callback
        return this
    }

    fun permissionIs(vararg permissions: String): PermissionFlow {
        permissionsList.addAll(permissions)
        return this
    }

    fun request(permissionCallback: PermissionCallback) {
        this.permissionCallback = permissionCallback

        val permissionsArray = permissionsList.toTypedArray()
        permissionsArray.forEach {
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    it
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                grantedPermissions.add(it)
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, it)) {
                    shouldShowRationale.add(it)
                } else {
                    pendingRequestPermissions.add(it)
                }
            }
        }
        checkNext()
    }

    private fun checkNext() {
        val rationaleDialog = rationaleCallback
        if (shouldShowRationale.isNotEmpty()) {
            // 如果可以显示用户提醒，那么等待用户响应
            if (rationaleDialog != null) {
                val permissions = shouldShowRationale.removeFirst()
                rationaleDialog.showRationale(activity, permissions) {
                    if (it) {
                        pendingRequestPermissions.add(permissions)
                    }
                    checkNext()
                }
                return
            }
            pendingRequestPermissions.addAll(shouldShowRationale)
            shouldShowRationale.clear()
        }
        if (pendingRequestPermissions.isEmpty()) {
            selfPermissionCallback.onPermissionsResult(PermissionResult(arrayOf(), intArrayOf()))
            return
        }
        requestLauncher.requestPermission(pendingRequestPermissions.toTypedArray(), selfPermissionCallback)
    }

    fun interface PermissionFeedback {
        fun onFeedback(isAllowed: Boolean)
    }

    fun interface RationaleCallback {
        fun showRationale(activity: Activity, permissions: String, feedback: PermissionFeedback)
    }

}

fun Fragment.startPermissionFlow(requestLauncher: RequestLauncher? = null): PermissionFlow {
    val activity = this.requireActivity()
    val launcher: RequestLauncher = when {
        requestLauncher != null -> requestLauncher
        this is RequestLauncher -> this
        activity is RequestLauncher -> activity
        else -> throw RuntimeException("RequestLauncher not found")
    }
    return PermissionFlow(activity, launcher)
}

fun Activity.startPermissionFlow(requestLauncher: RequestLauncher? = null): PermissionFlow {
    return PermissionFlow(this, requestLauncher ?: (this as RequestLauncher))
}